package com.pedrohidalgo.trafficflow.processor

import com.pedrohidalgo.trafficflow.ProcessorErrors.NotFoundTrafficMeasurements
import com.pedrohidalgo.trafficflow._

class TrafficProcessorSpec extends BaseSpec {

  "Traffic Processor" should {
    val defaultTrafficMeasurements = List(
      TrafficMeasurement(
        measurementTime = MeasurementTime(80000),
        measurements = List(
          RoadSegment(
            Intersection(Avenue('A'), Street(1)),
            Intersection(Avenue('B'), Street(1)),
            transitTime = TransitTime(20)))),
      TrafficMeasurement(
        measurementTime = MeasurementTime(80000),
        measurements = List(
          RoadSegment(
            Intersection(Avenue('D'), Street(1)),
            Intersection(Avenue('E'), Street(1)),
            transitTime = TransitTime(40)))),
      TrafficMeasurement(
        measurementTime = MeasurementTime(80000),
        measurements = List(
          RoadSegment(
            Intersection(Avenue('X'), Street(1)),
            Intersection(Avenue('Y'), Street(1)),
            transitTime = TransitTime(30)))))

    "implement generateBestRoute" which {

      "does not return a route when there are no traffic measurements" in {
        val absolutePath = "some path"
        val initialIntersection = Intersection(Avenue('A'), Street(1))
        val finalIntersection = Intersection(Avenue('B'), Street(1))

        val findTrafficMeasurements = (_: String) => Nil

        val findBestRoute = (_: List[TrafficMeasurement], _: Intersection, _: Intersection) => {
          fail("should not be called")
        }

        TrafficProcessor.generateBestRoute(findTrafficMeasurements, findBestRoute)(
          absolutePath, initialIntersection, finalIntersection) shouldEqual Left(NotFoundTrafficMeasurements)
      }

      "returns the traffic measurements" in {
        val absolutePath = "some path"
        val initialIntersection = Intersection(Avenue('A'), Street(1))
        val finalIntersection = Intersection(Avenue('B'), Street(1))

        val expectedBaseRoute = BestRoute(
          initialIntersection,
          finalIntersection,
          List(
            RoadSegment(
              Intersection(Avenue('D'), Street(1)),
              Intersection(Avenue('E'), Street(1)),
              transitTime = TransitTime(40))),
          TransitTime(30.343))

        val findTrafficMeasurements = (_: String) => {
          defaultTrafficMeasurements
        }

        val findBestRoute = (_: List[TrafficMeasurement], _: Intersection, _: Intersection) => {
          Right(expectedBaseRoute)
        }

        TrafficProcessor.generateBestRoute(findTrafficMeasurements, findBestRoute)(
          absolutePath, initialIntersection, finalIntersection) shouldEqual Right(expectedBaseRoute)
      }
    }

    "implement findAverageTransitTimeBetweenTwoContiguousIntersections" which {

      "returns the default transit time when there is no road segments data available" in {
        val startIntersection = Intersection(Avenue('A'), Street(1))
        val endIntersection = Intersection(Avenue('B'), Street(1))
        val defaultTransitTime = TransitTime(10)
        val trafficMeasurements = Nil

        TrafficProcessor.findAverageTransitTimeBetweenTwoContiguousIntersections(
          trafficMeasurements, startIntersection, endIntersection, defaultTransitTime) shouldEqual defaultTransitTime
      }

      "find average transit time" in {
        val startIntersection = Intersection(Avenue('A'), Street(1))
        val endIntersection = Intersection(Avenue('B'), Street(1))
        val defaultTransitTime = TransitTime(10)
        val trafficMeasurements = List(
          TrafficMeasurement(
            measurementTime = MeasurementTime(80000),
            measurements = List(
              RoadSegment(
                startIntersection,
                endIntersection,
                transitTime = TransitTime(20)))),
          TrafficMeasurement(
            measurementTime = MeasurementTime(80000),
            measurements = List(
              RoadSegment(
                startIntersection,
                endIntersection,
                transitTime = TransitTime(50)))),
          TrafficMeasurement(
            measurementTime = MeasurementTime(80000),
            measurements = List(
              RoadSegment(
                startIntersection,
                endIntersection,
                transitTime = TransitTime(20)))))

        TrafficProcessor.findAverageTransitTimeBetweenTwoContiguousIntersections(
          trafficMeasurements, startIntersection, endIntersection, defaultTransitTime) shouldEqual TransitTime(30)
      }
    }

    "implement findAverageTransitTime" which {

      "throws exception if there is no transit time provided" in {
        the[ArithmeticException] thrownBy TrafficProcessor.findDefaultTransitTime(Nil) should have message "Division undefined"
      }

      "returns the average transit time" in {
        TrafficProcessor.findDefaultTransitTime(defaultTrafficMeasurements) shouldEqual TransitTime(30)
      }
    }

    "implement moveRight" which {

      "returns no intersection when there is no possible movement to the right" in {
        val currentIntersection = Intersection(Avenue('F'), Street(4))
        val lastAvenue = Avenue('F')

        TrafficProcessor.moveRight(currentIntersection, lastAvenue) shouldEqual None
      }

      "returns the intersection on the right" in {
        val currentIntersection = Intersection(Avenue('D'), Street(4))
        val lastAvenue = Avenue('H')

        TrafficProcessor.moveRight(currentIntersection, lastAvenue) shouldEqual Some(Intersection(Avenue('E'), Street(4)))
      }
    }

    "implement moveUp" which {

      "returns no intersection when there is no possible up movement" in {
        val currentIntersection = Intersection(Avenue('H'), Street(1))

        TrafficProcessor.moveUp(currentIntersection) shouldEqual None
      }

      "returns the intersection that is above the current one" in {
        val currentIntersection = Intersection(Avenue('D'), Street(4))

        TrafficProcessor.moveUp(currentIntersection) shouldEqual Some(Intersection(Avenue('D'), Street(3)))
      }
    }

    "implement findBestRoute" which {

      "gets best route from traffic measurements" in {
        val initialIntersection = Intersection(Avenue('A'), Street(2))
        val finalIntersection = Intersection(Avenue('B'), Street(1))

        val segmentA2toB2 = RoadSegment(
          startIntersection = Intersection(Avenue('A'), Street(2)),
          endIntersection = Intersection(Avenue('B'), Street(2)),
          transitTime = TransitTime(10))
        val segmentB2toB1 = RoadSegment(
          startIntersection = Intersection(Avenue('B'), Street(2)),
          endIntersection = Intersection(Avenue('B'), Street(1)),
          transitTime = TransitTime(10))

        val trafficMeasurements = List(
          TrafficMeasurement(
            measurementTime = MeasurementTime(80000),
            measurements = List(
              segmentA2toB2,
              RoadSegment(
                startIntersection = Intersection(Avenue('A'), Street(2)),
                endIntersection = Intersection(Avenue('A'), Street(1)),
                transitTime = TransitTime(10)),
              RoadSegment(
                startIntersection = Intersection(Avenue('A'), Street(1)),
                endIntersection = Intersection(Avenue('B'), Street(1)),
                transitTime = TransitTime(15)),
              segmentB2toB1)))

        val bestRouteResult = TrafficProcessor.findBestRoute(trafficMeasurements, initialIntersection, finalIntersection)

        bestRouteResult match {
          case Left(_) => fail("should not be left")
          case Right(bestRoute) =>
            bestRoute.initialIntersection shouldEqual initialIntersection
            bestRoute.finalIntersection shouldEqual finalIntersection
            bestRoute.roadSegments shouldEqual List(segmentA2toB2, segmentB2toB1)
            bestRoute.totalTransitTime.value shouldEqual BigDecimal(20)
        }
      }
    }
  }

}
