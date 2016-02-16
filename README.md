# grobid-quantities

Early work in progress.

## Install, build, run

First install the latest development version of GROBID as explained by the [documentation](http://grobid.readthedocs.org).

Copy the module quantities as sibling sub-project to grobid-core, grobid-trainer, etc.:
> cp -r grobid-quantities grobid/

Try compiling everything with:
> cd PATH-TO-GROBID/grobid/

> mvn -Dmaven.test.skip=true clean install

Train the quantity model:
> cd PATH-TO-GROBID/grobid/grobid-quantities

> mvn generate-resources -Ptrain_quantities

The model will be saved under grobid-home/models/quantities (the directory must exist).

Run some test: 
> cd PATH-TO-GROBID/grobid/grobid-quantities

> mvn compile test

## Generation of training data

Similarly as for Grobid, with executable name ```createTrainingQuantities```, for example: 

> java -jar target/grobid-quantities-0.4.0-SNAPSHOT.one-jar.jar -gH ../grobid-home/ -gP ../grobid-home/config/grobid.properties -dIn ~/grobid/grobid-quantities/src/test/resources/ ~/test/ -exe createTrainingQuantities 

The input directory can contain PDF (.pdf, scientific articles only), XML/TEI (.xml or .tei, for patents and scientific articles) and text files (.txt).
