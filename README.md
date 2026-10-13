# Crates

[![CI](https://github.com/randspace0/crates/actions/workflows/ci.yml/badge.svg)](https://github.com/randspace0/crates/actions/workflows/ci.yml)

An unofficial [crates.io](https://crates.io) client for Android.

- Browse the crates.io front page: new, most downloaded, just updated, popular keywords and categories
- Search with sorting and infinite scroll
- Crate pages with README, metadata, install snippets, versions, dependencies and owners
- Log in with GitHub and see the crates you own
- Light and dark themes; navigation rail and side-by-side layout on tablets

## Build

Requires JDK 17+ and the Android SDK (platform 37).

```sh
./gradlew assembleDebug   # build
./gradlew lint test       # checks
```

## License

Licensed under [MIT License](LICENSE)
