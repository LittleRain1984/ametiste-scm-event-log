package org.ametiste.scm.log.controller;

import org.ametiste.scm.log.service.ReplayOperationException;
import org.ametiste.scm.log.service.ReplayTaskNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class EventReplayExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(EventReplayController.class);

    @ExceptionHandler(ReplayTaskNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Requested replay task not found")
    public void handleReplayTaskNotFoundException(ReplayTaskNotFoundException e) {
        if (logger.isErrorEnabled()) {
            logger.error(e.getMessage(), e);
        }
    }

    @ExceptionHandler(ReplayOperationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleReplayOperationException(ReplayOperationException e) {
        if (logger.isErrorEnabled()) {
            logger.error(e.getMessage(), e);
        }
        return e.getMessage();
    }
}
