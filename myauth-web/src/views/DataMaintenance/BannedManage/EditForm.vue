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
          v-if="!this.entity.id"
        >
          <a-select
            v-model="entity.fromSoftId"
            allowClear
            showSearch
            :filterOption="filterOption"
            placeholder="所属软件"
            @change="softChange"
          >
            <a-select-option :key="0">全局</a-select-option>
            <a-select-option
              v-for="item in softListEx"
              :key="item.id"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="封禁对象"
          prop="value"
        >
          <a-input
            v-model="entity.value"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="封禁到期时间"
          prop="toTime"
          extra="不填则为永久"
        >
          <a-date-picker
            v-model="entity.toTime"
            show-time
            valueFormat="X"
            placeholder="封禁到期时间"
          />
        </a-form-model-item>
        <a-form-model-item
          label="封禁原因"
          prop="why"
        >
          <a-input
            v-model="entity.why"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="封禁类型"
          prop="type"
        >
          <a-select v-model="entity.type">
            <a-select-option :key="1">机器码</a-select-option>
            <a-select-option :key="2">IP</a-select-option>
            <a-select-option :key="3">用户</a-select-option>
          </a-select>
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
        fromSoftId: [{ required: true, message: '必填' }],
        value: [{ required: true, message: '必填' }],
        type: [{ required: true, message: '必填' }]
      },
      softListEx: [],
      title: '编辑回复',
      softId: null
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
      this.getSoftListEx()
    },
    openForm (id, title) {
      this.init()
      if (id) {
        this.$http.post('/myauth/web/getBan', { id }).then(resJson => {
          if (resJson.success) {
            const temp = { ...resJson.result }
              temp.toTime = temp.toTime == -1 ? null : moment.unix(resJson.result.toTime)
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
        const entityX = { ...this.entity }
        try {
          entityX.toTime = this.entity.toTime ? this.entity.toTime.unix().toString() : -1
        } catch (error) {
          entityX.toTime = this.entity.toTime ? this.entity.toTime : -1
        }
        const url = this.behaviorType == 1 ? '/myauth/web/addBan' : this.behaviorType == 2 ? '/myauth/web/updBan' : ''
        this.confirmLoading = true
        if (url) {
          this.$http.post(url, entityX).then(resJson => {
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
