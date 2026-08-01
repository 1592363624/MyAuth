<template>
  <a-modal
    width="40%"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
    @cancel="handleSubmit"
  >
    <div slot="title">
      {{ title }}
    </div>
    <a-spin :spinning="confirmLoading">
      <a-form-model
        ref="form"
        :model="entity"
        :rules="rules"
        v-bind="layout"
      >
        <a-form-model-item
          label="关键字"
          prop="type"
        >
          <a-input
            v-model="entity.type"
            autocomplete="off"
          />

        </a-form-model-item>

        <a-form-model-item
          label="查询内容"
          prop="content"
        >
          <a-textarea
            v-model="entity.content"
            autocomplete="off"
            :rows="4"
          />
        </a-form-model-item>

        <a-form-model-item
          label="软件key"
          prop="skey"
        >
          <a-input
            v-model="entity.skey"
            autocomplete="off"
          />
        </a-form-model-item>

      </a-form-model>
      <div style="text-align:center;">
        <a-button
          @click="searchStorage"
          type="primary"
          icon="search"
          style="width:60%;"
        >查询</a-button>
      </div>
      <a-card
        v-if="data.id"
        title="额外存储信息"
        style="margin-top:5px;"
      >
        <a-form
          ref="form2"
          v-bind="layout"
        >
          <a-form-item label="软件">
            <a-input
              v-model="data.fromSoftName"
              autocomplete="off"
              disabled
            />
          </a-form-item>
          <a-form-item label="内容">
            <a-input
              v-model="data.content"
              autocomplete="off"
              disabled
            />
          </a-form-item>
          <a-form-item label="次数">
            <a-input
              v-model="data.number"
              autocomplete="off"
              disabled
            />
          </a-form-item>
          <a-form-item label="状态">
            <a-select
              v-model="data.status"
              disabled
            >
              <a-select-option :key="1">正常</a-select-option>
              <a-select-option :key="0">禁用</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="备注">
            <a-textarea
              v-model="data.remark"
              autocomplete="off"
              :rows="4"
              disabled
            />
          </a-form-item>
        </a-form>
      </a-card>
    </a-spin>
  </a-modal>
</template>

<script>
import moment from 'moment'
export default {
  data () {
    return {
      layout: {
        labelCol: { span: 3 },
        wrapperCol: { span: 18 }
      },
      visible: false,
      confirmLoading: false,
      entity: {},
      data: {},
      softListEx: [],
      rules: {
        type: [{ required: true, message: '必填' }],
        content: [{ required: true, message: '必填' }],
        skey: [{ required: true, message: '必填' }]
      },
      title: '查询额外存储信息'
    }
  },
  methods: {
    moment,
    init () {
      this.visible = true
      this.entity = {}
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    openForm () {
      this.init()
    },
    searchStorage () {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        let url = '/myauth/web/queryStorage'
        url += '?type=' + this.entity.type + '&content=' + this.entity.content + (this.entity.skey ? ('&skey=' + this.entity.skey) : '')
        this.confirmLoading = true
        if (url) {
          this.$http.get(url).then(resJson => {
            this.confirmLoading = false

            if (resJson.success) {
              this.data = resJson.result
              this.$message.success(resJson.msg)
            } else {
              this.$message.error(resJson.msg)
            }
          })
        }
      })
    },
    handleSubmit () {
      this.visible = false
      this.data = {}
    }
  }
}
</script>
