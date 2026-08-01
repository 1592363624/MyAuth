<template>
  <div>
    <a-modal
      :title="this.title"
      width="40%"
      :visible="visible"
      @ok="handleSubmit"
      @cancel="() => { this.visible = false }">
      <a-spin :spinning="loading">
        <a-list v-if="data.length > 0">
          <RecycleScroller
            v-infinite-scroll="handleInfiniteOnLoad"
            style="height: 400px"
            :items="data"
            :item-size="160"
            key-field="token"
            :infinite-scroll-disabled="busy"
            :infinite-scroll-distance="10">
            <a-list-item slot-scope="{item, index}">
              <a-list-item-meta>
                <div slot="description">
                  <a-tooltip placement="topLeft"><template slot="title">
                                                   {{ getFormatDate(item.lastTime) }}
                                                 </template>
                    <div>{{ getSubStr("登录时间：" + getFormatDate(item.lastTime), 80) }}</div>
                  </a-tooltip>
                  <a-tooltip placement="topLeft"><template slot="title">
                                                   {{ item.lastIp }}
                                                 </template>
                    <div>{{ getSubStr("登录IP：" + item.lastIp, 80) }}</div>
                  </a-tooltip>
                  <a-tooltip placement="topLeft"><template slot="title">
                                                   {{ item.deviceInfo }}
                                                 </template>
                    <div>{{ getSubStr("登录设备信息：" + item.deviceInfo, 80) }}</div>
                  </a-tooltip>
                  <a-tooltip placement="topLeft"><template slot="title">
                                                   {{ item.deviceCode }}
                                                 </template>
                    <div>{{ getSubStr("登录设备机器码：" + item.deviceCode, 80) }}</div>
                  </a-tooltip>
                  <a-tooltip placement="topLeft"><template slot="title">
                                                   {{ item.token }}
                                                 </template>
                    <div>{{ getSubStr("token：" + item.token, 80) }}</div>
                  </a-tooltip>
                  <a-divider />
                </div>
                <a slot="title">{{ (index + 1) + "、软件：" + item.fromSoftName + "，版本：" + item.fromVerName }}</a>
              </a-list-item-meta>
            </a-list-item>
          </RecycleScroller>
        </a-list>
        <div v-else>当前无在线设备</div>
      </a-spin>
    </a-modal>
  </div>
</template>

<script>
// 为扩展为无线列表保留，后面又需求再做
import infiniteScroll from 'vue-infinite-scroll'
import { RecycleScroller } from 'vue-virtual-scroller'
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
export default {
  directives: { infiniteScroll },
  components: {
    RecycleScroller
  },
  props: {
    afterSubmit: {
      type: Function,
      default: null
    }
  },
  data () {
    return {
      layout: {
        labelCol: { span: 5 },
        wrapperCol: { span: 18 }
      },
      visible: false,
      loading: false,
      loadingMore: false,
      showLoadingMore: true,
      data: [],
      title: '用户软件在线信息',
      softId: null,
      getSubStr: (str, length = 20) => {
        return str == null || str == '' ? '-' : str.length < length ? str : str.substr(0, length) + '…'
      },
      busy: false,
      user: {}
    }
  },
  methods: {
    init (user) {
      this.user = user
      this.visible = true
      this.data = []
    },
    getFormatDate (nS) {
      return this.TimeHelper.getFormatDate(nS)
    },
    openForm (user) {
      this.init(user)
      if (user.user && user.fromSoftId) {
        this.confirmLoading = true
        this.$http.post('/myauth/web/queryUserOnlineInfo', { user: user.user, fromSoftId: user.fromSoftId }).then(resJson => {
          this.confirmLoading = false
          if (resJson.success) {
            if (resJson.result) {
              const temp = []
              resJson.result.forEach(element => {
                temp.push({ ...element, fromSoftName: user.fromSoftName ? user.fromSoftName : '(未定义)' })
              })

              this.data = temp
            }
            this.afterSubmit()
          } else {
            this.$message.error(resJson.msg)
          }
        })
      }
    },
    handleSubmit () {
      this.visible = false
    },
    handleInfiniteOnLoad () {
      // 为扩展为无线列表保留
      this.openForm(this.user)
    }
  }
}
</script>

<style>
.demo-infinite-container {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  overflow: auto;
  padding: 8px 24px;
  height: 300px;
}
</style>
