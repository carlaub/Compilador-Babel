package Analyzer;

import java.util.HashMap;

/**
 * Classe per a representar un token i el seu lexema.
 */
public class Token {

    private Type token;
    private String lexema;
    private static HashMap<String, Type> tokenCodes;

    /**
     * Constructor públic de {@link Token}. Obté el codi de token a partir del lexema.
     * @param lexema Lexema del token
     */
    public Token(String lexema) {
        this.lexema = lexema.toUpperCase();

        if(tokenCodes == null) loadTypes();

        token = tokenCodes.get(this.lexema);
        if(token == null) {
            this.token = Type.ID;
        }

    }

    /**
     * Constructor públic de token.
     * @param token Codi del token
     * @param lexema Lexema del token
     */
    public Token(Type token, String lexema) {
        this.token = token;
        this.lexema = lexema;
    }

    /**
     * Mètode privat per a carregar a {@link #tokenCodes} els codis de tots els tokens segons el seu lexema.
     */
    private void loadTypes() {

        tokenCodes = new HashMap<>();

        tokenCodes.put("CERT", Type.LOGIC_CST);
        tokenCodes.put("FALS", Type.LOGIC_CST);

        tokenCodes.put("SENCER", Type.TIPUS_SIMPLE);
        tokenCodes.put("LOGIC", Type.TIPUS_SIMPLE);

        tokenCodes.put("AND", Type.AND);
        tokenCodes.put("OR", Type.OR);
        tokenCodes.put("NOT", Type.NOT);
        tokenCodes.put("PERREF", Type.TIPUS_PARAM);
        tokenCodes.put("PERVAL", Type.TIPUS_PARAM);
        tokenCodes.put("CONST", Type.CONST);
        tokenCodes.put("VAR", Type.VAR);
        tokenCodes.put("PROG", Type.PROG);
        tokenCodes.put("FIPROG", Type.FIPROG);
        tokenCodes.put("FUNCIO", Type.FUNCIO);
        tokenCodes.put("FUNC", Type.FUNC);
        tokenCodes.put("FIFUNC", Type.FIFUNC);
        tokenCodes.put("VECTOR", Type.VECTOR);
        tokenCodes.put("DE", Type.DE);
        tokenCodes.put("ESCRIURE", Type.ESCRIURE);
        tokenCodes.put("LLEGIR", Type.LLEGIR);
        tokenCodes.put("CICLE", Type.CICLE);
        tokenCodes.put("FINS", Type.FINS);
        tokenCodes.put("MENTRE", Type.MENTRE);
        tokenCodes.put("FER", Type.FER);
        tokenCodes.put("FIMENTRE", Type.FIMENTRE);
        tokenCodes.put("SI", Type.SI);
        tokenCodes.put("LLAVORS", Type.LLAVORS);
        tokenCodes.put("SINO", Type.SINO);
        tokenCodes.put("FISI", Type.FISI);
        tokenCodes.put("RETORNAR", Type.RETORNAR);
        tokenCodes.put("PERCADA", Type.PERCADA);
        tokenCodes.put("EN", Type.EN);
        tokenCodes.put("FIPER", Type.FIPER);
        tokenCodes.put("?", Type.TERNARIA);
    }


    public String getLexema() {
        return this.lexema;
    }

    public String getTokenName() {
        return this.token.toString();
    }

    public Type getToken(){ return this.token;}
}
