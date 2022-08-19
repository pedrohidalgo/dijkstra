package com.pedrohidalgo.trafficflow

import com.pedrohidalgo.trafficflow.parsing.{FileParser, RoadSegmentCodec}
import com.pedrohidalgo.trafficflow.processor.{CommandLineProcessor, TrafficProcessor}
import io.circe.syntax.EncoderOps

import scala.io.Source
import scala.io.StdIn.readLine

object Main {

  def main(args: Array[String]): Unit = {
    println("Starting the traffic flow application...")
    val findTrafficMeasurements = FileParser.findTrafficMeasurements(
      readFileContent,
      FileParser.parseFileContent)

    val absolutePath = readLine("Please, enter the absolute path of the data file: ")

    if (!FileParser.isPathValid(absolutePath, validatePath)) {
      println(s"Fatal error, the path '$absolutePath', is not valid or the file does not exists, the program will exit.")
      System.exit(1)
    }

    val initialInput = readLine("Please, enter the starting intersection, e.g.: A6 \n")
    val initialIntersectionOpt = CommandLineProcessor.getIntersectionFromInput(initialInput)

    val finalInput = readLine("Please, enter the ending intersection, e.g.: D2 \n")
    val finalIntersectionOpt = CommandLineProcessor.getIntersectionFromInput(finalInput)

    (initialIntersectionOpt, finalIntersectionOpt) match {
      case (None, _) =>
        println(s"Fatal error, the value '$initialInput', is not a valid initial intersection, the program will exit.")
        System.exit(1)
      case (_, None) =>
        println(s"Fatal error, the value '$finalInput', is not a valid final intersection, the program will exit.")
        System.exit(1)
      case (Some(initialIntersection), Some(finalIntersection)) =>
        println("Processing...")
        val startTimeMillis = System.currentTimeMillis()

        val bestRouteResult = TrafficProcessor.generateBestRoute(
          findTrafficMeasurements,
          TrafficProcessor.findBestRoute)(absolutePath, initialIntersection, finalIntersection)

        bestRouteResult match {
          case Left(error) =>
            println(ProcessorErrors.showErrorDescription(error))

          case Right(bestRoute) =>
            println("The Best Route was found, below are the details:")
            println(s"The starting intersection is: ${bestRoute.initialIntersection.avenue.value}${bestRoute.initialIntersection.street.value}")
            println(s"The ending intersection is: ${bestRoute.finalIntersection.avenue.value}${bestRoute.finalIntersection.street.value}")
            println("The sequence of road segments are: ")
            println(s"${bestRoute.roadSegments.map(RoadSegmentCodec.fromRoadSegment).asJson}")
            println(s"The total transit time is: ${bestRoute.totalTransitTime.value.doubleValue}")

            val endTimeMillis = System.currentTimeMillis()
            val durationSeconds = (endTimeMillis - startTimeMillis) / 1000
            println("#######################################")
            println("Processing execution in seconds:[" + durationSeconds + "]")
            println("#######################################")
        }
    }
  }

  private def validatePath(absolutePath: String): Boolean = {
    import java.nio.file.{Files, Paths}

    Files.exists(Paths.get(absolutePath))
  }

  private def readFileContent(absolutePath: String): String = {
    val bufferedSource = Source.fromFile(absolutePath)
    val fileContent = bufferedSource.getLines().mkString
    bufferedSource.close()
    fileContent
  }

}

