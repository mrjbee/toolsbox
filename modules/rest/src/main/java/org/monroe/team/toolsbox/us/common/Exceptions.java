package org.monroe.team.toolsbox.us.common;


final public class Exceptions {
    public static class IdNotFoundException extends InvalidIdException {

        public IdNotFoundException(String resourceId) {
            super(resourceId);
        }
    }
    public static class InvalidIdException extends InvalidRequestException {

        public final String id;

        public InvalidIdException(String id) {
            this.id = id;
        }
    }

    public static class InvalidRequestException extends Exception{
    }

}
