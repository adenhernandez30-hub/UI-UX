# AniLab — Dantotsu Foundation/Home/Detail Migration

This branch is intentionally limited to the first migration phase.

## Scope

1. Foundation: navigation, gesture behavior, transitions, animation patterns, theme primitives, image/card infrastructure, and other UI foundation pieces needed by Home and Anime Detail.
2. Home: reproduce the Dantotsu-style interaction and visual hierarchy as the initial AniLab baseline.
3. Anime Detail: reproduce the Dantotsu-style detail interaction and visual hierarchy as the initial AniLab baseline.

## Explicitly out of scope for this phase

- Premium / Diamond / Supporter features
- Social features
- Calendar / Library / Profile implementation
- Provider replacement
- Player replacement
- Manga / novel features
- Dantotsu account, Discord, GitHub, forum, extension, torrent, and download features

The player will be audited separately after the UI foundation is stable so the existing AniLab provider/player work is not accidentally replaced.

## Migration rule

Use Dantotsu as the source foundation for the agreed areas, but do not blindly copy unrelated application modules. Preserve the Dantotsu license and attribution requirements for any source that is actually redistributed.

## Completion rule

A phase is only marked complete after the code is in the repository and the relevant GitHub Actions build/check has passed. A copied source tree alone is not a green milestone.
