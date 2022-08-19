package com.pedrohidalgo.trafficflow

final case class Avenue(value: Char) extends AnyVal

object Avenue {

  def getPrevious(currentAvenue: Avenue): Option[Avenue] = {
    if (currentAvenue.value <= 'A') {
      None
    } else {
      val newLetter = (currentAvenue.value - 1).toChar
      Some(Avenue(newLetter))
    }
  }

  def getNext(currentAvenue: Avenue, maxAvenue: Avenue): Option[Avenue] = {
    if (currentAvenue.value >= maxAvenue.value) {
      None
    } else {
      val newLetter = (currentAvenue.value + 1).toChar
      Some(Avenue(newLetter))
    }
  }
}

final case class Street(value: Int) extends AnyVal

object Street {

  def getPrevious(currentStreet: Street): Option[Street] = {
    if (currentStreet.value <= 1) {
      None
    } else {
      Some(Street(currentStreet.value - 1))
    }
  }

  def getNext(currentStreet: Street, lastStreet: Street): Option[Street] = {
    if (currentStreet.value >= lastStreet.value) {
      None
    } else {
      Some(Street(currentStreet.value + 1))
    }
  }
}

final case class Intersection(avenue: Avenue, street: Street)

object Intersection {
  def toDescriptiveString(intersection: Intersection): String =
    intersection.avenue.value.toString + intersection.street.value
}

final case class TransitTime(value: BigDecimal) extends AnyVal

final case class RoadSegment(
  startIntersection: Intersection,
  endIntersection: Intersection,
  transitTime: TransitTime)

final case class MeasurementTime(value: Int) extends AnyVal

final case class TrafficMeasurement(
  measurementTime: MeasurementTime,
  measurements: List[RoadSegment])

final case class Route(
  roadSegments: List[RoadSegment],
  totalTransitTime: TransitTime)

object Route {
  val empty: Route = Route(Nil, TransitTime(0))

  def plusRoadSegment(route: Route, newRoadSegment: RoadSegment): Route = {
    val newTotalTransitTime = TransitTime(route.totalTransitTime.value + newRoadSegment.transitTime.value)
    Route(roadSegments = route.roadSegments :+ newRoadSegment, newTotalTransitTime)
  }

  def getLastIntersection(route: Route): Intersection = {
    route.roadSegments.last.endIntersection
  }
}

final case class BestRoute(
  initialIntersection: Intersection,
  finalIntersection: Intersection,
  roadSegments: List[RoadSegment],
  totalTransitTime: TransitTime)

final case class Stack(routes: List[Route])

object Stack {
  val empty: Stack = Stack(Nil)
}

sealed trait ProcessorError

object ProcessorErrors {
  final case class IntersectionInvalid(intersection: Intersection) extends ProcessorError
  final case object NotFoundTrafficMeasurements extends ProcessorError

  // Could use Cats Show instead: https://typelevel.org/cats/typeclasses/show.html
  def showErrorDescription(error: ProcessorError): String = {
    error match {
      case IntersectionInvalid(intersection) => s"Intersection '${Intersection.toDescriptiveString(intersection)}' is invalid"
      case NotFoundTrafficMeasurements => "Not found Traffic Measurements"
    }
  }
}
