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
                v-model="queryParam.id"
                placeholder="id"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-select
                v-model="queryParam.fromSoftId"
                allowClear
                showSearch
                :filterOption="filterOption"
                placeholder="软件"
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
              <a-select
                v-model="queryParam.role"
                allowClear
                showSearch
                :filterOption="filterOption"
                placeholder="角色"
                @change="roleChange"
              >
                <a-select-option
                  v-for="item in roleList"
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
                v-model="queryParam.user"
                placeholder="账号"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.qq"
                placeholder="QQ"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.regTime"
                placeholder="注册时间戳"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.lastTime"
                placeholder="最后登录时间戳"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.lastIp"
                placeholder="最后登录IP"
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
                placeholder="状态"
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
          <a @click="handleBehavior(record.id)">奖惩</a>
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
    <a-modal
      title="奖惩界面"
      :visible="visible"
      @ok="submitBehavior"
      @cancel="()=>{this.visible=false}"
    >
      <a-form-model :model="entity">
        <a-form-model-item
          label="变动金额"
          prop="money"
        >
          <a-select v-model="entity.flag">
            <a-select-option :value="1">
              奖励
            </a-select-option>
            <a-select-option :value="2">
              惩罚
            </a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item>
          <a-input
            v-model="entity.money"
            autocomplete="off"
          >
          </a-input>
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </a-card>
</template>

<script>
import EditForm from './EditForm'

const columns = [
  { title: 'Id', align: 'center', dataIndex: 'id', width: '8%' },
  { title: '所属软件', align: 'center', dataIndex: 'fromSoftName', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '账号', align: 'center', dataIndex: 'user', width: '8%' },
  { title: 'QQ', align: 'center', dataIndex: 'qq', width: '8%' },
  { title: '密码', align: 'center', dataIndex: 'pass', width: '8%' },
  { title: '角色名', align: 'center', dataIndex: 'roleName', width: '8%' },
  { title: '账户余额', align: 'center', dataIndex: 'money', width: '8%' },
  { title: '最后登录IP', align: 'center', dataIndex: 'lastIp', width: '8%' },
  { title: 'Token', align: 'center', dataIndex: 'token', width: '8%' },
  { title: '注册时间', align: 'center', dataIndex: 'regTime', width: '8%', scopedSlots: { customRender: 'time' } },
  { title: '最后登录时间', align: 'center', dataIndex: 'lastTime', width: '8%', scopedSlots: { customRender: 'time' } },
  { title: '状态', align: 'center', sorter: true, dataIndex: 'status', width: '8%', scopedSlots: { customRender: 'status' } },
  { title: '操作', dataIndex: 'action', align: 'center', fixed: 'right', scopedSlots: { customRender: 'action' } }
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
      queryParam: {},
      visible: false,

      getSubStr: (str, length = 20) => {
        return (str == null || str == '') ? '(无)' : str.length < 20 ? str : str.substr(0, length) + '…'
      },
      softListEx: [],
      roleList: [],
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
        .post('/myauth/web/getAdminList', {
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
      this.$refs.editForm.openForm(null, '新增管理员')
    },
    handleEdit (id) {
      this.behaviorType = 2
      this.$refs.editForm.openForm(id, '编辑管理员')
    },
    // 奖惩行为
    handleBehavior (id) {
      this.visible = true
      this.entity = {}
      this.entity.id = id
    },
    submitBehavior () {
      if (this.entity.flag == 2) {
        this.entity.money = 0 - this.entity.money
      }
      this.$http.post('/myauth/web/chaMoney', this.entity).then(resJson => {
        if (resJson.success) {
          this.$message.success(resJson.msg)
          this.visible = false

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
      this.$http.post('/myauth/web/delAdmin', { id }).then(resJson => {
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
            // this.softId = resJson.result[0].id
            this.queryParam.fromSoftId = resJson.result[0].id
            this.getRoleListEx('', this.queryParam.fromSoftId)
            this.getDataList()
          }
        } else {
          this.$message.error(resJson.msg)
          console.error(resJson)
        }
      })
    },
    getRoleListEx (name = '', softId) {
      if (softId) {
        this.$http.post('/myauth/web/getRoleListEx', { name, fromSoftId: softId }).then(resJson => {
          if (resJson.success) {
            this.roleList = resJson.result
            if (resJson.result.length > 0) {
              // this.queryParam.role = resJson.result[0].id
            }
          } else {
            this.$message.error(resJson.msg)
            console.error(resJson)
          }
        })
      }
    },
    softChange (value) {
      if (value) { this.getRoleListEx('', value) }
    },
    roleChange () {
      this.getDataList()
    },
    filterOption (input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    }
  }
}
</script>
