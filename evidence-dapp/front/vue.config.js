const vueConfig = {
  lintOnSave: false,
  devServer: {
    port: 8020,
    proxy: {
      '/api': {
        target: 'http://localhost:8080/',
        changeOrigin: true
      }
    }
  }
}
module.exports = vueConfig
