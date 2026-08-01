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
          label="所属软件"
          prop="fromSoftId"
        >
          <a-select
            v-model="entity.fromSoftId"
            allowClear
            showSearch
            :filterOption="filterOption"
            placeholder="所属软件"
            @change="softChange"
          >
            <a-select-option
              v-for="item in softListEx"
              :key="item.id"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="额外存储类型"
          prop="fromStorageTypeId"
        >
          <a-select
            v-model="entity.fromStorageTypeId"
            allowClear
            showSearch
            :filterOption="filterOption"
            placeholder="额外存储类型"
          >
            <a-select-option
              v-for="item in storageTypeListEx"
              :key="item.id"
            >{{ item.type }}</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="内容"
          prop="content"
        >
          <a-textarea
            v-model="entity.content"
            autocomplete="off"
            :rows="4"
          />
        </a-form-model-item>
        <a-form-model-item
          label="状态"
          prop="status"
        >
          <a-select v-model="entity.status">
            <a-select-option :key="1">正常</a-select-option>
            <a-select-option :key="0">禁用</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="备注"
          prop="remark"
        >
          <a-textarea
            v-model="entity.remark"
            autocomplete="off"
            :rows="4"
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
        fromStorageTypeId: [{ required: true, message: '必填' }],
        content: [{ required: true, message: '必填' }],
        status: [{ required: true, message: '必填' }]
      },
      softListEx: [],
      storageTypeListEx: [],
      title: '编辑额外存储',
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
      this.getSoftListEx()
    },
    openForm (id, title) {
      this.init()
      if (id) {
        this.$http.post('/myauth/web/getStorage', { id }).then(resJson => {
          if (resJson.success) {
            this.entity = resJson.result
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
        const url = this.behaviorType == 1 ? '/myauth/web/addStorage' : this.behaviorType == 2 ? '/myauth/web/updStorage' : ''
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
    softChange (value) {
      if (value) { this.getStorageTypeListEx(null, value) }
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
    getStorageTypeListEx (type = '', fromSoftId) {
      this.$http.post('/myauth/web/getStorageTypeListEx', { type, fromSoftId }).then(resJson => {
        if (resJson.success) {
          this.storageTypeListEx = resJson.result
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
