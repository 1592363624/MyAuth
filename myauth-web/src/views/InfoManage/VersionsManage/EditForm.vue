<template>
  <a-modal
    :title="this.title"
    width="40%"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
    @cancel="()=>{this.visible=false;this.isAddMsg = false}"
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
            show-search
            :filterOption="filterOption"
            placeholder="所属软件"
          >
            <a-select-option
              v-for="item in softListEx"
              :key="item.id"
              :value="item.id"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="版本号"
          prop="ver"
        >
          <a-input
            v-model="entity.ver"
            autocomplete="off"
          />
        </a-form-model-item>

        <a-form-model-item
          label="版本key"
          v-if="behaviorType==1"
        >
          <a-input
            v-model="entity.vkey"
            autocomplete="off"
            allowClear
          >
            <a-tooltip slot="addonAfter">
              <template slot="title">
                点击生成一个随机的uuid
              </template>
              <a-icon
                type="redo"
                @click="creatUuid"
              />
            </a-tooltip>

          </a-input>
        </a-form-model-item>

        <a-form-model-item
          label="更新日志"
          prop="updLog"
        >
          <a-textarea
            :rows="4"
            v-model="entity.updLog"
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
          label="更新模式"
          prop="updType"
        >
          <a-select v-model="entity.updType">
            <a-select-option :key="1">强制</a-select-option>
            <a-select-option :key="0">可选</a-select-option>
          </a-select>

        </a-form-model-item>

        <a-form-model-item
          label="是否添加回复"
          v-if="behaviorType==1"
        >
          <a-radio-group v-model="isAddMsg">
            <a-radio :value="false">
              不添加回复
            </a-radio>
            <a-radio :value="true">
              添加回复
            </a-radio>
          </a-radio-group>
        </a-form-model-item>

        <a-form-model-item
          label="关键词"
          prop="keyword"
          v-if="isAddMsg && behaviorType==1"
        >
          <a-input
            v-model="entity.keyword"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="回复内容"
          prop="msg"
          v-if="isAddMsg && behaviorType==1"
        >
          <a-textarea
            :rows="4"
            v-model="entity.msg"
            autocomplete="off"
          />
        </a-form-model-item>

      </a-form-model>
    </a-spin>
  </a-modal>
</template>

<script>
const uuid = require('node-uuid')
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
        ver: [{ required: true, message: '必填' }],
        status: [{ required: true, message: '必填' }],
        updLog: [{ required: true, message: '必填' }],
        updType: [{ required: true, message: '必填' }]
      },
      fromSoftId: 0,
      softListEx: [],
      vkey: '',
      title: '编辑版本',

      isAddMsg: false
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
      // if(this.behaviorType==1){
      //   this.creatUuid()
      // }
    },
    openForm (softId, vkey, title) {
      this.init()
      if (softId) {
        this.fromSoftId = softId
        // 新增或编辑时，所属软件都默认带入当前选中的软件
        this.$nextTick(() => {
          this.entity.fromSoftId = softId
        })
        // 修改
        if (vkey) {
          this.vkey = vkey
          this.$http.post('/myauth/web/getVersion', { vkey }).then(resJson => {
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
        let url = this.behaviorType == 1 ? '/myauth/web/addVersion' : this.behaviorType == 2 ? '/myauth/web/updVersion' : ''
        url = this.isAddMsg ? '/myauth/web/addVersionAndMsg' : url
        this.isAddMsg = false
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
    },
    creatUuid () {
      const uid = uuid.v4()
      this.entity.vkey = uid.toUpperCase()
      const entity = { ...this.entity }
      this.entity = entity
    }
  }
}
</script>
