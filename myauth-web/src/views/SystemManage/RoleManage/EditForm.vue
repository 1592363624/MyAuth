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
          label="角色名称"
          prop="name"
        >
          <a-input
            v-model="entity.name"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="所属软件"
          prop="fromSoftId"
          extra="不填则为超级管理员"
        >
          <a-select
            v-model="entity.fromSoftId"
            mode="multiple"
            allowClear
            showSearch
            :filterOption="filterOption"
            placeholder="所属软件"
          >
            <a-select-option
              v-for="item in softListEx"
              :key="item.id"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="折扣(%)"
          prop="discount"
        >
          <a-input-number
            v-model="entity.discount"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="授权菜单"
          prop="meunList"
        >
          <a-tree-select
            v-model="entity.meunList"
            allowClear
            treeCheckable
            :treeData="menuTreeData"
            placeholder="请选择授权菜单"
            :replaceFields="replaceFields"
            treeDefaultExpandAll
          ></a-tree-select>
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
        name: [{ required: true, message: '必填' }],
        // fromSoftId: [{ required: true, message: '必填' }],
        discount: [{ required: true, message: '必填' }]
      },
      softListEx: [],
      title: '编辑菜单',
      menuTreeData: [],
      replaceFields: { children: 'children', title: 'title', key: 'id', value: 'id' }
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
      this.getMenuTreeData()
      this.getSoftListEx()
    },
    openForm (id, title) {
      this.init()
      if (id) {
        this.$http.post('/myauth/web/getRole', { id }).then(resJson => {
          if (resJson.success) {
            this.entity = resJson.result
            const fromSoftIdArr = this.entity.fromSoftId
            this.entity.fromSoftId = fromSoftIdArr.split(',')

            if (this.entity.fromSoftId.length == 1 && this.entity.fromSoftId[0] == '0') {
              this.entity.fromSoftId = '超级管理员'
            } else {
              for (let i = 0; i < this.entity.fromSoftId.length; i++) {
                this.entity.fromSoftId[i] = this.entity.fromSoftId[i] * 1
              }
            }
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
        const arr = this.entity.fromSoftId
        if (!arr || arr == '' || arr == '超级管理员') {
          this.entity.fromSoftId = 0
        } else {
          if (arr.length == 1 && arr[0] != '超级管理员') {
            this.entity.fromSoftId = arr[0]
          } else {
            this.entity.fromSoftId = ''
            for (let i = 0; i < arr.length; i++) {
              if (arr[i] == '超级管理员') {
                continue
              }
              if (i < arr.length - 1) {
                this.entity.fromSoftId = this.entity.fromSoftId + arr[i]
                this.entity.fromSoftId = this.entity.fromSoftId + ','
              } else {
                this.entity.fromSoftId = this.entity.fromSoftId + arr[i]
              }
            }
          }
        }

        const url = this.behaviorType == 1 ? '/myauth/web/addRole' : this.behaviorType == 2 ? '/myauth/web/updRole' : ''
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
    getMenuTreeData () {
      this.$http
        .get('/myauth/web/getMenuListAll')
        .then(resJson => {
          this.loading = false
          if (resJson.success) {
            this.menuTreeData = resJson.result
          } else {
            this.$message.error(resJson.msg)
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
    }
  }
}
</script>
