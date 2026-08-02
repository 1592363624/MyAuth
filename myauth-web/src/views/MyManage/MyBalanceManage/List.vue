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
      <a-button
        type="primary"
        @click="userAgencyPassCard()"
      >使用代理余额卡密</a-button>
      <a-button
        type="primary"
        icon="plus"
        @click="addPay()"
      >充值</a-button>
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
                v-model="queryParam.afterMoney"
                placeholder="变动后的余额"
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
                v-model="queryParam.data"
                placeholder="变动描述"
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
              <a-select
                v-model="queryParam.type"
                placeholder="变动类型"
                @change="debounceSearch"
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
              <a-input-number
                v-model="queryParam.addTime"
                placeholder="变动时间戳"
                style="width:130px;"
              />
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
      :scroll="{x:1000}"
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
    </a-table>
    <a-modal
      title="使用代理余额卡密"
      width="40%"
      :visible="visible"
      :confirmLoading="confirmLoading"
      @ok="handleSubmit"
      @cancel="()=>{this.visible=false}"
    >
      <a-form-model
        ref="form"
        :model="entity"
        :rules="rules"
        v-bind="layout"
      >
        <a-form-model-item
          label="卡密"
          prop="ckey"
        >
          <a-input v-model="entity.ckey" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
    <a-modal
      title="充值"
      width="40%"
      :visible="visiblePay"
      :confirmLoading="confirmLoadingPay"
      @ok="handlePaySubmit"
      @cancel="()=>{this.visiblePay=false}"
    >
      <a-form-model
        ref="formPay"
        :model="entityPay"
        :rules="rulesPay"
        v-bind="layout"
      >
        <a-form-model-item
          label="通道"
          prop="id"
        >
          <a-select
            v-model="entityPay.payId"
            allowClear
            showSearch
            :filterOption="filterOption"
            placeholder="支付类型"
            @change="changeType"
          >
            <a-select-option
              v-for="item in payTypeList"
              :key="item.id"
            >{{ item.name }}</a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          label="通道名"
          prop="payDriver"
          hidden="true"
        >
          <a-input
            :disabled="true"
            v-model="entityPay.payDriver"
            autocomplete="off"
          >
          </a-input>
        </a-form-model-item>
        <a-form-model-item
          label="充值金额"
          prop="money"
        >
          <a-input-number
            :min="0"
            :step="0.01"
            v-model="entityPay.money"
          />（人民币￥）
        </a-form-model-item>
      </a-form-model>
    </a-modal>
    <pay-form
      ref="payForm"
      :afterSubmit="()=>{this.getDataList();this.visiblePay=false}"
    ></pay-form>
  </a-card>
</template>

<script>
import PayForm from '@/views/MyManage/MyBalanceManage/PayForm'
import { listMixin } from '@/utils/listMixin'
const columns = [
  { title: '类型', align: 'center', sorter: true, dataIndex: 'type', width: '8%' },
  { title: '变动时间', align: 'center', sorter: true, dataIndex: 'addTime', width: '8%', scopedSlots: { customRender: 'time' } },
  { title: '变动余额', align: 'center', sorter: true, dataIndex: 'money', width: '8%' },
  { title: '变动后的余额', align: 'center', sorter: true, dataIndex: 'afterMoney', width: '8%' },
  { title: '变动账号ID', align: 'center', sorter: true, dataIndex: 'adminId', width: '8%' },
  { title: '描述信息', align: 'center', sorter: true, dataIndex: 'data' }
]

export default {
  mixins: [listMixin],
  components: {
    PayForm
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
      queryParam: {},
      visible: false,
      visiblePay: false,

      getSubStr: (str, length = 20) => {
        return str == null ? '' : str.length < 20 ? str : str.substr(0, length) + '…'
      },

      entity: {},
      entityPay: {},
      payInfo: {},
      rules: {
        ckey: [{ required: true, message: '必填' }]
      },
      rulesPay: {
        money: [{ required: true, message: '必填' }],
        type: [{ required: true, message: '必填' }]
      },
      layout: {
        labelCol: { span: 5 },
        wrapperCol: { span: 18 }
      },
      confirmLoading: false,
      confirmLoadingPay: false,
      payTypeList: []
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
        .post('/myauth/web/getMyAlogList', {
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
    userAgencyPassCard () {
      this.visible = true
      this.entity = {}
    },
    handleSubmit () {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        const url = '/myauth/web/letACard'
        this.confirmLoading = true
        if (url) {
          this.$http.post(url, this.entity).then(resJson => {
            this.confirmLoading = false

            if (resJson.success) {
              this.$message.success(resJson.msg)
              this.visible = false
            } else {
              this.$message.error(resJson.msg)
            }
          })
        }
      })
    },
    changeType (key) {
      this.entityPay.payDriver = this.payTypeList.find(x => x.id == key).driver
    },
    getPayTypeList () {
      this.loading = true
      this.$http
        .get('/myauth/web/getAllPayType')
        .then(resJson => {
          this.loading = false
          if (resJson.success) {
            const result = resJson.result
            this.payTypeList = result
          } else {
            this.$message.error(resJson.msg)
          }
        })
    },
    addPay () {
      this.visiblePay = true
      this.entityPay = {}
      this.getPayTypeList()
    },
    handlePaySubmit () {
      this.$refs.formPay.validate(valid => {
        if (!valid) {
          return
        }
        const url = '/myauth/web/depositMoneyLink'
        this.confirmLoading = true
        if (url) {
          this.$http.post(url, this.entityPay).then(resJson => {
            this.confirmLoading = false

            if (resJson.success) {
              this.$message.success('获取订单链接成功，即刻跳转')
              this.visiblePayInfo = true
              const payInfo = resJson.result
              this.$refs.payForm.openForm(payInfo)
              window.open(resJson.msg, '_blank')
              // this.visible = false
            } else {
              this.$message.error(resJson.msg)
            }
          })
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
