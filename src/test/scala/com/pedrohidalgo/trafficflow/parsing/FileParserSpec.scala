package com.pedrohidalgo.trafficflow.parsing

import com.pedrohidalgo.trafficflow.{Avenue, BaseSpec, Intersection, MeasurementTime, RoadSegment, Street, TrafficMeasurement, TransitTime}

import java.io.FileNotFoundException
import java.util.concurrent.atomic.AtomicBoolean

class FileParserSpec extends BaseSpec {

  "File Processor" should {

    "implement findTrafficMeasurements" which {
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

      "throws a FileNotFoundException when the file can not be found" in {
        val absolutePath = "some invalid path"

        val readFileContent = (path: String) => {
          absolutePath shouldEqual path
          throw new FileNotFoundException(s"$path (Is a directory)")
        }

        val parseFileContent = (_: String) => {
          fail("should not be called")
        }

        the[FileNotFoundException] thrownBy FileParser.findTrafficMeasurements(
          readFileContent, parseFileContent)(absolutePath) should have message "some invalid path (Is a directory)"
      }

      "throws an exception when the parsing fails" in {
        val absolutePath = "some valid path"
        val fileContent = "some invalid content"

        val readFileContent = (_: String) => {
          fileContent
        }

        val parseFileContent = (content: String) => {
          fileContent shouldEqual content
          throw new Exception("Parsing error")
        }

        the[Exception] thrownBy FileParser.findTrafficMeasurements(
          readFileContent, parseFileContent)(absolutePath) should have message "Parsing error"
      }

      "returns a list of traffic measurements when a file is parsed correctly" in {
        val absolutePath = "some valid path"
        val fileContent = "some valid content"

        val readFileContent = (_: String) => {
          fileContent
        }

        val parseFileContent = (content: String) => {
          fileContent shouldEqual content
          defaultTrafficMeasurements
        }

        FileParser.findTrafficMeasurements(
          readFileContent, parseFileContent)(absolutePath) shouldEqual defaultTrafficMeasurements
      }
    }

    "implement isPathValid" which {

      "returns true when the validation is successful" in {
        val validatePathWasCalled = new AtomicBoolean(false)
        val absolutePath = "/some/valid/path"

        val validatePath: String => Boolean = (path: String) => {
          absolutePath shouldEqual path
          validatePathWasCalled.set(true)
          true
        }

        FileParser.isPathValid(absolutePath, validatePath) shouldEqual true
        validatePathWasCalled.get shouldBe true
      }
    }

    "implement parseFileContent" which {

      "throws an exception when the parsing fails" in {
        val fileContent = "some invalid content"

        val thrownException = the[Exception] thrownBy FileParser.parseFileContent(fileContent)
        thrownException.getMessage.contains("Parsing error") shouldEqual true
      }

      "returns a valid list of traffic measurements when the file content is valid" in {
        val fileContent =
          """{
            |    "trafficMeasurements": [
            |        {
            |            "measurementTime": 86544,
            |            "measurements": [
            |                {
            |                    "startAvenue": "A",
            |                    "startStreet": "1",
            |                    "transitTime": 28.000987663134676,
            |                    "endAvenue": "B",
            |                    "endStreet": "1"
            |                },
            |                {
            |                    "startAvenue": "A",
            |                    "startStreet": "2",
            |                    "transitTime": 59.71131185379898,
            |                    "endAvenue": "A",
            |                    "endStreet": "1"
            |                }
            |            ]
            |        },
            |        {
            |            "measurementTime": 84235,
            |            "measurements": [
            |                {
            |                    "startAvenue": "H",
            |                    "startStreet": "4",
            |                    "transitTime": 28.000987663134676,
            |                    "endAvenue": "H",
            |                    "endStreet": "3"
            |                },
            |                {
            |                    "startAvenue": "G",
            |                    "startStreet": "2",
            |                    "transitTime": 59.71131185379898,
            |                    "endAvenue": "H",
            |                    "endStreet": "2"
            |                }
            |            ]
            |        }
            |    ]
            |}""".stripMargin

        FileParser.parseFileContent(fileContent) shouldEqual List(
          TrafficMeasurement(
            measurementTime = MeasurementTime(86544),
            measurements = List(
              RoadSegment(
                startIntersection = Intersection(Avenue('A'), Street(1)),
                endIntersection = Intersection(Avenue('B'), Street(1)),
                transitTime = TransitTime(28.000987663134676)),
              RoadSegment(
                startIntersection = Intersection(Avenue('A'), Street(2)),
                endIntersection = Intersection(Avenue('A'), Street(1)),
                transitTime = TransitTime(59.71131185379898)))),
          TrafficMeasurement(
            measurementTime = MeasurementTime(84235),
            measurements = List(
              RoadSegment(
                startIntersection = Intersection(Avenue('H'), Street(4)),
                endIntersection = Intersection(Avenue('H'), Street(3)),
                transitTime = TransitTime(28.000987663134676)),
              RoadSegment(
                startIntersection = Intersection(Avenue('G'), Street(2)),
                endIntersection = Intersection(Avenue('H'), Street(2)),
                transitTime = TransitTime(59.71131185379898)))))
      }
    }
  }

}
