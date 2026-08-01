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
          label="上级菜单"
          prop="parentId"
        >
          <a-tree-select
            v-model="entity.parentId"
            allowClear
            :treeData="menuTreeData"
            placeholder="请选择上级菜单"
            :replaceFields="replaceFields"
            treeDefaultExpandAll
            @select="select"
          ></a-tree-select>
        </a-form-model-item>
        <!-- <a-form-model-item
          label="层级"
          prop="level"
        >
          <a-input-number
            v-model="entity.level"
            autocomplete="off"
            disabled
          />
        </a-form-model-item> -->
        <a-form-model-item
          label="排序"
          prop="sort"
        >
          <a-input-number
            v-model="entity.sort"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="类型"
          prop="type"
        >
          <a-select v-model="entity.type">
            <a-select-option :key="1">目录</a-select-option>
            <a-select-option :key="2">菜单</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="路径"
          prop="path"
          extra="为菜单时必填"
        >
          <a-input
            v-model="entity.path"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="标题"
          prop="title"
        >
          <a-input
            v-model="entity.title"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="图标"
          prop="icon"
        >
          <a-input
            v-model="entity.icon"
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
        // parentId: [{ required: true, message: '必填' }],
        level: [{ required: true, message: '必填' }],
        sort: [{ required: true, message: '必填' }],
        type: [{ required: true, message: '必填' }],
        title: [{ required: true, message: '必填' }]
      },
      softListEx: [],
      title: '编辑菜单',
      menuTreeData: [],
      replaceFields: { children: 'children', title: 'title', key: 'id', value: 'id' }
    }
  },
  watch: {
    'entity.parentId' (now, old) {
      if (now == null) {
        this.entity.level = 1
      }
    },
    deep: true
  },
  mounted () {
  },
  methods: {
    moment,
    init () {
      this.visible = true
      this.entity = { level: 1 }
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
      this.getMenuTreeData()
    },
    openForm (id, title) {
      this.init()
      if (id) {
        this.$http.post('/myauth/web/getMenu', { id }).then(resJson => {
          if (resJson.success) {
            const temp = { ...resJson.result }
            temp.authTime = temp.authTime == -1 ? null : moment.unix(resJson.result.authTime)
            this.entity = temp
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
        const url = this.behaviorType == 1 ? '/myauth/web/addMenu' : this.behaviorType == 2 ? '/myauth/web/updMenu' : ''
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
    select (value, node, extra) {
      this.entity.level = node.dataRef.level + 1
    }
  }
}
</script>
