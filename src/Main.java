import analyzer.SyntacticClean;

import java.io.IOException;


public class Main {

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("Es requereix el nom del fitxer com a paràmetre.");
			return;
		} else if (args.length > 1) {
			System.out.println("Es requereix el nom d'un únic fitxer com a paràmetre.");
			return;
		}

		String filename = args[0];

		try {
//            SyntacticAnalyzer syntactic = SyntacticAnalyzer.getInstance(filename);
			SyntacticClean syntactic = SyntacticClean.getInstance(filename);
			try {
				syntactic.programa();
			} catch (Exception e) {
				e.printStackTrace();
				syntactic.end();
			}

		} catch (IOException e) {
			System.out.println("No s'ha pogut obrir l'arxiu.");
		}

	}
}