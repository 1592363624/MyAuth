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
          label="账号"
          prop="user"
        >
          <a-input
            v-model="entity.user"
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
          label="昵称"
          prop="name"
        >
          <a-input
            v-model="entity.name"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="点数"
          prop="point"
        >
          <a-input-number
            v-model="entity.point"
            autocomplete="off"
            :min="0"
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
          label="授权到期时间"
          prop="authTime"
          extra="不填则为永久"
        >
          <a-date-picker
            v-model="entity.authTime"
            show-time
            valueFormat="X"
            placeholder="到期时间"
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
          label="备注"
          prop="remark"
        >
          <a-input
            v-model="entity.remark"
            autocomplete="off"
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
        user: [{ required: true, message: '必填' }]
      },
      softListEx: [],
      title: '编辑用户',
      verList: [],
      softId: null
    }
  },
  mounted () {
  },
  methods: {
    moment,
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
        this.getVerList()
        // 修改
        if (id) {
          this.$http.post('/myauth/web/getUser', { id }).then(resJson => {
            if (resJson.success) {
              const temp = { ...resJson.result }
              temp.authTime = temp.authTime == -1 ? null : moment.unix(resJson.result.authTime)
              this.entity = temp
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
        const entityX = { ...this.entity }
        try {
          entityX.authTime = this.entity.authTime ? this.entity.authTime.unix().toString() : '-1'
        } catch (error) {
          entityX.authTime = this.entity.authTime ? this.entity.authTime : '-1'
        }
        const url = this.behaviorType == 1 ? '/myauth/web/addUser' : this.behaviorType == 2 ? '/myauth/web/updUser' : ''
        this.confirmLoading = true
        if (url) {
          this.$http.post(url, entityX).then(resJson => {
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
    getVerList () {
      this.$http.post('/myauth/web/getVersionListEx', { fromSoftId: this.softId }).then(resJson => {
        if (resJson.success) {
          this.verList = resJson.result.sort(function (a, b) {
              const x = a.ver.toLowerCase()
              const y = b.ver.toLowerCase()
              if (x < y) { return 1 }
              if (x > y) { return -1 }
              return 0
          })
          // console.info("获取到版本集合",resJson.result)
        } else {
          this.$message.error(resJson.msg)
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
    }
  }
}
</script>
