import java.util.HashMap;

/**
 * Created by carlaurrea on 15/2/17.
 */
public class Token {

    private Type token;
    private String lexema;
    private static HashMap<String, Type> map;

    public Token(String lexema) {
        this.lexema = lexema;

        if(map == null) map = loadTypes();

        token = map.get(lexema.toUpperCase());
        if(token == null) {
            this.token = Type.TOKEN_ID;
        }

    }

    public Token(Type token, String lexema) {
        this.token = token;
        this.lexema = lexema;
    }

    private HashMap<String,Type> loadTypes() {

        map = new HashMap<>();

        map.put("CERT",Type.TOKEN_LOGIC_CST);
        map.put("FALS",Type.TOKEN_LOGIC_CST);

        map.put("AND",Type.TOKEN_LOGIC_OP);
        map.put("OR",Type.TOKEN_LOGIC_OP);
        map.put("NOT",Type.TOKEN_LOGIC_OP);

        map.put("PERREF",Type.TOKEN_PARAM);
        map.put("PERVAL",Type.TOKEN_PARAM);

        map.put("CONST",Type.TOKEN_DEC_CONST);
        map.put("VAR",Type.TOKEN_DEC_VAR);
        map.put("SENCER",Type.TOKEN_INT_TYPE);
        map.put("LOGIC",Type.TOKEN_LOGIC_TYPE);
        map.put("PROG",Type.TOKEN_PROG_INIT);
        map.put("FIPROG",Type.TOKEN_PROG_END);
        map.put("FUNCIO",Type.TOKEN_FUNC);
        map.put("FUNC",Type.TOKEN_FUNC_INIT);
        map.put("FIFUNC",Type.TOKEN_FUNC_END);
        map.put("VECTOR",Type.TOKEN_VECTOR);
        map.put("DE",Type.TOKEN_OF);
        map.put("ESCRIURE",Type.TOKEN_WRITE);
        map.put("LLEGIR",Type.TOKEN_READ);
        map.put("CICLE",Type.TOKEN_DO_INIT);
        map.put("FINS",Type.TOKEN_DO_END);
        map.put("MENTRE",Type.TOKEN_WHILE);
        map.put("FER",Type.TOKEN_BUCLE_INIT);
        //map.put("FER",Type.TOKEN_WHILE_INIT);
        map.put("FIMENTRE",Type.TOKEN_WHILE_END);
        map.put("SI",Type.TOKEN_IF);
        map.put("LLAVORS",Type.TOKEN_IF_INIT);
        map.put("SINO",Type.TOKEN_ELSE);
        map.put("FISI",Type.TOKEN_IF_END);
        map.put("RETORNAR",Type.TOKEN_RETURN);
        map.put("PERCADA",Type.TOKEN_FOR);
        map.put("EN",Type.TOKEN_IN);
        //map.put("FER",Type.TOKEN_FOR_INIT);
        map.put("FIPER",Type.TOKEN_FOR_END);
        return map;
    }


    public String getLexema() {
        return this.lexema;
    }

    public String getTokenName() {
        return this.token.toString();
    }
}
