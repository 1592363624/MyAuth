<template>
  <a-modal
    :title="this.title"
    width="40%"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
    @cancel="()=>{this.visible=false;}"
  >
    <a-spin :spinning="confirmLoading">
      <a-form-model
        ref="form"
        :model="entity"
        :rules="rules"
        v-bind="layout"
      >
        <a-form-model-item
          label="所属软件"
          prop="fromSoftId"
          v-if="this.fromSoftId"
        >
          <a-select
            v-model="entity.fromSoftId"
            allowClear
            show-search
            :filterOption="filterOption"
            placeholder="所属软件"
          >
            <a-select-option
              v-for="item in softListEx"
              :key="item.id"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="函数名称"
          prop="jsFun"
        >
          <a-input
            v-model="entity.jsFun"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="函数内容"
          prop="jsContent"
        >
          <a-textarea
            :rows="4"
            v-model="entity.jsContent"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="状态"
          prop="status"
        >
          <a-select v-model="entity.status">
            <a-select-option :key="1">正常</a-select-option>
            <a-select-option :key="0">停用</a-select-option>
          </a-select>
        </a-form-model-item>

        <a-form-model-item
          label="备注"
          prop="remark"
        >
          <a-textarea
            :rows="4"
            v-model="entity.remark"
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
        fromSoftId: [{ required: true, message: '必填' }],
        jsFun: [{ required: true, message: '必填' }],
        status: [{ required: true, message: '必填' }],
        jsContent: [{ required: true, message: '必填' }]
      },
      fromSoftId: 0,
      softListEx: [],
      vkey: '',
      title: '编辑函数'
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
    openForm (softId, id, title) {
      this.init()
      this.fromSoftId = softId
      if (id) {
        this.$http.post('/myauth/web/getJs', { id }).then(resJson => {
          if (resJson.success) {
            this.entity = resJson.result
            this.$nextTick(() => {
              this.entity.fromSoftId = softId
            })
          } else {
            this.$message.error(resJson.msg)
            console.error(resJson)
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
        const url = this.behaviorType == 1 ? '/myauth/web/addJs' : this.behaviorType == 2 ? '/myauth/web/updJs' : ''
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
    getSoftListEx (name = '') {
      this.$http.post('/myauth/web/getSoftListEx', { name }).then(resJson => {
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
