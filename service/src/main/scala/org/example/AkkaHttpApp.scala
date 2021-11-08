package org.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory

import java.net.URI
import java.net.http.HttpClient
import java.time.Duration
import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.{ExecutionContext, Future}

object AkkaHttpApp extends App {
  private val behavior: Behavior[Any] = Behaviors.empty
  implicit val system: ActorSystem[Any] = ActorSystem(behavior, "lowlevel", ConfigFactory.parseString(Conf.str).resolve())
  implicit val executionContext: ExecutionContext = system.executionContext
  val httpClient = HttpClient.newBuilder.connectTimeout(Duration.ofMinutes(999)).build()
  val serverBuilder = Http().newServerAt("localhost", 9090)
  val binding: Future[Http.ServerBinding] =
    serverBuilder
      .connectionSource()
      //todo: .throttle(1, 1.second)
      .to(Sink.foreach { connection =>
        connection.handleWithAsyncHandler {
          case HttpRequest(GET, _, _, _, _) =>
            val request = java.net.http.HttpRequest.newBuilder.GET().uri(URI.create("http://localhost:7070")).build()
            httpClient.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString).toScala
              .map(_ => {
                Thread.sleep(1)
                HttpResponse()
              })
        }
      }).run()
}

object Conf {
  val str: String =
    """
      |include "akka-http-version"
      |
      |akka.http {
      |  server {
      |    server-header = akka-http/${akka.http.version}
      |
      |    preview {
      |      # `Http().newServerAt(...).bindFlow` and `connectionSource()` are not supported.
      |      enable-http2 = off
      |    }
      |    idle-timeout = 999 min
      |    request-timeout = 999 min
      |    bind-timeout = 999 min
      |    default-http-port = 80
      |    default-https-port = 443
      |    linger-timeout = 999 min
      |    max-connections = 1024
      |    pipelining-limit = 1
      |    remote-address-header = off
      |    remote-address-attribute = off
      |    raw-request-uri-header = off
      |    transparent-head-requests = off
      |    verbose-error-messages = off
      |    response-header-size-hint = 512
      |    backlog = 100
      |    default-host-header = ""
      |    socket-options {
      |      so-receive-buffer-size = undefined
      |      so-send-buffer-size = undefined
      |      so-reuse-address = undefined
      |      so-traffic-class = undefined
      |      tcp-keep-alive = undefined
      |      tcp-oob-inline = undefined
      |      tcp-no-delay = undefined
      |    }
      |    termination-deadline-exceeded-response {
      |      status = 503 # ServiceUnavailable
      |    }
      |    parsing {
      |      max-content-length = 8m
      |      error-handler = "akka.http.DefaultParsingErrorHandler$"
      |    }
      |    log-unencrypted-network-bytes = off
      |    stream-cancellation-delay = 100 millis
      |
      |    http2 {
      |      max-concurrent-streams = 256
      |      request-entity-chunk-size = 65536 b
      |      incoming-connection-level-buffer-size = 10 MB
      |      incoming-stream-level-buffer-size = 512kB
      |      min-collect-strict-entity-size = 0
      |      outgoing-control-frame-buffer-size = 1024
      |      log-frames = false
      |      ping-interval = 0s
      |      ping-timeout = 0s
      |    }
      |    websocket {
      |      periodic-keep-alive-mode = ping
      |      periodic-keep-alive-max-idle = infinite
      |      log-frames = false
      |    }
      |  }
      |  client {
      |    user-agent-header = akka-http/${akka.http.version}
      |    connecting-timeout = 10s
      |    idle-timeout = 60 s
      |    request-header-size-hint = 512
      |    socket-options {
      |      so-receive-buffer-size = undefined
      |      so-send-buffer-size = undefined
      |      so-reuse-address = undefined
      |      so-traffic-class = undefined
      |      tcp-keep-alive = undefined
      |      tcp-oob-inline = undefined
      |      tcp-no-delay = undefined
      |    }
      |    proxy {
      |      https {
      |        host = ""
      |        port = 443
      |      }
      |    }
      |    parsing {
      |      max-content-length = infinite
      |    }
      |    log-unencrypted-network-bytes = off
      |    http2 {
      |      max-concurrent-streams = 256
      |      request-entity-chunk-size = 65536 b
      |      incoming-connection-level-buffer-size = 10 MB
      |      incoming-stream-level-buffer-size = 512kB
      |      outgoing-control-frame-buffer-size = 1024
      |      log-frames = false
      |      ping-interval = 0s
      |      ping-timeout = 0s
      |      max-persistent-attempts = 0
      |      base-connection-backoff = ${akka.http.host-connection-pool.base-connection-backoff}
      |      max-connection-backoff = ${akka.http.host-connection-pool.max-connection-backoff}
      |      completion-timeout = 3s
      |    }
      |    websocket {
      |      periodic-keep-alive-mode = ping
      |      periodic-keep-alive-max-idle = infinite
      |      log-frames = false
      |    }
      |    stream-cancellation-delay = 100 millis
      |  }
      |  host-connection-pool {
      |    max-connections = 4
      |    min-connections = 0
      |    max-retries = 5
      |    max-open-requests = 32
      |    max-connection-lifetime = infinite
      |    pipelining-limit = 1
      |    base-connection-backoff = 100ms
      |    max-connection-backoff = 2 min
      |    idle-timeout = 30 s
      |    keep-alive-timeout = infinite
      |    response-entity-subscription-timeout = 1.second
      |    client = {
      |    }
      |    per-host-override = []
      |  }
      |  parsing {
      |    max-uri-length             = 2k
      |    max-method-length          = 16
      |    max-response-reason-length = 64
      |    max-header-name-length     = 64
      |    max-header-value-length    = 8k
      |    max-header-count           = 64
      |    max-chunk-ext-length       = 256
      |    max-chunk-size             = 1m
      |    max-comment-parsing-depth  = 5
      |    max-to-strict-bytes = 8m
      |    uri-parsing-mode = strict
      |    cookie-parsing-mode = rfc6265
      |    illegal-header-warnings = on
      |    ignore-illegal-header-for = []
      |    modeled-header-parsing = on
      |    error-logging-verbosity = full
      |    illegal-response-header-name-processing-mode = error
      |    illegal-response-header-value-processing-mode = error
      |    conflicting-content-type-header-processing-mode = error
      |    header-cache {
      |      default = 12
      |      Content-MD5 = 0
      |      Date = 0
      |      If-Match = 0
      |      If-Modified-Since = 0
      |      If-None-Match = 0
      |      If-Range = 0
      |      If-Unmodified-Since = 0
      |      User-Agent = 32
      |    }
      |    tls-session-info-header = off
      |    ssl-session-attribute = off
      |  }
      |
      |}
      |""".stripMargin
}
