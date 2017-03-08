import Analyzer.LexicographicAnalyzer;
import Analyzer.SyntacticAnalyzer;
import Analyzer.Token;
import Analyzer.Type;

import java.io.*;


public class Main {

    public static void main(String[] args) {

        if(args.length == 0){
            System.out.println("Es requereix el nom del fitxer com a paràmetre.");
            return;
        } else if(args.length > 1){
            System.out.println("Es requereix el nom d'un únic fitxer com a paràmetre.");
            return;
        }

        String filename = args[0];

        try {
            SyntacticAnalyzer syntactic = SyntacticAnalyzer.getInstance(filename);

            syntactic.programa();
//            LexicographicAnalyzer lexic = LexicographicAnalyzer.getInstance(filename);
//
//            Token token;
//
//            do{
//                token = lexic.getToken();
//            }while (token.getToken() != Type.EOF);
//
//            lexic.close();
//            System.out.println("Anàlisi lexicogràfic realitzat correctament.");

        } catch (IOException e) {
            System.out.println("No s'ha pogut obrir l'arxiu.");
        }

    }
}