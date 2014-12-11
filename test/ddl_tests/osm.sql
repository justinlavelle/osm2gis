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

CREATE TABLE landuse (
	id serial,
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE coastline (
	id serial,
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE building (
	id serial,
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE building_indoor (
	id serial,
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE indoor (
	id serial,
	description text,
	level VARCHAR(20),
	website VARCHAR(255),
	wikipedia VARCHAR(255),
	image_url VARCHAR(255),
	video_url VARCHAR(255),
	audio_url VARCHAR(255),
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE railway (
	id serial,
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE trainroute (
	id serial,
	ref VARCHAR(255),
	route VARCHAR(255),
	network VARCHAR(255),
	operator VARCHAR(255),
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE boundary (
	id serial,
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE boundary_multipolygon (
	id serial,
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE railwaystation (
	id serial,
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE pofw (
	id serial,
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE poi (
	id serial,
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE waterway (
	id serial,
	width VARCHAR(25),
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE road (
	id serial,
	ref VARCHAR(255),
	road VARCHAR(255),
	oneway VARCHAR(25),
	maxspeed VARCHAR(25),
	superposition smallint,
	layer smallint,
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE route (
	id serial,
	ref VARCHAR(255),
	route VARCHAR(255),
	network VARCHAR(255),
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE network (
	id serial,
	operator VARCHAR(255),
	PRIMARY KEY (id)
) INHERITS (gisentity);

CREATE TABLE place (
	id serial,
	population integer,
	PRIMARY KEY (id)
) INHERITS (gisentity);



CREATE INDEX "water_osm_id_idx" ON "water" USING btree ("osm_id");
CREATE INDEX "landuse_osm_id_idx" ON "landuse" USING btree ("osm_id");
CREATE INDEX "coastline_osm_id_idx" ON "coastline" USING btree ("osm_id");
CREATE INDEX "building_osm_id_idx" ON "building" USING btree ("osm_id");
CREATE INDEX "building_indoor_osm_id_idx" ON "building_indoor" USING btree ("osm_id");
CREATE INDEX "indoor_osm_id_idx" ON "indoor" USING btree ("osm_id");
CREATE INDEX "railway_osm_id_idx" ON "railway" USING btree ("osm_id");
CREATE INDEX "trainroute_osm_id_idx" ON "trainroute" USING btree ("osm_id");
CREATE INDEX "boundary_osm_id_idx" ON "boundary" USING btree ("osm_id");
CREATE INDEX "boundary_multipolygon_osm_id_idx" ON "boundary_multipolygon" USING btree ("osm_id");
CREATE INDEX "railwaystation_osm_id_idx" ON "railwaystation" USING btree ("osm_id");
CREATE INDEX "pofw_osm_id_idx" ON "pofw" USING btree ("osm_id");
CREATE INDEX "poi_osm_id_idx" ON "poi" USING btree ("osm_id");
CREATE INDEX "waterway_osm_id_idx" ON "waterway" USING btree ("osm_id");
CREATE INDEX "road_osm_id_idx" ON "road" USING btree ("osm_id");
CREATE INDEX "route_osm_id_idx" ON "route" USING btree ("osm_id");
CREATE INDEX "network_osm_id_idx" ON "network" USING btree ("osm_id");
CREATE INDEX "place_osm_id_idx" ON "place" USING btree ("osm_id");




CREATE INDEX "water_type_idx" ON "water" USING btree ("type");
CREATE INDEX "landuse_type_idx" ON "landuse" USING btree ("type");
CREATE INDEX "coastline_type_idx" ON "coastline" USING btree ("type");
CREATE INDEX "building_type_idx" ON "building" USING btree ("type");
CREATE INDEX "building_indoor_type_idx" ON "building_indoor" USING btree ("type");
CREATE INDEX "indoor_type_idx" ON "indoor" USING btree ("type");
CREATE INDEX "railway_type_idx" ON "railway" USING btree ("type");
CREATE INDEX "trainroute_type_idx" ON "trainroute" USING btree ("type");
CREATE INDEX "boundary_type_idx" ON "boundary" USING btree ("type");
CREATE INDEX "boundary_multipolygon_type_idx" ON "boundary_multipolygon" USING btree ("type");
CREATE INDEX "railwaystation_type_idx" ON "railwaystation" USING btree ("type");
CREATE INDEX "pofw_type_idx" ON "pofw" USING btree ("type");
CREATE INDEX "poi_type_idx" ON "poi" USING btree ("type");
CREATE INDEX "waterway_type_idx" ON "waterway" USING btree ("type");
CREATE INDEX "road_type_idx" ON "road" USING btree ("type");
CREATE INDEX "route_type_idx" ON "route" USING btree ("type");
CREATE INDEX "network_type_idx" ON "network" USING btree ("type");
CREATE INDEX "place_type_idx" ON "place" USING btree ("type");


--Generated addGeometrySQL
Select AddGeometryColumn('water', 'geom', 4326, 'MULTIPOLYGON', 2);
Select AddGeometryColumn('landuse', 'geom', 4326, 'MULTIPOLYGON', 2);
Select AddGeometryColumn('coastline', 'geom', 4326, 'LINESTRING', 2);
Select AddGeometryColumn('building', 'geom', 4326, 'LINESTRING', 2);
Select AddGeometryColumn('indoor', 'geom', 4326,'POINT',2);
Select AddGeometryColumn('railway', 'geom', 4326, 'LINESTRING', 2);
Select AddGeometryColumn('boundary', 'geom', 4326, 'LINESTRING', 2);
Select AddGeometryColumn('boundary_multipolygon', 'geom', 4326, 'MULTIPOLYGON', 2);
Select AddGeometryColumn('railwaystation', 'geom', 4326,'POINT',2);
Select AddGeometryColumn('pofw', 'geom', 4326,'POINT',2);
Select AddGeometryColumn('poi', 'geom', 4326,'POINT',2);
Select AddGeometryColumn('waterway', 'geom', 4326, 'LINESTRING', 2);
Select AddGeometryColumn('road', 'geom', 4326, 'LINESTRING', 2);
Select AddGeometryColumn('place', 'geom', 4326,'POINT',2);

--Generated Join-Tables
CREATE TABLE trainroute_to_railway (
	trainroute_id int REFERENCES trainroute ON DELETE CASCADE,
	railway_id int REFERENCES railway ON DELETE CASCADE,
	PRIMARY KEY (trainroute_id,railway_id)
);

CREATE TABLE trainroute_to_railwaystation (
	trainroute_id int REFERENCES trainroute ON DELETE CASCADE,
	railwaystation_id int REFERENCES railwaystation ON DELETE CASCADE,
	role VARCHAR(255),
	PRIMARY KEY (trainroute_id,railwaystation_id)
);

CREATE TABLE building_indoor_to_indoor (
	building_indoor_id int REFERENCES building_indoor ON DELETE CASCADE,
	indoor_id int REFERENCES indoor ON DELETE CASCADE,
	role VARCHAR(255),
	PRIMARY KEY (building_indoor_id,indoor_id)
);

CREATE TABLE route_to_road (
	route_id int REFERENCES route ON DELETE CASCADE,
	road_id int REFERENCES road ON DELETE CASCADE,
	role VARCHAR(255),
	PRIMARY KEY (route_id,road_id)
);

CREATE TABLE network_to_route (
	network_id int REFERENCES network ON DELETE CASCADE,
	route_id int REFERENCES route ON DELETE CASCADE,
	role VARCHAR(255),
	PRIMARY KEY (network_id,route_id)
);

CREATE TABLE network_to_trainroute (
	network_id int REFERENCES network ON DELETE CASCADE,
	trainroute_id int REFERENCES trainroute ON DELETE CASCADE,
	role VARCHAR(255),
	PRIMARY KEY (network_id,trainroute_id)
);

commit;

--Generated views


			
				CREATE VIEW landuse_lookup AS
					SELECT First_Select.osm_id, 
					   First_Select.name, 
					   First_Select.type, 
					   landuse.geom
					FROM landuse,
					(SELECT MAX(osm_id) AS osm_id, name, type 
					FROM landuse
					WHERE name!='' AND type = 'glacier'
					GROUP BY name, type) as First_Select
					WHERE First_Select.osm_id = landuse.osm_id;
					
					INSERT INTO geometry_columns(
								f_table_catalog, f_table_schema, f_table_name, f_geometry_column, coord_dimension, srid, "type")
					VALUES ('', 'public', 'landuse_lookup', 'geom', 2, 4326, 'MULTIPOLYGON');
			
		

			
				CREATE VIEW place_lookup AS
					SELECT CASE WHEN type= 'country' then 1
								WHEN type= 'city' then 2
								WHEN type= 'town' then 3
								WHEN type= 'suburb' then 4
							WHEN type= 'village' then 5
								WHEN type= 'state' then 6
								WHEN type= 'region' then 7
								WHEN type= 'hamlet' then 8
								ELSE 1000
							END AS index,
						osm_id, name, type, (keyvalue->'is_in') AS is_in, geom
					FROM place
					WHERE name != ''
					ORDER BY index;
					
					INSERT INTO geometry_columns(
								f_table_catalog, f_table_schema, f_table_name, f_geometry_column, coord_dimension, srid, "type")
					VALUES ('', 'public', 'place_lookup', 'geom', 2, 4326, 'POINT');
			
		

			
				CREATE VIEW pofw_lookup AS
					SELECT CASE WHEN type='christian' then 1
						WHEN type='christian_evangelical' then 2
						WHEN type='christian_catholic' then 3
						WHEN type='christan_protestant' then 4
						WHEN type='christian_orthodox' then 5
						WHEN type='christian_anglican' then 6
						when type='christian_lutheran' then 7
						WHEN type='christian_methodist' then 8
						WHEN type='muslim' then 9
						WHEN type='jewish' then 10
						WHEN type='place_of_worship' then 11
						ELSE 1000
						END AS index,
						osm_id, name, type, geom
					FROM pofw
					WHERE name!=''
					ORDER BY index;
					
					INSERT INTO geometry_columns(
								f_table_catalog, f_table_schema, f_table_name, f_geometry_column, coord_dimension, srid, "type")
					VALUES ('', 'public', 'pofw_lookup', 'geom', 2, 4326, 'POINT');
			
		

			
				CREATE VIEW poi_lookup AS
					SELECT CASE WHEN type='bus_stop' then 1
						WHEN type='terminal' then 2
						WHEN type='hospital' then 3
						WHEN type='viewpoint' then 4
						WHEN type='university' then 5
						WHEN type='school' then 6
						WHEN type='kindergarten' then 7
						WHEN type='parking' then 8
						WHEN type='restaurant' then 9
						WHEN type='fast_food' then 10
						WHEN type='cafe' then 11
						WHEN type='bank' then 12
						WHEN type='hpost_office' then 13
						WHEN type='cinema' then 14
						ELSE 1000
						END AS index,
						osm_id, name, type, (poi.keyvalue->'url') AS url, geom				
					FROM poi
					WHERE name!=''
					ORDER BY index;		

					INSERT INTO geometry_columns(
								f_table_catalog, f_table_schema, f_table_name, f_geometry_column, coord_dimension, srid, "type")
					VALUES ('', 'public', 'poi_lookup', 'geom', 2, 4326, 'POINT');					
			
		

			
				CREATE VIEW indoor_view AS
					SELECT 
				        CASE
				            WHEN indoor.type::text = 'indoor'::text THEN 1
				            ELSE 1000
				        END AS index, 
				        indoor.osm_id, indoor.lastchange, indoor.name, indoor.description, indoor.level, indoor.website, indoor.wikipedia, indoor.image_url, indoor.video_url, indoor.audio_url, indoor.type, indoor.geom, building_indoor.osm_id AS building_osm_id, building_indoor.name AS building_name
				  	FROM indoor
				  	LEFT JOIN building_indoor_to_indoor ON indoor.id=building_indoor_to_indoor.indoor_id
				  	LEFT JOIN building_indoor ON building_indoor_to_indoor.building_indoor_id=building_indoor.id
				  	WHERE indoor.name::text <> ''::text
				  	ORDER BY index;

					INSERT INTO geometry_columns(
								f_table_catalog, f_table_schema, f_table_name, f_geometry_column, coord_dimension, srid, "type")
					VALUES ('', 'public', 'indoor_view', 'geom', 2, 4326, 'POINT');					
			
		

			
				CREATE VIEW railwaystation_lookup AS
					SELECT CASE WHEN type='station' then 1
						WHEN type='tram_stop' then 2
						WHEN type='halt' then 3
						ELSE 1000
						END AS index,
						osm_id, name, type, geom
					FROM railwaystation
					WHERE name!=''
					ORDER BY index;
					
					INSERT INTO geometry_columns(
								f_table_catalog, f_table_schema, f_table_name, f_geometry_column, coord_dimension, srid, "type")
					VALUES ('', 'public', 'railwaystation_lookup', 'geom', 2, 4326, 'POINT');
			
		

			
				CREATE VIEW road_lookup AS
					SELECT First_Select.osm_id, 
					   First_Select.name, 
					   First_Select.type, 
					   road.geom 
					FROM road, 
					(SELECT CASE WHEN type='motorway' then 1 
							 WHEN type='motorway_link' then 1 
							 WHEN type='trunk' then 2 
							 WHEN type='trunk_link' then 2 
							 WHEN type='primary' then 3 
							 WHEN type='primary_link' then 3 
							 WHEN type='secondary' then 4 
							 WHEN type='tertiary' then 5 
							 WHEN type='road' then 6 
							 WHEN type='unclassified' then 7 
							 WHEN type='residential' then 8 
							 WHEN type='living_street' then 9 
							 WHEN type='service' then 10 
							 ELSE 1000 
									 END AS index, 
									 MAX(osm_id) AS osm_id, name, type
					FROM road 
					WHERE name!='' 
					GROUP BY name, type) as First_Select 
					WHERE First_Select.osm_id = road.osm_id 
					ORDER BY index;
					
					INSERT INTO geometry_columns(
								f_table_catalog, f_table_schema, f_table_name, f_geometry_column, coord_dimension, srid, "type")
					VALUES ('', 'public', 'road_lookup', 'geom', 2, 4326, 'LINESTRING');
			
		

			
				CREATE VIEW water_lookup AS
					SELECT First_Select.osm_id, 
					   First_Select.name, 
					   First_Select.type, 
					   water.geom
					FROM water,
						(SELECT case WHEN type='water' then 1
							 WHEN type='reservoir' then 2
							 ELSE 1000
							 END AS index,
							 MAX(osm_id) AS osm_id, name, type
					FROM water
					WHERE name!=''
					GROUP BY name, type) as First_Select
					WHERE First_Select.osm_id = water.osm_id 
					ORDER BY index;
					
					INSERT INTO geometry_columns(
								f_table_catalog, f_table_schema, f_table_name, f_geometry_column, coord_dimension, srid, "type")
					VALUES ('', 'public', 'water_lookup', 'geom', 2, 4326, 'MULTIPOLYGON');					
			
		

			
				CREATE VIEW waterway_lookup AS
					SELECT First_Select.osm_id, 
					   First_Select.name, 
					   First_Select.type, 
					   waterway.geom
					FROM waterway,
					(SELECT case WHEN type='river' then 1
							 WHEN type='stream' then 2
							 WHEN type='canal' then 3
							 WHEN type='drain' then 4
							 ELSE 1000
							 END AS index,
							 MAX(osm_id) AS osm_id, name, type
					FROM waterway
					WHERE name!=''
					GROUP BY name, type) as First_Select 
					WHERE First_Select.osm_id = waterway.osm_id 
					ORDER BY index;

					INSERT INTO geometry_columns(
								f_table_catalog, f_table_schema, f_table_name, f_geometry_column, coord_dimension, srid, "type")
					VALUES ('', 'public', 'waterway_lookup', 'geom', 2, 4326, 'LINESTRING');
			
		