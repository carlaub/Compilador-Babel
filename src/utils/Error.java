package utils;

import analyzer.LexicographicAnalyzer;
import analyzer.Type;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Descriptor d'errors. S'encarrega d'escriure els errors trobats a un fitxer.
 * En cas que es tracti d'un warning escriu el seu tractament.
 */
public class Error {
    private static HashMap<TypeError, String> errorCodes;
    private static Error instance;
    private static BufferedWriter bwErr;
    private static LexicographicAnalyzer lexic;

    public static Error getInstance( ) {
        return instance;
    }

    /**
     * Mètode públic per a obtenir una instància del descriptor d'errors.
     * Com que s'utilitza el patró Singleton sempre retorna la mateixa instància.
     * @param fileName Arxiu on s'escriuran els errors trobats.
     * @return Instància única del descriptor d'errors.
     */
    public static Error getInstance(String fileName ) {
        if (instance == null) {
            instance = new Error(fileName);
        }
        return instance;
    }

    /**
     * Constructor privat de {@link Error}. Privat a causa del patró Singleton.
     * @param fileName Arxiu on s'escriuran els errors trobats.
     */
    private Error(String fileName) {
        File err = new File (fileName.split(Pattern.quote("."))[0]+".err");
        loadCodes();
        lexic = LexicographicAnalyzer.getInstance();

        try {
            bwErr = new BufferedWriter(new FileWriter(err));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mètode per a relacionar els codis d'error amb una petita descripció a {@link #errorCodes}.
     * Actualment en desús.
     */
    private void loadCodes() {
        errorCodes = new HashMap<>();
        errorCodes.put(TypeError.ERR_LEX_1,"Unknown character");
        errorCodes.put(TypeError.WAR_LEX_1, "Max length reached");
        errorCodes.put(TypeError.WAR_LEX_2, "String not closed");
        errorCodes.put(TypeError.ERR_SIN_1, "Unexpected token found");
		errorCodes.put(TypeError.ERR_SIN_2, "Missing token");
		errorCodes.put(TypeError.ERR_SIN_3, "Bad constant definition");
		errorCodes.put(TypeError.ERR_SIN_4, "Bad variable definition");
		errorCodes.put(TypeError.ERR_SIN_5, "Bad function definition");
		errorCodes.put(TypeError.ERR_SIN_6, "Unexpected token found after end of program");
		errorCodes.put(TypeError.ERR_SIN_7, "Bad instruction definition");
		errorCodes.put(TypeError.ERR_SIN_8, "Bad expression definition");
		errorCodes.put(TypeError.ERR_SIN_9, "FATAL ERROR");
    }

    /**
     * Mètode per a escriure un error al fitxer d'errors.
     * @param error Codi de l'error
     * @param numLine Número de la línia on s'ha trobat l'error
     * @param character Caràcter causant de l'error
     */
    public void insertError(TypeError error, int numLine, char character) {
        try {
            //Write error into *.err file
            switch (error){

                case ERR_LEX_1:
                    bwErr.write("[" + error.toString() +"] "+ numLine + ", Caràcter["+character+"] desconegut\n");
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mètode per a escriure un error al fitxer d'errors.
     * @param error Codi de l'error
     * @param numLine Número de la línia on s'ha trobat l'error
     * @param string String causant de l'error
     */
    public void insertError(TypeError error, int numLine, String string) {
        try {
            //Write error into *.err file
            switch (error){

                case WAR_LEX_1:
                    bwErr.write("[" + error +"] "+ numLine + ", Llargada màxima és 32 caràcters.\n");
                    bwErr.write("Canvi de "+string+" a "+string.substring(0, 31) + "\n");
                    break;

                case WAR_LEX_2:
                    bwErr.write("[" + error +"] "+ numLine + ", Falta tancar la cadena.\n");
                    bwErr.write("Canvi de <"+string+"> a <"+string + "\">\n");
                    break;

	            case ERR_SEM_16:
		            bwErr.write("[" + error +"] "+ lexic.getActualLine() + ", El tipus del paràmetre número "+ numLine +
				            "no coincideix amb el tipus dela seva declaració "+ string + "\n");
		            break;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 * @param error Codi de l'error
	 * @param numLine Número de la línia on s'ha trobat l'error
	 * @param types Conjunt de tokens esperats
	 * @param token Token causant de l'error
	 */
    public void insertError(TypeError error, int numLine, Type[] types, Type token) {
        try {
            //Si realment no cridarem aquesta funció en cap altre situació ens podem estalviar el switch
            switch (error){
				case ERR_SIN_1:
				case ERR_SIN_8:

                    bwErr.write("[" + error +"] "+ numLine + ", Esperava <"+
							Arrays.toString(types).replaceAll("[\\[\\]]","")+
							"> però he trobat <"+token+">.\n");
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 * @param error Codi de l'error
	 * @param numLine Número de la línia on s'ha trobat l'error
	 * @param token Token causant de l'error
	 */
	public void insertError(TypeError error, int numLine, Type token) {
		try {
			//Si realment no cridarem aquesta funció en cap altre situació ens podem estalviar el switch
			switch (error){
				case ERR_SIN_2:
					bwErr.write("[" + error +"] "+ numLine + ", Falta el token " + token+".\n");
					break;
				case ERR_SIN_7:
					bwErr.write("[" + error +"] "+ numLine+", Construcció d'instrucció "+token+" incorrecta.\n");
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 * @param error Codi de l'error
	 * @param numLine Número de la línia on s'ha trobat l'error
	 */
    public void insertError(TypeError error, int numLine) {
        try {
            switch(error) {
                case ERR_SIN_3:
                    bwErr.write("[" + error +"] "+ numLine + ", La construcció de la declaració de la " +
                            "constant no és correcta.\n");
                    break;
                case ERR_SIN_4:
                    bwErr.write("[" + error +"] "+ numLine + ", La construcció de la declaració de la " +
                            "variable no és correcta.\n");
                    break;
                case ERR_SIN_5:
                    bwErr.write("[" + error +"] "+ numLine + ", La capçalera de la funció conté errors.\n");
                    break;
                case ERR_SIN_6:
                    bwErr.write("[" + error +"] "+ numLine + ", Hi ha codi després de fi del programa.\n");
                    break;
                case ERR_SIN_9:
                    bwErr.write("[" + error +"] " + "El procediment principal conté errors.\n");
                    break;
				case ERR_SIN_10:
					bwErr.write("[" + error +"] "+ numLine  + ", El format de la instrucció és invàlid.\n");
					break;
	            case ERR_SEM_17:
		            bwErr.write("[" + error +"] "+ lexic.getActualLine()  + ", El paràmetre número "+ numLine +
				            "de la funció no es pot passar per referència.\n");

		            break;
            }
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 * @param error Codi de l'error
	 * @param string Cadena amb la informació a mostrar segons l'error
	 */
	public void insertError(TypeError error, String string) {
		int numLine = lexic.getActualLine();
		try {
			switch(error) {
				case ERR_SEM_1:
					bwErr.write("[" + error +"] "+ numLine + ", Constant <" + string + "> doblement definida.\n");
					break;
				case ERR_SEM_2:
					bwErr.write("[" + error +"] "+ numLine + ", Variable <" + string + "> doblement definida.\n");
					break;
				case ERR_SEM_3:
					bwErr.write("[" + error +"] "+ numLine + ", Funció <" + string + "> doblement definida.\n");
					break;
				case ERR_SEM_4:
					bwErr.write("[" + error +"] "+ numLine + ", Paràmetre <" + string + "> doblement definida.\n");
					break;
				case ERR_SEM_5:
					bwErr.write("[" + error +"] "+ numLine + ", Límits decreixents en el vector <" + string + ">.\n");
					break;
				case ERR_SEM_9:
					bwErr.write("[" + error +"] "+ numLine + ", L'identificador <" + string + "> no ha estat declarat.\n");
					break;
				case ERR_SEM_10:
					bwErr.write("[" + error +"] "+ numLine + ", L'identificador <" + string +
							"> en la instrucció LLEGIR no és una variable de tipus simple.\n");
					break;
				case ERR_SEM_11:
					bwErr.write("[" + error +"] "+ numLine + ", L'identificador <" + string +
							"> en part esquerra d'assignació no és una variable.\n");
					break;
				case ERR_SEM_13:
					bwErr.write("[" + error +"] "+ numLine + ", El tipus de l'índex d'accés del vector <" + string +
							"> no és sencer.\n");
					break;
				case ERR_SEM_22:
					bwErr.write("[" + error +"] "+ numLine + ", La funció <" + string +
							"> en part dreta de l'assignació no està sent invocada.\n");
					break;

			}
		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	/**
	 * Mètode per a escriure un error al fitxer d'errors.
	 * @param error Codi de l'error
	 */
	public void insertError(TypeError error) {
		int numLine = lexic.getActualLine();
		try {
			switch(error) {
				case ERR_SEM_6:
					bwErr.write("[" + error +"] "+ numLine + ", El tipus de l'expressió no és SENCER.\n");
					break;
				case ERR_SEM_7:
					bwErr.write("[" + error +"] "+ numLine + ", El tipus de l'expressió no és LOGIC.\n");
					break;
				case ERR_SEM_8:
					bwErr.write("[" + error +"] "+ numLine + ", La condició no és de tipus LOGIC.\n");
					break;
				case ERR_SEM_14:
					bwErr.write("[" + error +"] "+ numLine + ", El tipus de l'expressió en ESCRIURE no és simple o no és una constant cadena.\n");
					break;
				case ERR_SEM_19:
					bwErr.write("[" + error +"] "+ numLine + ", Retornar fora de funció.\n");
					break;
				case ERR_SEM_20:
					bwErr.write("[" + error +"] "+ numLine + ", L'expressió no és estàtica.\n");
					break;
				case ERR_SEM_21:
					bwErr.write("[" + error +"] "+ numLine + ", Divisió per 0.\n");
					break;

			}
		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	public void insertError (TypeError error, int numParamDecl, int numParamUs) {
		int numLine = lexic.getActualLine();
		try {
			bwErr.write("[" + error +"] "+ numLine + ", La funció en declaració té "+ numParamUs +
					" paràmetres mentre que en ús té " + numParamDecl + ".\n");
		} catch(IOException e) {

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
