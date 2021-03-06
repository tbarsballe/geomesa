/***********************************************************************
* Copyright (c) 2013-2016 Commonwealth Computer Research, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License, Version 2.0
* which accompanies this distribution and is available at
* http://www.opensource.org/licenses/apache2.0.php.
*************************************************************************/

package org.locationtech.geomesa.utils.stats

import org.opengis.feature.simple.SimpleFeature

/**
 * If the stats parser receives a string with multiple stats, a SeqStat will be used.
 *
 * @param stats a Sequence of individual Stat objects
 */
class SeqStat(val stats: Seq[Stat]) extends Stat {

  override type S = SeqStat

  override def observe(sf: SimpleFeature): Unit = stats.foreach(_.observe(sf))

  override def +(other: SeqStat): SeqStat =
    new SeqStat(stats.zip(other.stats).map { case (l, r) => l + r })

  override def +=(other: SeqStat): Unit = stats.zip(other.stats).foreach { case (l, r) => l += r }

  override def toJson: String = stats.map(_.toJson).mkString("[ ", ", ", " ]")

  override def isEmpty: Boolean = stats.forall(_.isEmpty)

  override def clear(): Unit = stats.foreach(_.clear())

  override def equals(other: Any): Boolean = other match {
    case that: SeqStat => stats == that.stats
    case _ => false
  }

  override def hashCode(): Int = stats.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
}
