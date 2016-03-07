#!/usr/bin/env bash

INPUT=$1

if [ -z "${INPUT}" ]; then 
echo "Missing filename. Usage extractUnits.sh *.xml"; 
exit 0
fi

grep -oh -E '<measure type=\"[A-Z]+\" unit=\"[^<]+\">([^<]+)</measure>' ${INPUT} | sed 's/<\/measure>//g' | sed -r 's/<measure type=\"[A-Z]+\" unit=\"[^<]+\">//g'
