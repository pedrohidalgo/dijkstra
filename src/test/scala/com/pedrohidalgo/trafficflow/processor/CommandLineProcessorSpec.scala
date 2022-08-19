package com.pedrohidalgo.trafficflow.processor

import com.pedrohidalgo.trafficflow.{Avenue, BaseSpec, Intersection, Street}

class CommandLineProcessorSpec extends BaseSpec {

  "CommandLine Processor" should {

    "implement getIntersectionFromInput" which {

      "returns no intersection when the input is invalid" in {
        CommandLineProcessor.getIntersectionFromInput(input = "DFSDF") shouldEqual None
        CommandLineProcessor.getIntersectionFromInput(input = "2A") shouldEqual None
        CommandLineProcessor.getIntersectionFromInput(input = "G2T") shouldEqual None
        CommandLineProcessor.getIntersectionFromInput(input = "GT3") shouldEqual None
      }

      "returns a valid intersection when the input is valid" in {
        CommandLineProcessor.getIntersectionFromInput(input = "a2") shouldEqual Some(Intersection(Avenue('A'), Street(2)))
        CommandLineProcessor.getIntersectionFromInput(input = "g25") shouldEqual Some(Intersection(Avenue('G'), Street(25)))
        CommandLineProcessor.getIntersectionFromInput(input = "B4") shouldEqual Some(Intersection(Avenue('B'), Street(4)))
        CommandLineProcessor.getIntersectionFromInput(input = "Z35") shouldEqual Some(Intersection(Avenue('Z'), Street(35)))
      }
    }
  }

}
