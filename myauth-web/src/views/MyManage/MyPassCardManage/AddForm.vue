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
          label="软件"
          prop="fromSoftId"
        >
          <a-select
            v-model="entity.fromSoftId"
            @select="softSelect"
          >
            <a-select-option
              v-for="item in mySoftListEx"
              :key="item.id"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="类型"
          prop="type"
        >
          <a-select
            v-model="entity.type"
            @select="select"
          >
            <a-select-option :key="1">期限</a-select-option>
            <a-select-option :key="2">余额</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="策略"
          prop="strategyId"
        >
          <a-select
            v-model="entity.strategyId"
            allowClear
            showSearch
            :filterOption="filterOption"
          >
            <a-select-option
              v-for="item in strategyListEx"
              :key="item.id"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="卡密前缀"
          prop="prefix"
        >
          <a-input v-model="entity.prefix" />
        </a-form-model-item>
        <a-form-model-item
          label="张数"
          prop="count"
        >
          <a-input-number
            v-model="entity.count"
            :min="0"
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
        type: [{ required: true, message: '必填' }],
        strategyId: [{ required: true, message: '必填' }],
        count: [{ required: true, message: '必填' }]
        // point: [{ required: true, message: '必填' }]
      },
      strategyListEx: [],
      mySoftListEx: [],
      fromSoftId: null,
      title: '生成卡密',
      softId: null
    }
  },
  mounted () {
  },
  methods: {
    init () {
      this.visible = true
      this.entity = { type: 1 }
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
      this.getMySoftListEx()
    },
    openForm (title) {
      this.init()
      if (title) {
        this.title = title
      }
    },
    handleSubmit () {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        const url = '/myauth/web/addMyCard'
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
    getStrategyListEx (fromSoftId = null, name = '', type = 1) {
      this.$http.post('/myauth/web/getStrategyListEx', { fromSoftId, name, type }).then(resJson => {
        if (resJson.success) {
          this.strategyListEx = resJson.result
          // 删除对象属性以实现最简单的表单清除
          const { strategyId, ...entity } = this.entity
          this.entity = entity
          // this.entity.strategyId = this.strategyListEx && this.strategyListEx.length ? this.strategyListEx[0].id : undefined
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
    },
    getMySoftListEx () {
      this.$http.post('/myauth/web/getMySoftListEx').then(resJson => {
        if (resJson.success) {
          this.mySoftListEx = resJson.result
          if (resJson.result.length > 0) {
            this.softId = resJson.result[0].id
          }
        } else {
          this.$message.error(resJson.msg)
          console.error(resJson)
        }
      })
    },
    select (value) {
      this.getStrategyListEx(this.fromSoftId, '', value)
    },
    softSelect (value) {
      this.fromSoftId = value
      this.getStrategyListEx(value, '', this.entity.type)
    }
  }
}
</script>
