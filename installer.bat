@echo off
REM Ask after osm2gis installation folder
SET /P osm2gisdir=Set osm2gis directory [%PROGRAMFILES%\osm2gis]:
if "%osm2gisdir%"=="" SET osm2gisdir=%PROGRAMFILES%\osm2gis
if NOT EXIST %osm2gisdir% mkdir "%osm2gisdir%"

REM unpack osm2gis windows zip into the directory
install\unzip -o osm2gis_win.zip -d "%osm2gisdir%"

REM make temp folder for differential update files
mkdir "%osm2gisdir%\temp"

REM Ask after the tomcat webapps folder
SET /P tomcatdir=Set tomcat directory [%PROGRAMFILES%\Apache Software Foundation\Tomcat 6.0\webapps]:
if "%tomcatdir%"=="" SET tomcatdir=%PROGRAMFILES%\Apache Software Foundation\Tomcat 6.0\webapps

REM Remove geoserver folder
net stop "Apache Tomcat 6"y
rmdir /S /Q "%tomcatdir%\geoserver"
rmdir /S /Q "%tomcatdir%\osm2gisdemo"
rmdir /S /Q "%tomcatdir%\mapcompare"
del "%tomcatdir%\geoserver.war"

REM add geoserver
type nul > "%tomcatdir%\geoserver.war"
xcopy /Y geoserver\geoserver.war "%tomcatdir%\geoserver.war"


REM copy www folder to tomcat as osm2gisdemo
xcopy  /i /Y /E geoserver\www\* "%tomcatdir%\osm2gisdemo"

REM copy mapcompare folder to tomcat as mapcompare
xcopy  /i /Y /E geoserver\mapcompare\* "%tomcatdir%\mapcompare"


net start "Apache Tomcat 6"

echo Try to reach Apache Tomcat 6 to generate the goeserver\WEB-INF\web.xml file.
echo This can take some minutes...
:Loop
ping localhost:8080/geoserver/ -n 2 -w 1000 > nul
if NOT EXIST "%tomcatdir%\geoserver\WEB-INF\web.xml" GOTO Loop

REM Stopping the Apache Tomcat 6.0 Service
echo try to stop Apache Tomcat 6 service...
net stop "Apache Tomcat 6"

REM Remove datafolder of geoserver
rmdir /S /Q "%tomcatdir%\geoserver\data"

REM Set Geoserver data folder
SET /P geoserverdir=Set geoserver data directory [%tomcatdir%\geoserver\data] (use \\ between folders):
if "%geoserverdir%"=="" (
	SET geoserverdir=%tomcatdir%\geoserver\data
) else (
	type nul > "%tomcatdir%\geoserver\WEB-INF\web.xml.bak" 
	xcopy /Y "%tomcatdir%\geoserver\WEB-INF\web.xml" "%tomcatdir%\geoserver\WEB-INF\web.xml.bak" 
	install\sed -e "/description/a <context-param><param-name>GEOSERVER_DATA_DIR</param-name><param-value>%geoserverdir%</param-value></context-param>" "%tomcatdir%\geoserver\WEB-INF\web.xml.bak" > "%tomcatdir%\geoserver\WEB-INF\web.xml"
)
REM Copy geoserver_data to geoserver data dir
xcopy  /i /Y /E geoserver\geoserver_data\* "%geoserverdir%"

REM Ask after geoserver admin password
SET /P geoserverpw=Set geoserver admin password [geoserver]:
if NOT "%geoserverpw%"=="" (
	type nul > "%geoserverdir%\security\users.properties.bak"
	xcopy /Y "%geoserverdir%\security\users.properties" "%geoserverdir%\security\users.properties.bak"
	install\sed -e "s/admin=geoserver/admin=%geoserverpw%/" "%geoserverdir%/security/users.properties.bak" > "%geoserverdir%/security/users.properties"
)

REM Ask after db name
SET /P dbname=Set db name:
if NOT "%dbname%"=="" (
	type nul > "%geoserverdir%\workspaces\osm\osm_ds\datastore.xml.bak"
	xcopy /Y "%geoserverdir%\workspaces\osm\osm_ds\datastore.xml" "%geoserverdir%\workspaces\osm\osm_ds\datastore.xml.bak"
	install\sed -e "s/database</%dbname%</" "%geoserverdir%/workspaces/osm/osm_ds/datastore.xml.bak" > "%geoserverdir%/workspaces/osm/osm_ds/datastore.xml"
	
	type nul > "%osm2gisdir%\config\osm2gis.properties.bak"
	xcopy /Y "%osm2gisdir%\config\osm2gis.properties" "%osm2gisdir%\config\osm2gis.properties.bak"
	install\sed -e "s/db.name=osm/db.name=%dbname%/" "%osm2gisdir%/config/osm2gis.properties.bak" > "%osm2gisdir%/config/osm2gis.properties"
)

REM Ask after db user
SET /P dbuser=Set db user:
if NOT "%dbuser%"=="" (
	xcopy /Y "%geoserverdir%\workspaces\osm\osm_ds\datastore.xml" "%geoserverdir%\workspaces\osm\osm_ds\datastore.xml.bak"
	install\sed -e "s/user</%dbuser%</" "%geoserverdir%/workspaces/osm/osm_ds/datastore.xml.bak" > "%geoserverdir%/workspaces/osm/osm_ds/datastore.xml"
	
	xcopy /Y "%osm2gisdir%\config\osm2gis.properties" "%osm2gisdir%\config\osm2gis.properties.bak"
	install\sed -e "s/db.username=osm/db.username=%dbuser%/" "%osm2gisdir%/config/osm2gis.properties.bak" > "%osm2gisdir%/config/osm2gis.properties"
)

REM Ask after db password:
SET /P dbpw=Set db password:
if NOT "%dbpw%"=="" (
	xcopy /Y "%geoserverdir%\workspaces\osm\osm_ds\datastore.xml" "%geoserverdir%\workspaces\osm\osm_ds\datastore.xml.bak"
	install\sed -e "s/passwd</%dbpw%</" "%geoserverdir%/workspaces/osm/osm_ds/datastore.xml.bak" > "%geoserverdir%/workspaces/osm/osm_ds/datastore.xml"
	
	xcopy /Y "%osm2gisdir%\config\osm2gis.properties" "%osm2gisdir%\config\osm2gis.properties.bak"
	install\sed -e "s/db.password=osm/db.password=%dbpw%/" "%osm2gisdir%/config/osm2gis.properties.bak" > "%osm2gisdir%/config/osm2gis.properties"
)

REM Ask after longitude min x
SET /P bblongminx=Set boundingbox longitude min x [-180.0]:
if "%bblongminx%"=="" SET bblongminx=-180.0
if NOT "%bblongminx%"=="" (
	rem type nul > "%geoserverdir%/services.xml.bak"
	rem xcopy /Y "%geoserverdir%\services.xml" "%geoserverdir%\services.xml.bak"
	rem install\sed -e "s/-180.0/%bblongminx%/" "%geoserverdir%/services.xml.bak" > "%geoserverdir%/services.xml"

	xcopy /Y "%osm2gisdir%\config\osm2gis.properties" "%osm2gisdir%\config\osm2gis.properties.bak"
	install\sed -e "s/osm2gis.importer.boundingboxlong.min=/osm2gis.importer.boundingboxlong.min=%bblongminx%/" "%osm2gisdir%/config/osm2gis.properties.bak" > "%osm2gisdir%/config/osm2gis.properties"
)

REM Ask after longitude max x
SET /P bblongmaxx=Set boundingbox longitude max x [180.0]:
if "%bblongmaxx%"=="" SET bblongmaxx=180.0
if NOT "%bblongmaxx%"=="" (
	rem xcopy /Y "%geoserverdir%\services.xml" "%geoserverdir%\services.xml.bak"
	rem install\sed -e "s/180.0/%bblongmaxx%/" "%geoserverdir%/services.xml.bak" > "%geoserverdir%/services.xml"

	xcopy /Y "%osm2gisdir%\config\osm2gis.properties" "%osm2gisdir%\config\osm2gis.properties.bak"
	install\sed -e "s/osm2gis.importer.boundingboxlong.max=/osm2gis.importer.boundingboxlong.max=%bblongmaxx%/" "%osm2gisdir%/config/osm2gis.properties.bak" > "%osm2gisdir%/config/osm2gis.properties"
)

REM Ask after latitude min y
SET /P bblatminy=Set boundingbox latitude min y [-90.0]:
if "%bblatminy%"=="" SET bblatminy=-90.0
if NOT "%bblatminy%"=="" (
	rem xcopy /Y "%geoserverdir%\services.xml" "%geoserverdir%\services.xml.bak"
	rem install\sed -e "s/-90.0/%bblatminy%/" "%geoserverdir%/services.xml.bak" > "%geoserverdir%/services.xml"

	xcopy /Y "%osm2gisdir%\config\osm2gis.properties" "%osm2gisdir%\config\osm2gis.properties.bak"
	install\sed -e "s/osm2gis.importer.boundingboxlat.min=/osm2gis.importer.boundingboxlat.min=%bblatminy%/" "%osm2gisdir%/config/osm2gis.properties.bak" > "%osm2gisdir%/config/osm2gis.properties"
)

REM Ask after latitude max y
SET /P bblatmaxy=Set boundingbox latitude max y [90.0]:
if "%bblatmaxy%"=="" SET bblatmaxy=90.0
if NOT "%bblatmaxy%"=="" (
	rem xcopy /Y "%geoserverdir%\services.xml" "%geoserverdir%\services.xml.bak"
	rem install\sed -e "s/90.0/%bblatmaxy%/" "%geoserverdir%/services.xml.bak" > "%geoserverdir%/services.xml"

	xcopy /Y "%osm2gisdir%\config\osm2gis.properties" "%osm2gisdir%\config\osm2gis.properties.bak"
	install\sed -e "s/osm2gis.importer.boundingboxlat.max=/osm2gis.importer.boundingboxlat.max=%bblatmaxy%/" "%osm2gisdir%/config/osm2gis.properties.bak" > "%osm2gisdir%/config/osm2gis.properties"
)

REM set the geoserverlocation to osm2gis.properties
xcopy /Y "%osm2gisdir%\config\osm2gis.properties" "%osm2gisdir%\config\osm2gis.properties.bak"
SET tomcatdir=%tomcatdir:\=\\\\%
echo %tomcatdir%
install\sed -e "s/osm2gis.geoserver.location=/osm2gis.geoserver.location=%tomcatdir%\\\\geoserver/" "%osm2gisdir%/config/osm2gis.properties.bak" > "%osm2gisdir%/config/osm2gis.properties"

REM Starting the Apache Tomcat 6.0 Service
net start "Apache Tomcat 6"
echo You succesfully installed OSM-in-a-Box
echo "Don't forget to set you personalised information on the Geoserver, under configuration-->server"
echo Next step is to do an --initial-import with the osm2gis
echo if you have done this you can go to the url: http://localhost:8080/osm2gisdemo/ to view your map!
pause