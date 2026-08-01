<template>
  <a-card :bordered="false">
    <div
      class="table-operator"
      style="margin-bottom:15px"
    >
      <!-- <a-button
        type="primary"
        icon="plus"
        @click="hanldleAdd(softId)"
      >新建</a-button> -->

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
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.Id"
                placeholder="日志ID"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.operationUser"
                placeholder="操作账号"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.operationUa"
                placeholder="操作UA"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.operationIp"
                placeholder="操作IP"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.operationTime"
                placeholder="操作时间"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.operationType"
                placeholder="操作内容"
                style="width:150px;"
              />
            </a-form-model-item>
          </a-col>

          <a-col
            :md="6"
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
            <!-- <div
              style="display:inline;margin-left:15px;"
              @click="expansionX"
            >
              <a>
                <a-icon :type="iconType" />{{expansion ? "收起" : "展开"}}
              </a>
            </div> -->
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
      :scroll="{x:1000}"
      :bordered="true"
      size="small"
    >

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

    </a-table>

  </a-card>
</template>

<script>
// import EditForm from './EditForm'

const columns = [
  { title: 'ID', align: 'center', dataIndex: 'id', width: '5%' },
  { title: '操作账号', align: 'center', dataIndex: 'operationUser', width: '8%' },
  { title: '操作UA', align: 'center', dataIndex: 'operationUa', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '操作IP', align: 'center', dataIndex: 'operationIp', width: '8%' },
  { title: '操作时间', align: 'center', dataIndex: 'operationTime', width: '8%', scopedSlots: { customRender: 'time' } },
  { title: '操作内容', align: 'center', dataIndex: 'operationType', width: '15%' }

  // { title: '操作', dataIndex: 'action', align:'center', scopedSlots: { customRender: 'action' } }
]

export default {
  components: {
    // EditForm
  },
  mounted () {
    this.getDataList()
  },
  data () {
    return {
      data: [],
      iconType: 'down',
      expansion: false,
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
      verList: [],
      eventList: [],

      softId: '',
      title: '',
      behaviorType: 1
    }
  },
  methods: {
     expansionX () {
      this.expansion = !this.expansion
      if (this.iconType == 'down') {
        this.iconType = 'up'
        } else if (this.iconType == 'up') this.iconType = 'down'
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
      this.selectedRowKeys = []
      this.loading = true
      this.$http
        .post('/myauth/web/getOperationLogList', {
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
    }
  }
}
</script>
