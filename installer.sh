#!/bin/bash

#-----------------------------------------------------------
#Set osm2gis Directory, and copy files
#-----------------------------------------------------------
echo "Set osm2gis directory [/data/osm2gis]:"
read osm2gisdir
if [ -z $osm2gisdir ]
	then osm2gisdir="/data/osm2gis"
fi
if [ ! -d $osm2gisdir ]
	then mkdir -p $osm2gisdir
fi
unzip -o osm2gis_unix.zip -d $osm2gisdir
chmod +x $osm2gisdir/osm2gis.sh
#-----------------------------------------------------------
#-----------------------------------------------------------

#-----------------------------------------------------------
#Create temp folder in osm2gisdir
#-----------------------------------------------------------
mkdir -p $osm2gisdir/temp
#-----------------------------------------------------------
#-----------------------------------------------------------

#-----------------------------------------------------------
#Set Tomcat Directory, copy .war file and set the Geoserver Data_dir
#-----------------------------------------------------------
echo "Set tomcat directory [/opt/tomcat-6.0/webapps]:" 
read tomcatdir
if [ -z $tomcatdir ] 
	then tomcatdir="/opt/tomcat-6.0/webapps"
fi

cp -rf geoserver/geoserver.war $tomcatdir
# copy www folder to tomcat as osm2gisdemo
cp -rf  geoserver/www/ $tomcatdir/osm2gisdemo
# copy mapcompare folder to tomcat as mapcompare
cp -rf geoserver/mapcompare/ $tomcatdir/mapcompare


while [ ! -d "$tomcatdir/geoserver" ]
	do
		sleep 1
	done
rm -rf $tomcatdir/geoserver/data
echo "Set geoserver data directory [$tomcatdir/geoserver/data]:"
read geoserverdir
if [ -z $geoserverdir ]
	then geoserverdir="$tomcatdir/geoserver/data"
	else
		sed -i.bak "/description/a <context-param><param-name>GEOSERVER_DATA_DIR</param-name><param-value>$geoserverdir</param-value></context-param>" $tomcatdir/geoserver/WEB-INF/web.xml
fi
cp -rf geoserver/geoserver_data/* $geoserverdir 
chown -R tomcat6:tomcat6 $geoserverdir
#-----------------------------------------------------------
#-----------------------------------------------------------




#-----------------------------------------------------------
#Set Geoserver Administrator password
#-----------------------------------------------------------
echo "Set geoserver admin password [geoserver]:"
read geoserverpw
if [ ! -z $geoserverpw ]
	then sed -i.bak "s/admin=geoserver/admin=$geoserverpw/" $geoserverdir/security/users.properties
fi
#-----------------------------------------------------------
#-----------------------------------------------------------


#-----------------------------------------------------------
#Set DB-Informations
#-----------------------------------------------------------
echo "Set db name:"
read dbname
if [ ! -z $dbname ]
	then 
		sed -i.bak "s/database</$dbname</" $geoserverdir/workspaces/osm/osm_ds/datastore.xml
		sed -i.bak "s/db.name=osm/db.name=$dbname/" $osm2gisdir/config/osm2gis.properties
fi
echo "Set db user:"
read dbuser
if [ ! -z $dbuser ]
	then 
		sed -i.bak "s/user</$dbuser</" $geoserverdir/workspaces/osm/osm_ds/datastore.xml
		sed -i.bak "s/db.username=osm/db.username=$dbuser/" $osm2gisdir/config/osm2gis.properties
fi
echo "Set db password:"
read dbpw
if [ ! -z $dbpw ]
	then 
		sed -i.bak "s/passwd</$dbpw</" $geoserverdir/workspaces/osm/osm_ds/datastore.xml
		sed -i.bak "s/db.password=osm/db.password=$dbpw/" $osm2gisdir/config/osm2gis.properties
fi
#-----------------------------------------------------------
#-----------------------------------------------------------



#-----------------------------------------------------------
#Set boundingbox
#-----------------------------------------------------------
echo "Set boundingbox longitude min x [-180.0]:"
read bblongminx
if [ -z $bblongminx ]
	then bblongminx="-180.0"
fi
if [ ! -z $bblongminx ]
	then 
		sed -i.bak "s/osm2gis.importer.boundingboxlong.min=.*/osm2gis.importer.boundingboxlong.min=$bblongminx/g" $osm2gisdir/config/osm2gis.properties
fi

echo "Set boundingbox longitude max x [180.0]:"
read bblongmaxx
if [ -z $bblongmaxx ]
	then bblongmaxx="180.0"
fi
if [ ! -z $bblongmaxx ]
	then 
		sed -i.bak "s/osm2gis.importer.boundingboxlong.max=.*/osm2gis.importer.boundingboxlong.max=$bblongmaxx/g" $osm2gisdir/config/osm2gis.properties
fi

echo "Set boundingbox latitude min y [-90.0]:"
read bblatminy
if [ -z $bblatminy ]
	then bblatminy="-90.0"
fi
if [ ! -z $bblatminy ]
	then 
		sed -i.bak "s/osm2gis.importer.boundingboxlat.min=.*/osm2gis.importer.boundingboxlat.min=$bblatminy/g" $osm2gisdir/config/osm2gis.properties
fi

echo "Set boundingbox latitude max y [90.0]:"
read bblatmaxy
if [ -z $bblatmaxy ]
	then bblatmaxy="90.0"
fi
if [ ! -z $bblatmaxy ]
	then 
		sed -i.bak "s/osm2gis.importer.boundingboxlat.max=.*/osm2gis.importer.boundingboxlat.max=$bblatmaxy/g" $osm2gisdir/config/osm2gis.properties
fi
#-----------------------------------------------------------
#-----------------------------------------------------------

set tomcatgeodir = $tomcatdir/geoserver
sed -i.bak "s:osm2gis.geoserver.location=.*:osm2gis.geoserver.location=$tomcatgeodir:g" $osm2gisdir/config/osm2gis.properties


echo "You successfully installed OSM-in-a-Box"
echo "Don't forget to set you personalised information on the Geoserver, under configuration-->server"
echo "Next step is to do an --initial-import with the osm2gis"
echo "if you have done this you can go to the url: http://localhost:8080/osm2gisdemo/ to view your map!"
