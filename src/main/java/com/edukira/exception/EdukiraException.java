package com.edukira.exception;
import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class EdukiraException extends RuntimeException {
    private final HttpStatus status;
    public EdukiraException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    public static EdukiraException notFound(String resource) {
        return new EdukiraException(resource + " não encontrado(a)", HttpStatus.NOT_FOUND);
    }
    public static EdukiraException forbidden() {
        return new EdukiraException("Acesso negado", HttpStatus.FORBIDDEN);
    }
    public static EdukiraException badRequest(String msg) {
        return new EdukiraException(msg, HttpStatus.BAD_REQUEST);
    }
}
