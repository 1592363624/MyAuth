<template>
  <div class="footer">
    <div class="links">

      <a
        href="https://beian.miit.gov.cn/"
        target="_blank"
      >{{ settings.internetContentProvider }}</a>
      <!-- <a
        href="https://github.com/sendya/ant-design-pro-vue"
        target="_blank"
      >
        <a-icon type="github" />
      </a>
      <a href="https://ant.design/">Ant Design</a>
      <a href="https://vue.ant.design/">Vue Antd</a> -->
    </div>
    <!-- <connect-status></connect-status> -->
    <div class="copyright">
      Powered by <a href="https://www.myauthx.cn/" target="_blank" title="Server By Daen,Lonely | Web By Shine">MyAuth</a> | Copyright
      <a-icon type="copyright" /><span> {{ settings.copyright }} | {{ settings.projectName }}</span>
    </div>
  </div>
</template>

<script>
// import ConnectStatus from '../tools/ConnectStatus'

export default {
  name: 'GlobalFooter',
  // components:{
  //   ConnectStatus
  // },
  data () {
    return {
      settings: {}
    }
  },
  mounted () {
    const serverConfigJson = localStorage.getItem('serverConfig')
    if (serverConfigJson) {
      this.settings = JSON.parse(serverConfigJson)
    }
  }
}
</script>

<style lang="less" scoped>
.footer {
  padding: 0 16px;
  margin: 48px 0 24px;
  text-align: center;

  .links {
    margin-bottom: 8px;

    a {
      color: rgba(0, 0, 0, 0.45);

      &:hover {
        color: rgba(0, 0, 0, 0.65);
      }

      &:not(:last-child) {
        margin-right: 40px;
      }
    }
  }
  .copyright {
    color: rgba(0, 0, 0, 0.45);
    font-size: 14px;
  }
}
</style>
