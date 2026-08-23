# Unit lexicon

The unit lexicon is the list of units grobid-quantities knows about. It is used in two places:

- as a **feature** of the quantities and units models, through
  `QuantityLexicon.inUnitNames(...)`: a token that belongs to a known unit is flagged as such
  in the feature vector,
- as the **resolution table** after the extraction, to give an extracted unit its type, its
  system, and the information needed to normalise it.

A unit that is missing from the lexicon can still be extracted - the models generalise - but it
comes out without a type and without a normalised value.

## Where it lives

| File                                                  | Content                                        |
|-------------------------------------------------------|------------------------------------------------|
| `src/main/resources/lexicon/en/units.json`            | the units themselves                           |
| `src/main/resources/lexicon/en/unit-vocabulary.json`  | the measurement type and system vocabularies   |
| `src/main/resources/lexicon/en/prefix.txt`            | SI prefixes and their symbols                  |
| `src/main/resources/lexicon/en/values.json`           | number words                                   |

Everything the lexicon needs is under `lexicon/en`, Java included: the type and system
vocabularies used to be enumerations in `UnitUtilities.java`, which meant a unit could not be
added with a new type without also editing Java, and an unknown type was only a warning in the
logs, the unit ending up untyped.

`UnitUtilities.Unit_Type` and `UnitUtilities.System_Type` are now read from
`unit-vocabulary.json` at class-loading time. They behave like the enumerations they replaced -
`valueOf(String)` throws on an unknown term, `toString()` returns the identifier, terms are
interned so `==` still holds - but the vocabulary itself is data.

Three tests keep the whole thing honest, and all of them are plain unit tests:

- `UnitsLexiconConsistencyTest` - every `type` and `system` used by `units.json` exists in the
  vocabulary, every unit has a name, no notation is declared twice
- `UnitUtilitiesVocabularyTest` - the vocabulary file and the classes agree
- `QuantityLexiconLoadingTest` - the lexicon actually *loads*, and a sample of units resolves to
  the expected type and system. The consistency test reads `units.json` as a JSON document, which
  is not the same thing: a notation separated only by `·` used to make the load throw and no test
  noticed.

## Adding a unit

``` json
{
  "notations": [
    { "raw": "km/h" },
    { "raw": "kmh" }
  ],
  "type": "VELOCITY",
  "system": "NON_SI",
  "supportsPrefixes": false,
  "names": [
    { "lemma": "kilometre per hour", "inflections": ["kilometres per hour"] }
  ]
}
```

- `notations` are the symbolic forms. **The first one is the canonical form**: it is what the
  `@unit` attribute of an annotation should use, see the
  [annotation guidelines](guidelines.md). A notation must be declared by a single unit,
  otherwise only the last one read is reachable.
- `names` are the spelled-out forms, one entry per spelling variant (`metre` and `meter` are two
  entries), each with its inflections.
- `type` is an `id` of the `types` array of `unit-vocabulary.json`. Use `UNKNOWN` when the
  measurement type has no entry yet rather than inventing one.
- `system` is an `id` of the `systems` array of the same file.
- `supportsPrefixes` tells whether the unit accepts SI prefixes. It defaults to `true` for
  `SI_BASE` and `SI_DERIVED` units and to `false` for the others.
- `skipNormalisation` marks units that have no conversion at all, e.g. `pH`.
- `constant` marks units that are physical constants, e.g. the Bohr magneton `μB`.

A notation can also carry its decomposition, which is used when the unit is a product:

``` json
{ "raw": "g/cm2", "product": [{ "base": "g" }, { "prefix": "c", "base": "m", "pow": "-2" }] }
```

## Adding a unit type

Add an entry to the `types` array of `unit-vocabulary.json`:

``` json
{ "id": "MAGNETIC_FLUX", "name": "magnetic flux" }
```

`id` is what `units.json` and the annotated corpus use, and what the TEI output writes into the
`type` attribute of a `<measure>`; `name` is the human-readable label the JSON output carries.
That is the only place to edit - there is no Java to change.

The normalisation of a new type still needs the corresponding quantity class to exist in the
`units-of-measurement` libraries, otherwise the unit is extracted and typed but not converted.

## A notation must not shadow a prefixed form

Every notation of a unit declaring `supportsPrefixes` is also indexed in its prefixed forms, and
lookup is case-insensitive. A new symbol therefore has to be checked against those expansions and
not only against the notations written in the file. Several common symbols lose that contest and
are deliberately **not** declared, the unit being reachable through its spelled-out name instead:

| Symbol | Not used because it collides with |
|--------|------------------------------------|
| `ha` (hectare)      | `hA`, hectoampere            |
| `ct` (carat)        | `cT`, centitesla             |
| `pc` (parsec)       | `pC`/`PC`, pico- and petacoulomb |
| `fc` (foot-candle)  | `fC`, femtocoulomb           |
| `cc` (cubic centimetre) | `cC`, centicoulomb       |
| `kat` (katal)       | `kat`, kilo-`at` (technical atmosphere) |
| `St` (stokes)       | `st`, stone                  |
| `Ah` (ampere hour)  | `aH`, attohenry              |

`cm/s` is not declared either: it is already reachable as centi- + `m/s`.

## Known gaps

- overlapping notations are not disambiguated: `oz` is claimed by the mass ounce and by the two
  fluid ounces, and `pH` overlaps with pico-henry - the latter is resolved by the side on which
  the value sits, see issue [#96](https://github.com/lfoppiano/grobid-quantities/issues/96)
- `mb` resolves to the millibarn rather than to the millibar, since `bar` does not declare
  `supportsPrefixes` and `b` (barn) does
- the lexicon is English only, see issue
  [#14](https://github.com/lfoppiano/grobid-quantities/issues/14)
- coverage is incremental and driven by the corpora we process, see issue
  [#92](https://github.com/lfoppiano/grobid-quantities/issues/92)

## Should we adopt OM instead?

Issue [#92](https://github.com/lfoppiano/grobid-quantities/issues/92) asks whether the
[Ontology of units of Measure](https://github.com/HajoRijgersberg/OM) should replace the lexicon.
Assessment, so the question can be closed or reopened deliberately:

**What OM is.** An OWL 2 ontology of units, quantities, dimensions and application areas,
distributed as RDF/Turtle under CC BY 4.0, version 2.0 since 2017, with a companion Java library
(`om-java-libs`) for unit conversion. Its coverage is far wider than ours - geometry, mechanics,
thermodynamics, electromagnetism, photometry, radiometry, nuclear physics, astronomy, earth
science, economics and more.

**Why it is attractive.** Coverage is the thing this lexicon will never win at by hand, and OM
carries dimensions and quantity kinds, which is exactly the vocabulary `unit-vocabulary.json`
maintains by hand.

**Why it is not a drop-in replacement.**

- *We need surface forms, OM models concepts.* The lexicon exists to answer "does this token look
  like a unit" and "which unit is this string". That needs inflections (`metres`/`meters`),
  spelling variants, and the messy notations found in papers (`kg/m2·yr`, `emu/cm^3`). An
  ontology labels a concept; it does not enumerate how authors write it.
- *Recognition needs the ambiguities resolved, not recorded.* The table above shows the real
  work is deciding that `ha` must not be indexed. A larger vocabulary makes that harder, not
  easier: importing every OM unit would multiply the collisions.
- *The type vocabulary is not free to change.* `Unit_Type` values appear in the annotated corpus
  and in the trained models. Swapping them for OM quantity IRIs would invalidate the corpus.
- *Normalisation is already delegated.* Conversion goes through the `units-of-measurement`
  (JSR-385) libraries, so OM's conversion library would replace a working component rather than
  fill a gap.

**Recommendation.** Do not adopt OM as the runtime lexicon. Use it as a *source* instead: mine it
for units we lack, and for the type of a unit we already extract but cannot classify, then add
them here in the format above, ambiguities checked. That keeps the recognition-oriented shape of
the lexicon while getting the coverage. Worth splitting into its own issue if pursued.
