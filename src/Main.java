import java.io.*;
import java.util.regex.Pattern;



public class Main {
    private static final String FILE_NAME = "babelProg.bab";
    private static BufferedWriter bwLex;

    public static void main(String[] args) {

        LexicographicAnalyzer lexic = LexicographicAnalyzer.getInstance(FILE_NAME);
        Token token;
        File lex;


        //Create .lex file
        //Divide fileName to ignore extension
        lex = new File (FILE_NAME.split(Pattern.quote("."))[0] + ".lex");

        try {
            bwLex = new BufferedWriter(new FileWriter(lex));
            do{
                token = lexic.getToken();
                System.out.println(lexic.getActualLine() + ":" + lexic.getActualChar()
                        + "< " + token.getTokenName() + ", " + token.getLexema() + " >");

                //write token information to the *.lex file
                bwLex.write("< " + token.getTokenName() + ", " + token.getLexema() + " >\n");

            }while (true);

        }catch (EOFException e){
            System.out.println("FIPROGRAMA");
        } catch (IOException e1) {
            e1.printStackTrace();
        }finally{
            if (bwLex != null) {
                try {
                    bwLex.close();
                    lexic.finalize();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}