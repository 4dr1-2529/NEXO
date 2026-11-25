package com.estacionnexo.api;

import com.estacionnexo.exception.ReglaIncumplida;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = OperacionesApi.class)
public class ErroresApi {
    @ExceptionHandler(ReglaIncumplida.class)
    ProblemDetail negocio(ReglaIncumplida error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, error.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    ProblemDetail duplicado() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
            "La solicitud ya fue procesada o coincide con un registro existente.");
    }
}
