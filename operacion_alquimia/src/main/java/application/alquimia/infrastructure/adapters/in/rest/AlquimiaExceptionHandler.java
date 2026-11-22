package application.alquimia.infrastructure.adapters.in.rest;

import application.alquimia.domain.exceptions.InsufficientDataException;
import application.alquimia.domain.exceptions.InvalidDeviceException;
import application.alquimia.domain.exceptions.LocationCalculationException;
import application.alquimia.domain.exceptions.MessageReconstructionException;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AlquimiaExceptionHandler {

    @ExceptionHandler({
            InsufficientDataException.class,
            LocationCalculationException.class,
            MessageReconstructionException.class
    })
    public ResponseEntity<Map<String, Object>> handleNotFoundErrors(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "No se puede determinar la posición o mensaje",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(InvalidDeviceException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(InvalidDeviceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Dispositivo inválido",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Formato JSON inválido",
                "message", "El cuerpo de la petición no tiene un formato JSON válido o esperado."
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", ex.getClass().getSimpleName(),
                "message", ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor"
        ));
    }
}
