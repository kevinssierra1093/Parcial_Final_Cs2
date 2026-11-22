package application.alquimia.domain.exceptions;

public class InvalidDeviceException extends RuntimeException {
    public InvalidDeviceException(String message) {
        super(message);
    }
}
