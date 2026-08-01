<template>
  <a-modal
    title="修改QQ"
    width="40%"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-spin :spinning="confirmLoading">
      <a-form :form="form">
        <a-form-item
          label="新QQ"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-input v-decorator="['newQQ', { rules: [{ required: true, message: '必填' }] }]" />
        </a-form-item>
      </a-form>
    </a-spin>
  </a-modal>
</template>

<script>

export default {
  props: {
    afterSubmit: {
      type: Function,
      default: () => {}
    }
  },
  data () {
    return {
      form: this.$form.createForm(this),
      labelCol: { xs: { span: 24 }, sm: { span: 7 } },
      wrapperCol: { xs: { span: 24 }, sm: { span: 13 } },
      visible: false,
      confirmLoading: false,
      formFields: {},
      entity: {}
    }
  },
  methods: {
    init () {
      this.entity = {}
      this.visible = true
      this.form.resetFields()
    },
    open () {
      this.init()
    },
    handleSubmit () {
      this.form.validateFields((errors, values) => {
        // 校验成功
        if (!errors) {
          this.entity = Object.assign(this.entity, this.form.getFieldsValue())

          this.confirmLoading = true
          this.$http.post('/myauth/web/editQQ', { qq: this.entity.newQQ }).then(resJson => {
            this.confirmLoading = false
            if (resJson.success) {
              this.$message.info('成功,' + resJson.msg)
              const userInfo = JSON.parse(localStorage.getItem('userInfo'))
              const user = { ...userInfo }
              user.qq = this.entity.newQQ
              localStorage.setItem('userInfo', JSON.stringify(user))
              setTimeout(() => {
                this.handleCancel()
                this.afterSubmit()
              }, 500)
            } else {
              this.$message.error('失败,' + resJson.msg)
            }
          })
        }
      })
    },
    handleCancel () {
      this.visible = false
    }
  }
}
</script>
