package org.monroe.team.toolsbox.transport;


import org.monroe.team.toolsbox.transport.rest.RestExceptions;
import org.springframework.http.HttpStatus;

final public class TransportExceptions {

    private TransportExceptions() {}

    public static class IdNotFoundException extends RestExceptions.DetailedRestException {

        public IdNotFoundException(String resourceId) {
            super(HttpStatus.NOT_FOUND,"not_found","Unavailable resource id "+resourceId);
        }
    }

    public static class InvalidIdException extends RestExceptions.DetailedRestException {

        public InvalidIdException(String id) {
            super(HttpStatus.BAD_REQUEST,"invalid_id","Invalid id format:"+id);

        }
    }

    public static class InvalidRequestException extends RestExceptions.DetailedRestException{

        public InvalidRequestException() {
            super(HttpStatus.BAD_REQUEST,"bad_format");
        }

        public InvalidRequestException(Throwable cause) {
            super(HttpStatus.BAD_REQUEST,"bad_format","", cause);
        }
    }

    public static class InvalidOperationException extends RestExceptions.DetailedRestException{

        public InvalidOperationException(String label, String msg, Throwable cause) {
            super(HttpStatus.CONFLICT, label, msg, cause);
        }
    }
}
