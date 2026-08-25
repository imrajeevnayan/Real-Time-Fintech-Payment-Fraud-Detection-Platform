package com.aegispay.shared.config;

import com.aegispay.ingestion.api.dto.TransactionRejectedException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** RFC 7807 responses for the entire application. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail onValidation(MethodArgumentNotValidException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        pd.setType(URI.create("https://aegispay.io/problems/validation"));
        pd.setTitle("Invalid transaction payload");
        pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList());
        return pd;
    }

    @ExceptionHandler(TransactionRejectedException.class)
    ProblemDetail onRejected(TransactionRejectedException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setType(URI.create("https://aegispay.io/problems/transaction-rejected"));
        pd.setTitle("Transaction rejected");
        return pd;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail onUnexpected(Exception ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Reference this response when contacting support.");
        pd.setType(URI.create("https://aegispay.io/problems/internal"));
        pd.setTitle("Internal error");
        return pd;
    }
}
