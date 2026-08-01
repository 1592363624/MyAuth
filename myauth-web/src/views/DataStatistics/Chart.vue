<template>
  <div>
    <a-row
      :gutter="20"
      style="margin-top:20px"
    >
      <a-col
        :md="12"
        :sm="24"
      >
        <a-card>
          <div
            id="userDatNew"
            style=" height:600px"
          ></div>
        </a-card>
      </a-col>
      <a-col
        :md="12"
        :sm="24"
      >
        <a-card>
          <div
            id="userDistribution"
            style=" height:600px"
          ></div>
        </a-card>
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
        <a-card>
          <div
            id="cardDistribution"
            style=" height:600px"
          ></div>
        </a-card>
      </a-col>
      <a-col
        :md="12"
        :sm="24"
      >
        <a-card>
          <div
            id="banTypeCount"
            style=" height:600px"
          ></div>
        </a-card>
      </a-col>
    </a-row>

  </div>

</template>

<script>
import * as echarts from 'echarts'
const tempData = [
  { count: 10, date: '2022-03-10' },
  { count: 10, date: '2022-03-11' },
  { count: 9, date: '2022-03-12' },
  { count: 8, date: '2022-03-13' },
  { count: 12, date: '2022-03-14' },
  { count: 14, date: '2022-03-15' },
  { count: 15, date: '2022-03-16' },
  { count: 16, date: '2022-03-17' }
]

export default {
  data () {
    return {
      title: '',
      keywords: '',
      description: '',
      userDatNewData: [],
      tempData
    }
  },
  mounted () {
    this.getUserDatNew()
    this.getUserDistribution()
    this.getCardDistribution()
    this.getBanTypeCount()
  },
  methods: {
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

      option && myChart.setOption(option)
    },
    getUserDatNew () {
      this.$http.get('/myauth/web/getUserDatNew').then(resJson => {
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
      this.$http.get('/myauth/web/getUserDistribution').then(resJson => {
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
      this.$http.get('/myauth/web/getCardDistribution').then(resJson => {
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
      this.$http.get('/myauth/web/getBanTypeCount').then(resJson => {
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
