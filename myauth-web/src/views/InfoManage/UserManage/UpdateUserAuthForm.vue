<template>
  <a-modal
    :title="this.title"
    width="40%"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
    @cancel="() => { this.visible = false }">
    <a-spin :spinning="confirmLoading">
      <a-form-model ref="form" :model="entity" :rules="rules" v-bind="layout">
        <a-form-model-item label="所属软件" prop="fromSoftId" :labelCol="{ span: 4 }" :wrapperCol="{ span: 20 }">
          <a-select v-model="entity.fromSoftId" allowClear showSearch :filterOption="filterOption" placeholder="所属软件">
            <a-select-option v-for="item in softListEx" :key="item.id">{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-row>
          <a-col :span="12">
            <a-form-model-item label="最小点数" prop="minPoint">
              <a-input-number v-model="entity.minPoint" autocomplete="off" :min="0" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="最大点数" prop="maxPoint">
              <a-input-number v-model="entity.maxPoint" autocomplete="off" :min="0" />
            </a-form-model-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="12">
            <a-form-model-item label="最早到期时间" prop="minAuthTime">
              <a-date-picker v-model="entity.minAuthTime" show-time valueFormat="X" placeholder="最早到期时间" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="最晚到期时间" prop="maxAuthTime">
              <a-date-picker v-model="entity.maxAuthTime" show-time valueFormat="X" placeholder="最晚到期时间" />
            </a-form-model-item>
          </a-col>
        </a-row>

        <a-row>
          <a-col :span="12">
            <a-form-model-item label="最早注册时间" prop="minRegTime">
              <a-date-picker v-model="entity.minRegTime" show-time valueFormat="X" placeholder="最早注册时间" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="最晚注册时间" prop="maxRegTime">
              <a-date-picker v-model="entity.maxRegTime" show-time valueFormat="X" placeholder="最晚注册时间" />
            </a-form-model-item>
          </a-col>
        </a-row>

        <a-row>
          <a-col :span="12">
            <a-form-model-item label="修改的点数" prop="updPoint" extra="减少时请输入负数">
              <a-input-number v-model="entity.updPoint" autocomplete="off" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="修改的授权时间" prop="updAuthTime" extra="减少时请输入负数，单位秒">
              <a-input-number v-model="entity.updAuthTime" autocomplete="off" />
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </a-spin>
  </a-modal>
</template>

<script>
import moment from 'moment'
import { Modal } from 'ant-design-vue'
export default {
  props: {
    afterSubmit: {
      type: Function,
      default: null
    }
  },
  data () {
    return {
      layout: {
        labelCol: { span: 8 },
        wrapperCol: { span: 16 }
      },
      visible: false,
      confirmLoading: false,
      entity: {},
      title: '批量授权页面',
      getSubStr: (str, length = 20) => {
        return str == null || str == '' ? '-' : str.length < length ? str : str.substr(0, length) + '…'
      },
      rules: {
        fromSoftId: [{ required: true, message: '必填' }]
      },
      user: {},
      softListEx: []
    }
  },
  mounted () {
    this.getSoftListEx()
  },
  methods: {
    moment,
    openForm () {
      this.entity = {}
      this.visible = true
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
    handleSubmit () {
      const modal = Modal.confirm({
        okText: '确定',
cancelText: '取消',
content: '是否进行批量授权操作',
        onOk: () => {
          modal.destroy()
          if (!this.entity.updPoint && !this.entity.updAuthTime) {
            this.$message.warning('点数变动或时间变动至少选一项')
            return
          }
          this.confirmLoading = true
          this.$http.post('/myauth/web/updateUserAuthInfo', this.entity).then(resJson => {
            this.confirmLoading = false
            if (resJson.success) {
              this.$message.success(resJson.msg)
              this.visible = false
            } else {
              this.$message.error(resJson.msg)
              console.error(resJson)
            }
            this.afterSubmit()
          })
        }
      })
    }
  }
}
</script>

<style></style>
