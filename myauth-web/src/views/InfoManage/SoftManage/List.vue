<template>
  <a-card :bordered="false">
    <div class="table-operator">
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
            :md="4"
            :sm="24"
          >
            <a-form-model-item label="条件查询">
              <a-input
                v-model="queryParam.skey"
                placeholder="软件skey(精准)"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.name"
                placeholder="软件名称"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item prop="status">
              <a-select
                v-model="queryParam.status"
                allowClear
                placeholder="状态"
              >
                <a-select-option :key="1">正常</a-select-option>
                <a-select-option :key="2">维护</a-select-option>
                <a-select-option :key="0">停用</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item prop="type">
              <a-select
                v-model="queryParam.type"
                allowClear
                placeholder="类型"
              >
                <a-select-option :key="1">免费</a-select-option>
                <a-select-option :key="0">收费</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item prop="genStatus">
              <a-select
                v-model="queryParam.genStatus"
                allowClear
                placeholder="数据是否加密"
              >
                <a-select-option :key="1">加密</a-select-option>
                <a-select-option :key="0">不加密</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item prop="bindDeviceCode">
              <a-select
                v-model="queryParam.bindDeviceCode"
                allowClear
                placeholder="是否绑定机器码"
              >
                <a-select-option :key="1">绑定</a-select-option>
                <a-select-option :key="0">不绑定</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item prop="register">
              <a-select
                v-model="queryParam.register"
                allowClear
                placeholder="是否开启注册"
              >
                <a-select-option :key="1">开启</a-select-option>
                <a-select-option :key="0">关闭</a-select-option>
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
          color="#87d068"
          v-else-if="text==2"
        >维护</a-tag>
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
          color="#108ee9"
          v-if="text==0"
        >收费</a-tag>
        <a-tag
          color="#F4A460"
          v-else-if="text==1"
        >免费</a-tag>
        <a-tag v-else>未知</a-tag>
      </span>

      <span
        slot="addTime"
        slot-scope="text"
      >
        {{ getFormatDate(text) }}
      </span>

      <span
        slot="yesNo"
        slot-scope="text"
      >
        <a-tag
          color="#f50"
          v-if="text==0"
        >否</a-tag>
        <a-tag
          color="#108ee9"
          v-else-if="text==1"
        >是</a-tag>
        <a-tag v-else>未知</a-tag>
      </span>

      <span
        slot="register"
        slot-scope="text"
      >
        <a-tag
          color="#f50"
          v-if="text==0"
        >关闭注册</a-tag>
        <a-tag
          color="#108ee9"
          v-else-if="text==1"
        >开启注册</a-tag>
        <a-tag v-else>未知</a-tag>
      </span>

      <span
        slot="action"
        slot-scope="text, record"
      >
        <template>
          <!-- <a
            v-if="!isMobile()"
            @click="hanldleVersion(record.id, record.name)"
          >版本管理</a>
          <a-divider
            v-if="!isMobile()"
            type="vertical"
          />
          <a
            v-if="!isMobile()"
            @click="hanldleMsg(record.id, record.name)"
          >回复管理</a>
          <a-divider
            v-if="!isMobile()"
            type="vertical"
          />
          <a
            v-if="!isMobile()"
            @click="hanldleCardPass(record.id, record.name)"
          >卡密管理</a>
          <a-divider
            v-if="!isMobile()"
            type="vertical"
          /> -->
          <a @click="handleEdit(record.id)">编辑</a>
          <a-divider type="vertical" />
          <a @click="routeToUpdateLog(record.skey)">查看更新日志</a>
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
    <!-- <div v-if="!isMobile()">
      <a-modal
        title="版本管理"
        width="80%"
        :visible="verVisible"
        @ok="()=>{this.verVisible=false}"
        @cancel="()=>{this.verVisible=false}"
      >
        <VersionsManage ref="versionsManage"></VersionsManage>
      </a-modal>
      <a-modal
        title="回复管理"
        width="80%"
        :visible="msgVisible"
        @ok="()=>{this.msgVisible=false}"
        @cancel="()=>{this.msgVisible=false}"
      >
        <MsgManage ref="msgManage"></MsgManage>
      </a-modal>

      <a-modal
        title="卡密管理"
        width="80%"
        :visible="passCardVisible"
        @ok="()=>{this.passCardVisible=false}"
        @cancel="()=>{this.passCardVisible=false}"
      >
        <CardPassManage ref="cardPassManage"></CardPassManage>
      </a-modal>
    </div> -->
  </a-card>
</template>

<script>
import EditForm from './EditForm'
/** 若需使用，请修改路径 */
// import VersionsManage from './VersionsManage/List'
// import MsgManage from './MsgManage/List'
// import CardPassManage from './CardPassManage/List'

// import { mixin, mixinDevice } from '@/utils/mixin/'

const columns = [
  { title: '软件名称', align: 'center', sorter: true, dataIndex: 'name', width: '8%' },
  { title: '软件skey', align: 'center', sorter: true, dataIndex: 'skey', width: '8%' },
  { title: '状态', align: 'center', sorter: true, dataIndex: 'status', width: '8%', scopedSlots: { customRender: 'status' } },
  { title: '类型', align: 'center', sorter: true, dataIndex: 'type', width: '8%', scopedSlots: { customRender: 'type' } },
  { title: '添加时间', align: 'center', sorter: true, dataIndex: 'addTime', width: '8%', scopedSlots: { customRender: 'addTime' } },
  { title: '数据加密秘钥', align: 'center', sorter: true, dataIndex: 'genKey', width: '7%' },
  { title: '数据加密与否', align: 'center', sorter: true, dataIndex: 'genStatus', width: '8%', scopedSlots: { customRender: 'yesNo' } },
  { title: '是否绑定机器码', align: 'center', sorter: true, dataIndex: 'bindDeviceCode', width: '7%', scopedSlots: { customRender: 'yesNo' } },
  { title: '注册情况', align: 'center', sorter: true, dataIndex: 'register', width: '7%', scopedSlots: { customRender: 'register' } },
  { title: '心跳有效时间(s)', align: 'center', sorter: true, dataIndex: 'heartTime', width: '7%' },
  { title: 'sign有效时间(s)', align: 'center', sorter: true, dataIndex: 'signTime', width: '7%' },
  { title: '同账号最大在线数', align: 'center', sorter: true, dataIndex: 'maxOnlineCount', width: '8%' },
  { title: '操作', dataIndex: 'action', align: 'center', fixed: 'right', scopedSlots: { customRender: 'action' } }
]

export default {
  // mixins: [mixin, mixinDevice],
  components: {
    EditForm
    // VersionsManage,
    // MsgManage,
    // CardPassManage
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
      verVisible: false,
      msgVisible: false,
      passCardVisible: false,
      behaviorType: 1
    }
  },
  methods: {
    routeToUpdateLog (skey) {
      // this.$router.push({name: 'UpdateLog', path: '/Soft/getUpdateLog/', query: {skey: skey}});
      const routeUrl = this.$router.resolve({
        path: '/Soft/getUpdateLog',
        query: { skey }
      })
      window.open(routeUrl.href, '_blank')
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
        .post('/myauth/web/getSoftList', {
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
      this.$refs.editForm.openForm()
    },
    // hanldleVersion(softId, name){
    //   this.verVisible = true
    //   setTimeout(() => {
    //     this.$refs.versionsManage.openForm(softId,"版本管理-"+name)
    //   }, 200);
    // },
    // hanldleMsg(softId, name){
    //   this.msgVisible = true
    //   setTimeout(() => {
    //     this.$refs.msgManage.openForm(softId,"回复管理-"+name)
    //   }, 200);
    // },
    // hanldleCardPass(softId, name){
    //   this.passCardVisible = true
    //   setTimeout(() => {
    //     this.$refs.cardPassManage.openForm(softId,"卡密管理-"+name)
    //   }, 200);
    // },
    handleEdit (id) {
      this.behaviorType = 2
      this.$refs.editForm.openForm(id)
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
      this.$http.post('/myauth/web/delSoft', { id }).then(resJson => {
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
