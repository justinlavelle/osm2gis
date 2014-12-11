#!/bin/sh
if [ $# -eq 0 ]
	then echo Use --help to show the manpage
	else java -jar osm2gis.jar $*
fi
