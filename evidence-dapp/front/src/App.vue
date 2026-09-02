<template>
  <div id="app">
    <div v-if="showShell" class="page-layout">
      <Header />
      <div class="page-body">
        <navigator></navigator>
        <div class="page-content">
          <router-view />
        </div>
      </div>
    </div>
    <router-view v-else />
  </div>
</template>

<script>
import Navigator from '@/components/Navigator'
import Header from '@/components/Header'

export default {
  name: 'App',
  components: { Navigator, Header },
  computed: {
    showShell() {
      const noShell = ['/login', '/register']
      return !noShell.includes(this.$route.path)
    }
  }
}
</script>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden;
}
</style>
