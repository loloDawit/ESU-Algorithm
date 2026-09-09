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

`ESU Algorithm/src/` is the reference, in Java. `web/src/` is a TypeScript port
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

## What has no automated coverage

The JavaFX views. Nothing checks them, and a button once went missing in a
rebuild and stayed missing until someone read the README. If you change the
desktop UI, run it and attach a screenshot. The browser version does have view
tests, in `web/test/render.test.ts`.

## Style

Follow what is already in the file you are editing. Javadoc and TSDoc on public
things, explaining *why* rather than restating the signature. Commit messages
in the imperative, saying what changes and what it was before.

## Scope

This is a teaching tool. Its job is making one algorithm legible, not becoming
a general graph library. Changes that make the search easier to follow are
easier to argue for than changes that add controls.

## Releasing

Tag a commit on `main` and push the tag. Everything else is automatic:

```bash
git tag -a v2.1.0 -m "v2.1.0"
git push origin v2.1.0
```

The release workflow runs both test suites, and publishes only if they pass.
The notes are generated from the commits and pull requests since the previous
tag, so they describe what actually changed rather than what someone remembered
to write down.
