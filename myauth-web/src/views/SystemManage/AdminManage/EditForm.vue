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
          label="账号"
          prop="user"
          v-if="!this.entity.id"
        >
          <a-input
            v-model="entity.user"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="QQ"
          prop="qq"
        >
          <a-input
            v-model="entity.qq"
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
          prop="fromSoftId"
        >
          <a-select
            v-model="entity.fromSoftId"
            allowClear
            showSearch
            :filterOption="filterOption"
            placeholder="软件"
            @change="softChange"
          >
            <a-select-option
              v-for="item in softListEx"
              :key="item.id"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>

        <a-form-model-item
          label="角色"
          prop="role"
        >
          <a-select
            v-model="entity.role"
            allowClear
            showSearch
            :filterOption="filterOption"
            placeholder="角色"
          >
            <a-select-option
              v-for="item in roleList"
              :key="item.id"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="账户余额"
          prop="money"
        >
          <a-input
            v-model="entity.money"
            autocomplete="off"
          />
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
        user: [{ required: true, message: '必填' }],
        pass: [{ required: true, message: '必填' }],
        role: [{ required: true, message: '必填' }],
        status: [{ required: true, message: '必填' }]
      },
      softListEx: [],
      roleList: [],
      title: '',
      softId: null
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
      setTimeout(() => {
        this.getRoleListEx()
      }, 200)
    },
    openForm (id, title) {
      this.init()
      if (id) {
        this.$http.post('/myauth/web/getAdmin', { id }).then(resJson => {
          if (resJson.success) {
            this.entity = resJson.result
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
        const url = this.behaviorType == 1 ? '/myauth/web/addAdmin' : this.behaviorType == 2 ? '/myauth/web/updAdmin' : ''
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
          if (resJson.result.length > 0) {
            this.softId = resJson.result[0].id
            this.entity.fromSoftId = resJson.result[0].id
            this.softChange()
          }
        } else {
          this.$message.error(resJson.msg)
          console.error(resJson)
        }
      })
    },
    getRoleListEx (name = '') {
      this.$http.post('/myauth/web/getRoleListEx', { name, fromSoftId: this.entity.fromSoftId }).then(resJson => {
        if (resJson.success) {
          this.roleList = resJson.result
          if (resJson.result.length > 0) {
            // this.entity.role = resJson.result[0].id
          }
        } else {
          this.$message.error(resJson.msg)
          console.error(resJson)
        }
      })
    },
    softChange () {
      this.getRoleListEx()
    },
    filterOption (input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    }
  }
}
</script>
