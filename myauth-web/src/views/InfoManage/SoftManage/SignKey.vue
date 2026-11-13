<template>
  <a-modal
    :title="title"
    width="76%"
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
      </template>
      <template slot="description">
        <div style="line-height: 1.9">
          <b>怎么用（三步）：</b>
          <br>
          1. 点「生成新密钥」，系统自动生成一对密钥，私钥留在服务器，无需你关心。
          <br>
          2. 点公钥右侧的「复制」。
          <br>
          3. 粘贴到客户端 <code>internal/myauth/hardcoded.go</code> 的
          <code>FeatureSignPublicKey</code>（同时把 <code>FeatureSignKid</code> 换成这里显示的 kid），重新编译客户端。
          <br>
          <span style="color: #fa8c16">
            只需要做一次。之后调整功能开关只改下表的「解锁点数」，客户端不用重新编译。
          </span>
        </div>
      </template>
    </a-alert>

    <a-alert
      v-if="!loading && keys.length === 0"
      type="warning"
      show-icon
      style="margin-bottom: 12px"
    >
      <template slot="message">该软件还没有签名密钥</template>
      <template slot="description">
        没有密钥时服务端无法签发功能票据，客户端会退回到旧的点数判断，功能锁实际上不生效。
        点下面的「生成新密钥」即可，或者等客户端首次申请票据时系统自动补发。
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
        slot="featureCount"
        slot-scope="text, record"
      >
        <a-tag :color="countFeatures(record) > 0 ? '#87d068' : '#f50'">
          {{ countFeatures(record) }} 项
        </a-tag>
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
      width="80%"
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
          <div style="line-height: 1.9">
            功能项由本软件自己声明，不同软件的功能清单互不影响。
            <b>「功能标识」必须与客户端代码里的字符串完全一致</b>（例如客户端调用
            <code>RequireFeatureActive("webshop")</code>，这里就填 <code>webshop</code>），
            填错等于没有上锁——客户端拿不到该标识会按「未知功能」放行。
          </div>
        </template>
      </a-alert>

      <a-form :labelCol="{ span: 5 }" :wrapperCol="{ span: 18 }">
        <a-form-item label="默认可见">
          <a-switch v-model="form.defaultVisible" @change="refreshRawText" />
          <span style="margin-left: 8px; color: #999">
            未在下表单独配置的功能默认是否可见（一般保持开启）
          </span>
        </a-form-item>

        <a-form-item label="感谢语">
          <a-input v-model="form.supportText" placeholder="未赞助用户点开锁时看到的提示文案" @change="refreshRawText" />
        </a-form-item>
      </a-form>

      <div style="margin-bottom: 8px">
        <a-button type="dashed" icon="plus" @click="addRow">添加功能项</a-button>
        <span style="margin-left: 10px; color: #999; font-size: 12px">
          解锁点数：0 = 所有人可用，正数 = 需达到该赞助点数，-1 = 永久关闭
        </span>
      </div>

      <a-table
        :columns="ruleColumns"
        :rowKey="row => row._uid"
        :dataSource="form.rows"
        :pagination="false"
        :bordered="true"
        size="small"
        :locale="{ emptyText: '还没有配置任何功能项，点上面的「添加功能项」开始' }"
      >
        <span slot="featureKey" slot-scope="text, record">
          <a-input
            v-model="record.key"
            placeholder="如 webshop"
            style="font-family: monospace"
            @change="refreshRawText"
          />
        </span>
        <span slot="label" slot-scope="text, record">
          <a-input v-model="record.label" placeholder="如 交易市场（可留空）" @change="refreshRawText" />
        </span>
        <span slot="minPoint" slot-scope="text, record">
          <a-input-number
            v-model="record.minPoint"
            :min="-1"
            :precision="0"
            style="width: 100%"
            @change="refreshRawText"
          />
        </span>
        <span slot="allowedUsers" slot-scope="text, record">
          <a-input v-model="record.allowedUsers" placeholder="留空表示无，多个用逗号分隔" @change="refreshRawText" />
        </span>
        <span slot="deniedUsers" slot-scope="text, record">
          <a-input v-model="record.deniedUsers" placeholder="留空表示无，多个用逗号分隔" @change="refreshRawText" />
        </span>
        <span slot="action" slot-scope="text, record">
          <a-popconfirm title="确认删除这个功能项？" @confirm="removeRow(record)">
            <a style="color: #f5222d">删除</a>
          </a-popconfirm>
        </span>
      </a-table>

      <div style="margin-top: 12px; color: #999; font-size: 12px">
        点数为 -1 的功能项会同时写入「全局禁用模块」，对所有账号生效。
      </div>

      <a-collapse style="margin-top: 12px">
        <a-collapse-panel key="raw" header="高级：直接编辑 JSON 原文">
          <a-textarea
            v-model="rulesText"
            :rows="14"
            style="font-family: monospace"
            @change="onRawChange"
          />
          <a-button size="small" style="margin-top: 8px" @click="syncFormFromText(false)">按原文刷新上方表单</a-button>
        </a-collapse-panel>
      </a-collapse>
    </a-modal>
  </a-modal>
</template>

<script>
const columns = [
  { title: '密钥ID (kid)', align: 'center', dataIndex: 'kid', width: '18%', scopedSlots: { customRender: 'kid' } },
  { title: '公钥（填入客户端 hardcoded.go）', align: 'center', dataIndex: 'publicKey', width: '34%', scopedSlots: { customRender: 'publicKey' } },
  { title: '功能项', align: 'center', dataIndex: 'featureCount', width: '9%', scopedSlots: { customRender: 'featureCount' } },
  { title: '状态', align: 'center', dataIndex: 'status', width: '9%', scopedSlots: { customRender: 'status' } },
  { title: '生成时间', align: 'center', dataIndex: 'addTime', width: '18%', scopedSlots: { customRender: 'addTime' } },
  { title: '操作', align: 'center', dataIndex: 'action', width: '12%', scopedSlots: { customRender: 'action' } }
]

const ruleColumns = [
  { title: '功能标识（对应客户端代码）', align: 'left', dataIndex: 'key', width: '20%', scopedSlots: { customRender: 'featureKey' } },
  { title: '显示名', align: 'left', dataIndex: 'label', width: '17%', scopedSlots: { customRender: 'label' } },
  { title: '解锁点数', align: 'center', dataIndex: 'minPoint', width: '11%', scopedSlots: { customRender: 'minPoint' } },
  { title: '白名单账号', align: 'center', dataIndex: 'allowedUsers', width: '18%', scopedSlots: { customRender: 'allowedUsers' } },
  { title: '黑名单账号', align: 'center', dataIndex: 'deniedUsers', width: '18%', scopedSlots: { customRender: 'deniedUsers' } },
  { title: '操作', align: 'center', dataIndex: 'action', width: '8%', scopedSlots: { customRender: 'action' } }
]

// 功能标识会拼进服务端签名的 canonical 字符串，含分隔符会破坏解析，前后端用同一套校验
const FEATURE_KEY_RE = /^[a-zA-Z0-9_-]{1,32}$/

let rowUidSeed = 0

function buildDefaultRules () {
  return {
    supportText: '感谢你的支持，这是我们持续维护的动力',
    defaultVisible: true,
    disabledModules: [],
    features: {}
  }
}

function splitList (s) {
  if (!s) {
    return []
  }
  return String(s)
    .split(/[,，\s]+/)
    .map(x => x.trim())
    .filter(x => x.length > 0)
}

export default {
  data () {
    return {
      visible: false,
      loading: false,
      softId: null,
      title: '功能票据密钥',
      keys: [],
      columns,
      ruleColumns,
      rulesVisible: false,
      rulesLoading: false,
      rulesText: '',
      editingKey: null,
      form: {
        defaultVisible: true,
        supportText: '',
        rows: []
      },
      formDirty: false
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
    countFeatures (record) {
      return this.parseFeatureCount(record && record.featureRules)
    },
    parseFeatureCount (rulesText) {
      if (!rulesText) {
        return 0
      }
      try {
        const root = JSON.parse(rulesText)
        return root && root.features ? Object.keys(root.features).length : 0
      } catch (e) {
        return 0
      }
    },
    handleGenerate () {
      const that = this
      this.$confirm({
        title: '确认生成新的密钥对？',
        content: '旧密钥会置为停用。旧客户端必须重新填入新公钥并重新编译，否则会验签失败、受控功能被锁。功能规则会沿用上一把密钥的配置。',
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
      this.rulesText = record.featureRules || JSON.stringify(buildDefaultRules(), null, 2)
      this.syncFormFromText(true)
      this.rulesVisible = true
    },
    // 把 JSON 原文解析进可视化表单；silent=true 时不弹错误提示
    syncFormFromText (silent) {
      let root
      try {
        root = JSON.parse(this.rulesText || '{}')
      } catch (e) {
        if (!silent) {
          this.$message.error('规则不是合法 JSON：' + e.message)
        }
        return false
      }
      if (!root || typeof root !== 'object') {
        root = buildDefaultRules()
      }
      const features = root.features && typeof root.features === 'object' ? root.features : {}
      // 以规则里实际声明的功能项为准，不做任何平台内置补齐
      const rows = Object.keys(features).map(k => {
        const rule = features[k] || {}
        return {
          _uid: ++rowUidSeed,
          key: k,
          label: rule.label || '',
          minPoint: typeof rule.minPoint === 'number' ? rule.minPoint : 0,
          allowedUsers: (rule.allowedUsers || []).join(','),
          deniedUsers: (rule.deniedUsers || []).join(',')
        }
      })
      this.form = {
        defaultVisible: root.defaultVisible !== false,
        supportText: root.supportText || '',
        rows
      }
      this.formDirty = false
      return true
    },
    addRow () {
      this.form.rows.push({
        _uid: ++rowUidSeed,
        key: '',
        label: '',
        minPoint: 0,
        allowedUsers: '',
        deniedUsers: ''
      })
      this.refreshRawText()
    },
    removeRow (record) {
      const idx = this.form.rows.indexOf(record)
      if (idx >= 0) {
        this.form.rows.splice(idx, 1)
      }
      this.refreshRawText()
    },
    // 表单 -> JSON 原文
    composeRulesFromForm () {
      const features = {}
      const disabledModules = []
      this.form.rows.forEach(row => {
        const key = (row.key || '').trim()
        if (!key) {
          return
        }
        const minPoint = typeof row.minPoint === 'number' ? row.minPoint : 0
        const rule = {
          minPoint,
          allowedUsers: splitList(row.allowedUsers),
          deniedUsers: splitList(row.deniedUsers)
        }
        if (row.label && row.label.trim()) {
          rule.label = row.label.trim()
        }
        features[key] = rule
        if (minPoint < 0) {
          disabledModules.push(key)
        }
      })
      return {
        supportText: this.form.supportText,
        defaultVisible: this.form.defaultVisible,
        disabledModules,
        features
      }
    },
    onRawChange () {
      this.formDirty = true
    },
    // 表单改动时回写 JSON 原文，让高级区始终反映当前表单
    refreshRawText () {
      this.rulesText = JSON.stringify(this.composeRulesFromForm(), null, 2)
      this.formDirty = false
    },
    // 提交前本地校验，把明显错误挡在请求之前
    validateForm () {
      const seen = {}
      for (let i = 0; i < this.form.rows.length; i++) {
        const row = this.form.rows[i]
        const key = (row.key || '').trim()
        if (!key) {
          this.$message.error(`第 ${i + 1} 行的功能标识不能为空`)
          return false
        }
        if (!FEATURE_KEY_RE.test(key)) {
          this.$message.error(`功能标识「${key}」不合法，只允许字母、数字、下划线、中划线，长度 1-32`)
          return false
        }
        if (seen[key]) {
          this.$message.error(`功能标识「${key}」重复了`)
          return false
        }
        seen[key] = true
      }
      return true
    },
    submitRules () {
      // 表单改动会实时回写到 rulesText，高级区直接改原文时 rulesText 就是提交内容
      let parsed
      try {
        parsed = JSON.parse(this.rulesText)
      } catch (e) {
        this.$message.error('规则不是合法 JSON：' + e.message)
        return
      }
      if (!this.validateForm()) {
        return
      }
      if (!parsed.features || Object.keys(parsed.features).length === 0) {
        this.$message.warning('还没有配置任何功能项，保存后客户端将无法取得功能票据')
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
