/**
 * Created by alexj on 15/2/2017.
 */

public class Main {
    public static void main(String[] args) {
        LexicographicAnalyzer lexic = LexicographicAnalyzer.getInstance("test.bab");


        //PROVES
        try {
            Token token = lexic.getToken();
            System.out.println("< " + token.getTokenName() + ", " + token.getLexema() + " >");
            while (true) {

                token = lexic.getToken();
                System.out.println(lexic.getLine() + "< " + token.getTokenName() + ", " + token.getLexema() + " >");
            }
        }catch (Exception e){System.out.println("FIPROGRAMA");}
        /*
        for (int i = 0; i < 15; i++) {
            token = lexic.getToken();
            System.out.println("< " + token.getTokenName() + ", " + token.getLexema() + " >");
        }
        */




    }
}