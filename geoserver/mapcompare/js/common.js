/*
 *  Geofabrik Tools
 *
 *  js/common.js
 *
 */

var mlon = null, mlat = null;

function parseParams(handler) {
    var perma = location.search.substr(1);
    if (perma != '') {
        paras = perma.split('&');
        for (var i = 0; i < paras.length; i++) {
            var p = paras[i].split('=');
            handler(p[0], p[1]);
        }
    }
}

/* ================================================== */

var available_tools = [
    { id: 'map',  name: 'Map',           loc: true, marker: true },
    { id: 'mc',   name: 'Map Compare',   loc: true, marker: false },
    { id: 'osmi', name: 'OSM Inspector', loc: true, marker: true }
];

function createNewOption(value, text, selected) {
    var option = document.createElement('option');
    jQuery(option).html(text);
    option.value = value;
    option.selected = selected;
    return option;
}

function chooseTool() {
    var chosen_tool = jQuery.grep(available_tools, function(el, i) {
        return el['id'] == jQuery('#tools-switcher').val();
    })[0];
    var link = '/' + chosen_tool['id'] + '/';
    if (chosen_tool['loc']) {
        var pos = getPosition();
        link += '?lon=' + pos.lon + '&lat=' + pos.lat + '&zoom=' + pos.zoom;
        if (chosen_tool['marker'] && mlon && mlat) {
            link += '&mlon=' + mlon + '&mlat=' + mlat;
        }
    }
    location.href = link;
}

function getPosition() {
    var center = map.getCenter().clone().transform(map.getProjectionObject(), proj4326);
    return new MapPosition(
        Math.round(center.lon * 100000) / 100000,
        Math.round(center.lat * 100000) / 100000,
        map.getZoom()
    );
}

/* ================================================== */

jQuery(document).ready(function() {
    jQuery('#tools-switcher').bind('change', chooseTool);
    jQuery('#tools-helpwin').jqm({
        ajax: 'help.html',
        trigger: 'a#tools-helptrigger',
        overlay: 0,
        onLoad: function(hash) { hash.w.jqmAddClose('.dialog-close'); }
    });
});

/* ================================================== */

function MapPosition(lon, lat, zoom) {
    this.lon = lon;
    this.lat = lat;
    this.zoom = zoom;
}

MapPosition.prototype.getLonLat = function() {
    return new OpenLayers.LonLat(this.lon, this.lat);
}

MapPosition.prototype.tileX = function() {
	if ((this.zoom < 3) || (this.zoom > 18)) {
        return 0;
    }
    return Math.round((1<<(this.zoom-3)) * (this.lon + 180.0) / 45.0);
}

MapPosition.prototype.tileY = function() {
    if ((this.zoom < 3) || (this.zoom > 18)) {
        return 0;
    }
    var l = this.lat / 180 * Math.PI;
    var pf = Math.log(Math.tan(l) + (1/Math.cos(l)));
    return Math.round((1<<(this.zoom-1)) * (Math.PI - pf) / Math.PI);
}

function createMapPositionFromTiles(x, y, zoom) {
    var lon;
    var lat;

    if ((zoom < 3) || (zoom > 18)) {
        lon = 0;
    } else {
        lon = (x+0.5) * 45.0 / (1<<(zoom-3)) - 180.0;
    }

    if ((zoom < 3) || (zoom > 18)) {
        lat = 0;
    } else {
        lat = Math.atan(sinh(Math.PI - (Math.PI*(y+0.5) / (1<<(zoom-1))))) * 180 / Math.PI;
    }

    return new MapPosition(lon, lat, zoom);
}

function sinh(x) {
    return (Math.exp(x)-Math.exp(-x))/2;
}

function createMapPosition(lon, lat, x, y, zoom) {
    if (x != null && y != null) {
        return createMapPositionFromTiles(x, y, zoom);
    } else if (lon != null && lat != null) {
        return new MapPosition(lon, lat, zoom);
    } else {
        return new MapPosition(0, 0, zoom);
    }
}

/* ================================================== */

function getTileURL(bounds) {
    var res = this.map.getResolution();
    var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
    var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
    var z = this.map.getZoom();
    var limit = Math.pow(2, z);

    if (y < 0 || y >= limit) {
        return '../img/404.png';
    } else {
        x = ((x % limit) + limit) % limit;
        return this.url + z + "/" + x + "/" + y + "." + this.type;
    }
}

/* ================================================== */
