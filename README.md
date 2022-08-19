# Dijkstra's algorithm in Scala

## Purpose
The Purpose of this small repository is to show an implementation of the Dijkstra's algorithm 
along with good Scala functional programming code.

Read the [Instructions for Processing the data](Instructions.md)

### Relevant logic in the application

* `Main.scala` application entry point. (Only place within the app with side effects)
* `TrafficProcessor.scala`: contains the main business logic for generating/calculating the best route
* `parsing package`: contains code related to the interaction with the data file
* `codecs`: contains classes used when encoding/decoding json

### TODO

- [ ] Use [Generator-driven property checks](https://www.scalatest.org/user_guide/generator_driven_property_checks) to generate random input for the unit tests
- [ ] Add more unit tests to test more cases and increase coverage

### Instructions to run the application

* Make sure your environment is setup to run `sbt` applications (java and sbt are installed in your system)
* Enter the SBT console.
* Available SBT commands:
  * `run`: execute the application
  * `test`: run all unit tests
  * `runCoverage`: execute all unit tests and generate the coverage report

