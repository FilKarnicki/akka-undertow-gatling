package org.example

import io.undertow.Undertow
import io.undertow.UndertowOptions.ENABLE_HTTP2
import io.undertow.server.{HttpHandler, HttpServerExchange}

object LaunchMeFirstApp extends App {
  val server: Undertow = Undertow.builder
    .addHttpListener(7070, "0.0.0.0")
    .setHandler(new FirstHandler)
    .setServerOption(ENABLE_HTTP2.asInstanceOf[org.xnio.Option[Any]], true)
    .setIoThreads(1000)
    .setWorkerThreads(1)
    .build

  server.start()
}

class FirstHandler extends HttpHandler {
  def handleRequest(exchange: HttpServerExchange): Unit = {
    if (exchange.isInIoThread) {
      exchange.dispatch(this)
      return
    }
    exchange.getRequestReceiver.receiveFullBytes((exchange, bytes) => {
      Thread.sleep(1000)
      exchange.getResponseSender.send("abc")
    })
  }
}
