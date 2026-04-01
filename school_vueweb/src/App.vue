<script setup lang="ts">
</script>

<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #333;
}

/* 确保所有文本元素都有清晰的颜色 */
body,
div,
span,
p,
h1,
h2,
h3,
h4,
h5,
h6,
table,
th,
td,
input,
button,
select,
textarea,
label,
a,
ul,
li {
  color: #333;
}

/* 白色背景的元素确保字体不是白色 */
[style*="background-color: white"],
[style*="background-color:#fff"],
[style*="background-color: #fff"] {
  color: #333 !important;
}

#app {
  width: 100%; 
  height: 100%; 
  position: relative;
}
</style>
