@ echo off
if(%1)==() GOTO EXITHELP
set params= 
:LOOP
if(%1)==() GOTO STARTJAVA
set params=%params% %1%
SHIFT
GOTO LOOP
:STARTJAVA
java -Xms128m -Xmx1024m -jar osm2gis.jar%params%
GOTO EXIT
:EXITHELP
echo Use --help to show the manpage
:EXIT