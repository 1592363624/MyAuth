<template>
  <a-card :bordered="false">
    <div
      class="table-operator"
      style="margin-bottom:15px"
    >
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
            :md="3"
            :sm="24"
          >
            <a-form-model-item label="条件查询">
              <a-input
                v-model="queryParam.name"
                placeholder="通道名"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-select
                v-model="queryParam.enabled"
                allowClear
                placeholder="是否启用"
              >
                <a-select-option :key="1">开启</a-select-option>
                <a-select-option :key="0">关闭</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.driver"
                placeholder="通道标识"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.content"
                placeholder="通道说明"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.updateTime"
                placeholder="修改时间(时间戳)"
              />
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
      :bordered="true"
      :scroll="{x:2000}"
      size="small"
    >
      <span
        slot="enabled"
        slot-scope="text"
      >
        <a-tag
          color="#f50"
          v-if="text==0"
        >关闭</a-tag>
        <a-tag
          color="#108ee9"
          v-else-if="text==1"
        >开启</a-tag>
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
          <a @click="handleEdit(record)">编辑</a>
          <!-- <a-divider type="vertical" />
          <a @click="handleBehavior(record.id)">奖惩</a>
          <a-divider type="vertical" />
          <a @click="handleDelete(record.id)">删除</a> -->
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
  { title: '通道名', align: 'center', sorter: true, dataIndex: 'name', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '通道标识', align: 'center', sorter: true, dataIndex: 'driver', width: '8%' },
  { title: '通道配置', align: 'center', sorter: true, dataIndex: 'config', width: '15%', scopedSlots: { customRender: 'longText' } },
  { title: '通道说明', align: 'center', sorter: true, dataIndex: 'content', width: '20%' },
  { title: '修改时间', align: 'center', sorter: true, dataIndex: 'updateTime', width: '8%', scopedSlots: { customRender: 'time' } },
  { title: '是否启用', align: 'center', sorter: true, dataIndex: 'enabled', width: '8%', scopedSlots: { customRender: 'enabled' } },
  { title: '操作', dataIndex: 'action', align: 'left', scopedSlots: { customRender: 'action' } }
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
      queryParam: {},
      visible: false,
      selectedRowKeys: [],
      selectedRows: [],
      rules: {
        money: [{ required: true, message: '必填' }]
      },

      getSubStr: (str, length = 20) => {
        return (str == null || str == '') ? '(无)' : str.length < 20 ? str : str.substr(0, length) + '…'
      },
      behaviorType: 1,
      entity: {
        flag: 1
      },
      layout: {
        labelCol: { span: 5 },
        wrapperCol: { span: 18 }
      }
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
      this.selectedRowKeys = []
      this.loading = true
      this.$http
        .post('/myauth/web/getEpay', {
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
    hasSelected () {
      return this.selectedRowKeys.length > 0
    },
    hanldleAdd () {
      this.behaviorType = 1
    },
    handleEdit (data) {
      this.behaviorType = 2
      this.$refs.editForm.openForm(data, '修改支付通道信息')
    },
    filterOption (input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    }
  }
}
</script>
