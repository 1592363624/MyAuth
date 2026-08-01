<template>
  <a-card :bordered="false">
    <div class="table-operator" style="margin-bottom:15px">
      <a-button type="primary" icon="plus" @click="hanldleAdd(softId)">新建</a-button>
      <a-button
        type="primary"
        icon="minus"
        @click="handleDelete(selectedRowKeys)"
        :disabled="!hasSelected()"
        :loading="loading">删除</a-button>
      <a-button type="primary" icon="redo" @click="getDataList()">刷新</a-button>
      <a-button type="primary" icon="plus" @click="updateUserAuthInfo()">用户批量添加授权</a-button>
    </div>

    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="10">
          <a-col :md="4" :sm="24">
            <a-form-model-item label="条件查询">
              <a-select
                v-model="queryParam.fromSoftId"
                allowClear
                showSearch
                :filterOption="filterOption"
                placeholder="所属软件"
                @change="softChange">
                <a-select-option v-for="item in softListEx" :key="item.id">{{ item.name }}</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col :md="2" :sm="24">
            <a-form-model-item prop="fromVerId">
              <a-select v-model="queryParam.fromVerId" allowClear @change="verChange" placeholder="所属版本">
                <a-select-option v-for="item in verList" :key="item.id">{{ item.ver }}</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col :md="2" :sm="24">
            <a-form-model-item>
              <a-input v-model="queryParam.fromAdminId" placeholder="管理员ID" />
            </a-form-model-item>
          </a-col>
          <a-col :md="2" :sm="24">
            <a-form-model-item>
              <a-input v-model="queryParam.user" placeholder="账号" />
            </a-form-model-item>
          </a-col>
          <a-col :md="2" :sm="24">
            <a-form-model-item>
              <a-input v-model="queryParam.name" placeholder="昵称" />
            </a-form-model-item>
          </a-col>
          <a-col :md="2" :sm="24">
            <a-form-model-item>
              <a-input v-model="queryParam.point" placeholder="点数" />
            </a-form-model-item>
          </a-col>
          <div v-if="expansion">
            <a-col :md="2" :sm="24">
              <a-form-model-item>
                <a-input v-model="queryParam.qq" placeholder="QQ" />
              </a-form-model-item>
            </a-col>
            <a-col :md="3" :sm="24">
              <a-form-model-item>
                <a-input v-model="queryParam.lastIp" placeholder="最后登录IP" />
              </a-form-model-item>
            </a-col>
            <a-col :md="3" :sm="24">
              <a-form-model-item>
                <a-input v-model="queryParam.lastTime" placeholder="最后登录时间戳" />
              </a-form-model-item>
            </a-col>
            <a-col :md="3" :sm="24">
              <a-form-model-item>
                <a-input v-model="queryParam.authTime" placeholder="授权到期时间戳" />
              </a-form-model-item>
            </a-col>
            <a-col :md="3" :sm="24">
              <a-form-model-item>
                <a-input v-model="queryParam.deviceInfo" placeholder="最后登录的设备" />
              </a-form-model-item>
            </a-col>
            <a-col :md="3" :sm="24">
              <a-form-model-item>
                <a-input v-model="queryParam.deviceCode" placeholder="最后登录的设备机器码" />
              </a-form-model-item>
            </a-col>
            <a-col :md="3" :sm="24">
              <a-form-model-item>
                <a-input v-model="queryParam.ckey" placeholder="卡密" />
              </a-form-model-item>
            </a-col>
            <a-col :md="3" :sm="24">
              <a-form-model-item>
                <a-input v-model="queryParam.remark" placeholder="备注" />
              </a-form-model-item>
            </a-col>
          </div>

          <a-col :md="6" :sm="24">
            <a-button type="primary" @click="getDataList">查询</a-button>
            <a-button style="margin-left: 8px" @click="() => (queryParam = {})">重置</a-button>
            <div style="display:inline;margin-left:15px;" @click="expansionX">
              <a> <a-icon :type="iconType" />{{ expansion ? '收起' : '展开' }} </a>
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
      :rowSelection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      :bordered="true"
      :scroll="{ x: 2000 }"
      size="small">
      <span slot="status" slot-scope="text">
        <a-tag color="#f50" v-if="text == 0">停用</a-tag>
        <a-tag color="#108ee9" v-else-if="text == 1">正常</a-tag>
        <a-tag color="#F4A460" v-else>未知</a-tag>
      </span>

      <span slot="online" slot-scope="text, record">
        <a-tag color="#cccccc" v-if="text == 0">离线</a-tag>
        <a-tag color="#008000" @click="queryUserOnlineInfo(record)" v-else-if="text > 0">在线:{{ text }}</a-tag>
        <a-tag v-else>未知</a-tag>
      </span>
      <span slot="timeOut" slot-scope="text">
        <a-tag color="#C0C0C0" v-if="isExpired(text)">已到期<br />({{ getFormatDate(text) }})</a-tag>
        <span v-else>{{ getFormatDate(text) }}</span>
      </span>
      <span slot="time" slot-scope="text">
        {{ getFormatDate(text) }}
      </span>

      <span slot="fromVer" slot-scope="text">
        <span v-if="text">{{ text }}</span>
        <span v-else>All</span>
      </span>

      <a-tooltip slot="longText" slot-scope="text">
        <span slot="title">
          {{ text }} </span>{{ getSubStr(text) }}
      </a-tooltip>

      <span slot="action" slot-scope="text, record">
        <template>
          <a @click="handleEdit(record.id, record.fromSoftId)">编辑</a>
          <a-divider type="vertical" />
          <a @click="handleDelete([record.id])">删除</a>
        </template>
      </span>
    </a-table>

    <edit-form ref="editForm" :afterSubmit="getDataList" :behaviorType="behaviorType"></edit-form>
    <user-online-info-form ref="userOnlineInfoForm" :afterSubmit="getDataList"></user-online-info-form>
    <update-user-auth-form ref="updateUserAuthForm" :afterSubmit="getDataList"></update-user-auth-form>
  </a-card>
</template>

<script>
import EditForm from './EditForm'
import UpdateUserAuthForm from './UpdateUserAuthForm'
import UserOnlineInfoForm from './UserOnlineInfoForm'
const columns = [
  { title: '所属软件', align: 'center', fixed: 'left', dataIndex: 'fromSoftName', width: 100 },
  { title: '所属版本', align: 'center', fixed: 'left', dataIndex: 'fromVerName', width: 100 },
  { title: '管理员ID', align: 'center', sorter: true, dataIndex: 'fromAdminId', width: 100 },
  { title: '用户', align: 'center', dataIndex: 'user', width: 100, scopedSlots: { customRender: 'longText' } },
  { title: '昵称', align: 'center', dataIndex: 'name', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '密码', align: 'center', dataIndex: 'pass', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '点数', align: 'center', sorter: true, dataIndex: 'point', width: '8%' },
  { title: 'QQ', align: 'center', dataIndex: 'qq', width: '8%', scopedSlots: { customRender: 'longText' } },
  {
    title: '注册时间',
    align: 'center',
    sorter: true,
    dataIndex: 'regTime',
    width: '10%',
    scopedSlots: { customRender: 'time' }
  },
  {
    title: '授权到期时间',
    align: 'center',
    sorter: true,
    dataIndex: 'authTime',
    width: 150,
    scopedSlots: { customRender: 'timeOut' }
  },
  {
    title: '最后登录时间',
    align: 'center',
    sorter: true,
    dataIndex: 'lastTime',
    width: '12%',
    scopedSlots: { customRender: 'time' }
  },
  {
    title: '最后登录IP',
    align: 'center',
    dataIndex: 'lastIp',
    width: '10%',
    scopedSlots: { customRender: 'longText' }
  },
  { title: '最后登录设备信息', align: 'center', dataIndex: 'deviceInfo', width: '13%' },
  { title: '最后登录设备机器码', align: 'center', dataIndex: 'deviceCode', width: '14%' },
  {
    title: '软件Key',
    align: 'center',
    dataIndex: 'fromSoftKey',
    width: '8%',
    scopedSlots: { customRender: 'longText' }
  },
  {
    title: '软件版本Key',
    align: 'center',
    dataIndex: 'fromVerKey',
    width: '10%',
    scopedSlots: { customRender: 'longText' }
  },
  { title: 'Token', align: 'center', dataIndex: 'token', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '卡密', align: 'center', dataIndex: 'ckey', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '在线状态', align: 'center', dataIndex: 'onlineType', width: '8%', scopedSlots: { customRender: 'online' } },
  { title: '备注', align: 'center', dataIndex: 'remark', width: '10%', scopedSlots: { customRender: 'longText' } },
  {
    title: '操作',
    dataIndex: 'action',
    align: 'center',
    fixed: 'right',
    width: 100,
    scopedSlots: { customRender: 'action' }
  }
]

export default {
  components: {
    EditForm,
    UserOnlineInfoForm,
    UpdateUserAuthForm
  },
  mounted () {
    this.getSoftListEx()
  },
  data () {
    return {
      data: [],
      selectedRowKeys: [],
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
        return str == null || str == '' ? '-' : str.length < 20 ? str : str.substr(0, length) + '…'
      },
      softListEx: [],
      verList: [],
      iconType: 'down',
      expansion: false,

      softId: '',
      title: '回复管理',
      behaviorType: 1
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
    onSelectChange (selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys
    },
    hasSelected () {
      return this.selectedRowKeys.length > 0
    },
    getFormatDate (nS) {
      return this.TimeHelper.getFormatDate(nS)
    },
    handleTableChange (pagination, filters, sorter) {
      this.pagination = { ...pagination }
      this.filters = { ...filters }
      this.sorter = { ...(sorter.column ? sorter : this.sorter0) }
      this.getDataList()
    },
    getDataList () {
      this.selectedRowKeys = []
      this.loading = true
      this.$http
        .post('/myauth/web/getUserList', {
          pageIndex: this.pagination.current,
          pageSize: this.pagination.pageSize,
          orders: [
            {
              column: this.sorter.order ? this.sorter.field || 'id' : null,
              asc: this.sorter.order == 'ascend' ? true : this.sorter.order == 'descend' ? false : null
            }
          ],
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
      this.$refs.editForm.openForm(null, softId, '新增用户')
    },
    handleEdit (id, softId) {
      this.behaviorType = 2
      this.$refs.editForm.openForm(id, softId, '编辑用户')
    },
    handleDelete (ids) {
      const thisObj = this
      // 序列拼接ids（,）
      let idsStr = ''
      ids.forEach(id => {
        idsStr += id + ','
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
      this.$http.post('/myauth/web/delUser', { ids }).then(resJson => {
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
      }
    },
    verChange (value) {
      this.queryParam.fromVerId = value
      if (value) this.getDataList()
    },
    getVerList () {
      this.queryParam.fromVerId = null

      this.$http.post('/myauth/web/getVersionListEx', { fromSoftId: this.queryParam.fromSoftId }).then(resJson => {
        if (resJson.success) {
          this.verList = resJson.result.sort(function (a, b) {
            const x = a.ver.toLowerCase()
            const y = b.ver.toLowerCase()
            if (x < y) {
              return 1
            }
            if (x > y) {
              return -1
            }
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
    filterOption (input, option) {
      return option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
    },
    queryUserOnlineInfo (user) {
      if (user) { this.$refs.userOnlineInfoForm.openForm(user) }
    },
    updateUserAuthInfo () {
      this.$refs.updateUserAuthForm.openForm()
    }
  }
}
</script>
