// ie polyfill
import 'core-js/stable'
import 'regenerator-runtime/runtime'

import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store/'
// import { VueAxios } from './utils/request'
import AxiosPlugin from '@/utils/plugin/axios-plugin'

// mock
// import './mock'

import bootstrap from './core/bootstrap'
import './core/use'
import './permission' // permission control
import './utils/filter' // global filter
import operatorPlugin from './utils/plugin/operator-plugin'
import TimeHelper from '@/utils/helper/TimeHelper'

import moment from 'moment'
import axios from 'axios'
Vue.prototype.$moment = moment
moment.prototype.toJSON = function () { return moment(this).format('YYYY-MM-DD HH:mm:ss') }

Vue.config.productionTip = false

// mount axios Vue.$http and this.$http
// Vue.use(VueAxios)
Vue.use(AxiosPlugin)
Vue.use(operatorPlugin)
Vue.prototype.TimeHelper = TimeHelper

Vue.directive('title', {
  inserted: function (el, binding) {
    document.title = el.dataset.title
  }
})

function getServerConfig () {
  return new Promise((resolve, reject) => {
    axios.get('./serverConfig.json').then(data => {
      localStorage.setItem('serverConfig', JSON.stringify(data.data))// �����ݹҵ�����ʹ��
      resolve()
    }).catch(error => {
      console.log(error)
      reject(error)
    })
  })
}

async function init () {
  await getServerConfig()
}

new Vue({
  router,
  store,
  // created: bootstrap,
  render: h => h(App),
  created () {
    init()
    bootstrap()
  }
}).$mount('#app')
