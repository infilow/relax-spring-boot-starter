package com.infilos.spring.utils

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.infilos.spring.utils.Respond.SuccedCode
import org.springframework.http._

import java.util.Optional
import scala.util.{Failure, Success, Try}

final case class Respond[+T](code: Int, data: T, message: String) {

  @JsonIgnore
  def isSuccedWithData: Boolean = isSucced && data != null

  @JsonIgnore
  def isSucced: Boolean = code == SuccedCode

  @JsonIgnore
  def asBytesResponse(objectMapper: ObjectMapper): ResponseEntity[Array[Byte]] = {
    val headers = new HttpHeaders()
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

    var bytes: Array[Byte] = null
    try {
      bytes = objectMapper.writeValueAsBytes(this)
    } catch {
      case e: Throwable =>
        bytes = String.format(s"{\"code\":-1,\"$message\":\"Serialize $toString failed: ${e.getMessage}\"}", this.toString, e.getMessage).getBytes
    }

    new ResponseEntity[Array[Byte]](bytes, headers, HttpStatus.OK);
  }

  override def toString: String =
    s"Respond(code=$code, data=$data, message=$message)"

  override def equals(obj: Any): Boolean = obj match {
    case that: Respond[_] =>
      this.canEqual(that) &&
        this.code == that.code &&
        this.data == that.data &&
        this.message == that.message
    case _ => false
  }

  override def canEqual(that: Any): Boolean = that.isInstanceOf[Respond[_]]

  override def hashCode(): Int = (code, data, message).##
}

object Respond {

  val SuccedCode: Int = 0
  val FailedCode: Int = -1
  val BlankMessage: String = ""

  def succed[T](): Respond[T] =
    Respond(SuccedCode, null.asInstanceOf[T], BlankMessage)

  def succed[T](code: Int, data: T): Respond[T] =
    Respond(code, data, BlankMessage)

  def succed[T](code: Int): Respond[T] =
    Respond(code, null.asInstanceOf[T], BlankMessage)

  def failed[T](code: Int, error: String): Respond[T] =
    Respond(code, null.asInstanceOf[T], error)

  def failed[T](code: Int, cause: Throwable, retry: Boolean = false): Respond[T] =
    Respond(
      code,
      null.asInstanceOf[T],
      s"${cause.getClass.getSimpleName}(${if (cause.getMessage == null) "" else cause.getMessage})"
    )

  def failed[T](): Respond[T] =
    Respond(FailedCode, null.asInstanceOf[T], BlankMessage)

  def of[T](either: Either[_, T]): Respond[T] = either match {
    case Left(value) => value match {
      case error: String => failed(error)
      case error: Throwable => failed(error)
      case error => failed(error.toString)
    }
    case Right(value) => succed(value)
  }

  def succed[T](data: T): Respond[T] =
    Respond(SuccedCode, data, BlankMessage)

  def failed[T](error: String): Respond[T] =
    Respond(FailedCode, null.asInstanceOf[T], error)

  def failed[T](cause: Throwable): Respond[T] = {
    Respond(
      FailedCode,
      null.asInstanceOf[T],
      s"${cause.getClass.getSimpleName}(${if (cause.getMessage == null) "" else cause.getMessage})"
    )
  }

  def ofFileBytes(filename: String, bytes: Array[Byte]): ResponseEntity[Array[Byte]] = {
    val headers = new HttpHeaders()
    headers.add(HttpHeaders.CONTENT_DISPOSITION, s"attachment; filename=$filename")
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)

    ofBytes(HttpStatus.OK, headers, bytes)
  }

  def ofFileBytes(filename: String, format: String, bytes: Array[Byte]): ResponseEntity[Array[Byte]] = {
    val mediaType: Optional[String] = Try(MediaType.parseMediaType(format)) match {
      case Success(value) => Optional.ofNullable(value).map(_.toString)
      case Failure(_) => MediaTypeFactory.getMediaType(filename).map(_.toString)
    }

    val headers = new HttpHeaders()
    headers.add(HttpHeaders.CONTENT_DISPOSITION, s"attachment; filename=$filename")
    headers.add(HttpHeaders.CONTENT_TYPE, mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE))

    ofBytes(HttpStatus.OK, headers, bytes)
  }

  def ofBytes(status: HttpStatus, headers: HttpHeaders, bytes: Array[Byte]): ResponseEntity[Array[Byte]] = {
    new ResponseEntity[Array[Byte]](bytes, headers, status)
  }
}
