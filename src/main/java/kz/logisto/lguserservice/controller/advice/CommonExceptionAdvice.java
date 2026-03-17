package kz.logisto.lguserservice.controller.advice;

import kz.logisto.lguserservice.data.model.ExceptionModel;
import kz.logisto.lguserservice.exception.ForbiddenException;
import kz.logisto.lguserservice.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonExceptionAdvice {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ExceptionModel> handleNotFoundException(NotFoundException exception) {
    return handleException(HttpStatus.NOT_FOUND, exception);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ExceptionModel> handleForbiddenException(ForbiddenException exception) {
    return handleException(HttpStatus.FORBIDDEN, exception);
  }

  private ResponseEntity<ExceptionModel> handleException(HttpStatus status, Exception exception) {
    if (StringUtils.hasText(exception.getMessage())) {
      return ResponseEntity
          .status(status)
          .body(new ExceptionModel(exception.getMessage()));
    }
    return ResponseEntity.status(status)
        .build();
  }
}
