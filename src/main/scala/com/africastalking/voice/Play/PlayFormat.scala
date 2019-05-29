package com.africastalking.voice.Play
import java.io.{BufferedOutputStream, File, FileNotFoundException, FileOutputStream}
import java.net.{HttpURLConnection, URL}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, formFieldMap, path, post}
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
object PlayFormat extends App {

  val host = "localhost"
  val port = 9090

  implicit val system       = ActorSystem("Dial-System")
  implicit val materializer = ActorMaterializer()

  def callIsActive(fields: Map[String, String]) =
    fields.getOrElse("isActive","") match {
      case "" =>  false
      case x if x.toInt == 1  => true
      case x if x.toInt == 0  => false
    }

  def sendPlayResponse =
    s"""
       |<Response>
       |<Play url="https://s3.eu-west-2.amazonaws.com/omo-demo/new/01_WELCOME.mp3" />
       |</Response>
     """.stripMargin

  def endCall = StatusCodes.OK

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

  def getRecordingUrl(fields: Map[String, String])  : String =
    fields.get("recordingUrl") match {
      case Some(url) => url
      case None      => ""
    }

  val routes = path("call") {
    post {
      formFieldMap { fields =>
        val recordingUrl = getRecordingUrl(fields)
        if(!recordingUrl.isEmpty){
          downloadRecording(recordingUrl)
        }
        if (callIsActive(fields)) {
          complete(sendPlayResponse)
        }
        else{
          complete(endCall)
        }
      }
    }
  }
  Http().bindAndHandle(routes,host,port).onComplete{
    case Success(value) => println(s"Server successfully started on ${value.localAddress}")
    case Failure(ex)    => println(s"Server could not start, ${ex.getMessage}")
  }

}
