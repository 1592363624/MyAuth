<template>
  <div>
    <div class="data-log">
      <a-spin
        :spinning="spinning"
        tip="获取软件更新中..."
        size="large"
      >
        <h1 style="text-align:center;">
          {{ spinning?"":data.softName }}
        </h1>
        <a-divider orientation="left">
          <h2>软件更新日志</h2>
        </a-divider>
        <a-back-top />
        <a-timeline class="time-line">
          <a-timeline-item
            v-for="(item, index) in data.updlogList"
            :key="index"
          >
            <div
              slot="dot"
              class="dots"
            >
              <a-icon
                type="smile"
                theme="twoTone"
                spin
                twoToneColor="#CC0033"
              />
            </div>
            <div>
              <h2>Ver:{{ item.ver+ " " + getFormatDate(item.upd_time) }}</h2>
              <div
                class="tag-view"
                v-for="(i2,index2) in item.upd_log_list"
                :key="index2"
              >
                <a-tag :color="colors[i2.tag-1]">{{ i2.tagInfo }}</a-tag>
                {{ i2.log }}
              </div>
            </div>
          </a-timeline-item>
        </a-timeline>
      </a-spin>
    </div>
  </div>
</template>

<script>
export default {
  created () {
    this.getUpdateLog(this.$route.query.skey)
  },
  data () {
    return {
      skey: '',
      data: { softName: '', updlogList: [] },
      colors: [
        '#009688',
        '#FFB800',
        '#1E9FFF',
        '#393D49',
        '#FF5722'
      ],
      spinning: true
    }
  },
  methods: {
    getLog (item) {
      if (!item.upd_log) return []
      const logs = item.upd_log.split('\n')
      const fullLogs = logs.map(this.setTagInfo)
      return fullLogs
    },
    setTagInfo (o) {
      const _regex = /(【)(.+?)(】)/g // 包含【】 括号
      const tempstr = o.match(_regex)
      const getTag = (str) => {
      // 不用switch case是为了防止不规范推送
        if (str.includes('新增')) return 1
        else if (str.includes('修复')) return 2
        else if (str.includes('优化')) return 3
        else if (str.includes('其他')) return 4
        else if (str.includes('删除')) return 5
        else return 0
      }
      const info = (tempstr ? (tempstr.length > 0 ? tempstr[0] : '') : '').replace('【', '').replace('】', '')
      const tag = getTag(info)
      return { tag, tagInfo: info, log: o.replace(tempstr ? (tempstr.length > 0 ? tempstr[0] : '') : '', '') }
    },
    setLogs (o) {
      const tagInfos = this.getLog(o)
      // console.info("tagInfos:",tagInfos)
      return { ver: o.ver, upd_time: o.upd_time, upd_log_list: tagInfos }
    },
    getFormatDate (nS) {
      if (!nS) return '-'
      if (parseInt(nS) == -1) return '永久'
      const date = new Date(parseInt(nS) * 1000)
      const formatDate = (now) => {
        const year = now.getFullYear()
        const month = this.dateAddZero((now.getMonth() + 1))
        const date = this.dateAddZero(now.getDate())
        const hour = this.dateAddZero(now.getHours())
        const minute = this.dateAddZero(now.getMinutes())
        const second = this.dateAddZero(now.getSeconds())
        return year + '-' + month + '-' + date + ' ' + hour + ':' + minute + ':' + second
      }
      return formatDate(date)
    },
    dateAddZero (num) {
      return num < 10 ? ('0' + num) : num
    },
    getUpdateLog (skey) {
      console.info('skey:', skey)
      const url = '/myauth/web/getUpdateLog?skey=' + skey
      this.$http.get(url).then(resJson => {
        if (resJson.success) {
          resJson.result.updlogList = resJson.result.updlogList.map(this.setLogs)

          this.data = resJson.result
          this.spinning = false
        } else {
          this.spinning = false
          this.$message.error(resJson.msg)
        }
      })
    }
  }
}
</script>

<style lang="less">
.data-log {
  // min-width: 400px;
  max-width: 600px;
  // height: 50%;
  overflow: visible;
  margin: auto;
  margin-top: 25px;
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;

  h1 {
    font-size: 24px;
  }
  h2 {
    font-size: 18px;
    margin-left: 3px;
  }
  .tag-view {
    line-height: 25px;
  }

  .time-line {
    // width: 300px;
  }

  .dots {
    margin-top: 6px;
    font-size: 20px;
  }
}
</style>
