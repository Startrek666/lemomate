<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<style>
#app {
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  height: 100%;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
}
</style>
