package org.monroe.team.toolsbox.transport.rest;

import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.logging.Logs;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//TODO: org.springframework.web.HttpMediaTypeNotSupportedException
@ControllerAdvice
public class GlobalController {

    public static Logger log = Logs.core;

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public void onUnexpectedException(Exception exception) throws Exception {
        log.error("Unexpected exception", exception);
        checkIfRequiredRethrow(exception);
    }

    @ExceptionHandler(value = RestExceptions.RestException.class)
    public void onExpectedException(HttpServletResponse servletResponse, RestExceptions.RestException exception) throws Exception {
        log.info("Rest request ends with "+exception.status.value() +"["+exception.status.getReasonPhrase()+"]");
        log.info("Rest request ends with " + exception.status.value() + "[" + exception.status.getReasonPhrase() + "]", exception);
        checkIfRequiredRethrow(exception);
        servletResponse.sendError(exception.status.value(),exception.status.getReasonPhrase());
    }


    @ExceptionHandler(value = RestExceptions.DetailedRestException.class)
    public @ResponseBody
    RestExceptions.DetailedRestException.ErrorDetails onExpectedDetailedException(
            HttpServletRequest request,HttpServletResponse servletResponse,
            RestExceptions.DetailedRestException exception) throws Exception {
        log.info("Rest request ends with "+exception.status.value() +"["+exception.label+":"+exception.msg+"]");
        log.info("Rest request ends with " + exception.status.value() + "[" + exception.label + ":" + exception.msg + "]", exception);
        checkIfRequiredRethrow(exception);

        if (!"application/json".equals(request.getContentType())){
            servletResponse.sendError(exception.status.value(),exception.label+":"+exception.msg);
            return null;
        } else {
            servletResponse.setStatus(exception.status.value());
            return exception.asError();
        }
    }


    private void checkIfRequiredRethrow(Exception exception) throws Exception {
        // If the exception is annotated with @ResponseStatus rethrow it and let
        // the framework handle it - like the OrderNotFoundException example
        // at the start of this post.
        // AnnotationUtils is a Spring Framework utility class.
        if (AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class) != null)
            throw exception;
    }

}
