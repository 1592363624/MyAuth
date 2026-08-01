<template>
  <a-modal
    width="40%"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
    @cancel="()=>{this.visible=false}"
  >
    <div slot="title">
      {{ title }}
      <!-- <a-tooltip>
        <div slot="title">
          <pre>填写说明：
1.收费模式
——1.1.账号+卡密
————账号可以为机器人QQ、机器码、卡密
————此时user为当前账号
————此时pass为空
————此时newuser为新账号
————此时ckey为最后一次使用的卡密
——1.2.账号+密码
————此时user为当前账号
————此时pass为当前密码
————此时newuser为新账号
————此时ckey为空
——1.3.账号+密码+卡密
————此时user为当前账号
————此时pass为当前密码
————此时newuser为新账号
————此时ckey为空
2.免费模式
——2.1.账号+密码
————此时user为当前账号
————此时pass为当前密码
————此时newuser为新账号
————此时ckey为空
——2.2.账号
————无法修改</pre>
        </div>
        <a-icon type="question-circle" />
      </a-tooltip> -->
    </div>
    <a-spin :spinning="confirmLoading">
      <a-form-model
        ref="form"
        :model="entity"
        :rules="rules"
        v-bind="layout"
      >
        <a-form-model-item
          label="账号"
          prop="user"
        >
          <a-input
            v-model="entity.user"
            autocomplete="off"
          />
        </a-form-model-item>

        <a-form-model-item
          label="密码"
          prop="pass"
        >
          <a-input-password
            v-model="entity.pass"
            placeholder="请输入密码"
          />
        </a-form-model-item>
        <a-form-model-item
          label="最后使用卡密"
          prop="ckey"
        >
          <a-input
            v-model="entity.ckey"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="新账号"
          prop="newUser"
        >
          <a-input
            v-model="entity.newUser"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="软件"
          prop="skey"
        >
          <a-select
            v-model="entity.skey"
            allowClear
            showSearch
            :filterOption="filterOption"
            placeholder="软件"
          >
            <a-select-option
              v-for="item in softListEx"
              :key="item.skey"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
      </a-form-model>
    </a-spin>
  </a-modal>
</template>

<script>
export default {
  data () {
    return {
      layout: {
        labelCol: { span: 5 },
        wrapperCol: { span: 18 }
      },
      visible: false,
      confirmLoading: false,
      entity: {},
      softListEx: [],
      rules: {
        user: [{ required: true, message: '必填' }],
        newUser: [{ required: true, message: '必填' }],
        skey: [{ required: true, message: '必填' }]
      },
      title: '修改用户'
    }
  },
  methods: {
    init () {
      this.visible = true
      this.entity = {}
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
      this.getSoftListEx()
    },
    openForm () {
      this.init()
    },
    handleSubmit () {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        const url = '/myauth/web/selfChangeUser'
        this.confirmLoading = true
        if (url) {
          this.$http.post(url, this.entity).then(resJson => {
            this.confirmLoading = false

            if (resJson.success) {
              this.$message.success(resJson.msg)
              this.visible = false
            } else {
              this.$message.error(resJson.msg)
            }
          })
        }
      })
    },
    getSoftListEx () {
      this.$http.get('/myauth/web/getSoftListSimple').then(resJson => {
        if (resJson.success) {
          this.softListEx = resJson.result
        } else {
          this.$message.error(resJson.msg)
          console.error(resJson)
        }
      })
    },
    filterOption (input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    }
  }
}
</script>
