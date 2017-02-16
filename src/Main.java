/**
 * Created by alexj on 15/2/2017.
 */

public class Main {
    public static void main(String[] args) {
        LexicographicAnalyzer lexic = LexicographicAnalyzer.getInstance("babelProg.bab");


        //PROVES
        Token token = lexic.getToken();
        System.out.println("< " + token.getTokenName() + ", " + token.getLexema() + " >");

        for (int i = 0; i < 15; i++) {
            token = lexic.getToken();
            System.out.println("< " + token.getTokenName() + ", " + token.getLexema() + " >");
        }





    }
}