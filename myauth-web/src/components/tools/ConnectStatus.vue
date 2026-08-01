<template>
  <div style="display:inline; text-overflow:hidden;">
    客户端信息（
    IP：<a>{{ ip }}</a>&emsp;
    UA：<a>{{ ua }}</a>）
  </div>
</template>

<script>
export default {
  data () {
    return {
      ip: '获取中……',
      ua: '获取中……'
    }
  },
  mounted () {
    this.checkConnect()
    // 在mounted 声明周期中创建定时器
    // const timer = setInterval(()=>{
    //   this.checkConnect()
    // }, 600000) // 初始化后，每10分钟秒执行1次
    // // 通过$once来监听定时器，在beforeDestroy钩子可以被清除
    // this.$once('hook:beforeDestroy',()=>{
    //   // 在页面销毁时，销毁定时器
    //   clearInterval(timer)
    // })
  },
  methods: {
    checkConnect () {
      this.$http.get('/myauth/web/connect').then(resJson => {
        if (resJson.success) {
          // this.$message.success('操作成功')
          // console.info("检查服务状态",resJson)
          if (resJson.result.ip) {
            this.ip = resJson.result.ip
          } else {
            this.ip = '获取失败，请检反馈管理员'
          }
          if (resJson.result.ua) {
            this.ua = resJson.result.ua
          } else {
            this.ua = '获取失败，请检反馈管理员'
          }
        } else {
          this.$message.error(resJson.msg)
        }
      })
    }
  }
}
</script>
