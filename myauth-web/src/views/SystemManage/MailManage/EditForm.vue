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
          label="发送类型"
          prop="sendType"
        >
          <a-input
            v-model="entity.sendType"
            autocomplete="off"
            :disabled="true"
          />
        </a-form-model-item>
        <a-form-model-item
          label="发送开关"
          prop="sendSwitch"
        >
          <a-select v-model="entity.sendSwitch">
            <a-select-option :key="1">开启</a-select-option>
            <a-select-option :key="0">关闭</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="主题"
          prop="sendTheme"
        >
          <a-input
            v-model="entity.sendTheme"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="标题"
          prop="sendTitle"
        >
          <a-input
            v-model="entity.sendTitle"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="发送模板"
          prop="sendTemplates"
        >
          <a-textarea
            :rows="10"
            v-model="entity.sendTemplates"
            autocomplete="off"
          />
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
    },
    /** 行为，1新增，2更新 */
    behaviorType: {
      type: Number,
      default: 1
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
      rules: {
        sendType: [{ required: true, message: '必填' }]
      },
      softListEx: [],
      roleList: [],
      title: '',
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
    formatXML (xml, tab) {
    let formatted = ''; let indent = ''
    tab = tab || '    '
    xml.split(/>\s*</).forEach(function (node) {
        if (node.match(/^\/\w/)) indent = indent.substring(tab.length)
        formatted += indent + '<' + node + '>\r\n'
        if (node.match(/^<?\w[^>]*[^/]$/)) indent += tab
    })
    return formatted.substring(1, formatted.length - 3)
    },
    openForm (id, title) {
      this.init()
      if (id) {
        this.$http.post('/myauth/web/getMailSend', { id }).then(resJson => {
          if (resJson.success) {
            this.entity = resJson.result
            if (this.entity.sendTemplates) {
              this.entity.sendTemplates = this.formatXML(this.entity.sendTemplates)
            }
          } else {
            this.$message.error(resJson.msg)
          }
        })
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
        const url = this.behaviorType == 2 ? '/myauth/web/updMailSend' : ''
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
    },
    filterOption (input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    }
  }
}
</script>
