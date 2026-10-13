# Privacy policy

_Last updated: 13 October 2026_

Crates is an unofficial, open-source Android client for [crates.io](https://crates.io). This policy explains what the app does with your data.

## What the app sends, and where

The app has no servers of its own and includes no analytics, advertising or crash-reporting SDKs. It only talks to these services, and only when you use the matching feature:

| Service | When | What |
| --- | --- | --- |
| crates.io (`crates.io`, `static.crates.io`) | Browsing, searching, opening crates, signing in, loading your crates | Your requests, plus your crates.io session cookie once you are signed in |
| GitHub (`github.com`) | Signing in | You log in on GitHub's own page, shown inside the app. The app does not read or store your GitHub password. |
| GitHub avatars (`avatars.githubusercontent.com`) | Showing owner and profile pictures | Image requests |
| Image hosts a README links to (for example GitHub or shields.io badges) | Opening a crate's README | Image requests, chosen by the crate's author |
| Sites linked from a crate (homepage, docs.rs, repository) | Only when you tap a link | They open in your browser |

These services handle your data under their own policies: [crates.io](https://foundation.rust-lang.org/policies/privacy-policy/) and [GitHub](https://docs.github.com/site-policy/privacy-policies/github-general-privacy-statement).

## What the app stores on your device

- The crates.io session cookie, so you stay signed in
- Cookies GitHub sets on its login page while you sign in (kept by the in-app browser until you sign out)
- Your crates.io user ID, login name, display name and avatar URL, to show your profile and crates
- Your theme choice
- A cache of recently loaded crates.io responses, so pages still show when you are offline

Only two of these are ever sent anywhere: the session cookie goes to crates.io to authenticate you, and your user ID goes to crates.io to list your crates. Signing out deletes the cookies and your profile. Uninstalling the app or clearing its data deletes everything. The app's data is excluded from Android backups and device transfers, so your session never moves to another device.

## Children

The app is a developer tool and is not directed at children.

## Contact

Questions or requests: open an issue at https://github.com/randspace0/crates/issues.
