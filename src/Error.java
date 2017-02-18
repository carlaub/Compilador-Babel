import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;


public class Error {
    private TypeError idError;
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
        try {
            bwErr = new BufferedWriter(new FileWriter(err));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void instertLexError(TypeError error, char character, int numLine) {
        try {
            //Write error into *.err file
            bwErr.write("[" + error.toString() +"] "+ numLine + ", "+ "Caràcter ["+character+"] desconegut\n");
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
