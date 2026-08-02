<template>
  <a-card :bordered="false">
    <div
      class="table-operator"
      style="margin-bottom:15px"
    >
      <a-button
        type="primary"
        icon="plus"
        @click="hanldleAdd"
      >新建</a-button>

      <a-button
        type="primary"
        icon="redo"
        @click="getDataList()"
      >刷新</a-button>
    </div>

    <a-table
      ref="table"
      :columns="columns"
      :rowKey="row => row.id"
      :dataSource="data"
      :pagination="pagination"
      :loading="loading"
      @change="handleTableChange"
      :scroll="{x:2000}"
      :bordered="true"
      size="small"
    >
      <span
        slot="type"
        slot-scope="text"
      >
        <a-tag
          color="#F4A460"
          v-if="text==1"
        >目录</a-tag>
        <a-tag
          color="#108ee9"
          v-else-if="text==2"
        >菜单</a-tag>
        <a-tag
          color="#f50"
          v-else
        >未知</a-tag>
      </span>

      <a-tooltip
        slot="longText"
        slot-scope="text"
      >
        <span slot="title">
          {{ text }}
        </span>{{ getSubStr(text) }}
      </a-tooltip>

      <a-tooltip
        slot="icon"
        slot-scope="text"
      >
        <span slot="title">
          {{ text }}
        </span>
        <a-icon
          v-if="text"
          :type="text"
        />
      </a-tooltip>

      <span
        slot="action"
        slot-scope="text, record"
      >
        <template>
          <a @click="handleEdit(record.id)">编辑</a>
          <a-divider type="vertical" />
          <a @click="handleDelete(record.id)">删除</a>
        </template>
      </span>
    </a-table>

    <edit-form
      ref="editForm"
      :afterSubmit="getDataList"
      :behaviorType="behaviorType"
    ></edit-form>
  </a-card>
</template>

<script>
import EditForm from './EditForm'
import { listMixin } from '@/utils/listMixin'

const columns = [
  { title: '标题', align: 'left', sorter: true, dataIndex: 'title', width: 40 },
  { title: '类型', align: 'center', sorter: true, dataIndex: 'type', width: '8%', scopedSlots: { customRender: 'type' } },
  { title: '层级', align: 'center', sorter: true, dataIndex: 'level', width: 40 },
  { title: '排序', align: 'center', sorter: true, dataIndex: 'sort', width: 40 },
  { title: '图标', align: 'center', sorter: true, dataIndex: 'icon', width: 40, scopedSlots: { customRender: 'icon' } },
  { title: '路由', align: 'center', sorter: true, dataIndex: 'path', width: 100 },
  { title: '描述', align: 'center', sorter: true, dataIndex: 'description', width: 100, scopedSlots: { customRender: 'longText' } },

  { title: '操作', dataIndex: 'action', align: 'center', fixed: 'right', width: 100, scopedSlots: { customRender: 'action' } }
]

export default {
  mixins: [listMixin],
  components: {
    EditForm
  },
  mounted () {
    this.getDataList()
  },
  data () {
    return {
      data: [],
      pagination: {
        current: 1,
        pageSize: 10,
        showTotal: (total, range) => `总数:${total} 当前:${range[0]}-${range[1]}`
      },
      filters: {},
      sorter: { field: 'Id', order: 'descend' },
      sorter0: { field: 'Id', order: 'descend' },
      loading: false,
      columns,
      queryParam: { fromSoftId: 1 },
      visible: false,

      getSubStr: (str, length = 20) => {
        return (str == null || str == '') ? '-' : str.length < 20 ? str : str.substr(0, length) + '…'
      },

      softId: '',
      title: '回复管理',
      behaviorType: 1
    }
  },
  methods: {
    openForm (softId, title) {
      if (!softId) {
        this.$message.error('缺少软件id')
        return
      }
      if (title) {
        this.title = title
      }
      this.visible = true
      this.softId = softId
      this.queryParam.fromSoftId = softId
      this.getDataList()
    },
    getFormatDate (nS) {
      return this.TimeHelper.getFormatDate(nS)
    },
    handleTableChange (pagination, filters, sorter) {
      this.pagination = { ...pagination }
      this.filters = { ...filters }
      this.sorter = { ...sorter.column ? sorter : this.sorter0 }
      this.getDataList()
    },
    getDataList () {
      this.loading = true
      this.$http
        .get('/myauth/web/getMenuListAll')
        .then(resJson => {
          this.loading = false
          if (resJson.success) {
            this.data = resJson.result
          } else {
            this.$message.error(resJson.msg)
          }
        })
    },
    hanldleAdd () {
      this.behaviorType = 1
      this.$refs.editForm.openForm(null, '新增菜单')
    },
    handleEdit (id) {
      this.behaviorType = 2
      this.$refs.editForm.openForm(id, '编辑菜单')
    },
    handleDelete (id) {
      const thisObj = this
      this.$confirm({
        title: '确认删除吗?',
        onOk () {
          return new Promise((resolve, reject) => {
            thisObj.submitDelete(id, resolve, reject)
          }).catch(() => console.log('Oops errors!'))
        }
      })
    },
    submitDelete (id, resolve, reject) {
      this.$http.post('/myauth/web/delMenu', { id }).then(resJson => {
        resolve()

        if (resJson.success) {
          this.$message.success(resJson.msg)

          this.getDataList()
        } else {
          this.$message.error(resJson.msg)
        }
      })
    }
  }
}
</script>
