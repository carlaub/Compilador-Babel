import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;


public class Error {
    private TypeError idError;
    private static HashMap<TypeError, String> errorCodes;
    private static Error instance;
    private static File err;
    private static BufferedWriter bwErr;


    public static Error getInstance(String fileName ) {
        if (instance == null) {
            instance = new Error(fileName);
        }
        return instance;
    }

    private Error(String fileName) {
        err = new File (fileName.split(Pattern.quote("."))[0]+".err");
        loadCodes();
        try {
            bwErr = new BufferedWriter(new FileWriter(err));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCodes() {
        errorCodes = new HashMap<>();
        errorCodes.put(TypeError.ERR_LEX_1,"Unknown character");
        errorCodes.put(TypeError.WAR_LEX_1, "Max length reached");
    }

    public void insertLexError(TypeError error, int numLine, char character) {
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

    public void insertLexError(TypeError error, int numLine, String string) {
        try {
            //Write error into *.err file
            switch (error){
                case WAR_LEX_1:
                    bwErr.write("[" + error.toString() +"] "+ numLine + ", Llargada màxima és 32 caràcters.\n");
                    bwErr.write("Canvi de "+string+" a "+string.substring(0, 31));
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeBuffer() {
        try {
            bwErr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
