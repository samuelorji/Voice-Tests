package com.africastalking.voice
package Record

import java.io._
import java.net.{HttpURLConnection, URL}

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.util.{Failure, Success}

object Record extends App {

  //this uses the Record Action

  val host = "localhost"
  val port = 9090

  implicit val system       = ActorSystem("Record-System")
  implicit val materializer = ActorMaterializer()

  def callIsActive(fields: Map[String, String]) =
    fields.getOrElse("isActive","") match {
      case "" =>  false
      case x if x.toInt == 1  => true
      case x if x.toInt == 0  => false
    }

  def sendPartialRecordResponse =
    s"""
       |<Response>
       |    <Record finishOnKey="#" maxLength="10" trimSilence="true" playBeep="true" callbackUrl="https://orji.ngrok.io/record">
       |		  <Say>Please tell us your name after the beep.</Say>
       |	  </Record>
       |</Response>
     """.stripMargin

  def endCall = StatusCodes.OK


  def getRecordingUrl(fields: Map[String, String])  : String =
    fields.get("recordingUrl") match {
      case Some(url) => url
      case None      => ""
    }

  def callDetailsHasRecordUrl (fields: Map[String, String]) =
    fields.get("recordingUrl") match {
      case Some(_) => true
      case None    => false
    }

  def downloadRecording(recordingUrl: String) = {
    try {
      val connection   = new URL(recordingUrl).openConnection().asInstanceOf[HttpURLConnection]
      val input        = connection.getInputStream
      val downloadFile = new File(s"./${System.currentTimeMillis().toString}.mp3")
      val outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile))
      val byteArray    = Stream.continually(input.read).takeWhile(_ != -1).map(_.toByte).toArray

      outputStream.write(byteArray)
      outputStream.flush()
      outputStream.close()
    } catch {
      case ex: FileNotFoundException =>
        println("Error Message " + ex.getMessage)
    }
  }

  val routes = path("call") {
    post {
      formFieldMap { fields =>
        if (callIsActive(fields)) {
          complete(sendPartialRecordResponse)
        }
        else{
          complete(endCall)
        }
      }
    }
  } ~
    post {
      formFieldMap { fields =>
        val recordingUrl = getRecordingUrl(fields)
        if (!recordingUrl.isEmpty) {
          downloadRecording(recordingUrl)
        }else{
          println("Recording Url not found!!!!!!!!!!!!!!!!!")
        }
        complete(endCall)
      }
  }
  Http().bindAndHandle(routes,host,port).onComplete{
    case Success(value) => println(s"Server successfully started on ${value.localAddress}")
    case Failure(ex)    => println(s"Server could not start, ${ex.getMessage}")
  }



}
