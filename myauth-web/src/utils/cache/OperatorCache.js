import { Axios } from '@/utils/plugin/axios-plugin'
import defaultSettings from '@/config/defaultSettings'
import ProcessHelper from '@/utils/helper/ProcessHelper'

const rootUrl = () => {
    if (ProcessHelper.isProduction() || ProcessHelper.isPreview()) {
        return defaultSettings.publishRootUrl
    } else {
        return defaultSettings.localRootUrl
    }
}
let permissions = []
let inited = false

const OperatorCache = {
    info: {},
    inited () {
        return inited
    },
    init (callBack) {
        if (inited) { callBack() } else {
            // Axios.post('/Base_Manage/Home/GetOperatorInfo').then(resJson => {
            //     this.info = resJson.Data.UserInfo
            //     permissions = resJson.Data.Permissions
            //     inited = true
            //     callBack()
            // })
            Axios.post('/myauth/web/checkLogin').then(resJson => {
                // 后端无用户权限获取接口，只有在登录时候才有
                // this.info = resJson.Data.UserInfo
                permissions = resJson
                if (resJson.success) {
                    inited = true
                    callBack()
                } else {
                    alert('检查登录状态遇到错误：' + JSON.stringify(resJson) + '，请刷新后再测试，若依然出现此种情况，请检查配置文件中后端请求接口根地址是否配置有误，当前根地址为：' + rootUrl() + '。\r\nMyAuth系统用户交流群：1016357430')
                }
            }).catch(err => {
                console.error('检查登录请求失败，请检查浏览器权限配置：', err)
            })
        }
    },
    hasPermission (thePermission) {
        return permissions.includes(thePermission)
    },
    clear () {
        inited = false
        permissions = []
        this.info = {}
    }
}

export default OperatorCache
