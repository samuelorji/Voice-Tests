package com.africastalking.voice.usecases

import java.io.{BufferedOutputStream, File, FileNotFoundException, FileOutputStream}
import java.net.{HttpURLConnection, URL}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, formFieldMap, path, post, _}
import akka.stream.ActorMaterializer
import com.africastalking.voice.GetDigits.GetDigitFormat.getDigitsFromCall

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object CustomerCare extends App{
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

  def dialWithSay(text : String , sequential : Boolean,nums : String*) =
    s"""
       |<Response>
       |<Say>$text </Say>${if(nums.mkString.isEmpty) "" else dial(sequential,nums :_*)}</Response>
     """.stripMargin
  def sendGetDigitResponse(text : String) =
    s"""
       |<Response>
       |<GetDigits numDigits="1" callbackUrl="https://iudico.serveo.net/getDigits">
       |<Say>$text </Say>
       |</GetDigits>
       |</Response>
     """.stripMargin

  def dial(sequential : Boolean,nums : String*) =
    s"""
       |<Dial phoneNumbers="${nums.mkString(",")}" sequential="$sequential" record="true" />
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

  val clienRelations = "+2348112172624"
  val finance        = "+2349098639517"


  val routes = path("call") {
    post {
      formFieldMap { fields =>
        val recordingUrl = getRecordingUrl(fields)
        if(!recordingUrl.isEmpty){
          downloadRecording(recordingUrl)
        }

        val numsToCall = ""
        if (callIsActive(fields)) {
          complete(sendGetDigitResponse("Welcome to Safe Border, to speak with Client Relations, press 1, to speak with Finance, press 2 "))
        }
        else{
          complete(endCall)
        }
      }
    }
  } ~ path("getDigits"){
      post{
      formFieldMap { fields =>
        val digits = getDigitsFromCall(fields)
        complete (if(!digits.isEmpty) {
          digits match {
            case "1" => dialWithSay("Please Wait while we transfer you to Client Relations", true , clienRelations)
            case "2" => dialWithSay("Please Wait while we transfer you to Finance", true , finance)
            case _   => sendGetDigitResponse("Invalid Entry, to speak with Client Relations, press 1, to speak with Finance, press 2")
          }
        }else{
          endCall
        }
        )

      }
    }
  }
  Http().bindAndHandle(routes,host,port).onComplete{
    case Success(value) => println(s"Server successfully started on ${value.localAddress}")
    case Failure(ex)    => println(s"Server could not start, ${ex.getMessage}")
  }

}
