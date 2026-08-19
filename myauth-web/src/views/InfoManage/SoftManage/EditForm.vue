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
          label="软件名"
          prop="name"
        >
          <a-input
            v-model="entity.name"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="状态"
          prop="status"
        >
          <a-select v-model="entity.status">
            <a-select-option :key="1">正常</a-select-option>
            <a-select-option :key="2">维护</a-select-option>
            <a-select-option :key="0">停用</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="是否收费"
          prop="type"
        >
          <a-select v-model="entity.type">
            <a-select-option :key="1">免费</a-select-option>
            <a-select-option :key="0">收费</a-select-option>
          </a-select>
        </a-form-model-item>

        <a-form-model-item
          label="数据加密密钥"
          prop="genKey"
          extra="仅支持英文和数字,长度必须为16位"
        >
          <a-input
            v-model="entity.genKey"
            autocomplete="off"
          />
        </a-form-model-item>

        <a-form-model-item
          label="数据是否加密"
          prop="genStatus"
        >
          <a-select v-model="entity.genStatus">
            <a-select-option :key="1">加密</a-select-option>
            <a-select-option :key="0">不加密</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="是否绑定机器码"
          prop="bindDeviceCode"
        >
          <a-select v-model="entity.bindDeviceCode">
            <a-select-option :key="1">绑定</a-select-option>
            <a-select-option :key="0">不绑定</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="是否开启注册"
          prop="register"
        >
          <a-select v-model="entity.register">
            <a-select-option :key="1">开启</a-select-option>
            <a-select-option :key="0">关闭</a-select-option>
          </a-select>
        </a-form-model-item>
        <!-- <a-form-model-item
          label="是否允许多地登录"
          prop="multipleLogin"
        >
          <a-select v-model="entity.multipleLogin">
            <a-select-option :key="1">允许</a-select-option>
            <a-select-option :key="0">禁止</a-select-option>
          </a-select>
        </a-form-model-item> -->
        <a-form-model-item
          label="心跳有效时间(S)"
          prop="heartTime"
          extra="每次登录或心跳后持续在线的时间(不宜太小)"
        >
          <a-input-number
            v-model="entity.heartTime"
            :min="30"
          />
        </a-form-model-item>
        <a-form-model-item
          label="sign有效时间(S)"
          prop="signTime"
          extra="客户端与服务端的误差时间允许值(不宜太小)"
        >
          <a-input-number
            v-model="entity.signTime"
            :min="30"
          />
        </a-form-model-item>
        <a-form-model-item
          label="同账号最大在线数"
          prop="maxOnlineCount"
          extra="每个账号最多登录几个设备在线(0为不限制)"
        >
          <a-input-number
            v-model="entity.maxOnlineCount"
            :min="0"
          />
        </a-form-model-item>
        <a-form-model-item
          label="是否允许顶号登录"
          prop="kickLogin"
          extra="开启后新登录会顶掉旧会话；关闭后在线数达到上限时拒绝新的登录"
        >
          <a-select v-model="entity.kickLogin">
            <a-select-option :key="1">允许顶号</a-select-option>
            <a-select-option :key="0">禁止顶号</a-select-option>
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
        name: [{ required: true, message: '必填' }],
        status: [{ required: true, message: '必填' }],
        type: [{ required: true, message: '必填' }],
        genKey: [
          { required: true, message: '必填' },
          { len: 16, message: '密钥长度限定为16位' },
          { pattern: /^[A-Za-z0-9]+$/, message: '密钥限定为字母数字混合' }
        ],
        genStatus: [{ required: true, message: '必填' }],
        bindDeviceCode: [{ required: true, message: '必填' }],
        // multipleLogin: [{ required: true, message: '必填' }],
        heartTime: [{ required: true, message: '必填' }],
        signTime: [{ required: true, message: '必填' }],
        register: [{ required: true, message: '必填' }],
        maxOnlineCount: [{ required: true, message: '必填' }]
      },
      title: '编辑软件'
    }
  },
  methods: {
    init () {
      this.visible = true
      this.entity = {}
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    openForm (id) {
      this.init()

      if (id) {
        this.$http.post('/myauth/web/getSoft', { id }).then(resJson => {
          if (resJson.success) {
            this.entity = resJson.result
          } else {
            this.$message.error(resJson.msg)
          }
        })
      }
    },
    handleSubmit () {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        const url = this.behaviorType == 1 ? '/myauth/web/addSoft' : this.behaviorType == 2 ? '/myauth/web/updSoft' : ''
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
    }
  }
}
</script>
