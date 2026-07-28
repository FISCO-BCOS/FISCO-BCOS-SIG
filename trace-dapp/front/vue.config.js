const vueConfig = {
    devServer: {
        port: 8022,
        historyApiFallback: true,
        proxy: {
            '/api': {
                target: 'http://localhost:8082',
                changeOrigin: true
            }
        }
    }
}
module.exports = vueConfig
