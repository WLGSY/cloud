<template>
  <router-view v-slot="{ Component, route }">
    <transition :name="'page'" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup>
// 根组件 — 页面过渡动画
</script>

<style>
/* 全局基础样式已迁移至 src/styles/theme.css */
</style>
