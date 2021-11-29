# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added
+ New BidLSTM + CRF models for quantities, values and units parsing #129

### Fixed
+ Fixes incorrect boxes colors #125

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
