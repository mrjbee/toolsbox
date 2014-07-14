package org.monroe.team.toolsbox.transport.timer;

import org.springframework.http.HttpStatus;

public final class Exceptions {

    private Exceptions() {}

    public static class RestException extends RuntimeException{

        public final HttpStatus status;

        public RestException(HttpStatus status) {
            this.status = status;
        }

        public RestException(HttpStatus status, Throwable cause) {
            super(cause);
            this.status = status;
        }
    }

    public static class DetailedRestException extends RestException{

        public final String label;
        public final String msg;

        public DetailedRestException(HttpStatus status, String label) {
            super(status);
            this.label = label;
            this.msg = "";
        }

        public DetailedRestException(HttpStatus status, String label, String msg) {
            super(status);
            this.label = label;
            this.msg = msg;
        }

        public DetailedRestException(HttpStatus status, String label, String msg, Throwable cause) {
            super(status, cause);
            this.label = label;
            this.msg = msg;
        }

        public ErrorDetails asError(){
            return new ErrorDetails(label, msg);
        }

        public static class ErrorDetails {
            public final String label;
            public final String msg;

            public ErrorDetails(String label, String msg) {
                this.label = label;
                this.msg = msg;
            }
        }
    }

}
