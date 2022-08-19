package com.pedrohidalgo.trafficflow.parsing

import com.pedrohidalgo.trafficflow.TrafficMeasurement

import io.circe.parser.decode

object FileParser {

  def findTrafficMeasurements(
    readFileContent: String => String,
    parseFileContent: String => List[TrafficMeasurement]): String => List[TrafficMeasurement] = {
    (absolutePath: String) =>
      parseFileContent(readFileContent(absolutePath))
  }

  def isPathValid(
    absolutePath: String,
    validatePath: String => Boolean): Boolean = {
    validatePath(absolutePath)
  }

  def parseFileContent(fileContent: String): List[TrafficMeasurement] = {
    decode[TrafficMeasurementsCodec](fileContent) match {
      case Left(error) =>
        throw new Exception("Parsing error: " + error)
      case Right(response) =>
        TrafficMeasurementsCodec.toTrafficMeasurements(response)
    }
  }

}

