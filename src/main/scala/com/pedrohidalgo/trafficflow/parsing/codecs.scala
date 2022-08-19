package com.pedrohidalgo.trafficflow.parsing

import com.pedrohidalgo.trafficflow._
import io.circe._
import io.circe.generic.semiauto._

final case class RoadSegmentCodec(
  startAvenue: String,
  startStreet: String,
  transitTime: Double,
  endAvenue: String,
  endStreet: String)

object RoadSegmentCodec {
  implicit val roadSegmentEncoder: Encoder[RoadSegmentCodec] = deriveEncoder
  implicit val roadSegmentDecoder: Decoder[RoadSegmentCodec] = deriveDecoder

  def fromRoadSegment(roadSegment: RoadSegment): RoadSegmentCodec = {
    RoadSegmentCodec(
      roadSegment.startIntersection.avenue.value.toString,
      roadSegment.startIntersection.street.value.toString,
      roadSegment.transitTime.value.doubleValue,
      roadSegment.endIntersection.avenue.value.toString,
      roadSegment.endIntersection.street.value.toString)
  }

  def toRoadSegment(codec: RoadSegmentCodec): RoadSegment = {
    RoadSegment(
      startIntersection = Intersection(Avenue(codec.startAvenue.charAt(0)), Street(codec.startStreet.toInt)),
      endIntersection = Intersection(Avenue(codec.endAvenue.charAt(0)), Street(codec.endStreet.toInt)),
      transitTime = TransitTime(codec.transitTime))
  }

}

final case class TrafficMeasurementCodec(measurementTime: Int, measurements: List[RoadSegmentCodec])

object TrafficMeasurementCodec {
  implicit val trafficMeasurementDecoder: Decoder[TrafficMeasurementCodec] = deriveDecoder

  def toTrafficMeasurement(codec: TrafficMeasurementCodec): TrafficMeasurement = {
    TrafficMeasurement(
      measurementTime = MeasurementTime(codec.measurementTime),
      measurements = codec.measurements.map(RoadSegmentCodec.toRoadSegment))
  }
}

final case class TrafficMeasurementsCodec(trafficMeasurements: List[TrafficMeasurementCodec])

object TrafficMeasurementsCodec {
  implicit val trafficMeasurementsDecoder: Decoder[TrafficMeasurementsCodec] = deriveDecoder

  def toTrafficMeasurements(codec: TrafficMeasurementsCodec): List[TrafficMeasurement] = {
    codec.trafficMeasurements.map(TrafficMeasurementCodec.toTrafficMeasurement)
  }
}

