<template>
  <a-modal
    width="40%"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
    @cancel="()=>{this.visible=false}"
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
          label="账号"
          prop="user"
        >
          <a-input
            v-model="entity.user"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="卡密"
          prop="ckey"
        >
          <a-input
            v-model="entity.ckey"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="密码"
          prop="pass"
        >
          <a-input
            v-model="entity.pass"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="软件"
          prop="skey"
        >
          <a-select
            v-model="entity.skey"
            allowClear
            showSearch
            :filterOption="filterOption"
            placeholder="软件"
          >
            <a-select-option
              v-for="item in softListEx"
              :key="item.skey"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
      </a-form-model>
    </a-spin>
  </a-modal>
</template>

<script>
export default {
  data () {
    return {
      layout: {
        labelCol: { span: 5 },
        wrapperCol: { span: 18 }
      },
      visible: false,
      confirmLoading: false,
      entity: {},
      softListEx: [],
      rules: {
        user: [{ required: true, message: '必填' }],
        skey: [{ required: true, message: '必填' }]
      },
      title: '自助解绑'
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
    },
    openForm () {
      this.init()
    },
    handleSubmit () {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (!this.entity.pass && !this.entity.skey) {
          this.$message.warning('卡密与密码至少填写一个')
          return
        }
        const url = '/myauth/web/selfUnbind'
        this.confirmLoading = true
        if (url) {
          this.$http.post(url, this.entity).then(resJson => {
            this.confirmLoading = false

            if (resJson.success) {
              this.$message.success(resJson.msg)
              this.visible = false
            } else {
              this.$message.error(resJson.msg)
            }
          })
        }
      })
    },
    getSoftListEx () {
      this.$http.get('/myauth/web/getSoftListSimple').then(resJson => {
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
