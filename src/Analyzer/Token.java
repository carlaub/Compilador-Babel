package Analyzer;

import java.util.HashMap;


public class Token {

    private Type token;
    private String lexema;
    private static HashMap<String, Type> tokenCodes;

    public Token(String lexema) {
        this.lexema = lexema;

        if(tokenCodes == null) loadTypes();

        token = tokenCodes.get(lexema.toUpperCase());
        if(token == null) {
            this.token = Type.TOKEN_ID;
        }

    }

    public Token(Type token, String lexema) {
        this.token = token;
        this.lexema = lexema;
    }

    private void loadTypes() {

        tokenCodes = new HashMap<>();

        tokenCodes.put("CERT", Type.TOKEN_LOGIC_CST);
        tokenCodes.put("FALS", Type.TOKEN_LOGIC_CST);

        tokenCodes.put("AND", Type.TOKEN_LOGIC_OP);
        tokenCodes.put("OR", Type.TOKEN_LOGIC_OP);
        tokenCodes.put("NOT", Type.TOKEN_LOGIC_OP);

        tokenCodes.put("PERREF", Type.TOKEN_PARAM);
        tokenCodes.put("PERVAL", Type.TOKEN_PARAM);

        tokenCodes.put("CONST", Type.TOKEN_DEC_CONST);
        tokenCodes.put("VAR", Type.TOKEN_DEC_VAR);
        tokenCodes.put("SENCER", Type.TOKEN_INT_TYPE);
        tokenCodes.put("LOGIC", Type.TOKEN_LOGIC_TYPE);
        tokenCodes.put("PROG", Type.TOKEN_PROG_INIT);
        tokenCodes.put("FIPROG", Type.TOKEN_PROG_END);
        tokenCodes.put("FUNCIO", Type.TOKEN_FUNC);
        tokenCodes.put("FUNC", Type.TOKEN_FUNC_INIT);
        tokenCodes.put("FIFUNC", Type.TOKEN_FUNC_END);
        tokenCodes.put("VECTOR", Type.TOKEN_VECTOR);
        tokenCodes.put("DE", Type.TOKEN_OF);
        tokenCodes.put("ESCRIURE", Type.TOKEN_WRITE);
        tokenCodes.put("LLEGIR", Type.TOKEN_READ);
        tokenCodes.put("CICLE", Type.TOKEN_DO_INIT);
        tokenCodes.put("FINS", Type.TOKEN_DO_END);
        tokenCodes.put("MENTRE", Type.TOKEN_WHILE);
        tokenCodes.put("FER", Type.TOKEN_BUCLE_INIT);
        //tokenCodes.put("FER",Analyzer.Type.TOKEN_WHILE_INIT);
        tokenCodes.put("FIMENTRE", Type.TOKEN_WHILE_END);
        tokenCodes.put("SI", Type.TOKEN_IF);
        tokenCodes.put("LLAVORS", Type.TOKEN_IF_INIT);
        tokenCodes.put("SINO", Type.TOKEN_ELSE);
        tokenCodes.put("FISI", Type.TOKEN_IF_END);
        tokenCodes.put("RETORNAR", Type.TOKEN_RETURN);
        tokenCodes.put("PERCADA", Type.TOKEN_FOR);
        tokenCodes.put("EN", Type.TOKEN_IN);
        //tokenCodes.put("FER",Analyzer.Type.TOKEN_FOR_INIT);
        tokenCodes.put("FIPER", Type.TOKEN_FOR_END);
        tokenCodes.put("?", Type.TOKEN_TERNARY);
    }


    public String getLexema() {
        return this.lexema;
    }

    public String getTokenName() {
        return this.token.toString();
    }

    public Type getToken(){ return this.token;}
}
