package org.dcsa.ebl.error;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InputDecodingErrorHandler {

    @ExceptionHandler(DecodingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    // Spring's default handler for unknown JSON properties is useless for telling the user
    // what they did wrong.  Unwrap the inner UnrecognizedPropertyException, which has a
    // considerably better message.
    public void handleCustomException(DecodingException ce) {
        if (ce.getCause() instanceof UnrecognizedPropertyException) {
            throw new InputParsingException(ce.getCause().getLocalizedMessage(), ce);
        }
        throw new InputParsingException(ce.getLocalizedMessage(), ce);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InputParsingException extends RuntimeException {
        public InputParsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
