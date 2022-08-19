package com.pedrohidalgo.trafficflow.processor

import com.pedrohidalgo.trafficflow.ProcessorErrors.{IntersectionInvalid, NotFoundTrafficMeasurements}
import com.pedrohidalgo.trafficflow.{Avenue, BestRoute, Intersection, ProcessorError, RoadSegment, Route, Stack, Street, TrafficMeasurement, TransitTime}

import scala.annotation.tailrec

object TrafficProcessor {

  val sortRoutesByLessTransitTime: (Route, Route) => Boolean = {
    (route1: Route, route2: Route) => route1.totalTransitTime.value < route2.totalTransitTime.value
  }

  def generateBestRoute(
    findTrafficMeasurements: String => List[TrafficMeasurement],
    findBestRoute: (List[TrafficMeasurement], Intersection, Intersection) => Either[ProcessorError, BestRoute]): (
    String, Intersection, Intersection) => Either[ProcessorError, BestRoute] = {
    (absolutePath: String, initialIntersection: Intersection, finalIntersection: Intersection) =>
      val trafficMeasurements = findTrafficMeasurements(absolutePath)
      trafficMeasurements match {
        case Nil => Left(NotFoundTrafficMeasurements)
        case _ => findBestRoute(trafficMeasurements, initialIntersection, finalIntersection)
      }
  }

  def findBestRoute(
    trafficMeasurements: List[TrafficMeasurement],
    initialIntersection: Intersection,
    finalIntersection: Intersection): Either[ProcessorError, BestRoute] = {
    val lastAvenue = findLastAvenue(trafficMeasurements)
    val lastStreet = findLastStreet(trafficMeasurements)

    val isValidInitialIntersection = isValidIntersection(initialIntersection, lastAvenue, lastStreet)
    val isValidFinalIntersection = isValidIntersection(finalIntersection, lastAvenue, lastStreet)

    (isValidInitialIntersection, isValidFinalIntersection) match {
      case (false, _) => Left(IntersectionInvalid(initialIntersection))
      case (_, false) => Left(IntersectionInvalid(finalIntersection))
      case _ =>
        val defaultTransitTime = findDefaultTransitTime(trafficMeasurements)

        val route = processMoves(
          currentIntersection = initialIntersection,
          currentRoute = Route.empty,
          currentStack = Stack.empty,
          visitedIntersections = Set.empty,
          trafficMeasurements,
          defaultTransitTime,
          finalIntersection,
          lastAvenue,
          lastStreet)

        Right(BestRoute(initialIntersection, finalIntersection, route.roadSegments, route.totalTransitTime))
    }

  }

  /**
   * Implemented the Dijkstra's algorithm
   */
  @tailrec
  private[processor] def processMoves(
    currentIntersection: Intersection,
    currentRoute: Route,
    currentStack: Stack,
    visitedIntersections: Set[Intersection],
    trafficMeasurements: List[TrafficMeasurement],
    defaultTransitTime: TransitTime,
    finalIntersection: Intersection,
    lastAvenue: Avenue,
    lastStreet: Street): Route = {
    val updatedVisitedIntersections = visitedIntersections + currentIntersection

    val possibleMoves = findPossibleNextIntersectionsToMove(
      updatedVisitedIntersections, currentIntersection, lastAvenue, lastStreet)

    val routesToBeAdded: List[Route] = possibleMoves.map(newIntersection => {
      val transitTime = findAverageTransitTimeBetweenTwoContiguousIntersections(
        trafficMeasurements, currentIntersection, newIntersection, defaultTransitTime)

      val newRoadSegment = RoadSegment(currentIntersection, newIntersection, transitTime)
      Route.plusRoadSegment(currentRoute, newRoadSegment)
    })

    val allRoutes = (currentStack.routes ++ routesToBeAdded).sortWith(sortRoutesByLessTransitTime)
    val shortestRoute = allRoutes.head

    val newIntersection = Route.getLastIntersection(shortestRoute)
    if (newIntersection == finalIntersection) {
      shortestRoute
    } else {
      processMoves(
        currentIntersection = newIntersection,
        currentRoute = shortestRoute,
        currentStack = Stack(allRoutes.tail),
        visitedIntersections = updatedVisitedIntersections,
        trafficMeasurements,
        defaultTransitTime,
        finalIntersection,
        lastAvenue,
        lastStreet)
    }
  }

  private[processor] def findPossibleNextIntersectionsToMove(
    visitedIntersections: Set[Intersection],
    current: Intersection,
    lastAvenue: Avenue,
    lastStreet: Street): List[Intersection] = {

    List(
      moveLeft(current),
      moveRight(current, lastAvenue),
      moveUp(current),
      moveDown(current, lastStreet))
      .flatten
      .filterNot(visitedIntersections.contains)
  }

  private[processor] def moveLeft(currentIntersection: Intersection): Option[Intersection] = {
    Avenue.getPrevious(currentIntersection.avenue) match {
      case Some(nextAvenue) => Some(Intersection(nextAvenue, currentIntersection.street))
      case None => None
    }
  }

  private[processor] def moveRight(currentIntersection: Intersection, maxAvenue: Avenue): Option[Intersection] = {
    Avenue.getNext(currentIntersection.avenue, maxAvenue) match {
      case Some(nextAvenue) => Some(Intersection(nextAvenue, currentIntersection.street))
      case None => None
    }
  }

  private[processor] def moveUp(currentIntersection: Intersection): Option[Intersection] = {
    Street.getPrevious(currentIntersection.street) match {
      case Some(previousStreet) => Some(Intersection(currentIntersection.avenue, previousStreet))
      case None => None
    }
  }

  private[processor] def moveDown(currentIntersection: Intersection, maxStreet: Street): Option[Intersection] = {
    Street.getNext(currentIntersection.street, maxStreet) match {
      case Some(nextStreet) => Some(Intersection(currentIntersection.avenue, nextStreet))
      case None => None
    }
  }

  private[processor] def findAverageTransitTimeBetweenTwoContiguousIntersections(
    trafficMeasurements: List[TrafficMeasurement],
    startIntersection: Intersection,
    endIntersection: Intersection,
    defaultTransitTime: TransitTime): TransitTime = {
    val roadSegments = trafficMeasurements
      .flatMap(_.measurements)
      .filter(roadSegment => {
        roadSegment.startIntersection == startIntersection && roadSegment.endIntersection == endIntersection
      })

    if (roadSegments.isEmpty) {
      defaultTransitTime // the default transit time is used when there is no measurements for a road segment
    } else {
      TransitTime(roadSegments.map(_.transitTime.value).sum / roadSegments.length)
    }
  }

  private[processor] def findDefaultTransitTime(trafficMeasurements: List[TrafficMeasurement]): TransitTime = {
    val allTransitTimes = trafficMeasurements
      .flatMap(_.measurements.map(m => m.transitTime.value))

    val avg = allTransitTimes.sum / allTransitTimes.length
    TransitTime(avg)
  }

  private[processor] def findLastAvenue(trafficMeasurements: List[TrafficMeasurement]): Avenue = {
    val maxChar = trafficMeasurements
      .flatMap(_.measurements.flatMap(m => List(m.startIntersection.avenue.value, m.endIntersection.avenue.value)))
      .max

    Avenue(maxChar)
  }

  private[processor] def findLastStreet(trafficMeasurements: List[TrafficMeasurement]): Street = {
    val maxInt = trafficMeasurements
      .flatMap(_.measurements.flatMap(m => List(m.startIntersection.street.value, m.endIntersection.street.value)))
      .max

    Street(maxInt)
  }

  private[processor] def isValidIntersection(
    initialIntersection: Intersection,
    lastAvenue: Avenue,
    lastStreet: Street): Boolean = {
    if (initialIntersection.avenue.value < 1
      || initialIntersection.street.value < 1
      || initialIntersection.avenue.value > lastAvenue.value
      || initialIntersection.street.value > lastStreet.value) {
      false
    } else {
      true
    }
  }

}
