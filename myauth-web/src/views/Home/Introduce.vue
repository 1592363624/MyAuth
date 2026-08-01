<template>
  <a-card :bordered="false">
    <a-card title="服务信息">
      <p>标题：{{ title }}</p>
      <p>关键字：{{ keywords }}</p>
      <p>描述：{{ description }}</p>
      <p>当前JDK版本：<b style="color:red">{{ JDKVersion }}</b></p>
      <p>当前后端版本：<b style="color:red">{{ MyAuthVersion }}</b></p>
      <p>当前前端版本：<b style="color:red">{{ settings.projectVersion }}</b></p>
    </a-card>
  </a-card>
</template>

<script>
import defaultSettings from '@/config/defaultSettings'

export default {
  data () {
    return {
      title: '',
      keywords: '',
      description: '',
      JDKVersion: '',
      MyAuthVersion: '',
      settings: ''
    }
  },
  mounted () {
    this.getWebInfo()
    this.settings = defaultSettings
  },
  methods: {
    getWebInfo () {
      this.$http.get('/myauth/web/getWebInfo').then(resJson => {
        if (resJson.success) {
          if (resJson.result.title) {
            this.title = resJson.result.title
          }
          if (resJson.result.keywords) {
            this.keywords = resJson.result.keywords
          }
          if (resJson.result.description) {
            this.description = resJson.result.description
          }
          if (resJson.result.JDKVersion) {
            this.JDKVersion = resJson.result.JDKVersion
          }
          if (resJson.result.MyAuthVersion) {
            this.MyAuthVersion = resJson.result.MyAuthVersion
          }
        } else {
          this.$message.error(resJson.msg)
        }
      })
    }
  }
}
</script>
