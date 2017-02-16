import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class LexicographicAnalyzer {
    private static LexicographicAnalyzer instance;
    private static int nLine;
    private static int nChar;
    private static String line;
    private static Scanner file;


    public static LexicographicAnalyzer getInstance(String fileName) {
        if (instance == null) instance = new LexicographicAnalyzer(fileName);
        return instance;
    }

    public int getLine(){ return nLine;}

    private LexicographicAnalyzer(String fileName) {
        nLine = 1;
        nChar = 0;

        try {
            file = new Scanner(new FileReader(fileName));
            if(file.hasNext()){
                line = file.nextLine();

                //ADD '\n'
                line = line + '\n';
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public Token getToken() throws Exception {
        char character;
        int state = 0;
        String lexema = "";
        while(true) {


            character = line.charAt(nChar);


            switch (state) {
                case 0:
                    if (character == ' ' || character == '\t') {
                        state = 0;
                        nChar++;
                    } else if (character == '\n') {
                        nLine++;
                        nChar = 0;
                        if (file.hasNext()) {
                            line = file.nextLine();

                            //ADD '\n'
                            line = line + '\n';
                        } else {
                            throw new Exception("");
                            // EOF !

                        }
                        state = 0;
                    } else if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(character) != -1) {
                        state = 1;
                    } else if ("0123456789".indexOf(character) != -1) {
                        state = 2;
                    } else if (character == '"') {
                        state = 3;
                    } else if ("<>=".indexOf(character) != -1) {
                        state = 4;
                    } else if ("/+-*".indexOf(character) != -1) {
                        state = 5;
                    } else if ("()[],;:.".indexOf(character) != -1){
                        //Special symbols
                        state = 6;
                    } else {

                        //ERROR, INVALID CHARACTER
                        //(Class Error)

                        System.out.println("ERROR CARACTER: "+ character);

                        //PROVISIONAL
                        return new Token(Type.TOKEN_ERR, "");
                    }
                    break;
                case 1:
                    if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_".indexOf(character) != -1) {
                        state = 1;
                        lexema = lexema + character;
                        nChar++;
                    } else {
                        return new Token(lexema);
                    }
                    break;
                case 2:
                    if ("0123456789".indexOf(character) != -1) {
                        state = 2;
                        lexema = lexema + character;
                        nChar++;

                    } else {
                        return new Token(Type.TOKEN_INT, lexema);
                    }
                    break;
                case 3:

                    do{
                        lexema = lexema + character;
                        nChar++;
                        character = line.charAt(nChar);
                    }while(character != '"');
                    nChar++;

                    return new Token(Type.TOKEN_STRING, lexema+'"');
                case 4:
                    switch (character){
                        case '=':
                            nChar++;
                            if(line.indexOf(nChar) == '='){
                                return new Token(Type.TOKEN_EQUALS, "==");
                            } else {
                                return new Token(Type.TOKEN_SET,"=");
                            }
                        case '>':
                            nChar++;
                            if(line.indexOf(nChar) == '='){
                                return new Token(Type.TOKEN_GTEQ, ">=");
                            } else {
                                return new Token(Type.TOKEN_GT, ">");
                            }
                        case '<':
                            nChar++;
                            if(line.indexOf(nChar) == '='){
                                return new Token(Type.TOKEN_LTEQ, "<=");
                            } else if (line.indexOf(nChar) == '>'){
                                return new Token(Type.TOKEN_DIFF, "<>");
                            } else {
                                return new Token(Type.TOKEN_LT, "<");
                            }
                    }

                case 5:
                    nChar++;
                    switch (character) {
                        case '+':
                            return new Token(Type.TOKEN_SUM, "+");
                        case '-':
                            return new Token(Type.TOKEN_RES, "-");
                        case '/':
                            return new Token(Type.TOKEN_DIV, "/");
                        case '*':
                            return new Token(Type.TOKEN_MUL, "*");
                }

                case 6:
                    nChar++;
                    switch(character) {
                        //Case ".."
                        case '.':
                            if (line.charAt(nChar) == '.') {
                                nChar ++;
                                return new Token (Type.TOKEN_DPOINT, "..");
                            } else {

                                //CASE ONLY ".", ERROR!

                                //TEMPORAL
                                System.out.println("ERROR CARACTER: "+ character);
                                return new Token(Type.TOKEN_ERR, "");
                            }

                        case '(':
                            return new Token(Type.TOKEN_OPARENT, "(");
                        case ')':
                            return new Token(Type.TOKEN_CPARENT, ")");
                        case '[':
                            return new Token(Type.TOKEN_OCLAU, "[");
                        case ']':
                            return new Token(Type.TOKEN_CCLAU, "]");
                        case ',':
                            return new Token(Type.TOKEN_COMA, ",");
                        case ';':
                            return new Token(Type.TOKEN_SEMICOLON, ";");
                        case ':':
                            return new Token(Type.TOKEN_COLON, ":");

                    }
            }
        }
    }

}
