package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
                    bwErr.write("[" + error.toString() +"] "+ numLine + ", Llargada màxima és 32 caràcters.\n");
                    bwErr.write("Canvi de "+string+" a "+string.substring(0, 31) + "\n");
                    break;

                case WAR_LEX_2:
                    bwErr.write("[" + error.toString() +"] "+ numLine + ", Falta tancar la cadena.\n");
                    bwErr.write("Canvi de <"+string+"> a <"+string + "\">\n");
                    break;
                case ERR_SIN_1:
                    bwErr.write("[" + error.toString() +"] "+ numLine + ", Falta tancar la cadena.\n");
                    break;


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertError(TypeError error, int numLine) {
        try {
            switch(error) {
                case ERR_SIN_3:
                    bwErr.write("[" + error.toString() +"] "+ numLine + ", La construcció de la declaració de la " +
                            "constant no és correcta.\n");

                    break;
                case ERR_SIN_4:
                    bwErr.write("[" + error.toString() +"] "+ numLine + ", La construcció de la declaració de la " +
                            "variable no és correcta.\n");
                    break;
                case ERR_SIN_5:
                    bwErr.write("[" + error.toString() +"] "+ numLine + ", La capçalera de la funció conté errors.\n");
                    break;
                case ERR_SIN_6:
                    bwErr.write("[" + error.toString() +"] "+ numLine + ", Hi ha codi després de fi del programa.\n");
                    break;
                case ERR_SIN_9:
                    bwErr.write("[" + error.toString() +"] " + "El procediment principal conté errors.\n");
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
