/***********************************************************************
* Copyright (c) 2013-2016 Commonwealth Computer Research, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License, Version 2.0
* which accompanies this distribution and is available at
* http://www.opensource.org/licenses/apache2.0.php.
*************************************************************************/

package org.locationtech.geomesa.utils.geotools

import java.lang.{Boolean => jBoolean}

import org.geotools.data.{DataUtilities, FeatureWriter}
import org.geotools.factory.Hints
import org.geotools.feature.simple.SimpleFeatureTypeBuilder
import org.geotools.filter.identity.FeatureIdImpl
import org.opengis.feature.simple.{SimpleFeature, SimpleFeatureType}

/** Utilities for re-typing and re-building [[SimpleFeatureType]]s and [[SimpleFeature]]s while
  * preserving user data which the standard Geo Tools utilities do not do.
  */
object FeatureUtils {

  /** Retypes a [[SimpleFeatureType]], preserving the user data.
   *
   * @param orig the original
   * @param propertyNames the property names for the new type
   * @return the new [[SimpleFeatureType]]
   */
  def retype(orig: SimpleFeatureType, propertyNames: Array[String]): SimpleFeatureType = {
    val mod = SimpleFeatureTypeBuilder.retype(orig, propertyNames)
    mod.getUserData.putAll(orig.getUserData)
    mod
  }

  /** Retypes a [[SimpleFeature]], preserving the user data.
    *
    * @param orig the source feature
    * @param targetType the target type
    * @return the new [[SimpleFeature]]
    */
  def retype(orig: SimpleFeature, targetType: SimpleFeatureType): SimpleFeature = {
    val mod = DataUtilities.reType(targetType, orig, false)
    mod.getUserData.putAll(orig.getUserData)
    mod
  }

  /** A new [[SimpleFeatureType]] builder initialized with ``orig`` which, when ``buildFeatureType`` is
    * called will, as a last step, add all user data from ``orig`` to the newly built [[SimpleFeatureType]].
    *
    * @param orig the source feature
    * @return a new [[SimpleFeatureTypeBuilder]]
    */
  def builder(orig: SimpleFeatureType): SimpleFeatureTypeBuilder = {
    val builder = new SimpleFeatureTypeBuilder() {

      override def buildFeatureType(): SimpleFeatureType = {
        val result = super.buildFeatureType()
        result.getUserData.putAll(orig.getUserData)
        result
      }
    }

    builder.init(orig)
    builder
  }

  def copyToWriter(writer: FeatureWriter[SimpleFeatureType, SimpleFeature],
                   sf: SimpleFeature,
                   overrideFid: Boolean = false): SimpleFeature = {
    val toWrite = writer.next()
    toWrite.setAttributes(sf.getAttributes)
    toWrite.getUserData.putAll(sf.getUserData)
    if (overrideFid || jBoolean.TRUE == sf.getUserData.get(Hints.USE_PROVIDED_FID).asInstanceOf[jBoolean]) {
      toWrite.getIdentifier.asInstanceOf[FeatureIdImpl].setID(sf.getID)
      toWrite.getUserData.put(Hints.USE_PROVIDED_FID, java.lang.Boolean.TRUE)
    }
    toWrite
  }
}
