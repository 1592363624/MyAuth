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
                v-model="queryParam.money"
                placeholder="变动余额"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.afterMoney"
                placeholder="变动后的余额"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.adminId"
                placeholder="变动账号Id"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.data"
                placeholder="变动描述"
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
                placeholder="变动类型"
              >
                <a-select-option key="管理员奖惩">管理员奖惩</a-select-option>
                <a-select-option key="生成卡密">生成卡密</a-select-option>
                <a-select-option key="添加授权">添加授权</a-select-option>
                <a-select-option key="使用卡密">使用卡密</a-select-option>
                <a-select-option key="后台充值">后台充值</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.fromUser"
                placeholder="变动账号"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input-number
                v-model="queryParam.addTime"
                placeholder="变动时间戳"
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
              type="primary"
              style="margin-left: 8px"
              @click="handleDeleteX"
            >按条件删除</a-button>
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
        slot="fromVer"
        slot-scope="text"
      >
        <span v-if="text">{{ text }}</span>
        <span v-else>All</span>
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
          <!-- <a @click="handleEdit(record.id, record.fromSoftId )">编辑</a>
          <a-divider type="vertical" /> -->
          <a @click="handleDelete(record.id)">删除</a>
        </template>
      </span>
    </a-table>

  </a-card>
</template>

<script>
// import EditForm from './EditForm'

const columns = [
  { title: '类型', align: 'center', dataIndex: 'type', width: '8%' },
  { title: '变动时间', align: 'center', dataIndex: 'addTime', width: '8%', scopedSlots: { customRender: 'time' } },
  { title: '变动余额', align: 'center', dataIndex: 'money', width: '8%' },
  { title: '变动后的余额', align: 'center', dataIndex: 'afterMoney', width: '8%' },
  { title: '变动账号ID', align: 'center', dataIndex: 'adminId', width: '8%' },
  { title: '描述信息', align: 'center', dataIndex: 'data' }

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
      this.selectedRowKeys = []
      this.loading = true
      this.$http
        .post('/myauth/web/getAlogList', {
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
    handleDeleteX () {
      this.$http.post('/myauth/web/delAlog', { ...this.queryParam }).then(resJson => {
        if (resJson.success) {
          this.$message.success('操作成功：' + resJson.msg)

          this.getDataList()
        } else {
          this.$message.error(resJson.msg)
        }
      })
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
      this.$http.post('/myauth/web/delPlog', { id }).then(resJson => {
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
