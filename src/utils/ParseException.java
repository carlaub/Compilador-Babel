package utils;

/**
 * Excepció creada amb l'objectiu d'identificar aquelles excepcions donades a l'hora de parsejar el codi font.
 * L'encarregat de fer aquest parse és l'analitzador sintàctic, així que serà la classe {@link Analyzer.SyntacticAnalyzer}
 * la que utilitzarà aquesta excepció.
 */
public class ParseException extends Exception {
	/**
	 * Mètode constructor de l'excepció, necessita el tipus d'error {@link TypeError} per poder ser llençada.
	 * @param typeError Tipus d'error causant del llençament de l'excepció
	 */
    public ParseException(TypeError typeError) {
        super(typeError.toString());
    }
}
