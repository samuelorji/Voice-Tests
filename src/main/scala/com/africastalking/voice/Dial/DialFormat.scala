//package com.africastalking.voice
//package Dial
//
//import scala.util.{Failure, Success}
//import scala.concurrent.ExecutionContext.Implicits.global
//
//import java.io.{BufferedOutputStream, File, FileNotFoundException, FileOutputStream}
//import java.net.{HttpURLConnection, URL}
//
//import akka.actor.ActorSystem
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.model.StatusCodes
//import akka.http.scaladsl.server.Directives._
//import akka.stream.ActorMaterializer
//
//
//
//object DialFormat extends App {
//
//
//  val host = "localhost"
//  val port = 9090
//
//  implicit val system       = ActorSystem("Record-System")
//  implicit val materializer = ActorMaterializer()
//
//  def callIsActive(fields: Map[String, String]) =
//    fields.getOrElse("isActive","") match {
//      case "" =>  false
//      case x if x.toInt == 1  => true
//      case x if x.toInt == 0  => false
//    }
//
//  def sendDialResponse =
//    s"""
//       |<Response>
//       |    <Record finishOnKey="#" maxLength="10" trimSilence="true" playBeep="true" callbackUrl="https://orji.ngrok.io/record">
//       |		  <Say>Please tell us your name after the beep.</Say>
//       |	  </Record>
//       |</Response>
//     """.stripMargin
//
//  def endCall = StatusCodes.OK
//
//
//
//
//
//  val routes = path("call") {
//    post {
//      formFieldMap { fields =>
//        if (callIsActive(fields)) {
//          complete(sendDialResponse)
//        }
//        else{
//          complete(endCall)
//        }
//      }
//    }
//  } ~
//    post {
//      formFieldMap { fields =>
//        val recordingUrl = getRecordingUrl(fields)
//        if (!recordingUrl.isEmpty) {
//          downloadRecording(recordingUrl)
//        }else{
//          println("Recording Url not found!!!!!!!!!!!!!!!!!")
//        }
//        complete(endCall)
//      }
//    }
//  Http().bindAndHandle(routes,host,port).onComplete{
//    case Success(value) => println(s"Server successfully started on ${value.localAddress}")
//    case Failure(ex)    => println(s"Server could not start, ${ex.getMessage}")
//  }
//
//
//}
