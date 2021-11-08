package org.example


import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

class UndertowSimulation extends Simulation {
  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://localhost:8080")
    .enableHttp2
    .shareConnections

  val scn: ScenarioBuilder = scenario("Undertow simulation")
    .exec(
      http("request_1") // 8
        .get("/"))

  setUp(scn.inject(atOnceUsers(10000))).protocols(httpProtocol)
}

class AkkaSimulation extends Simulation {
  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://localhost:9090")
    .enableHttp2
    .shareConnections

  val scn: ScenarioBuilder = scenario("Akka Simulation")
    .exec(
      http("request_1") // 8
        .get("/"))

  setUp(scn.inject(atOnceUsers(10000))).protocols(httpProtocol)
}
