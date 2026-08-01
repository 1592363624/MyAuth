<template>
  <div class="main">
    <a-spin :spinning="loading">
      <a-form
        id="formLogin"
        class="user-layout-login"
        ref="formLogin"
        :form="form"
        @submit="handleSubmit"
      >
        <a-tabs
          :activeKey="customActiveKey"
          :tabBarStyle="{ textAlign: 'center', borderBottom: 'unset' }"
          @change="handleTabClick"
        >
          <a-tab-pane
            key="tab1"
            tab="账号密码登录"
          >
            <a-form-item>
              <a-input
                size="large"
                type="text"
                placeholder="请输入用户名"
                v-decorator="['user', { rules: [{ required: true, message: '请输入用户名' }] }]"
              >
                <a-icon
                  slot="prefix"
                  type="user"
                  :style="{ color: 'rgba(0,0,0,.25)' }"
                />
              </a-input>
            </a-form-item>

            <a-form-item>
              <a-input-password
                size="large"
                placeholder="请输入密码"
                v-decorator="['pass', { rules: [{ required: true, message: '请输入密码' }] }]"
              >
                <a-icon
                  slot="prefix"
                  type="lock"
                  :style="{ color: 'rgba(0,0,0,.25)' }"
                />
              </a-input-password>
            </a-form-item>
            <a-form-item>
              <a-checkbox v-decorator="['savePwd', { valuePropName: 'checked' }]">记住密码</a-checkbox>
              <a-tooltip>
                <span slot="title">
                  自助暂未开放，请联系管理员进行密码重置
                </span>
                <a-button
                  type="link"
                  icon="question-circle"
                  style="color:rgb(84,84,86);margin-left:45%"
                >
                  忘记密码
                </a-button>
              </a-tooltip>
            </a-form-item>
          </a-tab-pane>
          <!-- <a-tab-pane
            key="tab2"
            tab="扫码登录"
          >
            <a-form-item>
              暂未开放，敬请期待
            </a-form-item>

          </a-tab-pane> -->
        </a-tabs>

        <a-form-item style="margin-top:24px">
          <a-button
            size="large"
            type="primary"
            htmlType="submit"
            class="login-button"
          >登录</a-button>
          <a-tooltip>
            <div slot="title">
              <a-button
                type="link"
                icon="search"
                @click="hanldleStorage"
              >
                查询额外存储信息
              </a-button><br />
              <a-button
                type="link"
                icon="edit"
                @click="hanldleUserSearch"
              >
                信息查询
              </a-button><br />
              <a-button
                type="link"
                icon="edit"
                @click="hanldleSelfUnbind"
              >
                自助解绑
              </a-button><br />
              <a-button
                type="link"
                icon="user-add"
                @click="hanldleRegist"
              >
                自助账号注册
              </a-button><br />
              <a-button
                type="link"
                icon="edit"
                @click="hanldleChangeUser"
              >
                自助账号修改
              </a-button><br />
              <a-button
                type="link"
                icon="edit"
                @click="hanldleSelfPassCard"
              >
                自助使用卡密
              </a-button>
            </div>
            <a-button
              size="large"
              class="login-button"
            >自助功能</a-button>
          </a-tooltip>
        </a-form-item>
      </a-form>

      <change-user ref="editForm"></change-user>
      <regist-user ref="register"></regist-user>
      <user-info-search ref="userSearch"></user-info-search>
      <storage-search ref="storageSearch"></storage-search>
      <self-pass-card ref="selfPassCard"></self-pass-card>
      <self-Unbind ref="selfUnbind"></self-Unbind>
    </a-spin>
  </div>
</template>

<script>
import TokenCache from '@/utils/cache/TokenCache'
import ChangeUser from '@/views/Home/ChangeUser'
import RegistUser from '@/views/Home/RegistUser'
import UserInfoSearch from '@/views/Home/UserInfoSearch'
import StorageSearch from '@/views/Home/StorageSearch'
import SelfPassCard from '@/views/Home/SelfPassCard'
import SelfUnbind from '@/views/Home/SelfUnbind'
export default {
  components: {
    ChangeUser,
    RegistUser,
    UserInfoSearch,
    StorageSearch,
    SelfPassCard,
    SelfUnbind
  },
  data () {
    return {
      loading: false,
      customActiveKey: 'tab1',
      form: this.$form.createForm(this)
    }
  },
  mounted () {
    const userName = localStorage.getItem('user')
    const password = localStorage.getItem('pass')
    if (userName && password) {
      this.form.setFieldsValue({ userName, password, savePwd: true })
    }
  },
  methods: {
    handleTabClick (key) {
      this.customActiveKey = key
      // this.form.resetFields()
    },
    hanldleChangeUser () {
      this.$refs.editForm.openForm()
    },
    hanldleRegist () {
      this.$refs.register.openForm()
    },
    hanldleUserSearch () {
      this.$refs.userSearch.openForm()
    },
    hanldleStorage () {
      this.$refs.storageSearch.openForm()
    },
    hanldleSelfPassCard () {
      this.$refs.selfPassCard.openForm()
    },
    hanldleSelfUnbind () {
      this.$refs.selfUnbind.openForm()
    },
    handleSubmit (e) {
      e.preventDefault()

      this.form.validateFields((errors, values) => {
        // 校验成功
        if (!errors) {
          this.loading = true
          this.$http.post('/myauth/web/login', values).then(resJson => {
            this.loading = false

            if (resJson.success) {
              TokenCache.setToken(resJson.result.token)
              localStorage.setItem('userInfo', JSON.stringify(resJson.result))
              // 保存密码
              if (values.savePwd) {
                localStorage.setItem('user', values.user)
                localStorage.setItem('pass', values.pass)
              } else {
                localStorage.removeItem('user')
                localStorage.removeItem('pass')
              }
              this.$router.push({ path: '/' })
            } else {
              this.$message.error(resJson.msg)
              console.info('请求失败', resJson)
            }
          })
        }
      })
    }
  }
}
</script>

<style lang="less" scoped>
.user-layout-login {
  label {
    font-size: 14px;
  }

  .getCaptcha {
    display: block;
    width: 100%;
    height: 40px;
  }

  .forge-password {
    font-size: 14px;
  }

  button.login-button {
    margin: 5%;
    padding: 0 15px;
    font-size: 16px;
    height: 40px;
    width: 40%;
  }

  .user-login-other {
    text-align: left;
    margin-top: 24px;
    line-height: 22px;

    .item-icon {
      font-size: 24px;
      color: rgba(0, 0, 0, 0.2);
      margin-left: 16px;
      vertical-align: middle;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: #1890ff;
      }
    }

    .register {
      float: right;
    }
  }
}
</style>
