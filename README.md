# Signaa-gent

A rewrite of a subset of Reagent's features, using Signaali for the reactivity.

Treat it as a toy project unless it works for you.

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
- Compatible with React 19 (the latest).
- Compatible with Re-frame 1.4.3 (the latest) via the `fi-metosin/re-signaali` fork.

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

If you tried his library, please let us know how it went.
