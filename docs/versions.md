# Updating Versions of Java and/or node

## Updating the Java version

When updating the version of Java used, the following places need to be adjusted:

* `Versions` section of the README.md
* `pom.xml` file
* `.java-version` file (used by Github Actions scripts)
* `Dockerfile` used for deploying on Dokku (the `openjdk-NN-jdk` apt package and `JAVA_HOME`)

In addition, any Maven plugin that **reads compiled `.class` files** must support the
new Java class-file format, or it fails at run time even though the code compiles.
Check these versions in `pom.xml` and bump them if needed:

* `jacoco-maven-plugin` (test coverage)
* `pitest-maven` and `pitest-junit5-plugin` (mutation testing)
* `git-code-format-maven-plugin` and the `google-java-format` it bundles (formatting)

Also note that since JDK 23, `javac` does not run annotation processors such as Lombok
unless told to explicitly; `pom.xml` sets `<proc>full</proc>` on `maven-compiler-plugin`
for that reason. If a build ever fails with `cannot find symbol` for every Lombok-generated
getter, builder or `log` field, that setting is the first thing to check.

When the Spring Boot version changes, also check libraries tied to the Boot line,
such as `springdoc-openapi-starter-webmvc-ui`.

## Updating the node version

Places that name the node version (they must all agree):

* `Versions` section of the README.md
* `engines` section in `frontend/package.json` (also used by GitHub Actions: the shared workflows in
  `ucsb-cs156/workflows` and the Chromatic workflows call `actions/setup-node` with
  `node-version-file: frontend/package.json`, so no workflow edit is needed)
* `frontend/.nvmrc`
* `Dockerfile` used for deploying on Dokku (`ENV NODE_VERSION=`)
* `pom.xml`: the `app.frontend.nodeVersion` property, used by `frontend-maven-plugin`

`frontend/nvm-pj.sh` reads the version from `package.json`, so it needs no change.
Afterwards, `grep -rIn "<old version>" --exclude-dir=node_modules --exclude-dir=target .` should
find nothing but `package-lock.json`.

After changing the version, if a local `mvn` build fails in `npm ci` with
`Class extends value undefined is not a constructor or null`, delete `target/node` and
`target/node_modules` (stale npm from the previous node version left in the plugin's install dir).

### Updating frontend dependencies at the same time

Notes from the move to node 24.21.0
([issue #23](https://github.com/ucsb-cs156/proj-dining-caching-proxy/issues/23)). The same update
was done first in [proj-courses #355](https://github.com/ucsb-cs156/proj-courses/issues/355) and
[proj-dining #159](https://github.com/ucsb-cs156/proj-dining/issues/159); their notes cover
dependencies this repo does not have (react-query 3, recharts, `rollup-plugin-visualizer`,
`storybook-addon-remix-react-router`, `eslint-plugin-react`).

* `npm audit`, `npm outdated` and `npm ci 2>&1 | grep deprecated` show what needs attention.
  If `npm install` fails with a confusing `ERESOLVE` after editing versions, regenerate:
  `rm -rf node_modules package-lock.json && npm install`. Finish with `npm audit fix` (without
  `--force`) for transitive dependencies such as `qs` that no direct version bump reaches.
* npm 11 blocks dependency install scripts by default (`npm warn install-scripts ...`). After
  reviewing the listed packages, `npm install-scripts approve <pkg>` in `frontend/` and commit the
  resulting `allowScripts` block in `package.json` (here: `esbuild`, `msw`, `fsevents`). Note that
  `approve --all` only covers the packages npm reported on the *last* install; the macOS-only
  `fsevents` first shows up on a later `npm ci`, so re-check with `npm install-scripts ls`.
* Verify with all of: `npm run lint`, `npm run check-format`, `npm test`, `npm run build`,
  `npm run build-storybook`, a Stryker run (see below), and a `mvn -Pproduction` build. Unit tests
  alone do not catch Storybook or Stryker breakage.
* Breaking changes hit in this repo:
  * Storybook 10 + msw-storybook-addon 3: `initialize()` and the root `mswLoader` export are gone.
    `.storybook/preview.jsx` now imports `mswLoader` from `msw-storybook-addon/csf3` and *calls*
    it in `loaders`; `msw-storybook-addon` stays listed in `addons` in `.storybook/main.js`.
    Caught only by `npm run build-storybook`, not by unit tests.
  * vite 8 (Rolldown): no config change was needed because `vite.config.ts` is already ESM with
    no `__dirname` or `rollupOptions`. The `pom.xml` up-to-date check for the frontend build used
    to name `vite.config.js`, which does not exist; it now names `vite.config.ts`.
  * ESLint 10 works here (unlike proj-courses/proj-dining, this repo does not use
    `eslint-plugin-react`, which crashes on ESLint 10). `typescript-eslint`,
    `eslint-plugin-react-hooks` 7 and `eslint-plugin-react-refresh` all declare ESLint 10 support.
  * Nothing else needed code changes: already on react 19, react-router 7, `@tanstack/react-query` 5,
    vitest 4, jest-dom 6 (→ 7, no `extend-expect` imports), react-hooks 7 with
    `configs.flat.recommended`.
* Held back on purpose:
  * `vitest`/`@vitest/coverage-v8` stay on 4.x: with vitest 5 Stryker maps no tests to mutants, so
    every mutant survives (found in proj-courses by bisecting).
  * `typescript` stays on 6.x: `typescript-eslint` 8 requires `typescript < 6.1`.
  * `@types/node` stays on 24.x to match the Node major in use (26.x tracks Node 26).
  * `react-router` stays on 7.x and `@tanstack/react-table` on 8.x: each major is a separate
    migration with API changes, not a Node-compatibility matter.
* Stryker 10 adds a `CallExpression` mutator (deletes bare call statements). proj-courses and
  proj-dining excluded it to keep Stryker 9 strictness; here a sample run found no surviving
  `CallExpression` mutants, so it is left enabled. If the main-branch mutation job reports new
  survivors of that type, either write the missing tests or exclude it via
  `mutator: { excludedMutations: ["CallExpression"] }` in `stryker.config.mjs`.
* The PR mutation job (`33-frontend-pr-mutation-testing`) mutates every `src/main` file whose test
  file changed, and aborts if *any* test fails in its initial dry run — so a green `npm test` does
  not guarantee that job passes. Note that its file mapping only understands `.js`/`.jsx` names:
  a changed `.test.tsx` is not mapped, and a `.test.jsx` whose source is `.tsx` maps to a
  nonexistent `.jsx` path and is skipped. Check locally with
  `npx stryker run --mutate <comma-separated src/main files>` in `frontend/`; a survivor that also
  survives on a clean `main` checkout is pre-existing, not caused by the upgrade.
