-- Generated DDL from xml2ddl (OSM-in-a-Box)
-- Generated on 2010.11.17 at 14:07:23 

CREATE TABLE gisentity (
	osm_id bigint NOT NULL,
	lastchange TIMESTAMP,
	type VARCHAR(255),
	name VARCHAR(255),
	keyvalue hstore
);

CREATE TABLE water (
	id serial,
	PRIMARY KEY (id)
) INHERITS (gisentity);

--Generated addGeometrySQL
Select AddGeometryColumn('water', 'geom', 4326, 'MULTIPOLYGON', 2);

--Generated Join-Tables
commit;

--Generated views
