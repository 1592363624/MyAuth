<template>
  <div class="user-wrapper">
    <div class="content-box">
      <!-- <a href="https://pro.loacg.com/docs/getting-started" target="_blank">
        <span class="action">
          <a-icon type="question-circle-o"></a-icon>
        </span>
      </a> -->
      <!-- <notice-icon class="action" /> -->
      <a-spin
        tip="Loading..."
        :spinning="loading"
      >
        <a-dropdown
          :visible="visible"
          :trigger="['hover']"
        >
          <span
            class="action ant-dropdown-link user-dropdown-menu"
            @click="visibleChange"
          >
            <span>欢迎，{{ op.user }}</span>
            <a-avatar
              size="small"
              style="margin-left:5px;"
            >
              <img
                slot="icon"
                :src="imgSrc"
              />
            </a-avatar>
          </span>
          <a-menu
            slot="overlay"
            class="user-dropdown-menu-wrapper"
          >
            <a-menu-item key="0">
              <a href="javascript:;">
                <a-icon type="user" />
                <span>用户名：{{ op.user }}</span>
              </a>
            </a-menu-item>
            <a-menu-divider />
            <a-menu-item key="1">
              <a
                href="javascript:;"
                @click="handleChangeQQ"
              >
                <a-icon type="qq" />
                <span>QQ：{{ op.qq }}</span>
              </a>
              <ChangeQQForm
                ref="changeQQ"
                :afterSubmit="getMyInfo"
              ></ChangeQQForm>
            </a-menu-item>
            <a-menu-divider />
            <a-menu-item
              key="3"
              v-if="op.user!='admin'"
            >
              <a href="javascript:;">
                <a-icon type="money-collect" />
                <span>余额：{{ op.money }}￥</span>
              </a>
            </a-menu-item>
            <a-menu-divider v-if="op.user!='admin'" />

            <a-menu-item key="2">
              <a href="javascript:;">
                <a-icon type="robot" />
                <span>角色：{{ op.roleName }}</span>
              </a>
            </a-menu-item>
            <a-menu-divider />

            <a-menu-item
              key="4"
              v-if="op.fromSoftName"
            >
              <a href="javascript:;">
                <a-icon type="slack" />
                <span>软件：{{ op.fromSoftName }}</span>
              </a>
            </a-menu-item>
            <a-menu-divider v-if="op.fromSoftName" />
            <a-menu-item key="5">
              <a
                href="javascript:;"
                @click="handleChangePwd()"
              >
                <a-icon type="lock" />
                <span>修改密码</span>
              </a>
              <change-pwd-form ref="changePwd"></change-pwd-form>
            </a-menu-item>
            <a-menu-divider />
            <!-- <a-menu-item key="6">
            <a @click="checkToken">
              <a-icon type="check-circle" />
              <span>检查token</span>
            </a>
          </a-menu-item>
          <a-menu-divider /> -->
            <a-menu-item key="7">
              <a
                href="javascript:;"
                @click="handleLogout()"
              >
                <a-icon type="logout" />
                <span>退出登录</span>
              </a>
            </a-menu-item>
          </a-menu>
        </a-dropdown>
      </a-spin>
    </div>
  </div>
</template>

<script>
// import NoticeIcon from '@/components/NoticeIcon'
// import { mapActions, mapGetters } from 'vuex'
// import OperatorCache from '@/utils/cache/OperatorCache'
import TokenCache from '@/utils/cache/TokenCache'
import ChangePwdForm from './ChangePwdForm'
import ChangeQQForm from './ChangeQQForm.vue'

export default {
  name: 'UserMenu',
  components: {
    // NoticeIcon
    ChangePwdForm,
    ChangeQQForm
  },
  data () {
    return {
      visible: false,
      op: {},
      imgSrc: '',
      loading: false
    }
  },
  mounted () {
    this.opCache()
  },
  methods: {
    opCache () {
      // return OperatorCache.info
      const cache = JSON.parse(localStorage.getItem('userInfo'))
      this.op = cache
      this.imgSrc = 'https://q1.qlogo.cn/g?b=qq&nk=' + cache.qq + '&s=640'
    },
    // ...mapActions(['Logout']),
    // ...mapGetters(['nickname', 'avatar']),
    // 手动触发以控制刷新
    visibleChange () {
      this.visible = !this.visible
      if (this.visible) {
        this.getMyInfo(this.visible)
      }
    },
    getMyInfo (visible) {
      if (visible) this.loading = true
      this.$http.get('/myauth/web/getMyInfo').then(resJson => {
        if (resJson.success) {
          this.op = resJson.result
          this.imgSrc = 'https://q1.qlogo.cn/g?b=qq&nk=' + resJson.result.qq + '&s=640'
          localStorage.setItem('userInfo', JSON.stringify(resJson.result))
        } else {
          this.$message.error('获取用户信息失败，用户信息可能会出现延迟：' + resJson.msg)
        }
        if (visible) this.loading = false
      })
    },
    handleLogout () {
      this.visible = false
      this.$confirm({
        title: '提示',
        content: '真的要注销登录吗 ?',
        onOk () {
          TokenCache.deleteToken()
          localStorage.removeItem('userInfo')

          // OperatorCache.clear()
          location.href = '/'
          location.reload()
          // that.$router.push({ path: '/user/login' })
        }
      })
    },
    handleChangePwd () {
      this.visible = false
      this.$refs.changePwd.open()
    },
    checkToken () {
      this.$http.post('/myauth/web/checkLogin').then(resJson => {
        if (resJson.success) {
          this.$message.success(resJson.msg)
        } else {
          this.$message.error(resJson.msg)
        }
      })
    },
    handleChangeQQ () {
      this.visible = false
      this.$refs.changeQQ.open()
    }
  }
}
</script>
