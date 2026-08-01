<template>
  <a-card :bordered="false">
    <div
      class="table-operator"
      style="margin-bottom:15px"
    >
      <a-button
        type="primary"
        icon="plus"
        @click="hanldleAdd()"
      >新建</a-button>
      <a-button
        type="primary"
        icon="minus"
        @click="handleDelete(selectedRowKeys)"
        :disabled="!hasSelected()"
        :loading="loading"
      >删除</a-button>
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
            <a-select
              v-model="queryParam.fromStorageTypeId"
              allowClear
              showSearch
              :filterOption="filterOption"
              @change="storageTypeChange"
              placeholder="所属存储类型"
            >
              <a-select-option
                v-for="item in storageTypeListEx"
                :key="item.id"
              >{{ item.type }}</a-select-option>
            </a-select>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.content"
                placeholder="内容"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.number"
                placeholder="次数"
              />
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
                placeholder="回复状态"
              >
                <a-select-option :key="1">正常</a-select-option>
                <a-select-option :key="0">禁用</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.remark"
                placeholder="备注"
              />
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
      :scroll="{x:1000}"
      :rowSelection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
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
        >禁用</a-tag>
        <a-tag
          color="#108ee9"
          v-else-if="text==1"
        >正常</a-tag>
        <a-tag
          color="#F4A460"
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

      <span
        slot="time"
        slot-scope="text"
      >
        {{ getFormatDate(text) }}
      </span>

      <span
        slot="action"
        slot-scope="text, record"
      >
        <template>
          <a @click="handleEdit(record.id)">编辑</a>
          <a-divider type="vertical" />
          <a @click="handleDelete([record.id])">删除</a>
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

const columns = [
  { title: '所属软件', align: 'center', dataIndex: 'fromSoftName', width: '8%' },
  { title: '所属存储类型', align: 'center', dataIndex: 'fromStorageTypeName', width: '8%' },
  { title: '内容', align: 'center', dataIndex: 'content', width: '25%' },
  { title: '次数', align: 'center', sorter: true, dataIndex: 'number', width: '8%' },
  { title: '状态', align: 'center', sorter: true, dataIndex: 'status', width: '8%', scopedSlots: { customRender: 'status' } },
  { title: '备注', align: 'center', dataIndex: 'remark', scopedSlots: { customRender: 'longText' } },
  { title: '操作', dataIndex: 'action', align: 'center', width: '250px', scopedSlots: { customRender: 'action' } }
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
      queryParam: { fromSoftId: 1 },
      visible: false,

      getSubStr: (str, length = 20) => {
        return str == null ? '' : str.length < 20 ? str : str.substr(0, length) + '…'
      },
      softListEx: [],
      storageTypeListEx: [],

      softId: '',
      behaviorType: 1,

      selectedRowKeys: [],
      selectedRows: []
    }
  },
  methods: {
    getFormatDate (nS) {
      return this.TimeHelper.getFormatDate(nS)
    },
    handleTableChange (pagination, filters, sorter) {
      this.pagination = { ...pagination }
      this.filters = { ...filters }
      this.sorter = { ...sorter.column ? sorter : this.sorter0 }
      this.getDataList()
    },
    onSelectChange (selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
    },
    hasSelected () {
      return this.selectedRowKeys.length > 0
    },
    getDataList () {
      this.selectedRowKeys = []
      this.loading = true
      this.$http
        .post('/myauth/web/getStorageList', {
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
    hanldleAdd () {
      this.behaviorType = 1
      this.$refs.editForm.openForm(null, '新增额外存储')
    },
    handleEdit (id) {
      this.behaviorType = 2
      this.$refs.editForm.openForm(id, '编辑额外存储')
    },
    handleDelete (ids) {
      const thisObj = this
      // 序列拼接ids（,）
      let idsStr = ''
      ids.forEach(id => {
        idsStr += (id + ',')
      })
      this.$confirm({
        title: '确认删除吗?',
        onOk () {
          return new Promise((resolve, reject) => {
            thisObj.submitDelete(idsStr, resolve, reject)
          }).catch(() => console.log('Oops errors!'))
        }
      })
    },
    submitDelete (ids, resolve, reject) {
      this.$http.post('/myauth/web/delStorage', { ids }).then(resJson => {
        resolve()

        if (resJson.success) {
          this.$message.success(resJson.msg)

          this.getDataList()
        } else {
          this.$message.error(resJson.msg)
        }
      })
    },
    softChange (value) {
      if (value) {
        this.getDataList()
        this.getStorageTypeListEx(null, value)
      }
    },
    getSoftListEx (name = '') {
      this.$http.post('/myauth/web/getSoftListEx', { name }).then(resJson => {
        if (resJson.success) {
          this.softListEx = resJson.result
          if (resJson.result.length > 0) {
            this.softId = resJson.result[0].id
            this.queryParam.fromSoftId = resJson.result[0].id
            this.getDataList()
            this.getStorageTypeListEx(null, this.queryParam.fromSoftId)
          }
        } else {
          this.$message.error(resJson.msg)
          console.error(resJson)
        }
      })
    },
    storageTypeChange (value) {
      this.queryParam.fromStorageTypeId = value
      if (value) { this.getDataList() }
    },
    getStorageTypeListEx (type = '', fromSoftId) {
      this.queryParam.fromStorageTypeId = ''
      this.$http.post('/myauth/web/getStorageTypeListEx', { type, fromSoftId }).then(resJson => {
        if (resJson.success) {
          this.storageTypeListEx = resJson.result
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
