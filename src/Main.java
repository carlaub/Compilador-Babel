import java.io.*;
import java.util.regex.Pattern;



public class Main {
    private static final String FILE_NAME = "test.bab";

    public static void main(String[] args) {

        LexicographicAnalyzer lexic = LexicographicAnalyzer.getInstance(FILE_NAME);
        Token token;

            do{
                token = lexic.getToken();

                //write token information to the *.lex file
                //bwLex.write("< " + token.getTokenName() + ", " + token.getLexema() + " >\n");

            }while (token.getToken() != Type.TOKEN_EOF);
            lexic.finalize();

    }
}