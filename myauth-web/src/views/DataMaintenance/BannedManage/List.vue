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
                <a-select-option :key="0">全局</a-select-option>
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
                v-model="queryParam.value"
                placeholder="封禁对象"
                @change="debounceSearch"
                @pressEnter="getDataList"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.addTime"
                placeholder="封禁添加时间"
                @change="debounceSearch"
                @pressEnter="getDataList"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.toTime"
                placeholder="封禁到期时间戳"
                @change="debounceSearch"
                @pressEnter="getDataList"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.why"
                placeholder="why"
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
                allowClear
                placeholder="封禁类型"
              >
                <a-select-option :key="1">机器码</a-select-option>
                <a-select-option :key="2">IP</a-select-option>
                <a-select-option :key="3">账号</a-select-option>
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
      :rowSelection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      :scroll="{x:1000}"
      @change="handleTableChange"
      :bordered="true"
      size="small"
    >
      <span
        slot="type"
        slot-scope="text"
      >
        <a-tag
          color="#f50"
          v-if="text==1"
        >机器码</a-tag>
        <a-tag
          color="#108ee9"
          v-else-if="text==2"
        >IP</a-tag>
        <a-tag
          color="#CCCC33"
          v-else-if="text==3"
        >账号</a-tag>
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
import { listMixin } from '@/utils/listMixin'

const columns = [
  { title: '所属软件', align: 'center', sorter: true, dataIndex: 'fromSoftName', width: '8%' },
  { title: '封禁对象', align: 'center', sorter: true, dataIndex: 'value', width: '8%' },
  { title: '封禁原因', align: 'center', sorter: true, dataIndex: 'why', width: '8%' },
  { title: '封禁添加时间', align: 'center', sorter: true, dataIndex: 'addTime', width: '15%', scopedSlots: { customRender: 'time' } },
  { title: '封禁到期时间', align: 'center', sorter: true, dataIndex: 'toTime', width: '15%', scopedSlots: { customRender: 'time' } },
  { title: '封禁类型', align: 'center', sorter: true, dataIndex: 'type', width: '8%', scopedSlots: { customRender: 'type' } },
  { title: '操作', dataIndex: 'action', align: 'center', fixed: 'right', scopedSlots: { customRender: 'action' } }
]

export default {
  mixins: [listMixin],
  components: {
    EditForm
  },
  mounted () {
    this.getSoftListEx()
    this.init()
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

      getSubStr: (str, length = 20) => {
        return str == null ? '' : str.length < 20 ? str : str.substr(0, length) + '…'
      },
      softListEx: [],

      softId: '',
      title: '回复管理',
      behaviorType: 1,

      selectedRowKeys: [],
      selectedRows: []
    }
  },
  methods: {
    init () {
      this.visible = true
      this.queryParam = {}
      this.getDataList()
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
        .post('/myauth/web/getBanList', {
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
      this.$refs.editForm.openForm(null, '新增封禁')
    },
    handleEdit (id) {
      this.behaviorType = 2
      this.$refs.editForm.openForm(id, '编辑封禁')
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
      this.$http.post('/myauth/web/delBan', { ids }).then(resJson => {
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
      // if(value)
        this.getDataList()
    },
    getSoftListEx (name = '') {
      this.$http.post('/myauth/web/getSoftListEx', { name }).then(resJson => {
        if (resJson.success) {
          this.softListEx = resJson.result
          // if(resJson.result.length>0){
          //   this.softId = resJson.result[0].id
          //   this.queryParam.fromSoftId = resJson.result[0].id
          //   this.getDataList()
          // }
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
