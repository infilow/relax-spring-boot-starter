package com.infilos.spring.track.api;

public enum Audit {
  Nowhere, // meaningless default value placeholder for annotation
  Constant, // extract attribute value just as the constant value
  ReqPath, // extract attribute value from http request's url path
  ReqQuery, // extract attribute value from http request's url query
  ReqHeader, // extract attribute value from http request's header
  ReqCookie, // extract attribute value from http request's cookies
  ReqSession, // extract attribute value from http request's session
  ReqBody, // extract attribute value from http request's body
  ReqMethod, // extract attribute value just as the method's name
  ReqParam, // extract attribute value from method parameters
  ResStatus, // extract attribute value from http/method as int status code
  ResValue, // extract attribute value from method returned value as json
  ResCause, // extract attribute value from method throwed causes's class & message
  ResHeader // extract attribute value from http respond's header
}
