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
      width="72%"
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
          规则由服务端计算后签名下发，客户端不再自行判断。下面每个功能的「解锁点数」填 0 表示所有人可用；
          填正数表示该账号赞助点数达到这个数才解锁；填 -1 表示永久关闭。白名单/黑名单按账号填写，用逗号分隔。
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

      <a-table
        :columns="ruleColumns"
        :rowKey="row => row.key"
        :dataSource="form.rows"
        :pagination="false"
        :bordered="true"
        size="small"
      >
        <span slot="label" slot-scope="text, record">
          {{ record.label }}
          <a-tooltip :title="record.key">
            <a-icon type="info-circle" style="color: #999; margin-left: 4px" />
          </a-tooltip>
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
      </a-table>

      <div style="margin-top: 12px; color: #999; font-size: 12px">
        点数为负数的功能等价于加入「全局禁用模块」，对所有账号生效。
      </div>

      <a-collapse style="margin-top: 12px">
        <a-collapse-panel key="raw" header="高级：直接编辑 JSON 原文">
          <a-textarea
            v-model="rulesText"
            :rows="14"
            style="font-family: monospace"
            @change="onRawChange"
          />
          <a-button size="small" style="margin-top: 8px" @click="syncFormFromText">按原文刷新上方表单</a-button>
        </a-collapse-panel>
      </a-collapse>
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

// 与后端 FeatureSignUtil.KNOWN_FEATURES 保持一致的展示名映射。
// 后端不认识的 key 一律忽略，这里缺失的 key 会退化为显示原始 key。
const FEATURE_LABELS = {
  palschema: '帕鲁图鉴',
  'pal-monitor': '帕鲁监控',
  admin: '管理功能',
  boss_operations: 'BOSS 运营',
  points_system: '积分系统',
  webshop: '交易市场',
  cloud_pack: '云存档包'
}

const ruleColumns = [
  { title: '功能', align: 'left', dataIndex: 'label', scopedSlots: { customRender: 'label' } },
  { title: '解锁点数', align: 'center', dataIndex: 'minPoint', width: '15%', scopedSlots: { customRender: 'minPoint' } },
  { title: '白名单账号', align: 'center', dataIndex: 'allowedUsers', width: '25%', scopedSlots: { customRender: 'allowedUsers' } },
  { title: '黑名单账号', align: 'center', dataIndex: 'deniedUsers', width: '25%', scopedSlots: { customRender: 'deniedUsers' } }
]

// 默认规则：不填规则时用这个兜底，保证表格永远有行可编辑
function buildDefaultRules () {
  return {
    supportText: '感谢你的支持，这是我们持续维护的动力',
    defaultVisible: true,
    disabledModules: [],
    features: {}
  }
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
      const rows = []
      // 先排已知功能，再补上规则里出现的未知 key（避免丢配置）
      const keys = Object.keys(FEATURE_LABELS)
      Object.keys(features).forEach(k => {
        if (keys.indexOf(k) === -1) {
          keys.push(k)
        }
      })
      keys.forEach(k => {
        const rule = features[k] || {}
        rows.push({
          key: k,
          label: FEATURE_LABELS[k] || k,
          minPoint: typeof rule.minPoint === 'number' ? rule.minPoint : 0,
          allowedUsers: (rule.allowedUsers || []).join(','),
          deniedUsers: (rule.deniedUsers || []).join(',')
        })
      })
      this.form = {
        defaultVisible: root.defaultVisible !== false,
        supportText: root.supportText || '',
        rows
      }
      this.formDirty = false
      return true
    },
    // 表单改动后重新拼回 JSON 原文，提交时统一走 JSON
    composeRulesFromForm () {
      const features = {}
      const disabledModules = []
      this.form.rows.forEach(row => {
        if (!row.key) {
          return
        }
        const minPoint = typeof row.minPoint === 'number' ? row.minPoint : 0
        features[row.key] = {
          minPoint,
          allowedUsers: splitList(row.allowedUsers),
          deniedUsers: splitList(row.deniedUsers)
        }
        if (minPoint < 0) {
          disabledModules.push(row.key)
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
    // 表单改动时把结果回写进高级区的 JSON 原文，用户展开就能看到对应关系，
    // 同时把 formDirty 复位，提交时统一按表单拼装。
    refreshRawText () {
      if (!this.form.rows.length) {
        return
      }
      this.rulesText = JSON.stringify(this.composeRulesFromForm(), null, 2)
      this.formDirty = false
    },
    submitRules () {
      // 表单改动会经 refreshRawText 实时回写到 rulesText，
      // 高级区直接改原文时 rulesText 就是要提交的内容，两条路径最终都统一提交 rulesText。
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

function splitList (s) {
  if (!s) {
    return []
  }
  return String(s)
    .split(/[,，\s]+/)
    .map(x => x.trim())
    .filter(x => x.length > 0)
}
</script>
