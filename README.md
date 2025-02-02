# Signaa-gent

A rewrite of a subset of [Reagent](https://github.com/reagent-project/reagent)'s features,
using [Signaali](https://github.com/metosin/signaali) for the reactivity.

This project was created to help people who want to use [Re-signaali](https://github.com/metosin/re-signaali)
while still having some Reagent components in their codebase.
It might be suitable to you if you are in the process of migrating a large Reagent+Re-frame codebase
toward another React wrapper, like [UIx](https://github.com/pitch-io/uix) or
[Helix](https://github.com/lilactown/helix).

## Project status

[![Clojars Project](https://img.shields.io/clojars/v/fi.metosin/signaa-gent.svg)](https://clojars.org/fi.metosin/signaa-gent)
[![Slack](https://img.shields.io/badge/slack-signaali-orange.svg?logo=slack)](https://clojurians.slack.com/app_redirect?channel=signaali)
[![cljdoc badge](https://cljdoc.org/badge/fi.metosin/signaa-gent)](https://cljdoc.org/d/fi.metosin/signaa-gent)

Signaa-gent is currently [experimental](https://github.com/topics/metosin-experimental).

Treat it as a toy project *unless* it works for you 😄

## Install

In your deps.edn:

```clojure
{:fi.metosin/signaa-gent {:mvn/version "..."}
```

## Features:

- A drop-in replacement of Reagent covering the most commonly used parts of its API.
- Implementation easy to understand.
- Glitch-free reactivity provided by Signaali.
- No jumping caret issues.
- Can embed and be embedded into any existing React-based app (React, UIx, Helix, etc ...)
- Compatible with [React](https://react.dev/) 19 (the latest).
- Compatible with [Re-frame](https://github.com/day8/re-frame/) 1.4.3 (the latest)
  via the [Re-signaali](https://github.com/metosin/re-signaali) fork.

### Caveats:

- No effort was made to make it behave *exactly* like Reagent.
- Does not cover all the Reagent legacy features:
  - No support for Reagent class components, only functions.
  - No support for Cursor, Track, Wrap.
  - No createRoot, use another React wrapper like in the [test app](test/app/src/core.cljs).
- No effort was made on the performance side.

## Parts the Reagent API covered

Everything which is tested in the [test app](test/app/src/core.cljs) is supported.

See:
- The [hiccup section](test/app/src/hiccup.cljs)
- The [Reagent section](test/app/src/reagent.cljs)
- The [interop section](test/app/src/interop.cljs)

## Feedback

If you tried his library, please [let us know](https://clojurians.slack.com/app_redirect?channel=signaali)
how it went.
