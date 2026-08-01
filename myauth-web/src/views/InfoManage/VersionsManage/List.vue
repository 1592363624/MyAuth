<template>
  <!-- <a-modal
    :title="this.title"
    width="80%"
    :visible="visible"
    @ok="()=>{this.visible=false}"
    @cancel="()=>{this.visible=false}"
  > -->
  <a-card :bordered="false">
    <div
      class="table-operator"
      style="margin-bottom:15px"
    >
      <a-button
        type="primary"
        icon="plus"
        @click="hanldleAdd(softId)"
      >新建</a-button>

      <a-button
        type="primary"
        icon="redo"
        @click="getDataList()"
      >刷新</a-button>
    </div>

    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="10">
          <a-col
            :md="4"
            :sm="24"
          >
            <a-form-model-item label="条件查询">
              <a-select
                v-model="queryParam.fromSoftId"
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
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.ver"
                placeholder="版本号(精准)"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.vkey"
                placeholder="版本key(精准)"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-select
                v-model="queryParam.updType"
                allowClear
                placeholder="更新模式"
              >
                <a-select-option :key="1">强制</a-select-option>
                <a-select-option :key="0">可选</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-select
                v-model="queryParam.status"
                allowClear
                placeholder="状态"
              >
                <a-select-option :key="1">正常</a-select-option>
                <a-select-option :key="0">停用</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>

          <a-col
            :md="4"
            :sm="24"
          >
            <a-button
              type="primary"
              @click="getDataList"
            >查询</a-button>
            <a-button
              style="margin-left: 8px"
              @click="() => (queryParam = {})"
            >重置</a-button>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <a-table
      ref="table"
      :columns="columns"
      :rowKey="row => row.id"
      :dataSource="data"
      :pagination="pagination"
      :loading="loading"
      :scroll="{x:2000}"
      @change="handleTableChange"
      :bordered="true"
      size="small"
    >
      <span
        slot="status"
        slot-scope="text"
      >
        <a-tag
          color="#f50"
          v-if="text==0"
        >停用</a-tag>
        <a-tag
          color="#108ee9"
          v-else-if="text==1"
        >正常</a-tag>
        <a-tag
          color="#F4A460"
          v-else
        >未知</a-tag>
      </span>

      <span
        slot="updType"
        slot-scope="text"
      >
        <a-tag
          color="#f50"
          v-if="text==0"
        >可选</a-tag>
        <a-tag
          color="#108ee9"
          v-else-if="text==1"
        >强制</a-tag>
        <a-tag
          color="#F4A460"
          v-else
        >未知</a-tag>
      </span>

      <span
        slot="updTime"
        slot-scope="text"
      >
        {{ getFormatDate(text) }}
      </span>

      <a-tooltip
        slot="longText"
        slot-scope="text"
      >
        <span slot="title">
          {{ text }}
        </span>{{ getSubStr(text) }}
      </a-tooltip>

      <span
        slot="action"
        slot-scope="text, record"
      >
        <template>
          <a @click="handleEdit(record.fromSoftId, record.vkey)">编辑</a>
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
  <!-- </a-modal> -->
</template>

<script>
import EditForm from './EditForm'

const columns = [
  { title: '所属软件', align: 'center', dataIndex: 'fromSoftName', width: '8%' },
  { title: '版本号', align: 'center', sorter: true, dataIndex: 'ver', width: '8%' },
  { title: '版本key', align: 'center', dataIndex: 'vkey', width: '15%' },
  { title: '更新时间', align: 'center', sorter: true, dataIndex: 'updTime', width: '10%', scopedSlots: { customRender: 'updTime' } },
  { title: '更新模式', align: 'center', sorter: true, dataIndex: 'updType', width: '8%', scopedSlots: { customRender: 'updType' } },
  { title: '状态', align: 'center', sorter: true, dataIndex: 'status', width: '8%', scopedSlots: { customRender: 'status' } },
  { title: '更新日志', align: 'center', dataIndex: 'updLog', width: '20%', scopedSlots: { customRender: 'longText' } },
  { title: '操作', dataIndex: 'action', align: 'center', scopedSlots: { customRender: 'action' } }
]

export default {
  components: {
    EditForm
  },
  mounted () {
    this.getSoftListEx()
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
      queryParam: { fromSoftId: 0 },
      visible: false,

      getSubStr: (str, length = 20) => {
        return str == null ? '' : str.length < 20 ? str : str.substr(0, length) + '…'
      },

      softListEx: [],

      softId: 0,
      title: '版本管理',
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
    softChange (value) {
      if (value) { this.getDataList() }
    },
    getDataList () {
      this.selectedRowKeys = []
      this.loading = true
      this.$http
        .post('/myauth/web/getVersionList', {
          pageIndex: this.pagination.current,
          pageSize: this.pagination.pageSize,
          orders: [{
            column: this.sorter.order ? this.sorter.field || 'id' : null,
            asc: this.sorter.order == 'ascend' ? true : this.sorter.order == 'descend' ? false : null
          }],
          // ...this.filters
          ...this.queryParam
        })
        .then(resJson => {
          this.loading = false
          if (resJson.success) {
            const result = resJson.result
            if (result.records) {
              this.data = result.records
            }
            const pagination = { ...this.pagination }
            pagination.total = result.total
            this.pagination = pagination
          } else {
            this.$message.error(resJson.msg)
          }
        })
    },
    hanldleAdd (softId) {
      this.behaviorType = 1
      this.$refs.editForm.openForm(softId, null, '新增版本')
    },
    handleEdit (softId, vkey) {
      this.behaviorType = 2
      this.$refs.editForm.openForm(softId, vkey, '编辑版本')
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
      this.$http.post('/myauth/web/delVersion', { id }).then(resJson => {
        resolve()

        if (resJson.success) {
          this.$message.success(resJson.msg)

          this.getDataList()
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
            this.queryParam.fromSoftId = resJson.result[0].id
            this.getDataList()
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
