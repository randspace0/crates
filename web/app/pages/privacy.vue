<template><div v-html="html" /></template>

<script setup lang="ts">
import { marked } from 'marked'
import md from '../privacy.md?raw'
const html = marked.parse(md) as string
useHead({ title: 'Privacy policy · Crates' })
</script>
