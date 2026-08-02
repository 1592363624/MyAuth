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
            <a-form-model-item prop="fromVerId">
              <a-select
                v-model="queryParam.fromVerId"
                allowClear
                @change="verChange"
                placeholder="所属版本"
              >
                <a-select-option
                  v-for="item in verList"
                  :key="item.id"
                >{{ item.ver }}</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item prop="fromEventId">
              <a-select
                v-model="queryParam.fromEventId"
                allowClear
                @change="eventChange"
                placeholder="所属事件"
              >
                <a-select-option
                  v-for="item in eventList"
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
                v-model="queryParam.fromUser"
                placeholder="变动账号"
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
                v-model="queryParam.addTime"
                placeholder="变动时间戳"
                @change="debounceSearch"
                @pressEnter="getDataList"
              />
            </a-form-model-item>
          </a-col>

          <div v-if="expansion">
            <a-col
              :md="2"
              :sm="24"
            >
              <a-form-model-item>
                <a-input
                  v-model="queryParam.point"
                  placeholder="变动点数"
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
                  placeholder="变动后点数"
                  v-model="queryParam.afterPoint"
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
                  v-model="queryParam.seconds"
                  placeholder="变动秒数"
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
                  v-model="queryParam.afterSeconds"
                  placeholder="变动后秒数"
                  @change="debounceSearch"
                  @pressEnter="getDataList"
                />
              </a-form-model-item>
            </a-col>
          </div>

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

    <!-- <edit-form
      ref="editForm"
      :afterSubmit="getDataList"
      :behaviorType="behaviorType"
    ></edit-form> -->
  </a-card>
</template>

<script>
// import EditForm from './EditForm'
import { listMixin } from '@/utils/listMixin'

const columns = [
  { title: '所属软件', align: 'center', sorter: true, dataIndex: 'fromSoftName', width: '8%' },
  { title: '所属版本', align: 'center', sorter: true, dataIndex: 'fromVer', width: '8%' },
  { title: '所属事件', align: 'center', sorter: true, dataIndex: 'fromEventName', width: '8%' },
  { title: '点数变动值', align: 'center', sorter: true, dataIndex: 'point', width: '8%' },
  { title: '变动后点数', align: 'center', sorter: true, dataIndex: 'afterPoint', width: '8%' },
  { title: '秒数变动值', align: 'center', sorter: true, dataIndex: 'seconds', width: '8%' },
  { title: '变动后秒数', align: 'center', sorter: true, dataIndex: 'afterSeconds', width: '8%' },
  { title: '变动账号', align: 'center', sorter: true, dataIndex: 'fromUser', width: '8%' },
  { title: '变动时间', align: 'center', sorter: true, dataIndex: 'addTime', width: '8%', scopedSlots: { customRender: 'time' } },
  { title: '备注', align: 'center', sorter: true, dataIndex: 'remark', width: '8%', scopedSlots: { customRender: 'longText' } }
  // { title: '操作', dataIndex: 'action', align:'center', scopedSlots: { customRender: 'action' } }
]

export default {
  mixins: [listMixin],
  components: {
    // EditForm
  },
  mounted () {
    this.getSoftListEx()
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
        .post('/myauth/web/getPlogList', {
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
      this.$http.post('/myauth/web/delPlog', { ...this.queryParam }).then(resJson => {
        if (resJson.success) {
          this.$message.success(resJson.msg)

          this.getDataList()
        } else {
          this.$message.error(resJson.msg)
        }
      })
    },
    hanldleAdd (softId) {
      this.behaviorType = 1
      // this.$refs.editForm.openForm(null, softId, "新增")
    },
    handleEdit (id, softId) {
      this.behaviorType = 2
      // this.$refs.editForm.openForm(id, softId, "编辑")
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
    },
    softChange (value) {
      if (value) {
        this.getVerList()
        this.getDataList()
        this.getEventList()
      }
    },
    verChange (value) {
      this.queryParam.fromVerId = value
      if (value) { this.getDataList() }
    },
    eventChange (value) {
      this.queryParam.fromEventId = value
      if (value) { this.getDataList() }
    },
    getVerList () {
      this.queryParam.fromVerId = null
      this.$http.post('/myauth/web/getVersionListEx', { fromSoftId: this.queryParam.fromSoftId }).then(resJson => {
        if (resJson.success) {
          this.verList = resJson.result.sort(function (a, b) {
              const x = a.ver.toLowerCase()
              const y = b.ver.toLowerCase()
              if (x < y) { return 1 }
              if (x > y) { return -1 }
              return 0
          })
          // if(resJson.result.length>0){
          //   this.queryParam.fromVerId = resJson.result[0].id
          // }
          // console.info("获取到版本集合",resJson.result)
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
            this.getVerList()
            this.getDataList()
          }
        } else {
          this.$message.error(resJson.msg)
          console.error(resJson)
        }
      })
    },
    getEventList () {
      this.queryParam.fromEventId = null
      this.$http.post('/myauth/web/getEventList', {
 fromSoftId: this.queryParam.fromSoftId,
pageIndex: 1,
pageSize: 100,
        orders: [{
            column: 'id',
            asc: true
          }]
}).then(resJson => {
        if (resJson.success) {
          this.eventList = resJson.result.records
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
