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
          label="策略名称"
          prop="name"
        >
          <a-input
            v-model="entity.name"
            autocomplete="off"
          />
        </a-form-model-item>
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
          >
            <a-select-option
              v-for="item in softListEx"
              :key="item.id"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>

        <a-form-model-item
          label="价格"
          prop="price"
        >
          <a-input
            v-model="entity.price"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="面额"
          prop="value"
        >
          <a-input
            v-model="entity.value"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="类型"
          prop="type"
        >
          <a-select
            v-model="entity.type"
            placeholder="类型"
          >
            <a-select-option :key="1">期限</a-select-option>
            <a-select-option :key="2">余额</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="状态"
          prop="status"
        >
          <a-select
            v-model="entity.status"
            placeholder="状态"
          >
            <a-select-option :key="1">正常</a-select-option>
            <a-select-option :key="0">禁用</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="排序"
          prop="sort"
        >
          <a-input-number
            v-model="entity.sort"
            autocomplete="off"
          />
        </a-form-model-item>
      </a-form-model>
    </a-spin>
  </a-modal>
</template>

<script>
import moment from 'moment'
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
        name: [{ required: true, message: '必填' }],
        fromSoftId: [{ required: true, message: '必填' }],
        value: [{ required: true, message: '必填' }],
        price: [{ required: true, message: '必填' }],
        sort: [{ required: true, message: '必填' }],
        status: [{ required: true, message: '必填' }],
        type: [{ required: true, message: '必填' }]
      },
      softListEx: [],
      title: '编辑策略',
      menuTreeData: [],
      replaceFields: { children: 'children', title: 'title', key: 'id', value: 'id' }
    }
  },
  mounted () {
  },
  methods: {
    moment,
    init () {
      this.visible = true
      this.entity = {}
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
      this.getMenuTreeData()
      this.getSoftListEx()
    },
    openForm (id, title) {
      this.init()
      if (id) {
        this.$http.post('/myauth/web/getStrategy', { id }).then(resJson => {
          if (resJson.success) {
            const temp = { ...resJson.result }
            temp.authTime = temp.authTime == -1 ? null : moment.unix(resJson.result.authTime)
            this.entity = temp
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
        const url = this.behaviorType == 1 ? '/myauth/web/addStrategy' : this.behaviorType == 2 ? '/myauth/web/updStrategy' : ''
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
    getMenuTreeData () {
      this.$http
        .get('/myauth/web/getMenuListAll')
        .then(resJson => {
          this.loading = false
          if (resJson.success) {
            this.menuTreeData = resJson.result
          } else {
            this.$message.error(resJson.msg)
          }
        })
    },
    getSoftListEx (name = '') {
      this.$http.post('/myauth/web/getSoftListEx', { name }).then(resJson => {
        if (resJson.success) {
          this.softListEx = resJson.result
          if (resJson.result.length > 0) {
            this.softId = resJson.result[0].id
          }
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
