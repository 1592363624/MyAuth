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
——1.账号+卡密
————账号可以为机器人QQ、机器码、卡密
————此时user为账号
————此时pass为空
————此时ckey为卡密
——2.账号+密码
————先注册，再使用卡密授权期限
————此时user为账号
————此时pass为密码
————此时ckey为空
——3.账号+密码+卡密
————先注册，使用卡密授权期限
————此时user为账号
————此时pass为密码
————此时ckey为卡密
2.免费模式
——1.账号+密码
————此时user为账号
————此时pass为密码
——2.账号
————此时user可为机器码（如果要实现同一机器码多个账号，例如多开软件，可以用运行目录的MD5）
————此时pass可为空</pre>
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
          label="卡密"
          prop="ckey"
        >
          <a-input
            v-model="entity.ckey"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="用户名"
          prop="name"
        >
          <a-input
            v-model="entity.name"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="QQ"
          prop="qq"
        >
          <a-input
            v-model="entity.qq"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="备注"
          prop="remark"
        >
          <a-input
            v-model="entity.remark"
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
        skey: [{ required: true, message: '必填' }]
      },
      title: '注册账号'
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
        const url = '/myauth/web/selfRegister'
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
