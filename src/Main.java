import java.io.EOFException;

/**
 * Created by alexj on 15/2/2017.
 */

public class Main {
    public static void main(String[] args) {

        LexicographicAnalyzer lexic = LexicographicAnalyzer.getInstance("test.bab");
        Token token;

        //PROVES
        try {
            do{
                token = lexic.getToken();
                System.out.println(lexic.getActualLine() + ":" + lexic.getActualChar()
                        + "< " + token.getTokenName() + ", " + token.getLexema() + " >");
            }while (true);
        }catch (EOFException e){System.out.println("FIPROGRAMA");}


    }
}