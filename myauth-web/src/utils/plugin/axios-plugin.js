import axios from 'axios'
import TokenCache from '@/utils/cache/TokenCache'
import defaultSettings from '@/config/defaultSettings'// 内部的配置文件
import ProcessHelper from '@/utils/helper/ProcessHelper'

const serverConfig = JSON.parse(localStorage.getItem('serverConfig'))

const rootUrl = () => {
  if (serverConfig) {
    if (ProcessHelper.isProduction() || ProcessHelper.isPreview()) {
      return serverConfig.publishRootUrl
    } else {
      return serverConfig.localRootUrl
    }
  }
}

export const Axios = axios.create({
  baseURL: rootUrl(),
  timeout: serverConfig ? serverConfig.apiTimeout : defaultSettings.apiTimeout
})

// 在发送请求之前做某件事
Axios.interceptors.request.use(config => {
  // 设置以 form 表单的形式提交参数，如果以 JSON 的形式提交表单，可忽略
  // if (config.method === 'post') {
  //     // JSON 转换为 FormData
  //     const formData = new FormData()
  //     Object.keys(config.data).forEach(key => formData.append(key, config.data[key]))
  //     config.data = formData
  // }
  if (TokenCache.getToken()) {
    config.headers.token = TokenCache.getToken()
  }
  return config
}, erroror => {
  return Promise.reject(erroror)
})

// 返回状态判断(添加响应拦截器)
Axios.interceptors.response.use(res => {
  // console.info("返回正常:", res)
  // console.info("serverConfig:", serverConfig)
  if (res.data) {
    // 自定义错误处理
    if (res.data.code == 401 || res.data.code == 402 || res.data.code == 403 || res.data.code == 405) {
      // 401未传递token参数
      // 402账号异地登录
      // 403token超过有效期
      // 405账号被禁用
      // this.$message.error(res.data.msg)
      TokenCache.deleteToken()
      localStorage.removeItem('userInfo')
      location.href = '/'
      return Promise.resolve({ Success: false, Msg: res.data.msg })
    }
  }
  return res.data
}, error => {
  if (error && error.response) {
    console.info('请求返回错误', error)
    switch (error.response.status) {
      case 400:
        error.message = '请求错误'
        break

      case 401:
        error.message = '未授权，请登录'
        TokenCache.deleteToken()
        localStorage.removeItem('userInfo')
        location.href = '/'
        break

      case 403:
        error.message = '拒绝访问'
        break

      case 404:
        error.message = `请求地址出错: ${error.response.config.url}`
        break

      case 408:
        error.message = '请求超时'
        break

      case 500:
        error.message = '服务器内部错误'
        break

      case 501:
        error.message = '服务未实现'
        break

      case 502:
        error.message = '网关错误'
        break

      case 503:
        error.message = '服务不可用'
        break

      case 504:
        error.message = '网关超时'
        break

      case 505:
        error.message = 'HTTP版本不受支持'
        break

      default:
    }
  }

  return Promise.resolve({ Success: false, Msg: error.message })
})

export default {
  install (Vue) {
    Object.defineProperty(Vue.prototype, '$http', { value: Axios })
    Object.defineProperty(Vue.prototype, '$rootUrl', { value: rootUrl() })
  }
}
