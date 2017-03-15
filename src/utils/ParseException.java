package utils;


public class ParseException extends Exception {
    public ParseException(TypeError typeError) {
        super(typeError.toString());
    }
}
