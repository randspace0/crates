<template>
  <main>
    <NuxtPage />
    <footer>
      <NuxtLink to="/">Home</NuxtLink> ·
      <NuxtLink to="/privacy">Privacy</NuxtLink> ·
      <a href="https://github.com/randspace0/crates">Source</a>
      <p>Not affiliated with or endorsed by crates.io or the Rust Foundation.</p>
    </footer>
  </main>
</template>

<style>
:root { color-scheme: light dark; --fg: #1c1b1f; --bg: #fffbfe; --muted: #6b6870; --accent: #b7410e; }
@media (prefers-color-scheme: dark) { :root { --fg: #e6e1e5; --bg: #141218; --muted: #a09ca5; --accent: #ff9a62; } }
body { margin: 0; background: var(--bg); color: var(--fg); font: 16px/1.6 system-ui, sans-serif; }
main { max-width: 760px; margin: 0 auto; padding: 24px 16px; }
a { color: var(--accent); }
table { border-collapse: collapse; display: block; overflow-x: auto; }
th, td { border: 1px solid var(--muted); padding: 6px 10px; text-align: left; }
footer { margin-top: 48px; color: var(--muted); font-size: 14px; }
</style>
