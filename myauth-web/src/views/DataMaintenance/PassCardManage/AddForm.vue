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
          >
            <a-select-option
              v-for="item in softListEx"
              :key="item.id"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="秒数"
          prop="seconds"
        >
          <a-input-group compact>
            <a-input-number
              v-model="entity.seconds"
              :min="-1"
            />
            <div style="margin:5px">快捷选择：</div>

            <a-input-number
              v-model="timeValue"
              :min="0"
            />
            <a-select
              allowClear
              v-model="timeType"
              style="width:80px"
            >
              <a-select-option :key="1">分钟</a-select-option>
              <a-select-option :key="2">小时</a-select-option>
              <a-select-option :key="3">天</a-select-option>
              <a-select-option :key="4">周</a-select-option>
              <a-select-option :key="5">月(30天)</a-select-option>
              <a-select-option :key="6">年(365天)</a-select-option>
              <a-select-option :key="-1">永久</a-select-option>
            </a-select>
            <a-button
              type="primary"
              style="margin-left:5px"
              @click="creatTimesapn"
            >生成</a-button>
          </a-input-group>
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
import ClipboardHelper from '@/utils/helper/ClipboardHelper'
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
        count: [{ required: true, message: '必填' }]
        // point: [{ required: true, message: '必填' }]
      },
      softListEx: [],
      title: '编辑卡密',
      softId: null,
      timeValue: 0,
      timeType: 3
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
        const url = this.behaviorType == 1 ? '/myauth/web/addCard' : this.behaviorType == 2 ? '/myauth/web/updCard' : ''
        this.confirmLoading = true
        if (url) {
          this.$http.post(url, this.entity).then(resJson => {
            this.confirmLoading = false

            if (resJson.success) {
              this.$message.success(resJson.msg + '，新生成的卡密已复制进剪切板，注意覆盖以及使用！！！')
              this.afterSubmit()
              ClipboardHelper.copyToClipboard(resJson.result.ckeyStr)
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
    creatTimesapn () {
      switch (this.timeType) {
        case -1:this.entity.seconds = -1; break
        case 1:this.entity.seconds = this.timeValue * 60; break
        case 2:this.entity.seconds = this.timeValue * 60 * 60; break
        case 3:this.entity.seconds = this.timeValue * 60 * 60 * 24; break
        case 4:this.entity.seconds = this.timeValue * 60 * 60 * 24 * 7; break
        case 5:this.entity.seconds = this.timeValue * 60 * 60 * 24 * 30; break
        case 6:this.entity.seconds = this.timeValue * 60 * 60 * 24 * 365; break
        default:break
      }
      this.entity = { ...this.entity }
    }
  }
}
</script>
