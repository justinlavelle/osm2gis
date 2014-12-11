// JavaScript Document

$(document).ready(function(){
	
	// download current Version
	$.ajax({
	  url: "state.txt",
	  context: document.body,
	  success: function(data){
		res = JSON.parse(data);
		$("#lastUpdate").html("Map data until "+res.time+" | ");
	  }
	});
	var offset_map;
	var offset_menu;
						   
	/*@cc_on
   		/*@if (@_jscript_version >= 5)
      		offset_map = 33;
			offset_menu = 99;
			offset_copyright = 55;
			offset_maprights = 18;
   		@else @*/
      		offset_map = 63;
			offset_menu = 99;
			offset_copyright = 55;
			offset_maprights = 18;

   /*@end
	@*/
	
	//calculate map and search menu height
	$("#map").height(($(window).height() - offset_map));
	$("#sliderContent").height(($(window).height() - offset_menu));
	$("#resultset").height(($(window).height() - offset_menu));
	$("#copyright").css('top', $(window).height() + offset_copyright);
	$("#maprights").css('top', $(window).height() - offset_maprights);
	$("#results").html("<div class='noresult'>No search results.</div>");
	
	//on browser resizes, adjust the map search menu height
	$(window).bind("resize", resizeWindow);
	function resizeWindow( e ) {
			var newWindowHeight = $(window).height();
			$("#map").height(($(window).height() - offset_map));
			$("#sliderContent").height(($(window).height() - offset_menu));
			$("#resultset").height(($(window).height() - offset_menu));
			$("#copyright").css('top', $(window).height() + offset_copyright);
			$("#maprights").css('top', $(window).height() - offset_maprights);
	}
	
	//change css style of input field on focus
	$("#filter").focus(function() {
			$(this).attr('class', 'input_focus');
	});
	
	//change css style of input field on blur
	$("#filter").blur(function() {
			$(this).attr('class', 'input');
	});
	
	//on click behavior side menu button -> slide in and out
	$("#sb").click(function(){
		if ($("#sb").attr('src') == 'img/btn_sb_open.png') {
			$("#slider").animate({ marginLeft: "227px" }, 300);
			$("#sb").attr('src', 'img/btn_sb_close.png');
			$("#openCloseIdentifier").show();
			setTimeout(function() {map.getControl('panzoombar').moveTo(new OpenLayers.Pixel(230, 15)); }, 300);
			setTimeout(function() { $(".olControlScaleLine").css('left', 250); }, 300);
		} else {
			$("#slider").animate({ marginLeft: "0px" }, 200);
			$("#sb").attr('src', 'img/btn_sb_open.png');
			$("#openCloseIdentifier").hide();
			map.getControl('panzoombar').moveTo(new OpenLayers.Pixel(9, 15));
			$(".olControlScaleLine").css('left', 10);
			
		}
	});
	
	//on click behavior of search button -> make search request and show search results
	$("#search_btn").click(function(){
		if ($('#filter').val() != "") {
		search_request();
		if ($("#sb").attr('src') == 'img/btn_sb_open.png') {
			$("#slider").animate({ marginLeft: "227px" }, 0);
			$("#sb").attr('src', 'img/btn_sb_close.png');
			$("#openCloseIdentifier").show();
			map.getControl('panzoombar').moveTo(new OpenLayers.Pixel(230, 15));
			setTimeout(function() { $(".olControlScaleLine").css('left', 250); }, 300);
		}
		}
	});
	
	//trigger search button on ENTER
	$(document).keyup(function(event){
    if (event.keyCode == 13) {
        $("#search_btn").click();
    }
	});	
	
});

function search_request() {
	var url = "../geoserver/wfs";
	var keyword = $("#filter").val();
	
	// Please comment out the following code line if running on windows server!!
	keyword = $.URLEncode(keyword);

	var query_request = "<Filter><Or>" + create_islikequery(keyword) + create_islikequery(keyword + ' *') + create_islikequery(keyword + '-*') + create_islikequery('* ' + keyword) + create_islikequery('*-' + keyword) + create_islikequery('* ' + keyword + ' *') + create_islikequery('* ' + keyword + '-*') + create_islikequery('*-' + keyword + ' *') + create_islikequery('*-' + keyword + '-*') + "</Or></Filter>";

	$("#results").html("");
	$("#loading").html('<img src="img/loading.gif"/>');
	
	var types = new Array("place_lookup", "waterway_lookup", "water_lookup", "landuse_lookup", "railwaystation_lookup", "poi_lookup", "pofw_lookup", "road_lookup");
	
	//asynchronous call of getfeature request for every table to search in
	var n = 0;
	jQuery.whileAsync({
        delay: 1000,
        bulk: 0,
        test: function() { return n < types.length },
        loop: function()
        { 
			$.ajax({
  				type: "GET",
  				url: url,
				data: "request=getfeature&service=wfs&version=1.1.0&typename=osm:" + types[n] + "&outputformat=json&filter=" + query_request,
  				dataType: "json",
				scriptCharset: "iso-8859-1",
				success: function(jsonobj){
     				display_res(jsonobj);;
   				}
			});
			n++
        },
        end: function()
        { 
                $("#loading").html("");
				if ($("#results").html() == "") $("#results").html("<div class='noresult'>No search results.</div>");
        }
	})
	
	
	
}

function create_islikequery(like) {
	return query = '<PropertyIsLike wildCard="*" singleChar="." escape="!" matchCase="false"><PropertyName>name</PropertyName><Literal>' + like + '</Literal></PropertyIsLike>';
}

function display_res(data) {
	
	for (i = 0; i < data.features.length; i++) {
		var f_typ = data.features[i].properties.type;
		var f_name = data.features[i].properties.name;
		var f_tabname = data.features[i].id.split('.')[0];
		f_tabname = f_tabname.split('_')[0];
		var f_info = "";
		var f_geomtype = data.features[i].geometry.type;
		var lon = data.features[i].geometry.coordinates[1];
		var lat = data.features[i].geometry.coordinates[0];
		var zoom = getzoom(f_tabname, f_typ);
			
		var ico = "";
		ico = "<img src='img/" + f_typ + ".png' align='absmiddle' onerror='this.src=&quot;img/" + f_tabname + ".png&quot;;'/> ";

		if (f_geomtype == "LineString") {
			lon = data.features[i].geometry.coordinates[0][1];
			lat = data.features[i].geometry.coordinates[0][0];
		}
		if (f_geomtype == "MultiPolygon") {
			var coordinates = data.features[i].geometry.coordinates[0][0];
			lon = coordinates[0][1];
			lat = coordinates[0][0];
		}
		
		if (f_tabname == "poi") {
			if (data.features[i].properties.url != null) {
				var poi_url = data.features[i].properties.url;
				if (poi_url.split('.')[0] == 'www') poi_url = "http://" + poi_url;
				f_info = "<div><a href='" + poi_url + "' target='_blank' class='resulturl'>" + poi_url + "</a></div>";
			}
		}
		
		if (f_tabname == "place") {
			if (data.features[i].properties.is_in != null) {
				var info = data.features[i].properties.is_in;
				var formatstr = info.replace(/,/g, ", ");
				f_info = "<div class='resultinfo'>Is in: " + formatstr + "</div>";
			}
		}
		
		$("#results").append("<div class='resultitem'><div><span onClick='setMyCenterMarker(" + lon + "," + lat + "," + zoom + ");'>" + f_name + "</span></div></ br></ br>" + ico + f_typ + f_info + "</div>");
			
	}
}

//get zoom depending on feature type
function getzoom(f_tabname, f_typ) {
	var zoom;
	switch(f_tabname) {
			case "place": switch(f_typ) {
							case "country": zoom = 9;
										break;
							case "state": zoom = 10;
										break;
							case "region": zoom = 10;
										break;
							case "city": zoom = 12;
										break;
							case "town": zoom = 14;
										break;
							case "suburb": zoom = 14;
										break;
							case "village": zoom = 15;
										break;
							case "hamlet": zoom = 16;
										break;
							default: zoom = 12;
						}
						break;
			case "road": switch(f_typ) {
							case "motorway": zoom = 12;
										break;
							case "primary": zoom = 14;
										break;
							case "secondary": zoom = 15;
										break;
							case "tertiary": zoom = 16;
										break;
							default: zoom = 17;
						}
						break;
			case "water": zoom = 13;
						break;
			case "waterway": zoom = 14;
						break;
			case "railwaystation": zoom = 16;
						break;
			case "poi": zoom = 17;
						break;
			default: zoom = 10;
		}
		return zoom;	
}