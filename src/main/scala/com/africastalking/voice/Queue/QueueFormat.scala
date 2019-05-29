package com.africastalking.voice.Queue

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success}

object QueueFormat extends App {

    val host = "localhost"
    val port = 9090

    implicit val system       = ActorSystem("GetDigit-System")
    implicit val materializer = ActorMaterializer()

    def callIsActive(fields: Map[String, String]) =
      fields.getOrElse("isActive","") match {
        case "" =>  false
        case x if x.toInt == 1  => true
        case x if x.toInt == 0  => false
      }

    def endCall = StatusCodes.OK

    def getDigitsFromCall(fields: Map[String, String]) =
      fields.getOrElse("dtmfDigits","")

  val agentCallerNum = "+2348112172624"

  def getCallerNumber(fields: Map[String, String]) =
    fields("callerNumber")

  def sendDequeueResponse(num : String) =
    s"""
       |<Response>
       |    <Dequeue name="samuel" phoneNumber="$num" />
       |</Response>
     """.stripMargin

  def sendEnqueueResponse =
    s"""
       |<Response>
       |    <Enqueue name="samuel" holdMusic="https://s3.eu-west-2.amazonaws.com/omo-demo/new/01_WELCOME.mp3" />
       |</Response>
     """.stripMargin

  val routes = {
    path("getDigits") {
      post {
        formFieldMap { fields =>
          val digits = getDigitsFromCall(fields)
          if (!digits.isEmpty) {
            println(digits)
          }
          complete(StatusCodes.OK)
        }
      }
    } ~
      path("call") {
        post {
          formFieldMap { fields =>
            if (callIsActive(fields)) {
              if (getCallerNumber(fields) == agentCallerNum) {
                complete(sendDequeueResponse(fields("destinationNumber")))
              } else {
                complete(sendEnqueueResponse)
              }
            } else {
              complete(endCall)
            }
          }
        }
      } ~
      path("events") {
        post {
          formFieldMap { fields =>
            println("From events URL")
            println(fields)
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
