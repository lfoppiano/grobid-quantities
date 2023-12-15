# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [0.8.0]

### Added

+ Docker image snapshots are built and pushed on dockerhub at each commit
+ new Dockerfile.local that does not clone from github

### Changed

+ Updated to Grobid version 0.8.0
+ Updated to Dropwizard version 4.x (from version 1.x)



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
