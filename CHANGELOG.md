# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [0.9.1]

### Changed

+ Updated to Grobid version 0.9.1
+ Migrated from Dropwizard 4 / Jetty 11 to Dropwizard 5.0.1 / Jetty 12.1.9 (Jakarta EE 10), following Grobid 0.9.1
+ Bumped Java toolchain from 17 to 21, required by Grobid 0.9.0
+ Bumped Gradle wrapper from 7.2 to 9.0.0
+ Bumped Kotlin from 1.8.21 to 2.2.20, to match the `kotlin-stdlib` Grobid 0.9.1 pulls transitively
+ Bumped Dockerfile runtime base image to `lfoppiano/grobid:0.9.1-full` and the builder image to JDK 21
+ Migrated the Shadow plugin from `com.github.johnrengelman.shadow` to `com.gradleup.shadow:8.3.10`, after the original repository was archived
+ Bumped dropwizard-guicey to 8.0.2, Jackson to 2.21.1, Guava to 33.5.0-jre, commons-io to 2.21.0, commons-lang3 to 3.20.0, commons-collections4 to 4.5.0, httpclient to 4.5.14 and JEP to 4.3.1
+ Bumped JUnit BOM to 5.14.1, EasyMock to 5.6.0 and MockK to 1.13.17
+ Migrated the `application` block to `mainClass` for Gradle 9, and corrected the main class to `org.grobid.service.main.GrobidQuantitiesApplication`
+ Replaced PowerMock with Mockito and removed it from the build
+ Removed the abandoned `com.github.kt3k.coveralls` plugin; coverage is uploaded by `coverallsapp/github-action@v2`, which reads the Jacoco XML report directly
+ Derive the git revision with a local `getGitRevision()` instead of the `com.palantir.git-version` plugin, which is dropped (#197)
+ Releases are cut from a `release`-named branch rather than from `master`, following Grobid (#197)
+ `GET /service/health` returns a JSON status document with the per-model loaded/failed breakdown, `200` when ready and `503` when not (#198)

### Added

+ Service status indicator in the web interface, green when `/service/health` reports the service ready and red otherwise (#198)
+ Version and git revision in the footer of the web interface, linking to the commit the service was built from (#198)
+ `requestQueueMaxSize`, `requestQueueMaxWait` and `requestQueueRejectStatus` settings, bounding the queue of requests waiting for a `maxParallelRequests` slot (#159, #196)

### Fixed

+ Long requests are no longer cut at 30 seconds: since Jetty 12 the connector's `idleTimeout` does not bound the response computation (#159, #196)
+ The request queue bounds are set explicitly from the configuration instead of inherited from the Jetty defaults (#159, #196)
+ `processResources` declares the version and the git revision as task inputs, so `revision.txt` is regenerated when either changes instead of staying `UP-TO-DATE` (#197)
+ `config-docker.yml` no longer carries an unknown `views` key, which aborted startup inside the Docker image (#196)
+ Docker build failure in `installModels`: the HuggingFace model repository is cloned with the official `hf` CLI instead of grgit, which also avoids leaving Xet/LFS pointer stubs
+ Worked around a Kotlin K2 `StackOverflowError` when compiling `LabellingUtilsTest.kt`, by raising the Kotlin daemon stack in `gradle.properties`
+ Spelling fix in exception messages ("occured" -> "occurred")

### Known issues

+ None currently.

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
