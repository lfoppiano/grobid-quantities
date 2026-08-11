# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Changed

+ Updated to Grobid version 0.9.1
+ Bumped Kotlin from 2.0.21 to 2.2.20 to match the `kotlin-stdlib` 2.2.20 that Grobid 0.9.1 now pulls transitively (the 2.0.21 compiler cannot read the newer stdlib metadata, which broke `compileKotlin`)
+ Bumped Dockerfile runtime base image from `lfoppiano/grobid:0.9.0-full` to `lfoppiano/grobid:0.9.1-full`

### Added

+ `requestQueueMaxSize`, `requestQueueMaxWait` and `requestQueueRejectStatus` configuration settings, bounding the queue of requests waiting for a `maxParallelRequests` slot. The effective values are logged at startup (#159)

### Fixed

+ Long requests are no longer cut short. Before the migration to Jetty 12, a request taking more than Jetty's default of 30 seconds was rejected with a 503 whatever `idleTimeout` was set to. Verified on the current version: a request taking ~70 seconds returns 200 with the complete response, both with `idleTimeout: 120 seconds` and with `idleTimeout: 5 seconds` - the connection idle timeout no longer bounds the response computation. The 503 seen under load comes from `maxParallelRequests` instead (#159). Reproduced again on a 274,430-character request: `200` with the complete 837 KB response after 42s, byte-identical under both `idleTimeout` settings; and with `maxParallelRequests: 1`, `requestQueueMaxWait: 2 seconds`, `requestQueueRejectStatus: 429`, four concurrent requests give one `200` after 28.3s and three `429`s after exactly 2.14s - the report's pattern, with the delay and status now taken from the configuration
+ The bounds on the request queue are no longer implicit (#159). Requests over `maxParallelRequests` are queued, and the queue used to be bounded by whatever the underlying Jetty version happened to default to. With Jetty 9's `QoSFilter` that was the servlet default async timeout of 30 seconds, which is why raising the connector's `idleTimeout` to 120 seconds did not stop requests from failing with `503` after ~30 seconds. Jetty 12's `QoSHandler` defaults differently again (1024-long queue, no waiting limit); all three bounds are now set explicitly from the configuration instead of inherited. Note that neither these settings nor `idleTimeout` bound how long a request may take to be answered once it holds a slot
+ `config-docker.yml` no longer carries a `views` key, which the configuration class does not declare. Since the application enables `FAIL_ON_UNKNOWN_PROPERTIES`, this aborted startup inside the Docker image with `Unrecognized field at: views`. Both shipped configuration files are now covered by a test that parses them the way the application does

### Known issues

+ `ValueParserTest.testTagValue_exponential_1/2` fail after the Grobid 0.9.1 upgrade: Grobid 0.9.1 changed where the English word-forms lexicon (`english.wf`) is loaded, so the tests' PowerMock `@SuppressStaticInitializationFor("...Lexicon")` mock no longer intercepts it and it loads against a null grobid-home. These should be migrated off PowerMock (already a standing TODO in `build.gradle`) or reworked to run against a real grobid-home. The other 152 unit tests pass.

## [0.9.0]

### Changed

+ Updated to Grobid version 0.9.0
+ Bumped Java toolchain from 17 to 21 (required by Grobid 0.9.0)
+ Bumped Gradle wrapper from 7.2 to 9.0.0
+ Bumped Kotlin from 1.8.21 to 2.0.21
+ Migrated Shadow plugin from `com.github.johnrengelman.shadow:7.1.0` to `com.gradleup.shadow:8.3.10` (new coordinates after the original repo was archived)
+ Bumped Dropwizard from 4.0.13 to 4.0.17, dropwizard-guicey from 7.0.0 to 7.3.1, dropwizard-metrics from 4.2.22 to 4.2.38
+ Bumped Jackson from 2.14.3 to 2.21.1
+ Bumped Guava from 31.0.1-jre to 33.5.0-jre, commons-io from 2.14.0 to 2.21.0, commons-lang3 from 3.12.0 to 3.20.0, commons-collections4 from 4.4 to 4.5.0, httpclient from 4.5.13 to 4.5.14
+ Bumped JEP from 4.0.2 to 4.3.1
+ Bumped JUnit BOM from 5.10.2 to 5.14.1, EasyMock from 5.2.0 to 5.6.0, MockK from 1.13.9 to 1.13.17
+ Migrated `application` block to use `mainClass` (Gradle 9 compatibility) and corrected the main class to `org.grobid.service.main.GrobidQuantitiesApplication`
+ Updated CI workflows and Dockerfile builder image to JDK 21
+ Removed the abandoned `com.github.kt3k.coveralls` Gradle plugin (unmaintained since 2020 and unable to detect GitHub Actions as a CI service). Coverage is already uploaded by the `coverallsapp/github-action@v2` step in the CI workflows, which reads the Jacoco XML report directly.

### Fixed

+ Spelling fix in exception messages ("occured" → "occurred")
+ Worked around a Kotlin 2.0.21 K2 compiler `StackOverflowError` in `IrConstDeclarationAnnotationTransformer` that surfaced when compiling `LabellingUtilsTest.kt` on CI runners with small default JVM stacks (the test file has seven methods with nested string-concatenation chains that produce a deeply nested `IrCall` tree). Bumped the Kotlin compiler daemon stack via `kotlin.daemon.jvmargs=-Xmx2g -Xss4m` in `gradle.properties`. To be removed once upstream Kotlin fixes the recursive visitor bug.
+ Fixed Docker build failure in the `installModels` step. The `downloadModelsGit` Gradle task was using `org.ajoberstar.grgit:5.3.0` (embedded jgit 6.10.x), whose Smart HTTP v2 protocol parser is incompatible with HuggingFace's git server and fails with `TransportException: Short read of block` during the initial `lsRefs` handshake. Even a working `git clone` would have left LFS/Xet pointer stubs instead of real model weights for files >10MB, requiring a follow-up `git lfs pull` / `git xet checkout` with their own failure modes. Replaced `Grgit.clone(...)` with the official HuggingFace CLI: `hf download sciencialab/grobid-quantities-models --local-dir <path>` invoked via a Gradle `Exec` task. Added Python 3.11 to the Docker builder image via the `ppa:deadsnakes/ppa` repository (matching the parent grobid runtime image's Python version) and installed the `hf` CLI via `https://hf.co/cli/install.sh`. The builder cleans up the HuggingFace cache, pip cache, venv, and `hf` wrapper after `installModels` finishes. Removed the `org.ajoberstar.grgit` plugin and the `git-lfs` apt package from the builder.

### Notes

+ PowerMock 2.0.9 is intentionally retained for now: 5 test classes still depend on it. Java 21 compatibility is achieved via additional `--add-opens` JVM flags in the `test` and `integration` Gradle tasks. The Mockito migration of these tests is tracked as deferred work.

## [0.8.2]

### Changed
+ Updated to Grobid version 0.8.2
+ Migrated models to Huggingface


## [0.8.0]

### Added

+ Docker image snapshots are built and pushed on dockerhub at each commit
+ new Dockerfile.local that does not clone from github
+ End 2 end evaluation using MeasEVAL (#164)

### Changed

+ Updated to Grobid version 0.8.0
+ Updated to Dropwizard version 4.x (from version 1.x)
+ Updated training data, removed some leftover callout references that were partially removed
+ Updated models and evaluations (available [here](https://grobid-quantities.readthedocs.io/en/latest/evaluation-scores/))

### Fixed 
+ Fixed and improved the word2number that now supports also fractions and other constructs #176, #110, #91
+ Fixed the segmentation issue for the quantified object. Now the spurious characters from PDF documents are removed #158 



## [0.7.3] – 2023-06-26

### Added

+ Added additional units in the lexicon
+ Added missing log when exception are raised
+ Introduced Kotlin for new development

### Changed

+ Upgrade to grobid 0.7.3 and support to JDK > 11
+ Updated Docker image to support JDK 17 and use the gradle distribution script instead of the JAR directly
+ Transitioned from circleci to GitHub actions

### Fixed

+ Fix notation lexicon #97
+ Fix list and labelled sequence extraction with DL BERT models #153
+ Improve recognition of composed units using sentence segmentation #155 #87

## [0.7.2] – 2023-01-20

### Added

+ Create holdout set by @lfoppiano in #145
+ Add additional DL and transformers models by @lfoppiano in #146

### Changed

Update to Grobid 0.7.2

### Fixed

+ Fix value parser's incorrect recognition by @lfoppiano in #141

## [0.7.1] – 2022-09-02

### Added

+ New BidLSTM_CRF models for quantities, values and units parsing #129
+ Add docker image on hub.docker.com #142
+ Update to Grobid 0.7.1 #137

### Changed

+ Use the grobid sentence segmentation for the quantified object sentence splitting #138

### Fixed

+ Fixes incorrect boxes colors #125
+ Fixed lexicon #134

## [0.7.0] – 2021-08-06

### Added

+ Docker image #128
+ Configurable number of parallel request
+ Various improvement in the unit normalisation and update of library Unit of measurement to version 2.x #95

### Changed

+ Retrained models with CRF
+ Grobid 0.7.0 #123

### Fixed

+ Coveralls build #127
+ Fixed command line parameters #119

## [0.6.0] – 2020-04-30

### Added

+ First official release
+ Extraction of quantities, units and values using CRF
+ Support for Text and PDF

### Changed

+ Added evaluation measurement and models

### Fixed

[Unreleased]: https://github.com/kermitt2/grobid/compare/0.6.0...HEAD

[0.6.0]: https://github.com/kermitt2/grobid/compare/0.6.0

<!-- markdownlint-disable-file MD024 MD033 -->
