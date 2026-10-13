<template>
  <header class="hero">
    <img src="/icon.png" alt="Crates app icon" width="96" height="96" />
    <h1>Crates</h1>
    <p>Search, explore and read Rust crates from crates.io on your phone or tablet. Unofficial client.</p>
    <p><a class="btn" href="https://play.google.com/store/apps/details?id=io.github.randspace0.crates">Get it on Google Play</a></p>
  </header>
  <ul>
    <li>Explore new, popular and recently updated crates, keywords and categories</li>
    <li>Search with sorting; load more as you scroll</li>
    <li>Crate pages: README, versions, dependencies, owners, <code>cargo add</code> snippets</li>
    <li>Log in with GitHub to see your own crates</li>
    <li>Dark mode, tablet layout, offline cache. No ads, no analytics</li>
  </ul>
  <div class="shots">
    <img v-for="s in shots" :key="s" :src="`/${s}.png`" alt="Crates app screenshot" loading="lazy" />
  </div>
</template>

<script setup lang="ts">
const shots = ['1_explore', '2_search', '3_readme']
</script>

<style scoped>
.hero { text-align: center; }
.hero img { border-radius: 20px; }
.btn { display: inline-block; padding: 10px 20px; border-radius: 999px; background: var(--accent); color: var(--bg); text-decoration: none; font-weight: 600; }
.shots { display: flex; gap: 12px; overflow-x: auto; }
.shots img { height: 420px; width: auto; border-radius: 12px; }
</style>
