<template>
  <a-modal
    :title="title"
    width="72%"
    :visible="visible"
    :footer="null"
    @cancel="()=>{this.visible=false}"
  >
    <a-alert
      type="info"
      show-icon
      style="margin-bottom: 12px"
    >
      <template slot="message">
        私钥只保存在服务端数据库，客户端仅内嵌公钥，因此破解者无法伪造功能票据。
        生成新密钥后，需要把新公钥填入客户端 <code>hardcoded.go</code> 的
        <code>FeatureSignPublicKey</code> 并重新编译，否则旧客户端会验签失败。
      </template>
    </a-alert>

    <div style="margin-bottom: 12px">
      <a-button
        type="primary"
        icon="plus"
        @click="handleGenerate"
      >生成新密钥</a-button>
      <a-button
        style="margin-left: 8px"
        icon="redo"
        @click="loadList"
      >刷新</a-button>
    </div>

    <a-table
      :columns="columns"
      :rowKey="row => row.id"
      :dataSource="keys"
      :loading="loading"
      :pagination="false"
      :bordered="true"
      size="small"
    >
      <span
        slot="kid"
        slot-scope="text"
      >
        <a-tag color="#108ee9">{{ text }}</a-tag>
      </span>

      <span
        slot="publicKey"
        slot-scope="text"
      >
        <a-tooltip :title="text">
          <code>{{ shortKey(text) }}</code>
        </a-tooltip>
        <a
          style="margin-left: 8px"
          @click="copyText(text)"
        >复制</a>
      </span>

      <span
        slot="status"
        slot-scope="text"
      >
        <a-tag :color="text == 1 ? '#87d068' : '#f50'">
          {{ text == 1 ? '启用' : '停用' }}
        </a-tag>
      </span>

      <span
        slot="addTime"
        slot-scope="text"
      >
        {{ getFormatDate(text) }}
      </span>

      <span
        slot="action"
        slot-scope="text, record"
      >
        <a @click="handleEditRules(record)">编辑规则</a>
      </span>
    </a-table>

    <a-modal
      title="编辑功能规则"
      width="62%"
      :visible="rulesVisible"
      :confirmLoading="rulesLoading"
      @ok="submitRules"
      @cancel="()=>{this.rulesVisible=false}"
    >
      <a-alert
        type="warning"
        show-icon
        style="margin-bottom: 12px"
      >
        <template slot="message">
          规则由服务端计算后签名下发，客户端不再自行判断。minPoint 为解锁所需赞助点数；
          allowedUsers 白名单免点数；deniedUsers 黑名单强制隐藏；disabledModules 全局禁用。
        </template>
      </a-alert>
      <a-textarea
        v-model="rulesText"
        :rows="16"
        style="font-family: monospace"
      />
    </a-modal>
  </a-modal>
</template>

<script>
const columns = [
  { title: '密钥ID (kid)', align: 'center', dataIndex: 'kid', width: '20%', scopedSlots: { customRender: 'kid' } },
  { title: '公钥（填入客户端 hardcoded.go）', align: 'center', dataIndex: 'publicKey', width: '38%', scopedSlots: { customRender: 'publicKey' } },
  { title: '状态', align: 'center', dataIndex: 'status', width: '10%', scopedSlots: { customRender: 'status' } },
  { title: '生成时间', align: 'center', dataIndex: 'addTime', width: '18%', scopedSlots: { customRender: 'addTime' } },
  { title: '操作', align: 'center', dataIndex: 'action', width: '14%', scopedSlots: { customRender: 'action' } }
]

export default {
  data () {
    return {
      visible: false,
      loading: false,
      softId: null,
      title: '功能票据密钥',
      keys: [],
      columns,
      rulesVisible: false,
      rulesLoading: false,
      rulesText: '',
      editingKey: null
    }
  },
  methods: {
    openForm (softId, softName) {
      this.softId = softId
      this.title = `功能票据密钥 - ${softName || ''}`
      this.visible = true
      this.keys = []
      this.loadList()
    },
    loadList () {
      if (!this.softId) {
        return
      }
      this.loading = true
      this.$http.post('/myauth/web/listFeatureSignKeys', { id: this.softId }).then(resJson => {
        this.loading = false
        if (resJson.success) {
          this.keys = resJson.result || []
        } else {
          this.$message.error(resJson.msg)
        }
      }).catch(() => {
        this.loading = false
      })
    },
    handleGenerate () {
      const that = this
      this.$confirm({
        title: '确认生成新的密钥对？',
        content: '旧密钥会置为停用。旧客户端必须重新填入新公钥并重新编译，否则会验签失败、受控功能被锁。',
        onOk () {
          return that.$http.post('/myauth/web/genFeatureSignKey', { id: that.softId }).then(resJson => {
            if (resJson.success) {
              that.$message.success('已生成，请复制公钥到客户端')
              that.loadList()
            } else {
              that.$message.error(resJson.msg)
            }
          })
        }
      })
    },
    handleEditRules (record) {
      this.editingKey = record
      this.rulesText = record.featureRules || ''
      this.rulesVisible = true
    },
    submitRules () {
      let parsed
      try {
        parsed = JSON.parse(this.rulesText)
      } catch (e) {
        this.$message.error('规则不是合法 JSON：' + e.message)
        return
      }
      this.rulesLoading = true
      this.$http.post('/myauth/web/updFeatureRules', {
        keyId: this.editingKey.id,
        softId: this.softId,
        rules: JSON.stringify(parsed)
      }).then(resJson => {
        this.rulesLoading = false
        if (resJson.success) {
          this.$message.success(resJson.msg)
          this.rulesVisible = false
          this.loadList()
        } else {
          this.$message.error(resJson.msg)
        }
      }).catch(() => {
        this.rulesLoading = false
      })
    },
    shortKey (k) {
      if (!k) {
        return ''
      }
      return k.length <= 20 ? k : k.slice(0, 12) + '...' + k.slice(-6)
    },
    copyText (text) {
      if (!text) {
        return
      }
      const ta = document.createElement('textarea')
      ta.value = text
      ta.style.position = 'fixed'
      ta.style.opacity = '0'
      document.body.appendChild(ta)
      ta.select()
      try {
        document.execCommand('copy')
        this.$message.success('公钥已复制')
      } catch (e) {
        this.$message.error('复制失败，请手动选中复制')
      }
      document.body.removeChild(ta)
    },
    getFormatDate (nS) {
      return this.TimeHelper ? this.TimeHelper.getFormatDate(nS) : nS
    }
  }
}
</script>
