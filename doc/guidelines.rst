.. topic:: Annotation guidelines

Annotation guidelines
=====================

Generating training data
------------------------

The first step of the annotation process is to generate training data from unlabeled documents based on the current models.
GROBID will create the training data corresponding to these documents in the right TEI format and with pre-annotations.
The annotation work then consists of manually checking the produced annotations and adding the missing one.
It is very important not to modify the text content in these generated files, not adding spaces or other characters, but only adding or moving XML tags.

When the training data has been manually corrected, move the file under the repository ``resouces/dataset/quantities/corpus/`` for retraining, or under ``resouces/dataset/quantities/evaluation/`` is the annotated data should be used for evalutation only.
To see the different evaluation options, see GROBID documentation on `training and evaluating <http://grobid.readthedocs.org/en/latest/Training-the-models-of-Grobid>`_.

Annotations
-----------

There are three types of measurements supported by grobid-quantities. Measur
ement corresponding to single value (or *atomic* value), to an interval (or range of values) or to a list.
We do not distinguish conjunctive and disjunctive lists at the present time.

List of unit types
~~~~~~~~~~~~~~~~~~

For the training annotation, the list of unit types (temperature, pressure, lenght, etc.) is controled and based on SI definitions. This control is normally exhaustive and contains currently 50 types. The unit types are given in the file ```src/main/java/org/grobid/core/utilities/UnitUtilities.java```. 
The given names of the unit types has to be used when annotating measurement. 

The list of units is however not controlled and GROBID supports units never seen before. 

Atomic values
~~~~~~~~~~~~~

We can distinguish two kinds of atomic values expressions:

Atomic values with units
^^^^^^^^^^^^^^^^^^^^^^^^

Following TEI, the numeric value is identify with the element ```<num>``` and the unit with the element ```<measure>``` where the unit type is given by the attribute ```@type```.
The indicate of the unit name by the attribute ```@unit``` is optional: if present it might be used to augment the unit lexicon if not yet represented in the lexicon.
A global ```<measure>``` element encodes the complete measurement (composed by the numeric value and the unit) associated with the measurement type given by the attribute ```@type``` ```value```.

Example 1:
::
   We monitored nutritional behaviour of amateur ski-mountaineering athletes during <measure type="value"><num>4</num>
   <measure type="TIME" unit="day">days</measure></measure> prior to a major competition to compare it with official
   recommendations and with the athletes' beliefs.

Example 2:
::
   ... more than <measure type="value"><num>20</num> <measure type="ENERGY" unit="MJ">MJ</measure></measure> (<measure
   type="value"><num>4,800</num> <measure type="ENERGY" unit="kcal">kcal</measure></measure>) for the shorter race route...


A percentage (and similar expression per mil and per ten thousand) has a unit type **Unit_Type.FRACTION**:
::
   <measure type="value"><num>5</num> <measure type="FRACTION" unit="%">%</measure></measure> of fat mass...


Atomic value without unit
^^^^^^^^^^^^^^^^^^^^^^^^^

They corresponding to a count (implicit **Unit_Type.COUNT**). The numeric value is encoded with element ``<num>`` and the global ``<measure>`` element indicating the measurement type is added.

For example: 
::
   consists of <measure type="value"><num>two</num></measure> different race routes

The implicit **Unit_Type.COUNT** type will be infer by this particular encoding. Not that this encoding is only relevant to countable quantities.


Intervals
~~~~~~~~~

An interval introduces a range of values. We can distinguish two kinds of interval expressions:

1. Bounded value
^^^^^^^^^^^^^

Interval defined by a lower bound value and an upper bound value:
::
   team races that can last from <measure type="interval"><num atLeast="4">4</num> to more than <num atMost="12">12</num>
   <measure type="TIME" unit="hour">h</measure></measure>


Note that an interval can be introduced by only one boundary value: 
::
  A rotor shaft according to any one of the preceding claims having a diameter of at least <measure type="interval"><num
  atLeast="1">1</num><measure type="LENGTH" unit="m">m</measure></measure>

  [..]sky positions lie within a <measure type="interval"><num atMost="7">7</num> <measure type="ANGLE" unit="°">°</measure>
  </measure> radius of other planets[..]


2. Base and differential value
^^^^^^^^^^^^^^^^^^^^^^^^^^^
Take the example
::
   4 women and 15 men, 30± 10 years, 176±7 cm, 70±9 kg, 15±5 % of fat mass, VO2max: 50±8 ml·kg−1·min−1 and 21 of race A

after two "counts", four measurements express intervals following this form.
::
  <measure type="value"><num>4</num></measure> women and <measure type="value"><num>15</num></measure> men,

Similarly as in the previous interval case, an attribute in element ``<num>``, here ``@type``, characterizes the
*base* value and the *differential/range* value.
::
  <measure type="interval"><num type="base">30</num> ± <num type="range">10</num><measure type="TIME" unit="year">years</measure></measure>,
  <measure type="interval"><num type="base">176</num> ± <num type="range">7</num><measure type="LENGTH" unit="cm">cm</measure></measure>,
  <measure type="interval"><num type="base">70</num> ± <num type="range">9</num> <measure type="MASS" unit="kg">kg</measure></measure>,
  <measure type="interval"><num type="base">15</num> ± <num type="range">5</num> <measure type="FRACTION" unit="%">%</measure></measure> of fat mass


If the quantity is expressed only in term of range (without base) it can be implicitly assumed that the base=0, see example ± 10 years
::
  <measure type="interval">± <num type="range">10</num><measure type="TIME" unit="year">years</measure></measure>

Notes about intervals
^^^^^^^^^^^^^^^^^^^^^

• Interval markers such as ``more than``, ``less than``, and so on, are left outside the annotation when it's possible (see issue `#35 <https://github.com/kermitt2/grobid-quantities/issues/35>`_).
Example:
::
  more than <measure type="interval"> <num atLeast="2">2</num> </measure> 

• An interval can be bounded with quantities expressed in different unit multiples (see issue `#45 <https://github.com/kermitt2/grobid-quantities/issues/45>`_).
For the sentence ``radii between 10 µm and 1 cm`` the result will be:
::
  grains with radii between <measure type="interval"><num atLeast="10">10</num> <measure type="LENGTH" unit="µm">µm</measure> and <num atMost="1">1</num> <measure type="LENGTH" unit="cm">cm</measure></measure>


Lists
~~~~~

Lists introduce series of values. The unit can be expressed per value or for several values at the same time.
A ``<measure>`` element encloses the whole list of values including their units:
::
   <measure type="list"><measure type="ENERGY" unit="cm^-1">cm-1</measure>: <num>3440</num>(br), <num>1662</num>,
   <num>1632</num>, <num>1575</num>, <num>1536</num>, <num>1498</num>, <num>1411</num>, <num>1370</num>, <num>1212</num>,
   <num>1006</num>, <num>826</num>, <num>751</num></measure>


List can be disjunctive or conjunctive, we do not distinguish the two kinds of list at the present time:
::
  batches of <measure type="list"><num>three</num> or <num>four</num></measure> observations

Additional items
~~~~~~~~~~~~~~~~

Dates
^^^^^
Dates are time measurements, they are thus also encoded in the training data as a complement to the other _TIME_ expressions involving time units.
In TEI P5, the dates are marked with a specific element ``<date>`` which can be contained in an element ``<measure>``.
The encoding is then straightforward for atomic values (with attribute ``@when``), intervals (with attribute ``@from-iso`` and ``@to-iso`` in case on min-max intervals) and lists:
::
  Comet C/2013 A1 (Siding Spring) will have a close encounter with Mars on <measure type="value">
  <date when="2014-10-19">October 19, 2014</date></measure>.

  The arrival time of these particles spans a <measure type="interval"><num type="range">20</num>-<measure type="TIME"
  unit="min">minute</measure> time interval centered at <date type="base" when="2014-10-19T20:09">October 19, 2014 at 20:09 TDB</date></measure>


  Observations took place from <measure type="interval"><date from-iso="2014-10-19">October 19, 2014</date> to
  <date to-iso="2014-10-25">October 25, 2014</date></measure>.

  Observations were performed on <measure type="list"><date when="2013-10-29">October 29, 2013</date>, on
  <date when="2014-01-21">Jan 21, 2014</date>, and on <date when="2014-03-11">March 11, 2014</date></measure>.

Special cases
^^^^^^^^^^^^^

**Room temperature** (Raumtemperatur, température ambiante, ...) is used very frequently in chemistry and related fields.
It can be considered as 20 °C (293 Kelvin), although not defined in a standard manner (https://de.wikipedia.org/wiki/Raumtemperatur).
::
  <measure type="value"><measure type="TEMPERATURE">Raumtemperatur</measure></measure>
  

Miscellaneous
~~~~~~~~~~~~~

Units without values
^^^^^^^^^^^^^^^^^^^^

**Case where it's not annotated**: 
When we refer to the units as such, to express something about the units, we are not using the units to quantify something with a value:
::
  and r H are the geocentric and heliocentric distances in cm and AU, respectively, and F comet and F
Like here for the units: ``cm`` and ``AU``.

**Case where it's annotated**: 
We could have units expressed without values, when the value is implicit:
::
  that can extend <measure type="interval"><measure type="LENGTH" unit="mm">millimeters</measure></measure> or even <measure type="interval"><measure type="LENGTH" unit="cm">centimeters</measure></measure> from the cell body 

here the value of millimeters and centimeters is unspecified (e.g. equivalent to ``several``), but we have a quantity and more precisely an interval.
See issue `#31 <https://github.com/kermitt2/grobid-quantities/issues/31>`_ 

Unprecise quantifiers
^^^^^^^^^^^^^^^^^^^^^

When used with units, quantifers like ``few``, ``several``, ``a couple``, ``a large amount of`` is annotated, and whatever quantifies even imprecisely :
::
  the reference solution becomes distinct from the ballistic solution only a <measure type="value"><num>couple</num> of <measure type="TIME" unit="week">weeks</measure></measure> before the encounter. 

Determiners is leaved outside (``couple`` as value and ``weeks`` as unit for ``a couple of weeks``). See issue `#34 <https://github.com/kermitt2/grobid-quantities/issues/34>`_

Constants
^^^^^^^^^

Precise number (for example ``c`` , the speed of light in vacuum) and imprecise numbers (for example ``π`` which has an infinite number of decimals) are annotated. See issue `#37 <https://github.com/kermitt2/grobid-quantities/issues/37>`_ 

Exponents for powers of ten
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Exponents might be rewritten in documents, for example 10 power -6 in pdf becomes ``10 &#x2212;6``.
The exponents for power of ten is written in the attribute when there is one, 10 power -6 will be written ``10^-6``.
Example in interval:
::
  <measure type="interval"><num atMost="10^-6">10 −6</num></measure>

See issue `#38 <https://github.com/kermitt2/grobid-quantities/issues/38>`_ 


Out of scope
~~~~~~~~~~~~

Only **expressions of quantities** is annotated, which can use numbers or alphabetical words.

Some numbers are also used for other stuff like markers, call-out, section number, identifiers, index, reference expressions, formula parameters, etc. and all these cases are out of scope. See issue `#36 <https://github.com/kermitt2/grobid-quantities/issues/36>`_

Some sequences not annotated
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Reference markers:
::
  lower than those derived by Vaubaillon et al. (2014) and Moorhead et al. (2014) computing the corresponding impact probabilities (Milani et al. 2005)

Figure/table titles, and other numbers who don't quantify anything:
::
    Figure 1 shows the residuals of C/2013 A1's observations
    [Figure 1 about here.]
    Table 1 contains the orbital elements of the computed solution.
    our new orbit solution (JPL solution 46)

Inline formulas, like:
::
    a minimum point of ∆v 2 = |∆v| 2 under the constraint that the particle reaches Mars, i.e., (ξ, ζ)(r, β, ∆v) = (0, 0).

Quantified substance
~~~~~~~~~~~~~~~~~~~~

The quantified substance is the substanced for which the measurement is expressed.  


Case not yet supported
~~~~~~~~~~~~~~~~~~~~~~

The following cases are not annotated at this stage. The sentence when these cases occur should be put in comments for the moment.  

**Sigma estimation**
::
  We selected the A 1 uncertainty so that its range would span from 0 au/d 2 to twice the nominal value at 3&#x3C3;.

**Intervals embedded in intervals**
::
  [..]only Mars is near enough that the orbital motion can extend a single viewing window from 45 days to as much as 60 to 90 days.

  For the wide scenario the uncertainty goes from 45 min down to 1–2 min.

Note: one possibility is to only mark the external boundaries of the interval.
::
  [..]only Mars is near enough that the orbital motion can extend a single viewing window from <measure type="interval">
  <num atLeast="45">45</num><measure type="TIME" unit="day">days</measure> to as much as 60 to <num atMost="90">90</num>
  <measure type="TIME" unit="day">days</measure></measure>.

  For the wide scenario the uncertainty goes from <measure type="interval"><num atLeast="45">45</num>
  <measure type="TIME" unit="days">min</measure> down to 1–<num atMost="2">2</num> <measure type="TIME" unit="min">min</measure></measure>.
  
**Discontinuous cases**

Quantities expressed by a power of ten multiplication (see issue `#42 <https://github.com/kermitt2/grobid-quantities/issues/42>`_):
::
  A1 (Siding Spring) will pass Mars with a close approach distance of 1.35 ± 0.05 × 10 5 km
or like:
::
  The gas production rates, Q(CO 2 ) = (3.52> ± 0.03) × 10 26 molecules s −1

