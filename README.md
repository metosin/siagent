# Siagent

A rewrite of a subset of [Reagent](https://github.com/reagent-project/reagent)'s features,
using [Signaali](https://github.com/metosin/signaali) for the reactivity.

This project was created to help people who want to use [Re-signaali](https://github.com/metosin/re-signaali)
while they still have some Reagent components in their codebase.
It might be suitable to you if you are in the process of migrating a large Reagent+Re-frame codebase
toward another React wrapper, like [UIx](https://github.com/pitch-io/uix) or
[Helix](https://github.com/lilactown/helix), or toward other libraries based on Signaali.

## Project status

[![Clojars Project](https://img.shields.io/clojars/v/fi.metosin/siagent.svg)](https://clojars.org/fi.metosin/siagent)
[![Slack](https://img.shields.io/badge/slack-signaali-orange.svg?logo=slack)](https://clojurians.slack.com/app_redirect?channel=signaali)
[![cljdoc badge](https://cljdoc.org/badge/fi.metosin/siagent)](https://cljdoc.org/d/fi.metosin/siagent)

Siagent is currently [experimental](https://github.com/metosin/open-source/blob/main/project-status.md#experimental).

Treat it as a toy project *unless* it works for you 😄

## Install

In your deps.edn:

```clojure
{:fi.metosin/siagent {:mvn/version "..."}
```

## Features:

- A drop-in replacement of Reagent covering the most commonly used parts of its API.
- Implementation easy to understand.
- Glitch-free reactivity provided by Signaali.
- No jumping caret issues in text fields.
- Can embed React components and be embedded into any existing React-based app (React, UIx, Helix, etc ...)
- Compatible with [React](https://react.dev/) 19 (the latest).
- Compatible with [Re-frame](https://github.com/day8/re-frame/) 1.4.3 (the latest)
  via the [Re-signaali](https://github.com/metosin/re-signaali) fork.

### Caveats:

- No special effort was made to make it behave *exactly* like Reagent.
- Does not cover all the Reagent legacy features:
  - No support for Reagent class components, only functions.
  - No support for Cursor, Track, Wrap.
  - No createRoot, use another React wrapper like in the [test app](test/app/src/app/core.cljs).
- No effort was made on the performance side.

## Parts the Reagent API covered

Everything which is tested in the [test app](test/app/src/app/core.cljs) is supported.

See:
- The [hiccup section](test/app/src/app/hiccup.cljs)
- The [Reagent section](test/app/src/app/reagent.cljs)
- The [interop section](test/app/src/app/interop.cljs)

## Running the tests

```shell
cd test/playwright
npm test
```

## Feedback

If you tried his library, please [let us know](https://clojurians.slack.com/app_redirect?channel=signaali)
how it went. Your feedback is important.

## License

This project is distributed under the [Eclipse Public License v2.0](LICENSE).

Copyright (c) Vincent Cantin and contributors.
