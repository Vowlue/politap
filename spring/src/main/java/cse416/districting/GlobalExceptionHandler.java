package cse416.districting;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import cse416.districting.dto.ExceptionResponse;

@ControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(value = Exception.class)
  public ExceptionResponse defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
    ExceptionResponse res = new ExceptionResponse();
    res.setException(e);
    res.setUrl(req.getRequestURI());
    System.out.println(e);
    return res;
  }
}