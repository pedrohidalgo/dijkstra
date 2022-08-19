package com.pedrohidalgo.trafficflow.processor

import com.pedrohidalgo.trafficflow.{Avenue, Intersection, Street}

import scala.util.Try

object CommandLineProcessor {

  def getIntersectionFromInput(input: String): Option[Intersection] = {
    Try {
      val avenue = Avenue(input.toUpperCase.charAt(0))
      val street = Street(input.takeRight(input.length - 1).toInt)
      Intersection(avenue, street)
    }.toOption
  }

}
