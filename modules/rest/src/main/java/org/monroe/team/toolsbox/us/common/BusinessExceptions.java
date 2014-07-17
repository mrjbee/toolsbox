package org.monroe.team.toolsbox.us.common;

final public class BusinessExceptions {

    private BusinessExceptions() {}

    public static  class  FileOperationFailException extends Exception{
        public FileOperationFailException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static  class  FileNotFoundException extends Exception{}
}
