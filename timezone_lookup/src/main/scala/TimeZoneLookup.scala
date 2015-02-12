package com.foursquare.geo.quadtree

import com.vividsolutions.jts.geom.{Coordinate, Geometry}
import org.geotools.data.{DataStoreFactorySpi, FileDataStore}
import java.io.FileNotFoundException
import java.net.URL
import org.geotools.data.shapefile.ShapefileDataStoreFactory
import org.geotools.data.simple.{SimpleFeatureIterator, SimpleFeatureSource}
import org.geotools.feature.simple.SimpleFeatureTypeImpl
import scala.collection.mutable

object TimeZoneLookup {
  def loadResource(resourceName: String): URL = {
    Option(getClass.getClassLoader.getResource(resourceName)).getOrElse{
      throw new FileNotFoundException("Could not find " + resourceName +
        " resource.  Check the classpath/deps?")
    }
  }

  def loadShapefile(url: URL, keyAttribute: String): Map[String, Geometry] = {
    val dataStoreParams: java.util.Map[String, java.io.Serializable] = new java.util.HashMap[String, java.io.Serializable]()
    dataStoreParams.put(ShapefileDataStoreFactory.URLP.key, url)
    dataStoreParams.put(ShapefileDataStoreFactory.MEMORY_MAPPED.key, java.lang.Boolean.TRUE)
    dataStoreParams.put(ShapefileDataStoreFactory.CACHE_MEMORY_MAPS.key, java.lang.Boolean.FALSE)
    val dataStoreFactory: DataStoreFactorySpi = new ShapefileDataStoreFactory()
    val dataStore = dataStoreFactory.createDataStore(dataStoreParams).asInstanceOf[FileDataStore]
    val featureSource: SimpleFeatureSource = dataStore.getFeatureSource()

    // determine the key, index, attribute names, and the number and size of the index levels
    if (featureSource.getSchema.getDescriptor(keyAttribute) == null)
      throw new IllegalArgumentException("Schema has no attribute named \""+keyAttribute+"\"")

    // would love to do toScala here, but though it looks like an iterator
    // and quacks like an iterator, it is not a java iterator.
    val iterator: SimpleFeatureIterator = featureSource.getFeatures.features

    val shapeMap = new mutable.HashMap[String, Geometry]
    try {
      while (iterator.hasNext) {
        val feature = iterator.next()
        val sourceGeometry = feature.getDefaultGeometry().asInstanceOf[Geometry]
        val keyValueCopy = feature.getAttribute(keyAttribute).toString
        shapeMap(keyValueCopy) = sourceGeometry
      }
    }

    shapeMap.toMap
  }

  private lazy val tzShapefile = loadShapefile(loadResource("tz_simplified.shp"), "TZID")

  def getTimeZoneGeometry(tz: String): Option[Geometry] = tzShapefile.get(tz)
}
