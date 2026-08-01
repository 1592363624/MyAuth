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
            <a-select-option
              v-for="item in softListEx"
              :key="item.id"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="事件名称"
          prop="name"
        >
          <a-input
            v-model="entity.name"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="点数变动值"
          prop="point"
        >
          <a-input-number v-model="entity.point" />
        </a-form-model-item>
        <a-form-model-item
          label="秒数变动值"
          prop="seconds"
        >
          <a-input-number v-model="entity.seconds" />
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
          label="每日限制次数"
          prop="dayCount"
          extra="0为不限制(按用户日志和事件名字来进行限制,SO 请不要随意删除修改这两项)"
        >
          <a-input-number v-model="entity.dayCount" />
        </a-form-model-item>
        <a-form-model-item
          label="总共限制次数"
          prop="allCount"
          extra="0为不限制(按用户日志和事件名字来进行限制,SO 请不要随意删除修改这两项)"
        >
          <a-input-number v-model="entity.allCount" />
        </a-form-model-item>
        <a-form-model-item
          label="允许触发日期"
          prop="runTime"
          extra="为空为不限制"
        >
          <a-date-picker
            v-model="startValue"
            :disabled-date="disabledStartDate"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="Start"
            @openChange="handleStartOpenChange"
            @ok="startOnOk"
          />
          -
          <a-date-picker
            v-model="endValue"
            :disabled-date="disabledEndDate"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="End"
            :open="endOpen"
            @openChange="handleEndOpenChange"
            @ok="endOnOk"
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
        fromSoftId: [{ required: true, message: '必填' }],
        keywnameord: [{ required: true, message: '必填' }]
      },
      softListEx: [],
      title: '编辑回复',
      startValue: null,
      endValue: null,
      endOpen: false,
      softId: null
    }
  },
  mounted () {
  },
  watch: {
    startValue (val) {
      if (val === null) {
        this.entity.startTime = 0
        console.log('startValue', val)
      } else {
        this.entity.startTime = val.unix()
        console.log('startValue', val.unix())
      }
    },
    endValue (val) {
      if (val === null) {
        this.entity.endTime = 0
        console.log('endValue', val)
      } else {
        this.entity.endTime = val.unix()
        console.log('endValue', val.unix())
      }
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
      this.startValue = null
      this.endValue = null
    },
    openForm (id, title) {
      this.init()
      if (id) {
        this.$http.post('/myauth/web/getEvent', { id }).then(resJson => {
          if (resJson.success) {
            this.entity = resJson.result
            this.startValue = this.entity.startTime === null || this.entity.startTime === 0 ? null : moment(this.getFormatDate(this.entity.startTime), 'YYYY-MM-DD HH:mm:ss')
            this.endValue = this.entity.endTime === null || this.entity.endTime === 0 ? null : moment(this.getFormatDate(this.entity.endTime), 'YYYY/MM/DD HH:mm:ss')
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
        const url = this.behaviorType == 1 ? '/myauth/web/addEvent' : this.behaviorType == 2 ? '/myauth/web/updEvent' : ''
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
    },
    getFormatDate (nS) {
      return this.TimeHelper.getFormatDate(nS)
    },
    disabledStartDate (startValue) {
      const endValue = this.endValue
      if (!startValue || !endValue) {
        return false
      }
      return startValue.valueOf() > endValue.valueOf()
    },
    disabledEndDate (endValue) {
      const startValue = this.startValue
      if (!endValue || !startValue) {
        return false
      }
      return startValue.valueOf() >= endValue.valueOf()
    },
    handleStartOpenChange (open) {
      if (!open) {
        this.endOpen = true
      }
    },
    handleEndOpenChange (open) {
      this.endOpen = open
    },
    startOnOk (value) {
      this.entity.startTime = value.unix()
    },
    endOnOk (value) {
      this.entity.endTime = value.unix()
    }
  }
}
</script>
