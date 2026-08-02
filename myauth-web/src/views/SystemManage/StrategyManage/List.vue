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
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.name"
                placeholder="策略名称"
                @change="debounceSearch"
                @pressEnter="getDataList"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-select
                v-model="queryParam.type"
                placeholder="类型"
                @change="debounceSearch"
              >
                <a-select-option :key="1">期限</a-select-option>
                <a-select-option :key="2">余额</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.value"
                placeholder="面额"
                @change="debounceSearch"
                @pressEnter="getDataList"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.price"
                placeholder="价格"
                @change="debounceSearch"
                @pressEnter="getDataList"
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
                placeholder="状态"
                @change="debounceSearch"
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
        >期限</a-tag>
        <a-tag
          color="#108ee9"
          v-else-if="text==2"
        >余额</a-tag>
        <a-tag
          color="#f50"
          v-else
        >未知</a-tag>
      </span>

      <span
        slot="status"
        slot-scope="text"
      >
        <a-tag
          color="#F4A460"
          v-if="text==1"
        >正常</a-tag>
        <a-tag
          color="#108ee9"
          v-else-if="text==0"
        >禁用</a-tag>
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
  { title: '策略名称', align: 'center', sorter: true, dataIndex: 'name', width: 40 },
  { title: '所属软件', align: 'center', sorter: true, dataIndex: 'fromSoftName', width: 40, scopedSlots: { customRender: 'longText' } },
  { title: '类型', align: 'center', sorter: true, dataIndex: 'type', width: 40, scopedSlots: { customRender: 'type' } },
  { title: '状态', align: 'center', sorter: true, dataIndex: 'status', width: 40, scopedSlots: { customRender: 'status' } },
  { title: '面额', align: 'center', sorter: true, dataIndex: 'value', width: 40 },
  { title: '价格', align: 'center', sorter: true, dataIndex: 'price', width: 40 },
  { title: '排序', align: 'center', sorter: true, dataIndex: 'sort', width: 40 },

  { title: '操作', dataIndex: 'action', align: 'center', fixed: 'right', width: 100, scopedSlots: { customRender: 'action' } }
]

export default {
  mixins: [listMixin],
  components: {
    EditForm
  },
  mounted () {
    this.getSoftListEx()
  },
  data () {
    return {
      data: [],
      softListEx: [],
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
      queryParam: {},
      visible: false,

      getSubStr: (str, length = 20) => {
        return (str == null || str == '') ? '(无)' : str.length < 20 ? str : str.substr(0, length) + '…'
      },

      softId: '',
      title: '回复管理',
      behaviorType: 1
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
    getDataList () {
      this.loading = true
      this.$http
        .post('/myauth/web/getStrategyList', {
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
      this.$refs.editForm.openForm(null, '新增策略')
    },
    handleEdit (id) {
      this.behaviorType = 2
      this.$refs.editForm.openForm(id, '编辑策略')
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
      this.$http.post('/myauth/web/delStrategy', { id }).then(resJson => {
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
    },
    softChange (value) {
      if (value) { this.getDataList() }
    }
  }
}
</script>
