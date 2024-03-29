package utils;

import analyzer.LexicographicAnalyzer;
import analyzer.Type;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Descriptor d'errors. S'encarrega d'escriure els errors trobats a un fitxer.
 * En cas que es tracti d'un warning escriu el seu tractament.
 */
public class Error {
	private static Error instance;
	private static BufferedWriter bwErr;
	private static LexicographicAnalyzer lexic;

	public static Error getInstance() {
		return instance;
	}

	/**
	 * Mètode públic per a obtenir una instància del descriptor d'errors.
	 * Com que s'utilitza el patró Singleton sempre retorna la mateixa instància.
	 *
	 * @param fileName Arxiu on s'escriuran els errors trobats.
	 * @return Instància única del descriptor d'errors.
	 */
	public static Error getInstance(String fileName) {
		if (instance == null) {
			instance = new Error(fileName);
		}
		return instance;
	}

	/**
	 * Constructor privat de {@link Error}. Privat a causa del patró Singleton.
	 *
	 * @param fileName Arxiu on s'escriuran els errors trobats.
	 */
	private Error(String fileName) {
		File err = new File(fileName.split(Pattern.quote("."))[0] + ".err");
		lexic = LexicographicAnalyzer.getInstance();

		try {
			bwErr = new BufferedWriter(new FileWriter(err));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 *
	 * @param error     Codi de l'error
	 * @param numLine   Número de la línia on s'ha trobat l'error
	 * @param character Caràcter causant de l'error
	 */
	public void insertError(TypeError error, int numLine, char character) {
		try {
			//Write error into *.err file
			switch (error) {

				case ERR_LEX_1:
					bwErr.write("[" + error.toString() + "] " + numLine + ", Caràcter[" + character + "] desconegut\n");
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 *
	 * @param error   Codi de l'error
	 * @param numLine Número de la línia on s'ha trobat l'error
	 * @param string  String causant de l'error
	 */
	public void insertError(TypeError error, int numLine, String string) {
		try {
			//Write error into *.err file
			switch (error) {

				case WAR_LEX_1:
					bwErr.write("[" + error + "] " + numLine + ", Llargada màxima és 32 caràcters.\n");
					bwErr.write("Canvi de " + string + " a " + string.substring(0, 31) + "\n");
					break;

				case WAR_LEX_2:
					bwErr.write("[" + error + "] " + numLine + ", Falta tancar la cadena.\n");
					bwErr.write("Canvi de <" + string + "> a <" + string + "\">\n");
					break;

				case ERR_SEM_16:
					bwErr.write("[" + error + "] " + lexic.getActualLine() + ", El tipus del paràmetre número " + numLine +
							" no coincideix amb el tipus de la seva declaració " + string + "\n");
					break;

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 *
	 * @param error   Codi de l'error
	 * @param numLine Número de la línia on s'ha trobat l'error
	 * @param types   Conjunt de tokens esperats
	 * @param token   Token causant de l'error
	 */
	public void insertError(TypeError error, int numLine, Type[] types, Type token) {
		try {
			//Si realment no cridarem aquesta funció en cap altre situació ens podem estalviar el switch
			switch (error) {
				case ERR_SIN_1:
				case ERR_SIN_8:

					bwErr.write("[" + error + "] " + numLine + ", Esperava <" +
							Arrays.toString(types).replaceAll("[\\[\\]]", "") +
							"> però he trobat <" + token + ">.\n");
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 *
	 * @param error   Codi de l'error
	 * @param numLine Número de la línia on s'ha trobat l'error
	 * @param token   Token causant de l'error
	 */
	public void insertError(TypeError error, int numLine, Type token) {
		try {
			//Si realment no cridarem aquesta funció en cap altre situació ens podem estalviar el switch
			switch (error) {
				case ERR_SIN_2:
					bwErr.write("[" + error + "] " + numLine + ", Falta el token " + token + ".\n");
					break;
				case ERR_SIN_7:
					bwErr.write("[" + error + "] " + numLine + ", Construcció d'instrucció " + token + " incorrecta.\n");
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 *
	 * @param error   Codi de l'error
	 * @param numLine Número de la línia on s'ha trobat l'error
	 */
	public void insertError(TypeError error, int numLine) {
		try {
			switch (error) {
				case ERR_SIN_3:
					bwErr.write("[" + error + "] " + numLine + ", La construcció de la declaració de la " +
							"constant no és correcta.\n");
					break;
				case ERR_SIN_4:
					bwErr.write("[" + error + "] " + numLine + ", La construcció de la declaració de la " +
							"variable no és correcta.\n");
					break;
				case ERR_SIN_5:
					bwErr.write("[" + error + "] " + numLine + ", La capçalera de la funció conté errors.\n");
					break;
				case ERR_SIN_6:
					bwErr.write("[" + error + "] " + numLine + ", Hi ha codi després de fi del programa.\n");
					break;
				case ERR_SIN_9:
					bwErr.write("[" + error + "] " + "El procediment principal conté errors.\n");
					break;
				case ERR_SIN_10:
					bwErr.write("[" + error + "] " + numLine + ", El format de la instrucció és invàlid.\n");
					break;
				case ERR_SEM_17:
					bwErr.write("[" + error + "] " + lexic.getActualLine() + ", El paràmetre número " + numLine +
							" de la funció no es pot passar per referència.\n");

					break;
			}
		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 *
	 * @param error  Codi de l'error
	 * @param string Cadena amb la informació a mostrar segons l'error
	 */
	public void insertError(TypeError error, String string) {
		int numLine = lexic.getActualLine();
		try {
			switch (error) {
				case ERR_SEM_1:
					bwErr.write("[" + error + "] " + numLine + ", Constant <" + string + "> doblement definida.\n");
					break;
				case ERR_SEM_2:
					bwErr.write("[" + error + "] " + numLine + ", Variable <" + string + "> doblement definida.\n");
					break;
				case ERR_SEM_3:
					bwErr.write("[" + error + "] " + numLine + ", Funció <" + string + "> doblement definida.\n");
					break;
				case ERR_SEM_4:
					bwErr.write("[" + error + "] " + numLine + ", Paràmetre <" + string + "> doblement definida.\n");
					break;
				case ERR_SEM_9:
					bwErr.write("[" + error + "] " + numLine + ", L'identificador <" + string + "> no ha estat declarat.\n");
					break;
				case ERR_SEM_10:
					bwErr.write("[" + error + "] " + numLine + ", L'identificador <" + string +
							"> en la instrucció LLEGIR no és una variable de tipus simple.\n");
					break;
				case ERR_SEM_11:
					bwErr.write("[" + error + "] " + numLine + ", L'identificador <" + string +
							"> en part esquerra d'assignació no és una variable.\n");
					break;
				case ERR_SEM_13:
					bwErr.write("[" + error + "] " + numLine + ", El tipus de l'índex d'accés al vector <" + string +
							"> no és sencer.\n");
					break;
				case ERR_SEM_22:
					bwErr.write("[" + error + "] " + numLine + ", La funció <" + string +
							"> en part dreta de l'assignació no està sent invocada.\n");
					break;
				case ERR_SEM_23:
					bwErr.write("[" + error + "] " + numLine + ", L'identificador <" + string +
							"> no és de tipus VECTOR.\n");
					break;
				case WAR_OPC_2:
					bwErr.write("[" + error + "] " + numLine + ", Accés al VECTOR <" + string +
							"> fora dels límits.\n");
					break;
				case ERR_SEM_21:
					bwErr.write("[" + error + "] " + numLine + ", Identificador <" + string +
							"> prèviament definit.\n");
					break;

			}
		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 *
	 * @param error Codi de l'error
	 */
	public void insertError(TypeError error) {
		int numLine = lexic.getActualLine();
		try {
			switch (error) {
				case ERR_SEM_5:
					bwErr.write("[" + error + "] " + numLine + ", Límits decreixents en el vector.\n");
					break;
				case ERR_SEM_6:
					bwErr.write("[" + error + "] " + numLine + ", El tipus de l'expressió no és SENCER.\n");
					break;
				case ERR_SEM_7:
					bwErr.write("[" + error + "] " + numLine + ", El tipus de l'expressió no és LOGIC.\n");
					break;
				case ERR_SEM_8:
					bwErr.write("[" + error + "] " + numLine + ", La condició no és de tipus LOGIC.\n");
					break;
				case ERR_SEM_14:
					bwErr.write("[" + error + "] " + numLine + ", El tipus de l'expressió en ESCRIURE no és simple o no és una constant cadena.\n");
					break;
				case ERR_SEM_19:
					bwErr.write("[" + error + "] " + numLine + ", Retornar fora de funció.\n");
					break;
				case ERR_SEM_20:
					bwErr.write("[" + error + "] " + numLine + ", L'expressió no és estàtica.\n");
					break;
				case WAR_OPC_1:
					bwErr.write("[" + error + "] " + numLine + ", Divisió per 0.\n");
					break;
				case ERR_SEM_24:
					bwErr.write("[" + error + "] " + numLine + ", Existeix un camí on no es retorna cap valor.\n");
					break;
				case WAR_OPC_3:
					bwErr.write("[" + error + "] " + numLine + ", Hi ha codi després d'un RETORNAR.\n");
					break;
				case WAR_OPC_4:
					bwErr.write("[" + error + "] " + numLine + ", Aquesta condició sempre avalua cert.\n" +
							"\tSempre s'executarà el següent fragment de codi.\n");
					break;
				case WAR_OPC_5:
					bwErr.write("[" + error + "] " + numLine + ", Aquesta condició sempre avalua fals.\n" +
							"\tMai s'executarà el fragment de codi.\n");
					break;
				case WAR_OPC_6:
					bwErr.write("[" + error + "] " + numLine + ", Aquesta condició sempre avalua fals.\n" +
							"\tSempre s'executarà el fragment de codi.\n");
					break;
				case WAR_OPC_7:
					bwErr.write("[" + error + "] " + numLine + ", Aquesta condició sempre avalua cert.\n" +
							"\tSempre s'executarà un únic cop el fragment de codi anterior.\n");
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 *
	 * @param error     Codi d'error
	 * @param integer1  Primer integer a mostrar segons el codi d'error
	 * @param interger2 Segon integer a mostrar segons el codi d'error
	 */
	public void insertError(TypeError error, int integer1, int interger2) {
		int numLine = lexic.getActualLine();
		try {
			bwErr.write("[" + error + "] " + numLine + ", La funció en declaració té " + interger2 +
					" paràmetres mentre que en ús té " + integer1 + ".\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 *
	 * @param error  Codi d'error
	 * @param string Cadena amb la informació a mostrar segons l'error
	 * @param tipus1 Primera cadena amb informació del tipus a mostrar
	 * @param tipus2 Segona cadena amb informació del tipus a mostrar
	 */
	public void insertError(TypeError error, String string, String tipus1, String tipus2) {
		int numLine = lexic.getActualLine();
		try {
			switch (error) {
				case ERR_SEM_12:
					bwErr.write("[" + error + "] " + numLine + ", La variable <" + string +
							"> i l'expressió de assignació tenen tipus diferents.\n\tEl tipus de la variable és <" + tipus1 +
							"> i el de l'expressió <" + tipus2 + ">.\n");
					break;
				case ERR_SEM_18:
					if (string.charAt(0) == '!') string = string.substring(1);
					bwErr.write("[" + error + "] " + numLine + ", El tipus de la funció <" + string +
							"> i el tipus de retorn són diferents.\n\tEl tipus de retorn de la funció és <" + tipus1 +
							"> i retorna <" + tipus2 + ">.\n");
					break;

			}
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	/**
	 * Mètode per a finalitzar l'ús del descriptor d'errors.
	 */
	public void closeBuffer() {
		try {
			bwErr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
