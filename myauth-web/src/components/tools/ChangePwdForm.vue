<template>
  <a-modal
    title="修改密码"
    width="40%"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-spin :spinning="confirmLoading">
      <a-form :form="form">
        <a-form-item label="原密码" :labelCol="labelCol" :wrapperCol="wrapperCol">
          <a-input type="password" v-decorator="['nowPass', { rules: [{ required: true, message: '必填' }] }]" />
        </a-form-item>
        <a-form-item label="新密码" :labelCol="labelCol" :wrapperCol="wrapperCol">
          <a-input type="password" v-decorator="['newPass', { rules: [{ required: true, message: '必填' }] }]" />
        </a-form-item>
      </a-form>
    </a-spin>
  </a-modal>
</template>

<script>
import OperatorCache from '@/utils/cache/OperatorCache'
import TokenCache from '@/utils/cache/TokenCache'

export default {
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
          this.$http.post('/myauth/web/editPass', this.entity).then(resJson => {
            this.confirmLoading = false

            if (resJson.success) {
              this.$message.success('操作成功，请重新登陆!')
              setTimeout(() => {
                TokenCache.deleteToken()
                OperatorCache.clear()
                location.reload()
                this.visible = false
              }, 1000)
            } else {
              this.$message.error(resJson.msg)
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
