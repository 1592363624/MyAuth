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
          label="关键词"
          prop="keyword"
        >
          <a-input
            v-model="entity.keyword"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="回复内容"
          prop="msg"
        >
          <a-textarea
            :rows="4"
            v-model="entity.msg"
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
          label="所属版本"
          prop="fromVerId"
        >
          <a-select
            v-model="entity.fromVerId"
            allowClear
          >
            <a-select-option
              v-for="item in verList"
              :key="item.id"
            >{{ item.ver }}</a-select-option>
          </a-select>
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
        keyword: [{ required: true, message: '必填' }],
        msg: [{ required: true, message: '必填' }],
        status: [{ required: true, message: '必填' }]
      },
      softListEx: [],
      title: '编辑回复',
      verList: [],
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
    openForm (id, softId, title) {
      this.init()
      if (softId) {
        this.softId = softId
        this.getVerList(softId)
        // 修改
        if (id) {
          this.$http.post('/myauth/web/getMsg', { id }).then(resJson => {
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
        const url = this.behaviorType == 1 ? '/myauth/web/addMsg' : this.behaviorType == 2 ? '/myauth/web/updMsg' : ''
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
      if (value) { this.getVerList(value) }
    },
    getVerList (softId) {
      this.$http.post('/myauth/web/getVersionListEx', { fromSoftId: softId }).then(resJson => {
        if (resJson.success) {
          this.verList = resJson.result.sort(function (a, b) {
              const x = a.ver.toLowerCase()
              const y = b.ver.toLowerCase()
              if (x < y) { return 1 }
              if (x > y) { return -1 }
              return 0
          })
          // console.info("获取到版本集合",resJson.result)
        } else {
          this.$message.error(resJson.msg)
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
