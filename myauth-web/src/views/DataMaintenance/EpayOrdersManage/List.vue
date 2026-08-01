<template>
  <a-card :bordered="false">
    <div
      class="table-operator"
      style="margin-bottom:15px"
    >
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
              <a-input
                v-model="queryParam.tradeNo"
                placeholder="支付平台订单号"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.outTradeNo"
                placeholder="商户订单号"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-select
                v-model="queryParam.type"
                allowClear
                placeholder="支付类型"
              >
                <a-select-option key="alipay">支付宝</a-select-option>
                <a-select-option key="qqpay">QQ钱包</a-select-option>
                <a-select-option key="wxpay">微信支付</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <div v-if="expansion">
            <a-col
              :md="3"
              :sm="24"
            >
              <a-tooltip>
                <span slot="title">
                  yyyy-MM-dd hh:mm:ss时间字符串
                </span>
                <a-form-model-item>
                  <a-input
                    v-model="queryParam.addtime"
                    placeholder="创建订单时间"
                  />
                </a-form-model-item>
              </a-tooltip>
            </a-col>
            <a-col
              :md="3"
              :sm="24"
            >
              <a-tooltip>
                <span slot="title">
                  yyyy-MM-dd hh:mm:ss时间字符串
                </span>
                <a-form-model-item>
                  <a-input
                    v-model="queryParam.endtime"
                    placeholder="完成交易时间"
                  />
                </a-form-model-item>
              </a-tooltip>
            </a-col>
            <a-col
              :md="3"
              :sm="24"
            >
              <a-form-model-item>
                <a-input
                  v-model="queryParam.name"
                  placeholder="商品名称"
                />
              </a-form-model-item>
            </a-col>
            <a-col
              :md="3"
              :sm="24"
            >
              <a-form-model-item>
                <a-input
                  v-model="queryParam.money"
                  placeholder="商品金额"
                />
              </a-form-model-item>
            </a-col>
            <a-col
              :md="3"
              :sm="24"
            >
              <a-form-model-item>
                <a-select
                  v-model="queryParam.status"
                  allowClear
                  placeholder="支付状态"
                >
                  <a-select-option :key="1">支付成功</a-select-option>
                  <a-select-option :key="0">未支付</a-select-option>
                </a-select>
              </a-form-model-item>
            </a-col>

            <a-col
              :md="3"
              :sm="24"
            >
              <a-form-model-item>
                <a-input
                  v-model="queryParam.fromAdminId"
                  placeholder="创建订单管理员Id"
                />
              </a-form-model-item>
            </a-col>
          </div>
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
            <div
              style="display:inline;margin-left:15px;"
              @click="expansionX"
            >
              <a>
                <a-icon :type="iconType" />{{ expansion ? "收起" : "展开" }}
              </a>
            </div>
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
        >未支付</a-tag>
        <a-tag
          color="#108ee9"
          v-else-if="text==1"
        >支付成功</a-tag>
        <a-tag
          color="#F4A460"
          v-else
        >未知</a-tag>
      </span>

      <span
        slot="type"
        slot-scope="text"
      >
        <a-tag
          color="#66CC99"
          v-if="text=='alipay'"
        >支付宝</a-tag>
        <a-tag
          color="#66CC99"
          v-else-if="text=='qqpay'"
        >QQ钱包</a-tag>
        <a-tag
          color="#66CC99"
          v-else-if="text=='wxpay'"
        >微信支付</a-tag>
        <a-tag v-else>其他</a-tag>
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
          <a @click="refresh(record.outTradeNo)">刷新订单</a>
          <a-divider type="vertical" />
          <a @click="handleDelete([record.id])">删除</a>
        </template>
      </span>
    </a-table>
  </a-card>
</template>

<script>

const columns = [
  { title: '支付平台订单号', align: 'center', dataIndex: 'tradeNo', width: '8%' },
  { title: '商户订单号', align: 'center', dataIndex: 'outTradeNo', width: '8%' },
  { title: '商品名称', align: 'center', sorter: true, dataIndex: 'name', width: '8%' },
  { title: '支付类型', align: 'center', dataIndex: 'type', width: '8%' },
  { title: '创建订单时间', align: 'center', sorter: true, dataIndex: 'addtime', width: '8%' },
  { title: '完成交易时间', align: 'center', sorter: true, dataIndex: 'endtime', width: '8%' },
  { title: '商品金额', align: 'center', sorter: true, dataIndex: 'money', width: '8%' },
  { title: '支付状态', align: 'center', sorter: true, dataIndex: 'status', width: '8%', scopedSlots: { customRender: 'status' } },
  { title: '创建订单管理员Id', align: 'center', dataIndex: 'fromAdminId', width: '8%' },
  { title: '操作', dataIndex: 'action', align: 'center', width: '250px', scopedSlots: { customRender: 'action' } }
]

export default {
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
        return str == null ? '' : str.length < 20 ? str : str.substr(0, length) + '…'
      },
      softListEx: [],
      storageTypeListEx: [],

      iconType: 'down',
      expansion: false,

      softId: '',
      behaviorType: 1,

      selectedRowKeys: [],
      selectedRows: []
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
        .post('/myauth/web/getEpayOrdersList', {
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
    refresh (outTradeNo) {
      if (outTradeNo) {
        this.$http.post('/myauth/web/getEpayOrder', { outTradeNo }).then(resJson => {
          if (resJson.success) {
            this.entity = resJson.result
            this.getDataList()
          } else {
            this.$message.error(resJson.msg)
          }
        })
      }
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
      this.$http.post('/myauth/web/delEpayOrders', { ids }).then(resJson => {
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
