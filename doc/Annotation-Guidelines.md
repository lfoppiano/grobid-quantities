<h1>Annotation guideleines for grobid-quantities</h1>

## Generating training data

The first step of the annotation process is to generate training data from unlabeled documents based on the current models. GROBID will create the training data corresponding to these documents in the right TEI format and with pre-annotations. The annotation work then consists of manually checking the produced annotations and adding the missing one. It is very important not to modify the text content in these generated files, not adding spaces or other characters, but only adding or moving XML tags. 

When the training data has been manually corrected, move the file under the repository resouces/dataset/quantities/corpus/ for retraining, or under resouces/dataset/quantities/evaluation/ is the annotated data should be used for evalutation only. To see the different evaluation options, see GROBID documentation on [training and evaluating](http://grobid.readthedocs.org/en/latest/Training-the-models-of-Grobid). 

## Annotations

There are three types of measurements supported by grobid-quantities. Measurement corresponding to single value (or _atomic_ value), to an interval (or range of values) or to a list. We do not distinguish conjuntive and disjunctive lists at the present time.  

### List of unit types

For the training annotation, the list of unit types (temperature, pressure, lenght, etc.) is controled and based on SI definitions. This control is normally exhaustive and contains currently 50 types. The unit types are given in the file ```src/main/java/org/grobid/core/utilities/UnitUtilities.java```. 
The given names of the unit types has to be used when annotating measurement. 

The list of units is however not controlled and GROBID supports units never seen before. 

### Atomic values

We can distinguish two kinds of atomic values expressions:

+ Atomic values with units: Following TEI, the numeric value is identify with the element ```<num>``` and the unit with the element ```<measure>``` where the unit type is given by the attribute @type. The indicate of the unit name by the attribute @unit is optional: if present it might be used to augment the unit lexicon if not yet represented in the lexicon. A global ```<measure>``` element encodes the complete measurement (composed by the numeric value and the unit) associated with the measurement type given by the attribute @type ```value```. 

```
We monitored nutritional behaviour of amateur ski-mountaineering athletes during <measure type="value"><num>4</num> <measure type="TIME" unit="day">days</measure></measure> prior to a major competition to compare it with official recommendations and with the athletes' beliefs.</p>

... more than <measure type="value"><num>20</num> <measure type="ENERGY" unit="MJ">MJ</measure></measure> (<measure type="value"><num>4,800</num> <measure type="ENERGY" unit="kcal">kcal</measure></measure>) for the shorter race route...

```

A pourcentage (and similar expression per mil and per ten thousand) has a unit type ```Unit_Type.FRACTION```: 

```
<measure type="value"><num>5</num> <measure type="FRACTION" unit="percentage">%</measure></measure> of fat mass...
```

+ atomic value without unit, corresponding to a count (implicit ```Unit_Type.COUNT```). The numeric value is encoded with element ```<num>``` and the global ```<measure>``` element indicating the measurement type is added. 

```
consists of <measure type="value"><num>two</num></measure> different race routes
```

The implicit ```Unit_Type.COUNT``` type will be infer by this particular encoding. Not that this encoding is only relevant to countable quantities. 


### Intervals

An interval introduces a range of values. We can distinguish two kinds of interval expressions:

+ Interval defined by a lower bound value and an upper bound value.

```
team races that can last from <measure type="interval"><num atLeast="4">4</num> to more than <num atMost="12">12</num> <measure type="TIME" unit="hour">h</measure></measure>
```

Note that an interval can be introduced by only one boundary value: 

```
A rotor shaft according to any one of the preceding claims having a diameter of at least <measure type="interval"><num atLeast="1">1</num><measure type="LENGTH" unit="m">m</measure></measure> 
```

+ Interval defined by a base value and a differential value. In the following example, after two "counts", four measurements express intervals following this form. Similarly as in the previous interval case, an attribute in element ```<num>```, here @type, characterizes the base value and the differential/range value. 

```
<measure type="value"><num>4</num></measure> women and <measure type="value"><num>15</num></measure> men, <measure type="interval"><num type="base">30</num> ± <num type="range">10</num> <measure type="TIME" unit="year">years</measure></measure>, <measure type="interval"><num type="base">176</num> ± <num type="range">7</num> <measure type="LENGTH" unit="cm">cm</measure></measure>, <measure type="interval"><num type="base">70</num> ± <num type="range">9</num> <measure type="MASS" unit="kg">kg</measure></measure>, <measure type="interval"><num type="base">15</num> ± <num type="range">5</num> <measure type="FRACTION" unit="percentage">%</measure></measure> of fat mass
```

### Lists

Lists introduce a serie of values. The unit can be expressed per value or for several values at the same time. A ```<measure>``` element encloses the whole list of values including their units: 

```
 <measure type="list"><measure type="ENERGY" unit="cm^-1">cm-1</measure>: <num>3440</num>(br), <num>1662</num>, <num>1632</num>, <num>1575</num>, <num>1536</num>, <num>1498</num>, <num>1411</num>, <num>1370</num>, <num>1212</num>, <num>1006</num>, <num>826</num>, <num>751</num></measure> 
```

### Special cases

+ Room temperature (Raumtemperatur, température ambiante, ...) is used very frequently in chemistry and related fields. It is 21 °C, although not really formally defined.

```
<measure type="value"><measure type="TEMPERATURE">Raumtemperatur</measure></measure>
```

### Quantified substance

The quantified substance is the substanced for which the measurement is expressed.  