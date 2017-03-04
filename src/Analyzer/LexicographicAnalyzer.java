package Analyzer;

import utils.Error;
import utils.TypeError;

import java.io.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class LexicographicAnalyzer {
    private static LexicographicAnalyzer instance;
    private static int nLine;
    private static int nChar;
    private static String line;
    private static Scanner file;
    private static BufferedWriter bwLex;

    private static Error errorManagement;


    public static LexicographicAnalyzer getInstance(String fileName) throws IOException {
        if (instance == null) instance = new LexicographicAnalyzer(fileName);
        return instance;
    }

    public int getActualLine(){ return nLine;}

    private LexicographicAnalyzer(String fileName) throws IOException {
        nLine = 1;
        nChar = 0;


        //Instance of error class
        errorManagement = Error.getInstance(fileName);
        File lex = new File (fileName.split(Pattern.quote("."))[0] + ".lex");

        bwLex = new BufferedWriter(new FileWriter(lex));

        //Read code
        file = new Scanner(new FileReader(fileName));
        if(file.hasNext()){
            line = file.nextLine();

            //ADD '\n'
            line = line + '\n';
        }

    }

    public Token getToken() {
        Token token = nextToken();
        System.out.println(nLine + ":" + nChar
                + "< " + token.getTokenName() + ", " + token.getLexema() + " >");
        try {
            bwLex.write("< " + token.getTokenName() + ", " + token.getLexema() + " >\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return token;
    }

    private Token nextToken(){
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
                            return new Token(Type.EOF, "EOF");
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
                    } else if ("()[],;:.?".indexOf(character) != -1){
                        //Special symbols
                        state = 6;
                    } else {

                        //ERROR, INVALID CHARACTER

                        errorManagement.insertLexError(TypeError.ERR_LEX_1, getActualLine(), character);

                        System.out.println("ERROR CARACTER: "+ character);

                        nChar++;
                        state = 0;
//                        //PROVISIONAL
//                        return new Analyzer.Token(Analyzer.Type.TOKEN_ERR, "");
                    }
                    break;
                case 1:
                    if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_".indexOf(character) != -1) {
                        state = 1;
                        lexema = lexema + character;
                        nChar++;
                    } else {
                        if(lexema.length()>32){
                            errorManagement.insertLexError(TypeError.WAR_LEX_1, nLine, lexema);
                            lexema = lexema.substring(0,31);
                        }
                        return new Token(lexema);
                    }
                    break;
                case 2:
                    if ("0123456789".indexOf(character) != -1) {
                        state = 2;
                        lexema = lexema + character;
                        nChar++;

                    } else {
                        return new Token(Type.SENCER_CST, lexema);
                    }
                    break;
                case 3:

                    do{
                        lexema = lexema + character;
                        nChar++;
                        character = line.charAt(nChar);
                    }while(character != '"');
                    nChar++;

                    return new Token(Type.CADENA, lexema+'"');
                case 4:
                    switch (character){
                        case '=':
                            nChar++;
                            if(line.charAt(nChar) == '='){
                                nChar++;
                                return new Token(Type.OP_RELACIONAL, "==");
                            } else {
                                return new Token(Type.IGUAL,"=");
                            }
                        case '>':
                            nChar++;
                            if(line.charAt(nChar) == '='){
                                nChar++;
                                return new Token(Type.OP_RELACIONAL, ">=");
                            } else {
                                return new Token(Type.OP_RELACIONAL, ">");
                            }
                        case '<':
                            nChar++;
                            if(line.charAt(nChar) == '='){
                                nChar++;
                                return new Token(Type.OP_RELACIONAL, "<=");
                            } else if (line.charAt(nChar) == '>'){
                                nChar++;
                                return new Token(Type.OP_RELACIONAL, "<>");
                            } else {
                                return new Token(Type.OP_RELACIONAL, "<");
                            }
                    }

                case 5:
                    nChar++;
                    switch (character) {
                        case '+':
                            return new Token(Type.SUMA, "+");
                        case '-':
                            return new Token(Type.RESTA, "-");
                        case '/':
                            if(line.charAt(nChar) == '/'){
                                nLine++;
                                nChar = 0;
                                if (file.hasNext()) {
                                    line = file.nextLine();
                                    line = line + '\n';
                                } else {
                                    return new Token(Type.EOF, "EOF");
                                }
                            }
                            else{
                                return new Token(Type.DIV, "/");
                            }
                            state = 0;
                            break;
                        case '*':
                            return new Token(Type.MUL, "*");
                    }
                    break;

                case 6:
                    nChar++;
                    switch(character) {
                        //Case ".."
                        case '.':
                            if (line.charAt(nChar) == '.') {
                                nChar ++;
                                return new Token(Type.DPOINT, "..");
                            } else {

                                //CASE ONLY ".", ERROR!
                                errorManagement.insertLexError(TypeError.ERR_LEX_1, getActualLine(), character);
                                //TEMPORAL
                                System.out.println("ERROR CARACTER: "+ character);
                                state = 0;

//                                return new Analyzer.Token(Analyzer.Type.TOKEN_ERR, "");
                            }
                            break;
                        case '(':
                            return new Token(Type.OPARENT, "(");
                        case ')':
                            return new Token(Type.CPARENT, ")");
                        case '[':
                            return new Token(Type.OCLAU, "[");
                        case ']':
                            return new Token(Type.CCLAU, "]");
                        case ',':
                            return new Token(Type.COMA, ",");
                        case ';':
                            return new Token(Type.SEMICOLON, ";");
                        case ':':
                            return new Token(Type.COLON, ":");
                        case '?':
                            return new Token(Type.TERNARIA, "?");

                    }
            }
        }
    }

    public void finalize() {
        errorManagement.closeBuffer();
        try {
            bwLex.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
