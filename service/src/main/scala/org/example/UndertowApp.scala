package org.example

import io.undertow.Undertow
import io.undertow.UndertowOptions.ENABLE_HTTP2
import io.undertow.server.{HttpHandler, HttpServerExchange}

import java.net.URI
import java.net.http.HttpClient
import java.time.Duration
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

object UndertowApp extends App {
  val server: Undertow = Undertow.builder
    .addHttpListener(8080, "0.0.0.0")
    .setHandler(new Handler)
    .setServerOption(ENABLE_HTTP2.asInstanceOf[org.xnio.Option[Any]], true)
    .setIoThreads(50)
    .setWorkerThreads(1)
    .build

  server.start()
}

class Handler extends HttpHandler {
  private val httpClient: HttpClient = HttpClient.newBuilder.connectTimeout(Duration.ofMinutes(999)).build()
  private val manyThreadsExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(500))
  private val fewThreadsExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(7))

  def handleRequest(exchange: HttpServerExchange): Unit = {
    if (exchange.isInIoThread) {
      exchange.dispatch(this)
      return
    }
    exchange.getRequestReceiver.receiveFullBytes((exchange, bytes) => {
      val request = java.net.http.HttpRequest.newBuilder.GET().uri(URI.create("http://localhost:7070")).build()
      Future {
        httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString)
      }(manyThreadsExecutionContext)
        .onComplete(_ => {
          Thread.sleep(1)
          exchange.getResponseSender.send("abc")
        })(fewThreadsExecutionContext)
    })
  }
}
