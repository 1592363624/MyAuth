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
          label="查询类型"
          prop="type"
        >
          <a-select
            v-model="entity.type"
            @change="softChange"
          >
            <a-select-option :key="1">普通用户</a-select-option>
            <a-select-option :key="2">管理员用户</a-select-option>
          </a-select>

        </a-form-model-item>

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
      <div style="text-align:center;">
        <a-button
          @click="searchUser"
          type="primary"
          icon="search"
          style="width:60%;"
        >查询</a-button>
      </div>
      <a-card
        v-if="data.user"
        title="用户信息"
        style="margin-top:5px"
      >
        <a-form
          ref="form2"
          v-bind="layout"
        >
          <a-form-item label="账号">
            <a-input
              v-model="data.user"
              autocomplete="off"
              disabled
            />
          </a-form-item>
          <a-form-item
            v-if="entity.type==1"
            label="用户名"
          >
            <a-input
              v-model="data.name"
              autocomplete="off"
              disabled
            />
          </a-form-item>
          <a-form-item label="QQ">
            <a-input
              v-model="data.qq"
              autocomplete="off"
              disabled
            />
          </a-form-item>
          <a-form-item label="注册时间">
            <a-date-picker
              v-model="data.regTime"
              show-time
              valueFormat="X"
              placeholder="为空表示永久"
              disabled
            />
          </a-form-item>
          <a-form-item
            v-if="entity.type==1"
            label="授权时间"
          >
            <a-date-picker
              v-model="data.authTime"
              show-time
              valueFormat="X"
              placeholder="授权时间"
              disabled
            />
          </a-form-item>
          <a-form-item
            v-if="entity.type==1"
            label="点数"
          >
            <a-input
              v-model="data.point"
              autocomplete="off"
              disabled
            />
          </a-form-item>

          <a-form-item
            v-if="entity.type==1"
            label="状态"
          >
            <a-input
              v-model="data.status"
              autocomplete="off"
              disabled
            />
          </a-form-item>

          <a-form-item
            v-if="entity.type==2"
            label="状态"
          >
            <a-select
              v-model="data.status"
              disabled
            >
              <a-select-option :key="1">正常</a-select-option>
              <a-select-option :key="0">禁用</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item
            v-if="entity.type==2"
            label="角色"
          >
            <a-input
              v-model="data.role"
              autocomplete="off"
              disabled
            />
          </a-form-item>
          <a-form-item label="软件">
            <a-input
              v-model="data.fromSoftName"
              autocomplete="off"
              disabled
            />
          </a-form-item>
          <a-form-item
            v-if="entity.type==1"
            label="备注"
          >
            <a-input
              v-model="data.remark"
              autocomplete="off"
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
        user: [{ required: true, message: '必填' }],
        skey: [{ required: true, message: '必填' }]
      },
      title: '查询账号信息'
    }
  },
  methods: {
    moment,
    init () {
      this.visible = true
      this.entity = { type: 1 }
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
      this.getSoftListEx()
    },
    openForm () {
      this.init()
    },
    softChange (value) {
      this.data = {}
    },
    searchUser () {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        let url = this.entity.type == 1 ? '/myauth/web/queryUserInfo' : this.entity.type == 2 ? '/myauth/web/queryAdminInfo' : ''
        url += '?user=' + this.entity.user + '&skey=' + this.entity.skey
        this.confirmLoading = true
        if (url) {
          this.$http.get(url).then(resJson => {
            this.confirmLoading = false

            if (resJson.success) {
              const temp = { ...resJson.result }
              temp.regTime = temp.regTime ? (temp.regTime == -1 ? null : moment.unix(resJson.result.regTime)) : null
              temp.authTime = temp.authTime ? (temp.authTime == -1 ? null : moment.unix(resJson.result.authTime)) : null
              this.data = temp
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
