/**
 * Created by carlaurrea on 15/2/17.
 */
public class Token {

    private Type token;
    private String lexema;

    public Token(String lexema) {
        this.lexema = lexema;
    }

    public Token(Type token, String lexema) {
        this.token = token;
        this.lexema = lexema;
    }
}
