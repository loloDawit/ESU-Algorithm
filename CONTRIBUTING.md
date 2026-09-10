# Contributing

Thanks for looking. This is a small project with an unusual shape — two
implementations of the same algorithm — so a few things are worth knowing
before you change anything.

## Getting it running

The desktop app needs **JDK 21** and nothing else:

```bash
./gradlew run     # the app
./gradlew test    # 43 tests
```

The browser version needs **Node 22**:

```bash
cd web
npm install
npm test          # 72 tests
npm run build     # -> docs/demo/, which the site loads
```

To see the site as it is published, build the demo and serve `docs/`:

```bash
cd docs && python3 -m http.server 8765
```

## The one rule that matters

**There are two implementations, and they must agree.**

`src/main/java/` is the reference, in Java. `web/src/` is a TypeScript port
that runs on the project page. They are held together by
`web/test/java-golden.json`, generated from the Java engine, which records for
each sample graph and size *which node every single step builds*, how many
subgraphs are found, and exactly which ones.

If you change how the algorithm behaves, you must change both, and regenerate
those fixtures. If you change only one, `npm test` fails and tells you.

The per-step trace is deliberate. Totals alone do not pin the traversal:
reversing the order candidates are taken in gives the same number of steps and
the same answers by a different route, and an earlier version of that test
passed with exactly that mutation in place. The visualizer shows the search one
step at a time, so the route is behaviour, not an implementation detail.

## Tests

Correctness rests on a **brute-force oracle**, in both languages: enumerate
every subset of the requested size, keep the connected ones, and require ESU to
return exactly that set with no duplicates. If you touch the algorithm, that is
the test that decides whether you were right.

Two other properties are pinned because they can fail silently:

- a step reached by replay must be identical to one reached by stepping
- the tree layout must centre parents over children without overlaps

When you add a test, check it can fail. Break the thing it covers on purpose
and watch it go red. A test that passes the moment you write it has told you
nothing — that is how the fixture problem above went unnoticed.

## Testing what is drawn

Both front ends are covered, in the only ways each can be.

The JavaFX views are built for real in `src/test/java/esu/algorithm/ui/`, on
the JavaFX thread and with the stylesheets applied, but without showing a
window. Those tests check what gets built and how it is styled: that every
control exists, that a tree box carries exactly one state, that every word can
be read against what is behind it. `FxTest` has the harness.

The demo is checked twice. `web/test/` runs it against a simulated DOM, which
is fast but has no layout engine and no stylesheets. `web/browser/` runs the
published page in real Chromium, which is the only thing here that can see
size, position and colour.

What none of this can tell you is whether the result looks *good*. A screenshot
still answers that, and it is worth attaching one to a pull request that
changes anything visual.

Every visual bug this project has shipped was a code path whose only consumer
was a picture, so when you add one, break it on purpose and watch the test go
red before you trust it.

## Style

Follow what is already in the file you are editing. Javadoc and TSDoc on public
things, explaining *why* rather than restating the signature. Commit messages
in the imperative, saying what changes and what it was before.

## Scope

This is a teaching tool. Its job is making one algorithm legible, not becoming
a general graph library. Changes that make the search easier to follow are
easier to argue for than changes that add controls.

## Releasing

Go to **Actions → Release → Run workflow** and pick which part of the version to
increase. `patch` is the default.

| Choice | v2.4.1 becomes |
|---|---|
| `patch` | v2.4.2 |
| `minor` | v2.5.0 |
| `major` | v3.0.0 |

The workflow reads the last tag, works out the next version, runs both test
suites, tags the commit and publishes. It refuses to run from anywhere but
`main`, and refuses to reuse a version that already exists.

The notes are generated from the commits and pull requests since the previous
tag, so they describe what actually changed rather than what someone remembered
to write down. There is no need to tag anything by hand.
