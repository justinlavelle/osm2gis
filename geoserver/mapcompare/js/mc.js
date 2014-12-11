/*
 *  Geofabrik Tools
 *
 *  mc/mc.js
 *
 */

var layertypes = new Array();
var layertypes_hash = new Object();

var maps = new Array();
var map;
var layers = new Array();
var markersLayer = new Array();
var marker = new Array();

var moving = false;
var movestarted = false;

var proj4326  = new OpenLayers.Projection('EPSG:4326'), projmerc  = new OpenLayers.Projection('EPSG:900913');

jQuery(document).ready(function() {
    OpenLayers.Util.onImageLoadError = function() { this.src = 'img/404.png'; }

    setMapHeight();
    jQuery(window).resize(setMapHeight);

    initLayerTypes();

    var mt = ['osminaboxba', 'mapnik'];
    var lon = 8.31531; //-28;
    var lat = 46.80323; //43;
    var zoom = 8; //2;
    var x = null;
    var y = null;

    parseParams(function(param, v) {
        switch (param) {
            case 'type': mt[0] = v;           break;
            case 'mt0':  mt[0] = v;           break;
            case 'mt1':  mt[1] = v;           break;
            case 'lon':    lon = Number(v);   break;
            case 'lat':    lat = Number(v);   break;
            case 'zoom':  zoom = parseInt(v); break;
            case 'z':     zoom = parseInt(v); break;
            case 'x':        x = parseInt(v); break;
            case 'y':        y = parseInt(v); break;
        }
    });

    var pos = createMapPosition(lon, lat, x, y, zoom);

    OpenLayers.ImgPath = OpenLayers._getScriptLocation() + '../img/';
	
	// make OL compute scale according to WMS spec
	OpenLayers.DOTS_PER_INCH = 25.4 / 0.28; 

    for (var n=0; n <= 1; n++) {
        initSelectOptions(n, mt[n]);
        maps[n] = new OpenLayers.Map('map' + n, {
            theme: null,
            numZoomLevels:19,
            controls: [],
            projection: projmerc,
            displayProjection: proj4326,
			maxResolution: 156543.0339,
			maxExtent: new OpenLayers.Bounds(-20037508, -20037508, 20037508, 20037508.34)
        });
        maps[n].addControl(new OpenLayers.Control.Navigation());
        maps[n].addControl(new OpenLayers.Control.MousePosition({ div: jQuery('#customMousePosition').get(0) }));
        if (n == 0) {
            maps[n].addControl(new OpenLayers.Control.PanZoomBar());
        }
        newLayer(n, mt[n]);
        setStartPos(n, pos.getLonLat(), pos.zoom);
        initMarker(n);
        maps[n].events.register('movestart', n, moveStart);
        maps[n].events.register('moveend',   n, moveEnd);
        maps[n].events.register('mousemove', n, mouseMove);
        maps[n].events.register('mouseover', n, mouseOver);
        maps[n].events.register('mouseout',  n, mouseOut);
    }
    map = maps[0];

    jQuery('#customMousePosition').hide();
    updatePermalink();
});

function setMapHeight() {
    var height = jQuery(window).height() -
        jQuery('#maprights').height();
    jQuery('.map').height(height - 140);
}

function initLayerTypes() {
    layertypes = [
		new LayerType('osminaboxba', 		'OSM-in-a-Box BA',	      function() { return new OpenLayers.Layer.WMS(				'Geoserver Cached OSM-Data EPSG:900913 png', 'http://sinv-56029.edu.hsr.ch/geoserver/wms', { width: '820', srs: 'EPSG:4326', layers: 'osm', height: '385', styles: '', tiled: 'true', tilesOrigin: '-180,-90', format: 'image/png' }, {buffer: 0}); }),	
		new LayerType('osminaboxsa', 		'OSM-in-a-Box SA',	      function() { return new OpenLayers.Layer.WMS(				'Geoserver Cached OSM-Data EPSG:900913 png', 'http://sinv-56020.edu.hsr.ch/geoserver/wms', { width: '820', srs: 'EPSG:4326', layers: 'osm', height: '385', styles: '', tiled: 'true', tilesOrigin: '-180,-90', format: 'image/png' }, {buffer: 0}); }),
		new LayerType('localhost', 		'localhost:8080',	      function() { return new OpenLayers.Layer.WMS(				'Geoserver Cached OSM-Data EPSG:900913 png', 'http://localhost:8080/geoserver/wms', { width: '820', srs: 'EPSG:4326', layers: 'osm', height: '385', styles: '', tiled: 'true', tilesOrigin: '-180,-90', format: 'image/png' }, {buffer: 0}); }),
		new LayerType('mapnik',         'OSM Mapnik',         function() { return new OpenLayers.Layer.OSM.Mapnik(		'OSM Mapnik');  }),
        new LayerType('tah',            'OSM Tiles@Home',     function() { return new OpenLayers.Layer.OSM.Osmarender(	'OSM Tiles@Home', { numZoomLevels: 19 }); }),
        new LayerType('cyclemap',       'OSM CycleMap',       function() { return new OpenLayers.Layer.OSM.CycleMap(	'OSM CycleMap'); }),
        new LayerType('cl',             'OSM Coastline',      function() { return new OpenLayers.Layer.XYZ(				'OSM Coastline', 'http://a.hypercube.telascience.org/tiles/1.0.0/coastline/${z}/${x}/${y}.png', { sphericalMercator: true }); }),
        new LayerType('googlemap',      'Google (Map)',       function() { return new OpenLayers.Layer.Google(			'Google (Map)',       { sphericalMercator: false, projection: proj4326                              }); }),
        new LayerType('googlesat',      'Google (Satellite)', function() { return new OpenLayers.Layer.Google(			'Google (Satellite)', { sphericalMercator: false, projection: proj4326, type: G_SATELLITE_MAP       }); }),
        new LayerType('googlehybrid',   'Google (Hybrid)',    function() { return new OpenLayers.Layer.Google(			'Google (Hybrid)',    { sphericalMercator: false, projection: proj4326, type: G_HYBRID_MAP          }); }),
        new LayerType('googlephys',     'Google (Physical)',  function() { return new OpenLayers.Layer.Google(			'Google (Physical)',  { sphericalMercator: false, projection: proj4326, type: G_PHYSICAL_MAP        }); }),
        new LayerType('googlemapmaker', 'Google (Map Maker)', function() { return new OpenLayers.Layer.Google(			'Google (Map Maker)', { sphericalMercator: false, projection: proj4326, type: G_MAPMAKER_NORMAL_MAP }); })
    ];
}

function initMarker(n) {
    markersLayer[n] = new OpenLayers.Layer.Markers("Marker");
    maps[n].addLayer(markersLayer[n]);
    marker[n] = new OpenLayers.Marker(maps[n].getCenter(),
        new OpenLayers.Icon('img/cross.png', new OpenLayers.Size(20, 20), new OpenLayers.Pixel(-10, -10))
    );
    markersLayer[n].setVisibility(false);
    markersLayer[n].addMarker(marker[n]);
}

function moveStart() {
    movestarted = true;
    markersLayer[0].setVisibility(false);
    markersLayer[1].setVisibility(false);
    return(false);
}

function moveEnd() {
    if (moving) {
        return;
    }
    moving = true;
    maps[1-this].setCenter(
        maps[this].getCenter().clone().transform(maps[this].getProjectionObject(), maps[1-this].getProjectionObject()),
        maps[this].getZoom()
    );
    moving = false;
    updatePermalink();
    movestarted = false;
    markersLayer[1-this].setVisibility(true);
    return(false);
}

function mouseMove(evt) {
    marker[1-this].moveTo(maps[this].getLayerPxFromViewPortPx(evt.xy));
    return(false);
}

function mouseOver(evt) {
    if (! movestarted) {
        markersLayer[1-this].setVisibility(true);
    }
    jQuery('#customMousePosition').show();
    return(false);
}

function mouseOut(evt) {
    markersLayer[0].setVisibility(false);
    markersLayer[1].setVisibility(false);
    jQuery('#customMousePosition').hide();
    return(false);
}

function initSelectOptions(n, type) {
    var sw = jQuery('#sw' + n);
    for (var i = 0; i < layertypes.length; i++) {
        var l = layertypes[i];
        var opt = document.createElement('option');
        opt.value = l.type;
        opt.text  = l.name;
        opt.style.padding = '1px';
        if (l.type == type) { opt.selected = true; }
        sw[0].options[i] = opt;
    }
    sw.bind('change', n, changeLayer);
}

function setStartPos(n, lonlat, zoom) {
    var center = lonlat.clone();
    center.transform(proj4326, maps[n].getProjectionObject());
    maps[n].setCenter(center, zoom);
}

function updatePermalink() {
    var pos = getPosition();

    jQuery('#permalink')[0].href = '?mt0=' + layers[0].type + '&mt1=' + layers[1].type + '&lon=' + pos.lon + '&lat=' + pos.lat + '&zoom=' + pos.zoom;
    jQuery('#customZoomLevel').html('Zoom=' + maps[0].getZoom());
}

function LayerType(type, name, create) {
    this.type = type;
    this.name = name;
    this.create = create;

    layertypes_hash[type] = this;
}

function MapLayer(layertype) {
    var lt = layertypes_hash[layertype];
    this.layer = lt;
    this.type  = lt.type;
    this.name  = lt.name;
    this.obj   = lt.create();
}

function newLayer(map, layertype) {
    layers[map] = new MapLayer(layertype);
    maps[map].addLayer(layers[map].obj);
}

function changeLayer(event) {
    var map = event.data;

    var oldproj   = maps[map].getProjectionObject();
    var oldcenter = maps[map].getCenter().clone();
    var oldzoom   = maps[map].getZoom();

    maps[map].removeLayer(maps[map].baseLayer);
    newLayer(map, event.target.value);
    try {
        layers[map].obj.setMapType();
    } catch(e) {
    }

    maps[map].setCenter(oldcenter.transform(oldproj, maps[map].getProjectionObject()), oldzoom);
    updatePermalink();
}

