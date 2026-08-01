<template>
  <a-modal
    :title="this.title"
    width="40%"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="()=>{this.visible=false;}"
    @cancel="()=>{this.visible=false;}"
  >
    <a-spin :spinning="confirmLoading">
      <a-form-model
        ref="form"
        :model="entity"
        :rules="rules"
        v-bind="layout"
      >
        <a-form-model-item
          label="函数名称"
          prop="jsFun"
        >
          <a-input
            v-model="entity.jsFun"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="函数内容"
          prop="jsContent"
        >
          <a-textarea
            :rows="4"
            v-model="entity.jsContent"
            autocomplete="off"
          />
        </a-form-model-item>
        <a-form-model-item
          label="执行参数"
          prop="parameters"
        >
          <!-- <a-textarea
            :rows="4"
            v-model="entity.parametersJson"
            autocomplete="off"
          /> -->
          <a-input
            placeholder="c1"
            style="width:100px;"
            v-model="data.c1"
          />
          <a-input
            placeholder="c2"
            style="width:100px;"
            v-model="data.c2"
          />
          <a-input
            placeholder="c3"
            style="width:100px;"
            v-model="data.c3"
          />
          <a-input
            placeholder="c4"
            style="width:100px;"
            v-model="data.c4"
          />
          <a-input
            placeholder="5"
            style="width:100px;"
            v-model="data.c5"
          />
          <a-input
            placeholder="c6"
            style="width:100px;"
            v-model="data.c6"
          />
          <a-input
            placeholder="c7"
            style="width:100px;"
            v-model="data.c7"
          />
          <a-input
            placeholder="c8"
            style="width:100px;"
            v-model="data.c8"
          />
          <a-input
            placeholder="c9"
            style="width:100px;"
            v-model="data.c9"
          />
          <a-input
            placeholder="c10"
            style="width:100px;"
            v-model="data.c10"
          />
        </a-form-model-item>

        <a-form-model-item label="执行结果">
          <a-textarea
            :rows="4"
            v-model="jsRes"
            autocomplete="off"
          />
          <a-button
            @click="runJsBackTest"
            type="primary"
          >执行</a-button>
        </a-form-model-item>

      </a-form-model>
      <!-- <a-button @click="runJsTest">执行测试</a-button> -->
    </a-spin>
  </a-modal>
</template>

<script>

export default {
  data () {
    return {
      title: '函数测试',
      layout: {
        labelCol: { span: 5 },
        wrapperCol: { span: 18 }
      },
      visible: false,
      confirmLoading: false,
      entity: {},
      rules: {
        // fromSoftId: [{ required: true, message: '必填' }],
      },
      data: {},
      jsRes: ''
    }
  },
  mounted () {
  },
  methods: {
    runJsTest () {

    },
    openForm (id, title) {
      this.visible = true
      this.entity = {}
      this.data = {}
      this.jsRes = ''
      if (id) {
        this.$http.post('/myauth/web/getJs', { id }).then(resJson => {
          if (resJson.success) {
            this.entity = resJson.result
          } else {
            this.$message.error(resJson.msg)
            console.error(resJson)
          }
        })
      }
      if (title) {
        this.title = title
      }
    },
    runJsBackTest () {
      if (this.entity.id) {
        this.$http.post('/myauth/web/runJs', { id: this.entity.id, data: { ...this.data } }).then(resJson => {
          if (resJson.success) {
            this.jsRes = resJson.result.ret
          } else {
            this.$message.error(resJson.msg)
            console.error(resJson)
          }
        })
      } else {
        // 调用后端方法传递funcstr和params进行解析
        this.$http.post('/myauth/web/testJs', { jsFun: this.entity.jsFun, jsContent: this.entity.jsContent, data: { ...this.data } }).then(resJson => {
          if (resJson.success) {
            this.jsRes = resJson.result.ret
          } else {
            this.$message.error(resJson.msg)
            console.error(resJson)
          }
        })
      }
    }
  }
}
</script>

<style>
</style>
