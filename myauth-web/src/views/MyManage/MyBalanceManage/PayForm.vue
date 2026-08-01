<template>
  <a-modal
    :title="this.title"
    width="40%"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="getEpayOrder"
    ok-text="刷新"
    @cancel="()=>{this.visible=false}"
    :afterClose="afterClose"
  >
    <a-spin :spinning="confirmLoading">
      <a-form-model
        ref="form"
        :model="entity"
        v-bind="layout"
      >
        <a-form-model-item label="支付类型">
          <a-input
            :disabled="true"
            v-model="entity.type"
          />
        </a-form-model-item>
        <a-form-model-item label="商户订单号">
          <a-input
            :disabled="true"
            v-model="entity.outTradeNo"
          />
        </a-form-model-item>
        <a-form-model-item label="支付平台订单号">
          <a-input
            :disabled="true"
            v-model="entity.tradeNo"
          />
        </a-form-model-item>
        <a-form-model-item label="创建订单时间">
          <a-input
            :disabled="true"
            v-model="entity.addtime"
          />
        </a-form-model-item>
        <a-form-model-item label="完成订单时间">
          <a-input
            :disabled="true"
            v-model="entity.endtime"
          />
        </a-form-model-item>
        <a-form-model-item label="商品名">
          <a-input
            :disabled="true"
            v-model="entity.name"
          />
        </a-form-model-item>
        <a-form-model-item label="商品金额">
          <a-input
            :disabled="true"
            v-model="entity.money"
          />
        </a-form-model-item>
        <a-form-model-item label="支付状态">
          <a-select
            v-model="entity.status"
            :disabled="true"
          >
            <a-select-option :key="1">支付成功</a-select-option>
            <a-select-option :key="0">未支付</a-select-option>
          </a-select>
        </a-form-model-item>
      </a-form-model>
    </a-spin>
  </a-modal>
</template>

<script>
export default {
  props: {
    afterSubmit: {
      type: Function,
      default: null
    },
    /** 行为，1新增，2更新 */
    behaviorType: {
      type: Number,
      default: 1
    }
  },
  data () {
    return {
      layout: {
        labelCol: { span: 5 },
        wrapperCol: { span: 18 }
      },
      visible: false,
      confirmLoading: false,
      entity: {},
      title: '订单信息',
      timer: 0
    }
  },
  mounted () {
  },
  methods: {
    init () {
      this.visible = true
      this.entity = {}
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
      // 创建一个定时器，刷新订单状态
      const refTime = 3000
      if (refTime > 0) {
        this.timer = setInterval(() => {
          console.info('自动刷新获取订单数据中，刷新周期(ms)：', refTime)
          this.getEpayOrder()
        }, refTime)
      }
    },
    openForm (entity) {
      this.init()
      this.entity = entity
    },
    afterClose () {
      clearInterval(this.timer)
      this.afterSubmit()
    },
    handleSubmit () {
    },
    getEpayOrder () {
      this.confirmLoading = true
      if (this.entity.outTradeNo) {
        this.$http.post('/myauth/web/getEpayOrder', { outTradeNo: this.entity.outTradeNo }).then(resJson => {
          this.confirmLoading = false
          if (resJson.success) {
            this.entity = resJson.result
            if (resJson.result && resJson.result.status == 1) {
              clearInterval(this.timer)
            }
          } else {
            this.$message.error(resJson.msg)
          }
        })
      }
    }
  }
}
</script>
