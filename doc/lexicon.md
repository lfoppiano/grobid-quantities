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

| File                                        | Content                                          |
|---------------------------------------------|--------------------------------------------------|
| `src/main/resources/lexicon/en/units.json`  | the units themselves                             |
| `src/main/resources/lexicon/en/prefix.txt`  | SI prefixes and their symbols                    |
| `src/main/resources/lexicon/en/values.json` | number words                                     |
| `src/main/java/org/grobid/core/utilities/UnitUtilities.java` | the `Unit_Type` and `System_Type` vocabularies |

The type and system vocabularies are Java enumerations, so a unit whose `type` or `system` is not
one of their values is loaded without it, with only a warning in the logs.
`UnitsLexiconConsistencyTest` checks the lexicon against both vocabularies at build time.

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
- `type` is a value of `UnitUtilities.Unit_Type`. Use `UNKNOWN` when the measurement type has no
  entry yet rather than inventing one.
- `system` is a value of `UnitUtilities.System_Type`.
- `supportsPrefixes` tells whether the unit accepts SI prefixes. It defaults to `true` for
  `SI_BASE` and `SI_DERIVED` units and to `false` for the others.
- `skipNormalisation` marks units that have no conversion at all, e.g. `pH`.
- `constant` marks units that are physical constants, e.g. the Bohr magneton `μB`.

A notation can also carry its decomposition, which is used when the unit is a product:

``` json
{ "raw": "g/cm2", "product": [{ "base": "g" }, { "prefix": "c", "base": "m", "pow": "-2" }] }
```

## Adding a unit type

Adding a value to `UnitUtilities.Unit_Type` is enough for the lexicon and the annotations to use
it, but the normalisation of a new type also needs the corresponding quantity class to exist in
the `units-of-measurement` libraries, otherwise the unit is extracted and typed but not converted.

## Known gaps

- overlapping notations are not disambiguated: `oz` is claimed by the mass ounce and by the two
  fluid ounces, and `pH` overlaps with pico-henry - the latter is resolved by the side on which
  the value sits, see issue [#96](https://github.com/lfoppiano/grobid-quantities/issues/96)
- the lexicon is English only, see issue
  [#14](https://github.com/lfoppiano/grobid-quantities/issues/14)
- coverage is incremental and driven by the corpora we process, see issue
  [#92](https://github.com/lfoppiano/grobid-quantities/issues/92)
