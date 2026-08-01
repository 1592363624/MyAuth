<template>
  <a-card :bordered="false">
    <a-form-model
      ref="form"
      :model="entity"
      :rules="rules"
    >
      <a-form-model-item
        label="网站标题"
        prop="seoTitle"
      >
        <a-input
          v-model="entity.seoTitle"
          autocomplete="off"
          :disabled="settingFlag"
        />
      </a-form-model-item>
      <a-form-model-item
        label="网站关键词"
        prop="seoKeywords"
      >
        <a-input
          v-model="entity.seoKeywords"
          autocomplete="off"
          :disabled="settingFlag"
        />
      </a-form-model-item>
      <a-form-model-item
        label="网站描述"
        prop="seoDescription"
      >
        <a-textarea
          v-model="entity.seoDescription"
          autocomplete="off"
          :disabled="settingFlag"
          :rows="4"
        />
      </a-form-model-item>
      <a-form-model-item
        label="开放接口key"
        prop="openApiKey"
      >
        <a-input
          v-model="entity.openApiKey"
          autocomplete="off"
          :disabled="settingFlag"
        />
      </a-form-model-item>
      <a-form-model-item
        label="自助注册"
        prop="selfRegisterStatus"
      >
        <a-select
          v-model="entity.selfRegisterStatus"
          autocomplete="off"
          :disabled="settingFlag"
        >
          <a-select-option :key="1">开启</a-select-option>
          <a-select-option :key="0">关闭</a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item
        label="自助修改账号"
        prop="selfChangeUserStatus"
      >
        <a-select
          v-model="entity.selfChangeUserStatus"
          autocomplete="off"
          :disabled="settingFlag"
        >
          <a-select-option :key="1">开启</a-select-option>
          <a-select-option :key="0">关闭</a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item
        label="自助使用卡密"
        prop="selfUseCkeyStatus"
      >
        <a-select
          v-model="entity.selfUseCkeyStatus"
          autocomplete="off"
          :disabled="settingFlag"
        >
          <a-select-option :key="1">开启</a-select-option>
          <a-select-option :key="0">关闭</a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item
        label="自助解绑"
        prop="selfUnbindStatus"
      >
        <a-select
          v-model="entity.selfUnbindStatus"
          autocomplete="off"
          :disabled="settingFlag"
        >
          <a-select-option :key="1">开启</a-select-option>
          <a-select-option :key="0">关闭</a-select-option>
        </a-select>
      </a-form-model-item>
    </a-form-model>
    <a-button
      @click="settingFlagChange"
      type="primary"
    >{{ flagText }}</a-button>
    <a-button
      style="margin-left:5px;"
      @click="submit"
      type="primary"
      :disabled="settingFlag"
    >提交</a-button>
  </a-card>
</template>

<script>

export default {
  mounted () {
   this.init()
  },
  data () {
    return {
      entity: {},
      rules: {
        seoTitle: [{ required: true, message: '必填' }],
        seoKeywords: [{ required: true, message: '必填' }],
        seoDescription: [{ required: true, message: '必填' }],
        openApiKey: [{ required: true, message: '必填' }]
      },
      layout: {
        labelCol: { span: 3 },
        wrapperCol: { span: 20 }
      },
      settingFlag: true,
      flagText: '修改设置信息'
    }
  },
  methods: {
    init () {
      this.$http.get('/myauth/web/getConfig').then(resJson => {
        if (resJson.success) {
          this.entity = resJson.result
        } else {
          this.$message.error(resJson.msg)
        }
      })
    },
    settingFlagChange () {
      this.settingFlag = !this.settingFlag
      if (this.settingFlag) this.flagText = '修改设置信息'
      else this.flagText = '取消'
    },
    submit () {
      this.$http.post('/myauth/web/editConfig', this.entity).then(resJson => {
        if (resJson.success) {
          this.$message.success(resJson.msg)
        } else {
          this.$message.error(resJson.msg)
        }
      })
    }
  }
}
</script>
