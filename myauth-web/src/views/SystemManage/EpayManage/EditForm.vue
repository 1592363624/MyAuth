<template>
  <div>
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
            label="通道标识"
            prop="driver"
          >
            <a-input
              v-model="entity.driver"
              autocomplete="off"
              :disabled="true"
            />
          </a-form-model-item>
          <a-form-model-item
            label="通道名称"
            prop="name"
          >
            <a-input
              v-model="entity.name"
              autocomplete="off"
            />
          </a-form-model-item>
          <a-form-model-item
            label="通道排序"
            prop="sort"
          >
            <a-input-number
              v-model="entity.sort"
              placeholder="越小越靠前"
              autocomplete="off"
            />
          </a-form-model-item>
          <a-form-model-item
            label="通道说明"
            prop="content"
          >
            <a-input
              v-model="entity.content"
              autocomplete="off"
            />
          </a-form-model-item>
          <a-form-model-item
            label="通道配置"
            prop="config"
          >
            <a-textarea
              :rows="4"
              v-model="entity.config"
              autocomplete="off"
              :disabled="true"
            />
            <a-button @click="getConfigList">使用参数编辑器配置</a-button>
          </a-form-model-item>
          <a-form-model-item
            label="是否启用"
            prop="enabled"
          >
            <a-select
              v-model="entity.enabled"
              placeholder="是否启用"
            >
              <a-select-option :key="1">开启</a-select-option>
              <a-select-option :key="0">关闭</a-select-option>
            </a-select>
          </a-form-model-item>
        </a-form-model>

      </a-spin>
    </a-modal>
    <a-modal
      title="配置编辑"
      width="30%"
      :visible="configVisible"
      @ok="subConfig"
      @cancel="()=>{this.configVisible=false}"
      :confirmLoading="loading"
    >
      <a-form-model>
        <a-form-model-item
          :rules="rules"
          v-for="(item, index) in configList"
          :key="item.id"
          :label="item.fieldContent"
        >
          <a-input
            v-model="configList[index]['fieldText']"
            autocomplete="off"
          >
          </a-input>
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </div>
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
        labelCol: { span: 3 },
        wrapperCol: { span: 20 }
      },
      loading: false,
      visible: false,
      confirmLoading: false,
      entity: {},
      rules: {
        driver: [{ required: true, message: '必填' }],
        sort: [{ required: true, message: '必填' }],
        name: [{ required: true, message: '必填' }],
        content: [{ required: true, message: '必填' }],
        config: [{ required: true, message: '必填' }],
        enabled: [{ required: true, message: '必填' }]
      },
      softListEx: [],
      title: '',
      softId: null,
      configVisible: false,
      configList: {}
    }
  },
  mounted () {
  },
  methods: {
    init () {
      this.visible = true
      // this.entity = {}
      this.$nextTick(() => {
        // this.$refs['form'].clearValidate()
      })
    },
    openForm (entity, title) {
      this.init()
      if (entity) {
        this.entity = entity
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
        const url = '/myauth/web/editEpay'
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
    subConfig () {
      // console.info(this.configList)
      this.entity.config = JSON.stringify(this.configList)
      this.configList = []
      this.configVisible = false
    },
    getConfigList () {
      this.configVisible = true
      this.configList = JSON.parse(this.entity.config)
    },
    filterOption (input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    }
  }
}
</script>
