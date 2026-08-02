<template>
  <div>
    <a-card :bordered="false">
      <div
        class="table-operator"
        style="margin-bottom:15px"
      >
        <a-button
          type="primary"
          icon="redo"
          @click="init()"
        >刷新</a-button>
      </div>

      <a-row :gutter="10">
        <a-col
          :md="4"
          :sm="24"
        >
          <a-card>
            <div slot="title">软件信息： <a-select
              v-model="queryParam.id"
              allowClear
              showSearch
              :filterOption="filterOption"
              placeholder="软件"
              @change="softChange"
              style="width:200px"
            >
              <a-select-option
                v-for="item in softListEx"
                :key="item.id"
              >{{ item.name }}</a-select-option>
            </a-select>
            </div>
            <a-form>
              <a-form-item label="软件名">
                <a-input
                  v-model="entity.softName"
                  disabled
                />
              </a-form-item>
              <a-form-item label="在线客户端数">
                <a-input
                  v-model="entity.onlineCount"
                  disabled
                />
              </a-form-item>
              <a-form-item label="总用户数">
                <a-input
                  v-model="entity.allCount"
                  disabled
                />
              </a-form-item>
              <a-form-item label="1天内新增用户数">
                <a-input
                  v-model="entity.nearly1"
                  disabled
                />
              </a-form-item>

              <a-form-item label="7天内新增用户数">
                <a-input
                  v-model="entity.nearly7"
                  disabled
                />
              </a-form-item>

              <a-form-item label="30天内新增用户数">
                <a-input
                  v-model="entity.nearly30"
                  disabled
                />
              </a-form-item>

            </a-form>
          </a-card>
        </a-col>
        <a-col
          :md="10"
          :sm="24"
        >
          <a-table
            ref="table"
            :columns="columns"
            :rowKey="row => row.id"
            :dataSource="data"
            :pagination="pagination"
            :loading="loading"
            @change="handleTableChange"
            :bordered="true"
            size="small"
          >
            <template slot="title">
              数据排行
            </template>
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
        </a-col>
        <a-col
          :md="10"
          :sm="24"
        >
          <a-table
            ref="table2"
            :columns="columns2"
            :rowKey="row => row.id"
            :dataSource="data2"
            :pagination="pagination2"
            :loading="loading2"
            @change="handleTableChange2"
            :bordered="true"
            size="small"
          >
            <template slot="title">
              设备排行
            </template>
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
        </a-col>
      </a-row>

    </a-card>
    <div>
      <a-row
        :gutter="20"
        style="margin-top:20px"
      >
        <a-col
          :md="12"
          :sm="24"
        >
          <a-spin :spinning="spinning1">
            <a-card>
              <div
                id="userDatNew"
                style=" height:600px"
              >

              </div>
            </a-card>
          </a-spin>
        </a-col>
        <a-col
          :md="12"
          :sm="24"
        >
          <a-spin :spinning="spinning2">
            <a-card>
              <div
                id="userDistribution"
                style=" height:600px"
              ></div>
            </a-card>
          </a-spin>
        </a-col>
      </a-row>

      <a-row
        :gutter="20"
        style="margin-top:20px"
      >
        <a-col
          :md="12"
          :sm="24"
        >
          <a-spin :spinning="spinning3">
            <a-card>
              <div
                id="cardDistribution"
                style=" height:600px"
              ></div>
            </a-card>
          </a-spin>
        </a-col>
        <a-col
          :md="12"
          :sm="24"
        >
          <a-spin :spinning="spinning4">
            <a-card>
              <div
                id="banTypeCount"
                style=" height:600px"
              ></div>
            </a-card>
          </a-spin>
        </a-col>
      </a-row>

    </div>
  </div>
</template>

<script>
// import Chart from './Chart.vue'
import * as echarts from 'echarts'
import { listMixin } from '@/utils/listMixin'

const columns = [
  { title: '软件名', align: 'center', sorter: true, dataIndex: 'fromSoftName', width: '8%' },
  { title: '类型', align: 'center', sorter: true, dataIndex: 'type', width: '8%' },
  { title: '内容', align: 'center', sorter: true, dataIndex: 'content', width: '8%', scopedSlots: { customRender: 'longText' } },
  { title: '次数', align: 'center', sorter: true, dataIndex: 'count', width: '8%' }
]

const columns2 = [
  { title: '软件名', align: 'center', sorter: true, dataIndex: 'fromSoftName', width: '8%' },
  { title: '设备信息', align: 'center', sorter: true, dataIndex: 'deviceInfo', width: '8%' },
  { title: '数量', align: 'center', sorter: true, dataIndex: 'count', width: '8%' }
]

export default {
  mixins: [listMixin],
  components: {
    // Chart
  },
  mounted () {
    const serverConfigJson = localStorage.getItem('serverConfig')
    if (serverConfigJson) {
      this.settings = JSON.parse(serverConfigJson)
    }
    this.getSoftListEx()
    // 创建一个定时器，刷新页面，配置项
    const refTime = this.settings.boardRefreshTime
    if (refTime && refTime > 0) {
      this.timer = setInterval(() => {
        console.info('自动刷新获取数据中，刷新周期(ms)：', refTime)
        this.init()
      }, refTime)
    }
  },
  beforeRouteLeave (to, from, next) {
    // 销毁组件，避免通过vue-router再次进入时，仍是上次的history缓存的状态
    this.$destroy(true)
    clearInterval(this.timer)
    next()
  },
  data () {
    return {
      timer: 0,
      data: [],
      data2: [],
      entity: {},
      selectedRowKeys: [],
      selectedRowKeys2: [],
      pagination: {
        current: 1,
        pageSize: 10,
        showTotal: (total, range) => `总数:${total} 当前:${range[0]}-${range[1]}`
      },
      pagination2: {
        current: 1,
        pageSize: 10,
        showTotal: (total, range) => `总数:${total} 当前:${range[0]}-${range[1]}`
      },
      filters: {},
      filters2: {},
      sorter: { field: 'Id', order: 'descend' },
      sorter0: { field: 'Id', order: 'descend' },
      sorter2: { field: 'id', order: 'descend' },
      loading: false,
      loading2: false,
      spinning1: true,
      spinning2: true,
      spinning3: true,
      spinning4: true,
      columns,
      columns2,
      queryParam: {},
      visible: false,

      getSubStr: (str, length = 20) => {
        return str == null ? '' : str.length < 20 ? str : str.substr(0, length) + '…'
      },
      softListEx: [],
      verList: [],
      eventList: [],

      softId: '',
      title: '',
      behaviorType: 1,

      userDatNewData: [],

      settings: {}
    }
  },
  methods: {
    init () {
      this.pagination = {
        current: 1,
        pageSize: 10,
        showTotal: (total, range) => `总数:${total} 当前:${range[0]}-${range[1]}`
      }
      this.pagination2 = {
        current: 1,
        pageSize: 10,
        showTotal: (total, range) => `总数:${total} 当前:${range[0]}-${range[1]}`
      }
      this.getUserDatNew()
      this.getUserDistribution()
      this.getCardDistribution()
      this.getBanTypeCount()
      this.getDataList()
    },
    getFormatDate (nS) {
      return this.TimeHelper.getFormatDate(nS)
    },
    handleTableChange (pagination, filters, sorter) {
      this.pagination = { ...pagination }
      this.filters = { ...filters }
      this.sorter = { ...sorter.column ? sorter : this.sorter0 }
      this.getDataRanking()
    },
     handleTableChange2 (pagination, filters, sorter) {
      this.pagination2 = { ...pagination }
      this.filters2 = { ...filters }
      this.sorter2 = { ...sorter }
      this.getUserDeviceInfoRanking()
    },
    getDataList () {
      this.pagination = {
        current: 1,
        pageSize: 10,
        showTotal: (total, range) => `总数:${total} 当前:${range[0]}-${range[1]}`
      }
      this.pagination2 = {
        current: 1,
        pageSize: 10,
        showTotal: (total, range) => `总数:${total} 当前:${range[0]}-${range[1]}`
      }
      this.getSoftStatisData()
      this.getDataRanking()
      this.getUserDeviceInfoRanking()
    },
    getSoftStatisData () {
      this.entity = {}
      this.$http.post('/myauth/web/getSoftStatisData', { id: this.queryParam.id }).then(resJson => {
        if (resJson.success) {
          this.entity = resJson.result
        } else {
          this.$message.error(resJson.msg)
          console.error(resJson)
        }
      })
    },
    setRowKey (o) {
      return { id: Math.random(), ...o }
    },
    getDataRanking () {
      this.selectedRowKeys = []
      this.data = []
      this.loading = true
      this.$http
        .post('/myauth/web/getDataRanking', {
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
            if (result.list && result.list.length) {
              this.data = result.list.map(this.setRowKey)
            }
            const pagination = { ...this.pagination }
            pagination.total = result.total
            this.pagination = pagination
          } else {
            this.$message.error(resJson.msg)
          }
        })
    },
    getUserDeviceInfoRanking () {
      this.selectedRowKeys2 = []
      this.data2 = []
      this.loading2 = true
      this.$http
        .post('/myauth/web/getUserDeviceInfoRanking', {
          pageIndex: this.pagination2.current,
          pageSize: this.pagination2.pageSize,
          orders2: {
            column: this.sorter.order ? this.sorter.field || 'id' : null,
            asc: this.sorter.order == 'ascend' ? true : this.sorter.order == 'descend' ? false : null
          },
          // ...this.filters
          ...this.queryParam
        })
        .then(resJson => {
          this.loading2 = false
          if (resJson.success) {
            let result = []
            result = resJson.result
             if (result.list && result.list.length) {
              this.data2 = result.list.map(this.setRowKey)
            }
            const pagination = { ...this.pagination2 }
            pagination.total = result.total
            this.pagination2 = pagination
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
            this.queryParam.id = resJson.result[0].id
            this.init()
          }
        } else {
          this.$message.error(resJson.msg)
          console.error(resJson)
        }
      })
    },
    softChange (value) {
      if (value) { this.getDataList() }
    },
    filterOption (input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    },
    setLineData (id, data, xField, yField, titleText = '未设置标题') {
      const chartDom = document.getElementById(id)
      const myChart = echarts.init(chartDom)
      const option = {
        title: {
          text: titleText,
          // subtext: 'Fake Data',
          left: 'center'
        },
        legend: {},
        tooltip: {},
        dataset: {
          source: data
        },
        xAxis: { type: 'category' },
        yAxis: {},
        series: [{
          type: 'line',
          encode: {
            x: xField,
            y: yField
          }
        }]
      }

      option && myChart.setOption(option)
    },
    setPieData (id, data, itemName, value, titleText = '未设置标题') {
      const chartDom = document.getElementById(id)
      const myChart = echarts.init(chartDom)
      const option = {
        title: {
          text: titleText,
          // subtext: 'Fake Data',
          left: 'center'
        },
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        dataset: {
          source: data
        },
        series: [
          {
            type: 'pie',
            radius: '50%',
            encode: {
              itemName,
              value
            }
          }
        ]
      }

      option && myChart.setOption(option)
    },
    setBarData (id, data, xField, yField, titleText = '未设置标题') {
      const chartDom = document.getElementById(id)
      const myChart = echarts.init(chartDom)
      const option = {
        title: {
          text: titleText,
          // subtext: 'Fake Data',
          left: 'center'
        },
        legend: {},
        tooltip: {},
        dataset: {
          source: data
        },
        xAxis: { type: 'category' },
        yAxis: {},
        series: [{
          type: 'bar',
          encode: {
            x: xField,
            y: yField
          }
        }]
      }

      myChart.setOption(option)
    },
    getUserDatNew () {
      this.spinning1 = true
      this.$http.get('/myauth/web/getUserDatNew').then(resJson => {
        this.spinning1 = false
        if (resJson.success) {
          if (resJson.result.list.length > 0) {
            this.setLineData('userDatNew', resJson.result.list, 'date', 'count', '近7天每日新增用户数')
          }
        } else {
          this.$message.error(resJson.msg)
        }
      })
    },
    getUserDistribution () {
      this.spinning2 = true
      this.$http.get('/myauth/web/getUserDistribution').then(resJson => {
        this.spinning2 = false
        if (resJson.success) {
          if (resJson.result.list.length > 0) {
            this.setPieData('userDistribution', resJson.result.list, 'fromSoftName', 'count', '用户分布比例')
          }
        } else {
          this.$message.error(resJson.msg)
        }
      })
    },
    getCardDistribution () {
      this.spinning3 = true
      this.$http.get('/myauth/web/getCardDistribution').then(resJson => {
        this.spinning3 = false
        if (resJson.success) {
          if (resJson.result.list.length > 0) {
            this.setPieData('cardDistribution', resJson.result.list, 'desc', 'count', '卡密状态比例')
          }
        } else {
          this.$message.error(resJson.msg)
        }
      })
    },
    getBanTypeCount () {
      this.spinning4 = true
      this.$http.get('/myauth/web/getBanTypeCount').then(resJson => {
        this.spinning4 = false
        if (resJson.success) {
          if (resJson.result.list.length > 0) {
            this.setPieData('banTypeCount', resJson.result.list, 'type', 'count', '封禁类型数量')
          }
        } else {
          this.$message.error(resJson.msg)
        }
      })
    }
  }
}
</script>
