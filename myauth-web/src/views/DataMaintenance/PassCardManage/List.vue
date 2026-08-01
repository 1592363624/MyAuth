<template>
  <a-card :bordered="false">
    <div
      class="table-operator"
      style="margin-bottom:15px"
    >
      <a-button
        type="primary"
        icon="plus"
        @click="hanldleAdd(softId)"
        v-if="softId"
      >生成卡密</a-button>
      <a-button
        type="primary"
        icon="minus"
        @click="handleDelete(selectedRowKeys)"
        :disabled="!hasSelected()"
        :loading="loading"
      >删除</a-button>
      <a-button
        type="primary"
        icon="minus"
        @click="handleBanCard(selectedRowKeys)"
        :disabled="!hasSelected()"
        :loading="loading"
      >禁用卡密</a-button>
      <a-button
        type="primary"
        icon="minus"
        @click="handleUnBanCard(selectedRowKeys)"
        :disabled="!hasSelected()"
        :loading="loading"
      >解禁卡密</a-button>
      <a-button
        type="primary"
        @click="copyCard(selectedRows)"
        :disabled="!hasSelected()"
        :loading="loading"
      >复制卡密</a-button>
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
                @change="softChange"
                placeholder="所属软件"
              >
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
                v-model="queryParam.fromAdminId"
                placeholder="管理员Id"
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
                placeholder="卡密(精准)"
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
                placeholder="秒数"
              />
            </a-form-model-item>
          </a-col>
          <a-col
            :md="2"
            :sm="24"
          >
            <a-form-model-item>
              <a-input
                v-model="queryParam.letUser"
                placeholder="使用人账号"
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
                <a-select-option :key="1">已使用</a-select-option>
                <a-select-option :key="2">被禁用</a-select-option>
                <a-select-option :key="0">未使用</a-select-option>
              </a-select>
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
              style="margin-left: 8px"
              @click="() => (queryParam = {})"
            >重置</a-button>
            <a-button
              icon="download"
              type="primary"
              :disabled="!queryParam.fromSoftId"
              style="margin-left: 8px"
              @click="downloadClick"
            >导出卡密</a-button>
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
          color="#F4A460"
          v-if="text==0"
        >未使用</a-tag>
        <a-tag
          color="#87d068"
          v-else-if="text==1"
        >已使用</a-tag>
        <a-tag
          color="#108ee9"
          v-else-if="text==2"
        >被禁用</a-tag>
        <a-tag
          color="#f50"
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
        slot="user"
        slot-scope="text"
      >
        <a v-if="text">{{ text }}</a>
        <span v-else>(未设置)</span>

      </span>

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
            v-if="record.status==0"
            @click="handleBanCard([record.id])"
          >禁用卡密</a>
          <a-divider
            v-if="record.status==0"
            type="vertical"
          />
          <a
            v-if="record.status==2"
            @click="handleUnBanCard([record.id])"
          >解禁卡密</a>
          <a-divider
            v-if="record.status==2"
            type="vertical"
          />
          <a
            v-if="record.status!=1"
            @click="handleEdit(record.id, record.fromSoftId )"
          >编辑</a>
          <a-divider
            v-if="record.status!=1"
            type="vertical"
          />
          <a @click="handleDelete([record.id])">删除</a>
        </template>
      </span>
    </a-table>

    <edit-form
      ref="editForm"
      :afterSubmit="getDataList"
    ></edit-form>
    <add-form
      ref="addForm"
      :afterSubmit="getDataList"
      :behaviorType="behaviorType"
    ></add-form>
  </a-card>
</template>

<script>
import EditForm from './EditForm'
import AddForm from './AddForm'
import TokenCache from '@/utils/cache/TokenCache'
import ClipboardHelper from '@/utils/helper/ClipboardHelper'

const columns = [
  { title: '所属软件', align: 'center', dataIndex: 'fromSoftName', width: '8%' },
  { title: '管理员Id', align: 'center', dataIndex: 'fromAdminId', width: '8%' },
  { title: '卡密', align: 'center', dataIndex: 'ckey', width: '8%' },
  { title: '点数', align: 'center', dataIndex: 'point', width: '8%' },
  { title: '秒数', align: 'center', dataIndex: 'seconds', width: '8%' },
  { title: '添加时间', align: 'center', sorter: true, dataIndex: 'addTime', width: '8%', scopedSlots: { customRender: 'time' } },
  { title: '使用时间', align: 'center', sorter: true, dataIndex: 'letTime', width: '8%', scopedSlots: { customRender: 'time' } },
  { title: '使用人账号', align: 'center', dataIndex: 'letUser', width: '8%', scopedSlots: { customRender: 'user' } },
  { title: '卡密状态', align: 'center', dataIndex: 'status', width: '8%', scopedSlots: { customRender: 'status' } },
  { title: '操作', dataIndex: 'action', align: 'center', fixed: 'right', scopedSlots: { customRender: 'action' } }
]

export default {
  components: {
    EditForm,
    AddForm
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
      queryParam: { fromSoftId: 0 },
      visible: false,
      selectedRowKeys: [],
      selectedRows: [],

      getSubStr: (str, length = 20) => {
        return str == null ? '' : str.length < 20 ? str : str.substr(0, length) + '…'
      },

      softListEx: [],

      softId: null,
      behaviorType: 1
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
      this.selectedRows = []
      this.loading = true
      this.$http
        .post('/myauth/web/getCardList', {
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
            // console.info(resJson.result)
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
    hanldleAdd (softId) {
      this.behaviorType = 1
      this.$refs.addForm.openForm(null, softId, '新增卡密')
    },
    handleEdit (id, softId) {
      this.$refs.editForm.openForm(id, softId, '编辑卡密')
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
      this.$http.post('/myauth/web/delCard', { ids }).then(resJson => {
        resolve()

        if (resJson.success) {
          this.$message.success(resJson.msg)

          this.getDataList()
        } else {
          this.$message.error(resJson.msg)
        }
      })
    },
    handleUnBanCard (ids) {
      const thisObj = this
      // 序列拼接ids（,）
      let idsStr = ''
      ids.forEach(id => {
        idsStr += (id + ',')
      })
      this.$confirm({
        title: '确认解禁吗?',
        onOk () {
          return new Promise((resolve, reject) => {
            thisObj.submitUnBanCard(idsStr, resolve, reject)
          }).catch(() => console.log('Oops errors!'))
        }
      })
    },
    submitUnBanCard (ids, resolve, reject) {
      this.$http.post('/myauth/web/unBanCard', { ids }).then(resJson => {
        resolve()

        if (resJson.success) {
          this.$message.success(resJson.msg)

          this.getDataList()
        } else {
          this.$message.error(resJson.msg)
        }
      })
    },
    copyCard (selectedRows) {
      let cpText = ''
      if (selectedRows && selectedRows.length > 0) {
        selectedRows.forEach((selectedRow) => {
          cpText += (selectedRow.ckey + '\r\n')
        })
        // 非本地或https无法使用
        // window.navigator.clipboard.writeText(cpText)
        //   .then(() => {
        //     this.$message.success("复制成功")
        //   })
        //   .catch(err => {
        //       this.$message.error(err)
        //   });
        ClipboardHelper.copyToClipboard(cpText)
      }
    },
    handleBanCard (ids) {
      const thisObj = this
      // 序列拼接ids（,）
      let idsStr = ''
      ids.forEach(id => {
        idsStr += (id + ',')
      })
      this.$confirm({
        title: '确认禁用吗?',
        onOk () {
          return new Promise((resolve, reject) => {
            thisObj.submitBanCard(idsStr, resolve, reject)
          }).catch(() => console.log('Oops errors!'))
        }
      })
    },
    submitBanCard (ids, resolve, reject) {
      this.$http.post('/myauth/web/banCard', { ids }).then(resJson => {
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
        const fromSoft = this.softListEx.find((item, index) => {
        return item.id == value
        })
        if (fromSoft) {
          this.queryParam.fromSoftName = fromSoft.name
        }
        this.getDataList()
      }
    },
    getSoftListEx (name = '') {
      this.$http.post('/myauth/web/getSoftListEx', { name }).then(resJson => {
        if (resJson.success) {
          this.softListEx = resJson.result
          if (resJson.result.length > 0) {
            this.softId = resJson.result[0].id
            this.queryParam.fromSoftId = resJson.result[0].id
            this.queryParam.fromSoftName = resJson.result[0].name
            this.getDataList()
          }
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
    },
    downloadClick () {
      // this.$router.push({path:this.$publishRootUrl+'/myauth/web/exportCard',query: {...this.queryParam}})
      // this.$router.push({path:this.$publishRootUrl+'/myauth/web/exportCard',query: {...this.queryParam}})
      // window.location.href =
      const href = this.$rootUrl + '/myauth/web/exportCard?fromSoftId=' +
        this.queryParam.fromSoftId +
        (this.queryParam.fromAdminId
? '&fromAdminId=' +
          this.queryParam.fromAdminId
: '') +
        (this.queryParam.ckey
? '&ckey=' +
          this.queryParam.ckey
: '') +
        (this.queryParam.point
? '&point=' +
          this.queryParam.point
: '') +
        (this.queryParam.seconds
? '&seconds=' +
          this.queryParam.seconds
: '') +
        (this.queryParam.letTime
? '&letTime=' +
          this.queryParam.letTime
: '') +
        (this.queryParam.letUser
? '&letUser=' +
          this.queryParam.letUser
: '') +
        (this.queryParam.status || this.queryParam.status == 0
? '&status=' +
          this.queryParam.status
: '') +
        '&token=' +
          TokenCache.getToken()

      // let routeUrl = this.$router.resolve({
      //   path: href,
      //   query: {...this.queryParam},
      // });
      // window.open(href, "_blank");

      const myDate = new Date()
      this.loading = true
      this.$http({ url: href, method: 'get', responseType: 'blob' }).then((resJson) => {
        this.loading = false
        console.info(resJson)
        this.download(resJson, this.queryParam.fromSoftName + '卡密' + myDate.getTime() + '.xlsx')
      })
      // this.$http({url:'/myauth/web/exportCard', method: 'post',responseType: 'blob',data:this.queryParam }).then((resJson)=> {
      //   this.loading = false
      //   this.download(resJson,this.queryParam.fromSoftName+"卡密"+myDate.getTime()+".xls")
      // });
    },
    download (data, filename) {
      if (!data) {
          return
      }
      const url = window.URL.createObjectURL(new Blob([data]))
      const link = document.createElement('a')
      link.style.display = 'none'
      link.href = url
      // 获取文件名
      // download 属性定义了下载链接的地址而不是跳转路径
      link.setAttribute('download', filename)
      document.body.appendChild(link)
      link.click()
    },
    // 获取管理员列表
    getAdminList () {
      // to do
    }
  }
}
</script>
