package com.zsecurity.demo.exceptions;

import com.zsecurity.demo.dtos.ResponseOrderDetails;
import io.jsonwebtoken.JwtException;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseOrderDetails.ApiError> exception(BadCredentialsException msg){
        ResponseOrderDetails.ApiError apiError = ResponseOrderDetails.ApiError.builder()
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .message(msg.getMessage()).build();

        return ResponseEntity.status(401).body(apiError);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ResponseOrderDetails.ApiError> invalidOtpException(InvalidOtpException msg){
        ResponseOrderDetails.ApiError apiError = ResponseOrderDetails.ApiError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .message(msg.getMessage()).build();

        return ResponseEntity.status(409).body(apiError);
    }

    @ExceptionHandler(InvalidOtpExpired.class)
    public ResponseEntity<ResponseOrderDetails.ApiError> invalidOtpExpired(InvalidOtpExpired msg){
        ResponseOrderDetails.ApiError apiError = ResponseOrderDetails.ApiError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .message(msg.getMessage()).build();

        return ResponseEntity.status(409).body(apiError);
    }


    @ExceptionHandler(UserAlreadyExistsWithEmail.class)
    public ResponseEntity<ResponseOrderDetails.ApiError> userAlreadyExistsWithEmail(UserAlreadyExistsWithEmail msg){
        ResponseOrderDetails.ApiError apiError = ResponseOrderDetails.ApiError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(msg.getMessage()).build();

        return ResponseEntity.badRequest().body(apiError);
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseOrderDetails.ApiError> authenticationException(AuthenticationException msg){
        ResponseOrderDetails.ApiError apiError = ResponseOrderDetails.ApiError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(msg.getMessage()).build();

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ResponseOrderDetails.ApiError> jtException(JwtException msg){
        ResponseOrderDetails.ApiError apiError = ResponseOrderDetails.ApiError.builder()
                .httpStatus(HttpStatus.FORBIDDEN)
                .message(msg.getLocalizedMessage()).build();

        return ResponseEntity.badRequest().body(apiError);
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ResponseOrderDetails.ApiError> usernameNotFoundException(UsernameNotFoundException msg){
        ResponseOrderDetails.ApiError apiError = ResponseOrderDetails.ApiError.builder()
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .message(msg.getMessage()).build();

        return ResponseEntity.badRequest().body(apiError);
    }
    @ExceptionHandler(UserNotYetLoggedInException.class)
    public ResponseEntity<ResponseOrderDetails.ApiError> userNotYseLoggedIn(UserNotYetLoggedInException msg){
        ResponseOrderDetails.ApiError apiError = ResponseOrderDetails.ApiError.builder()
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .message(msg.getMessage()).build();

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseOrderDetails.ApiError> exception(MethodArgumentNotValidException
                                                          msg){
        ResponseOrderDetails.ApiError apiError = ResponseOrderDetails.ApiError.builder()
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .message(msg.getBindingResult().getFieldError().getDefaultMessage()).build();

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(IncorrectOldPasswordException.class)
    public ResponseEntity<ResponseOrderDetails.ApiError> exception(IncorrectOldPasswordException
                                                      msg){
        ResponseOrderDetails.ApiError apiError = ResponseOrderDetails.ApiError.builder()
                .httpStatus(HttpStatus.FORBIDDEN)
                .message(msg.getMessage()).build();

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseOrderDetails.ApiError> exception(Exception
                                                      msg){
        ResponseOrderDetails.ApiError apiError = ResponseOrderDetails.ApiError.builder()
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .message(msg.getMessage()).build();

        return ResponseEntity.badRequest().body(apiError);
    }


}
