package com.africastalking.voice
package GetDigits

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.util.{ Failure, Success }

object GetDigitFormat extends App{

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

  def sendGetDigitResponse =
    s"""
       |<Response>
       |    <GetDigits timeout="30" numDigits="11" finishOnKey="#" callbackUrl="https://loquor.serveo.net/getDigits">
       |        <Say>Please Type in your phone number and the Hash sign when done  </Say>
       |    </GetDigits>
       |</Response>
     """.stripMargin

  def endCall = StatusCodes.OK

  def getDigitsFromCall(fields: Map[String, String]) =
    fields.getOrElse("dtmfDigits","")

  val routes =
  {
    path("getDigits") {
      post{
        formFieldMap { fields =>
         val digits = getDigitsFromCall(fields)
          if(!digits.isEmpty) {
            println(digits)
          }
          complete(StatusCodes.OK)
        }
      }
    } ~
    path ("call") {
      post{
        formFieldMap { fields =>
          println(fields)
          if(callIsActive(fields)){
            complete(sendGetDigitResponse)
          }else{
            complete(endCall)
          }
        }
      }
    }
  }
  Http().bindAndHandle(routes,host,port).onComplete{
    case Success(value) => println(s"Server successfully started on ${value.localAddress}")
    case Failure(ex)    => println(s"Server could not start, ${ex.getMessage}")
  }
}
