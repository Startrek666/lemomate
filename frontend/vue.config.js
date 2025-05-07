module.exports = {
  publicPath: process.env.NODE_ENV === 'production' ? '/lemomate/' : '/',
  outputDir: 'dist',
  assetsDir: 'static',
  productionSourceMap: false
}
