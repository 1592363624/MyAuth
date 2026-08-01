<template>
  <a-modal
    :title="this.title"
    width="40%"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
    @cancel="()=>{this.visible=false}"
  >
    <a-spin :spinning="confirmLoading">
      <a-form-model
        ref="form"
        :model="entity"
        :rules="rules"
        v-bind="layout"
      >
        <a-form-model-item
          label="秒数"
          prop="seconds"
        >
          <a-input-number
            v-model="entity.seconds"
            :min="-1"
          />
        </a-form-model-item>
        <a-form-model-item
          label="点数"
          prop="point"
        >
          <a-input-number
            v-model="entity.point"
            :min="0"
          />
        </a-form-model-item>
        <a-form-model-item
          label="卡密"
          prop="ckey"
        >
          <a-input v-model="entity.ckey" />
        </a-form-model-item>
      </a-form-model>
    </a-spin>
  </a-modal>
</template>

<script>
export default {
  props: {
    afterSubmit: {
      type: Function,
      default: null
    }
  },
  data () {
    return {
      layout: {
        labelCol: { span: 5 },
        wrapperCol: { span: 18 }
      },
      visible: false,
      confirmLoading: false,
      entity: {},
      rules: {},
      title: '编辑卡密',
      softId: null
    }
  },
  mounted () {
  },
  methods: {
    init () {
      this.visible = true
      this.entity = {}
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    openForm (id, softId, title) {
      this.init()
      if (softId) {
        this.softId = softId
        // 修改
        if (id) {
          this.$http.post('/myauth/web/getCard', { id }).then(resJson => {
            if (resJson.success) {
              this.entity = resJson.result
              this.$nextTick(() => {
                this.entity.fromSoftId = softId
              })
            } else {
              this.$message.error(resJson.msg)
            }
          })
        }
      }
      if (title) {
        this.title = title
      }
    },
    handleSubmit () {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (!this.entity.point && !this.entity.seconds) {
          this.$message.error('点数和秒数不可同时为空')
          return
        }
        const url = '/myauth/web/updCard'
        this.confirmLoading = true
        if (url) {
          this.$http.post(url, this.entity).then(resJson => {
            this.confirmLoading = false

            if (resJson.success) {
              this.$message.success(resJson.msg)
              this.afterSubmit()
              this.visible = false
            } else {
              this.$message.error(resJson.msg)
            }
          })
        }
      })
    }
  }
}
</script>
