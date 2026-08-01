<template>
  <div class="logo">
    <router-link :to="{ path: '/' }">
      <img src="/logo.png" />
      <h1 v-if="showTitle">{{ title }}</h1>
    </router-link>
  </div>
</template>

<script>

export default {
  name: 'Logo',
  components: {
  },
  props: {
    title: {
      type: String,
      default: process.env.VUE_APP_ProjectName,
      required: false
    },
    showTitle: {
      type: Boolean,
      default: true,
      required: false
    }
  }
}
</script>
