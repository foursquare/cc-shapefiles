cc-shapefiles
=============

These are utilities for performing in-memory geometry computations on countries and timezones.

There are three libraries that are pushed to maven.

timezone_revgeo
---------------
@ http://mvnrepository.com/artifact/com.foursquare/timezone_revgeo_2.10

given a lat,lng - return the olson timezone that contains it. based on http://efele.net/maps/tz/world/

usage: 

    scala> com.foursquare.geo.quadtree.TimeZoneRevGeo.getNearestTZ(40.74, -74.0)
    res1: Option[String] = Some(America/New_York)
    
    
country_revgeo
---------------
@ http://mvnrepository.com/artifact/com.foursquare/country_revgeo_2.10

given a lat,lng - return the country that contains it. Currently assigns golan heights to syria, need to fix that. Based on quattroshapes countries.

usage: 

    scala> com.foursquare.geo.quadtree.CountryRevGeo.getNearestCountryCode(40.74, -74.0)
    res3: Option[String] = Some(US)
    

timezone_lookup
---------------
@ https://oss.sonatype.org/content/repositories/releases/com/foursquare/timezone_lookup_2.10/ - coming soon to maven

given an olson timezone, return the geometry of the timezone polygon. based on http://efele.net/maps/tz/world/.
    
    scala> com.foursquare.geo.quadtree.TimeZoneLookup.getTimeZoneGeometry("America/New_York")
    res0: Option[com.vividsolutions.jts.geom.Geometry] = Some(MULTIPOLYGON (((-75.10046386615377 19.89988803775289, -75.08932966584179 19.60234481426834, -75.18104879659076 19.60234481357291, -75.23610687355463 19.889681816012217, -75.22768020652941 19.8909950266109, -75.22363662751913 19.89228725528046, -75.21430969648974 19.895650863229406, -75.24825286868851 19.90054607290844, -75.24827957146195 19.90054416556748, -75.24827957248903 19.900544166858886, -75.23229217629297 19.903871537068184, -75.23229217629297 19.904035568219594, -75.2323226938709 19.904897689838798, -75.23230743508203 19.905681610116662, -75.23
