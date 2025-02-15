package fr.mazure.vision;

/**
 * Custom exception class for handling AI-related errors.
 * Extends RuntimeException to provide unchecked exception handling.
 */
public class AiException extends RuntimeException {
    public AiException(final String message) {
        super(message);
    }

    public AiException(final String message, final Throwable cause) {
        super(message, cause);
    }
}