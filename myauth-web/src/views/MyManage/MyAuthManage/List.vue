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
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-select
                v-model="queryParam.fromSoftId"
                allowClear
                placeholder="软件"
              >
                <a-select-option
                  v-for="item in mySoftListEx"
                  :key="item.id"
                >{{ item.name }}</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col
            :md="4"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.user"
                placeholder="账号"
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
                v-model="queryParam.name"
                placeholder="昵称"
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
                v-model="queryParam.point"
                placeholder="点数"
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
                  v-model="queryParam.qq"
                  placeholder="QQ"
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
                  v-model="queryParam.lastIp"
                  placeholder="最后登录IP"
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
                  v-model="queryParam.lastTime"
                  placeholder="最后登录时间戳"
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
                  v-model="queryParam.authTime"
                  placeholder="授权到期时间戳"
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
                  v-model="queryParam.deviceInfo"
                  placeholder="最后登录的设备"
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
                  v-model="queryParam.deviceCode"
                  placeholder="最后登录的设备机器码"
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
                  v-model="queryParam.ckey"
                  placeholder="卡密"
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
                  v-model="queryParam.remark"
                  placeholder="备注"
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
      @change="handleTableChange"
      :bordered="true"
      :scroll="{x:2000}"
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
        slot="timeOut"
        slot-scope="text"
      >
        <a-tag
          color="#C0C0C0"
          v-if="isExpired(text)"
        >已到期<br>({{ getFormatDate(text) }})</a-tag>
        <span v-else>{{ getFormatDate(text) }}</span>
      </span>

      <span
        slot="online"
        slot-scope="text"
      >
        <a-tag
          color="#cccccc"
          v-if="text==0"
        >离线</a-tag>
        <a-tag
          color="#008000"
          v-else-if="text > 0"
        >在线:{{ text }}</a-tag>
        <a-tag v-else>未知</a-tag>
      </span>

      <span
        slot="time"
        slot-scope="text"
      >
        {{ getFormatDate(text) }}
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
    </a-table>
  </a-card>
</template>

<script>
import { listMixin } from '@/utils/listMixin'

const columns = [
  { title: '所属软件', align: 'center', sorter: true, fixed: 'left', dataIndex: 'fromSoftName', width: 100 },
  { title: '所属版本', align: 'center', sorter: true, fixed: 'left', dataIndex: 'fromVerName', width: 100 },
  { title: '管理员ID', align: 'center', sorter: true, dataIndex: 'fromAdminId', width: 100 },
  { title: '用户', align: 'center', sorter: true, dataIndex: 'user', width: 100, scopedSlots: { customRender: 'longText' } },
  { title: '昵称', align: 'center', sorter: true, dataIndex: 'name', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '密码', align: 'center', sorter: true, dataIndex: 'pass', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '点数', align: 'center', sorter: true, dataIndex: 'point', width: '8%' },
  { title: 'QQ', align: 'center', sorter: true, dataIndex: 'qq', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '注册时间', align: 'center', sorter: true, dataIndex: 'regTime', width: '8%', scopedSlots: { customRender: 'time' } },
  { title: '授权到期时间', align: 'center', sorter: true, dataIndex: 'authTime', width: 150, scopedSlots: { customRender: 'timeOut' } },
  { title: '最后登录时间', align: 'center', sorter: true, dataIndex: 'lastTime', width: '8%', scopedSlots: { customRender: 'time' } },
  { title: '最后登录IP', align: 'center', sorter: true, dataIndex: 'lastIp', width: '10%', scopedSlots: { customRender: 'longText' } },
  // { title: '软件Key', align:'center', dataIndex: 'fromSoftKey', width: '10%', scopedSlots: { customRender: 'longText' } },
  // { title: '软件版本Key', align:'center', dataIndex: 'fromVerKey', width: '10%', scopedSlots: { customRender: 'longText' } },
  // { title: 'Token', align:'center', dataIndex: 'token', width: '10%', scopedSlots: { customRender: 'longText' } },
  { title: '最后登录设备信息', align: 'center', sorter: true, dataIndex: 'deviceInfo', width: '10%' },
  { title: '最后登录设备机器码', align: 'center', sorter: true, dataIndex: 'deviceCode', width: '10%' },
  { title: '卡密', align: 'center', sorter: true, dataIndex: 'ckey', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '在线状态', align: 'center', sorter: true, dataIndex: 'onlineType', width: '8%', scopedSlots: { customRender: 'online' } },
  { title: '备注', align: 'center', sorter: true, dataIndex: 'remark', width: '15%', scopedSlots: { customRender: 'longText' } }
]

export default {
  mixins: [listMixin],
  mounted () {
    this.getDataList()
    this.getMySoftListEx()
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
      mySoftListEx: [],
      visible: false,

      getSubStr: (str, length = 20) => {
        return (str == null || str == '') ? '-' : str.length < 20 ? str : str.substr(0, length) + '…'
      },
      softListEx: [],
      verList: [],
      iconType: 'down',
      expansion: false,

      softId: '',
      title: '回复管理'
    }
  },
  methods: {
    isExpired (_authTime) {
      const nowTimestamp = this.$moment().unix()
      if (_authTime < nowTimestamp && _authTime != -1) {
        return true
      }
      return false
    },
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
        .post('/myauth/web/getMyUserList', {
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
    getMySoftListEx () {
      this.$http.post('/myauth/web/getMySoftListEx').then(resJson => {
        if (resJson.success) {
          this.mySoftListEx = resJson.result
          if (resJson.result.length > 0) {
            this.softId = resJson.result[0].id
          }
        } else {
          this.$message.error(resJson.msg)
          console.error(resJson)
        }
      })
    }
  }
}
</script>
