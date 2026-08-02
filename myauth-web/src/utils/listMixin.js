/**
 * 列表页通用 mixin
 * 提供：
 * 1. 防抖自动搜索 - 输入框内容变化后自动触发查询（无需手动点"查询"按钮）
 * 2. 排序默认值管理
 *
 * 使用方式：
 *   import { listMixin } from '@/utils/listMixin'
 *   export default { mixins: [listMixin] }
 *
 * 模板中在 a-input 上使用 @change="debounceSearch" 或 @pressEnter="getDataList"
 * 在 a-select 上使用 @change="debounceSearch"
 */

// 防抖延迟时间（毫秒）
const DEBOUNCE_DELAY = 500

export const listMixin = {
  data () {
    return {
      _debounceTimer: null
    }
  },
  methods: {
    /**
     * 防抖搜索 - 绑定到输入框的 @change 或 @input 事件
     * 用户停止输入 500ms 后自动触发 getDataList
     */
    debounceSearch () {
      if (this._debounceTimer) {
        clearTimeout(this._debounceTimer)
      }
      this._debounceTimer = setTimeout(() => {
        // 重置到第一页再搜索
        if (this.pagination) {
          this.pagination.current = 1
        }
        this.getDataList()
      }, DEBOUNCE_DELAY)
    },

    /**
     * 立即搜索 - 绑定到下拉框的 @change 事件（选择后立即查询）
     */
    immediateSearch () {
      if (this.pagination) {
        this.pagination.current = 1
      }
      this.getDataList()
    },

    /**
     * 重置搜索条件并刷新
     */
    resetSearch () {
      this.queryParam = {}
      if (this.pagination) {
        this.pagination.current = 1
      }
      this.$nextTick(() => {
        this.getDataList()
      })
    }
  },
  beforeDestroy () {
    if (this._debounceTimer) {
      clearTimeout(this._debounceTimer)
    }
  }
}

export default listMixin
