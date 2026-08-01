<template>
  <a-card :bordered="false">
    <div
      class="table-operator"
      style="margin-bottom:15px"
    >
      <a-button
        type="primary"
        icon="minus"
        @click="handleOpenSendMail(selectedRowKeys)"
        :disabled="!hasSelected()"
        :loading="loading"
      >启用通知</a-button>
      <a-button
        type="primary"
        icon="minus"
        @click="handleTurnOffSend(selectedRowKeys)"
        :disabled="!hasSelected()"
        :loading="loading"
      >关闭通知</a-button>
      <a-button
        type="primary"
        icon="redo"
        @click="getDataList()"
      >刷新</a-button>
      <a-button
        type="primary"
        icon="plus"
        @click="hanldleMailTest()"
      >邮件测试</a-button>
    </div>

    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="10">
          <a-col
            :md="3"
            :sm="24"
          >
            <a-form-model-item label="条件查询">
              <a-select
                v-model="queryParam.sendType"
                allowClear
                showSearch
                :filterOption="filterOption"
                placeholder="发送类型"
                @change="sendTypeChange"
              >
                <a-select-option
                  v-for="item in sendTypeList"
                  :key="item.sendType"
                >{{ item.sendType }}</a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-select
                v-model="queryParam.sendSwitch"
                allowClear
                placeholder="发送开关"
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
                v-model="queryParam.sendTheme"
                placeholder="主题"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.sendTitle"
                placeholder="标题"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.sendTemplates"
                placeholder="模板"
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
      :rowSelection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      @change="handleTableChange"
      :bordered="true"
      :scroll="{x:2000}"
      size="small"
    >
      <span
        slot="sendSwitch"
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
          <a
            v-if="record.sendSwitch==0"
            @click="handleOpenSendMail([record.id])"
          >启用通知</a>
          <a-divider
            v-if="record.sendSwitch==0"
            type="vertical"
          />
          <a
            v-if="record.sendSwitch==1"
            @click="handleTurnOffSend([record.id])"
          >关闭通知</a>
          <a-divider
            v-if="record.sendSwitch==1"
            type="vertical"
          />
          <a @click="handleEdit(record.id)">编辑</a>
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
    <a-modal
      title="邮件测试界面"
      :visible="visible"
      @ok="submitSendEmain"
      @cancel="()=>{this.visible=false}"
      :confirmLoading="loading"
      ok-text="发送"
    >
      <a-form-model
        :rules="rules"
        :model="entity"
      >
        <a-form-model-item
          label="收件邮箱"
          prop="sendMail"
        >
          <a-input
            v-model="entity.sendMail"
            autocomplete="off"
          >
          </a-input>
        </a-form-model-item>
        <a-form-model-item
          label="主题名称"
          prop="theme"
        >
          <a-input
            v-model="entity.theme"
            autocomplete="off"
          >
          </a-input>
        </a-form-model-item>
        <a-form-model-item
          label="邮件内容"
          prop="txt"
        >
          <a-textarea
            v-model="entity.txt"
            autocomplete="off"
            :rows="4"
          >
          </a-textarea>
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </a-card>
</template>

<script>
import EditForm from './EditForm'

const columns = [
  { title: '发送类型', align: 'center', dataIndex: 'sendType', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '发送开关', align: 'center', dataIndex: 'sendSwitch', width: '8%', scopedSlots: { customRender: 'sendSwitch' } },
  { title: '主题', align: 'center', dataIndex: 'sendTheme', width: '15%' },
  { title: '标题', align: 'center', dataIndex: 'sendTitle', width: '15%' },
  { title: '模板', align: 'center', dataIndex: 'sendTemplates', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '操作', dataIndex: 'action', align: 'left', scopedSlots: { customRender: 'action' } }
]

export default {
  components: {
    EditForm
  },
  mounted () {
    this.getSendTypeList()
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
        sendMail: [{ required: true, message: '必填' }],
        theme: [{ required: true, message: '必填' }],
        txt: [{ required: true, message: '必填' }]
      },

      getSubStr: (str, length = 20) => {
        return (str == null || str == '') ? '(无)' : str.length < 20 ? str : str.substr(0, length) + '…'
      },
      sendTypeList: [],
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
        .post('/myauth/web/getMailSendList', {
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
    onSelectChange (selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
    },
    hasSelected () {
      return this.selectedRowKeys.length > 0
    },
    hanldleAdd () {
      this.behaviorType = 1
      // this.$refs.editForm.openForm(null,  "新增邮件配置")
    },
    handleEdit (id) {
      this.behaviorType = 2
      this.$refs.editForm.openForm(id, '编辑邮件配置')
    },
    getSendTypeList () {
      this.$http.get('/myauth/web/getSendTypeList').then(resJson => {
        if (resJson.success) {
          this.softListEx = resJson.result
          if (resJson.result.length > 0) {
            // this.queryParam.sendType = resJson.result[0].sendType
            this.sendTypeList = resJson.result
            this.getDataList()
          }
        } else {
          this.$message.error(resJson.msg)
          console.error(resJson)
        }
      })
    },
    sendTypeChange (value) {
      if (value) { this.getDataList() }
    },
    hanldleMailTest () {
      this.entity = {}
      this.visible = true
    },
    submitSendEmain () {
      this.loading = true
      this.$http
        .post('/myauth/web/sendMailTest', this.entity)
        .then(resJson => {
          this.loading = false
          if (resJson.success) {
            this.$message.success(resJson.msg)
          } else {
            this.$message.error(resJson.msg)
          }
        })
    },
    handleOpenSendMail (ids) {
      const thisObj = this
      // 序列拼接ids（,）
      let idsStr = ''
      ids.forEach(id => {
        idsStr += (id + ',')
      })
      this.$confirm({
        title: '确认启用吗?',
        onOk () {
          return new Promise((resolve, reject) => {
            thisObj.submitOpenSendMail(idsStr, resolve, reject)
          }).catch(() => console.log('Oops errors!'))
        }
      })
    },
    submitOpenSendMail (ids, resolve, reject) {
      this.$http.post('/myauth/web/openSendMail', { ids }).then(resJson => {
        resolve()

        if (resJson.success) {
          this.$message.success(resJson.msg)

          this.getDataList()
        } else {
          this.$message.error(resJson.msg)
        }
      })
    },
    handleTurnOffSend (ids) {
      const thisObj = this
      // 序列拼接ids（,）
      let idsStr = ''
      ids.forEach(id => {
        idsStr += (id + ',')
      })
      this.$confirm({
        title: '确认关闭吗?',
        onOk () {
          return new Promise((resolve, reject) => {
            thisObj.submitTurnOffSend(idsStr, resolve, reject)
          }).catch(() => console.log('Oops errors!'))
        }
      })
    },
    submitTurnOffSend (ids, resolve, reject) {
      this.$http.post('/myauth/web/turnOffSend', { ids }).then(resJson => {
        resolve()

        if (resJson.success) {
          this.$message.success(resJson.msg)

          this.getDataList()
        } else {
          this.$message.error(resJson.msg)
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
