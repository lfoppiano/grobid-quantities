# grobid-quantities

Early work in progress.

## Install, build, run

First install GROBID as explained by the [documentation](http://grobid.readthedocs.org).

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

