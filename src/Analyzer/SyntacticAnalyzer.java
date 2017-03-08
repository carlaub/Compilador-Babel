package Analyzer;

import java.io.IOException;

public class SyntacticAnalyzer {
    private static SyntacticAnalyzer instance;
    private static LexicographicAnalyzer lexic;
    private static Token lookahead;

    public static SyntacticAnalyzer getInstance(String fileName) throws IOException {
        if (instance == null) instance = new SyntacticAnalyzer(fileName);
        return instance;
    }

    private SyntacticAnalyzer(String fileName) throws IOException {
        lexic = LexicographicAnalyzer.getInstance(fileName);


    }

    public void accept(Type type) {

    }

    public void programa () {
        lookahead = lexic.getToken();

        decl();
        accept(Type.PROG);
        llista_inst();
        accept(Type.FIPROG);
    }

    private void decl() {
        decl_cte_var();
        decl_func();
    }

    private void decl_cte_var() {
        switch(lookahead.getToken()) {
            case CONST:
                accept(Type.CONST);
                accept(Type.ID);
                accept(Type.IGUAL);
                exp();
                accept(Type.SEMICOLON);
                break;
            case VAR:
                accept(Type.VAR);
                accept(Type.ID);
                accept(Type.COLON);
                tipus();
                accept(Type.SEMICOLON);
                break;
            default: return;
        }
        decl_cte_var();
    }

    private void decl_func() {
        switch (lookahead.getToken()) {
            case FUNCIO:
                accept(Type.FUNCIO);
                accept(Type.ID);
                accept(Type.OPARENT);
                llista_param();
                accept(Type.CPARENT);
                accept(Type.COLON);
                accept(Type.TIPUS_SIMPLE);
                accept(Type.SEMICOLON);
                decl_cte_var();
                accept(Type.FUNC);
                llista_inst();
                accept(Type.FIFUNC);
                accept(Type.SEMICOLON);
                decl_func();
                break;
            default: return;
        }
    }


    private void llista_param() {
        switch (lookahead.getToken()) {
            case TIPUS_PARAM:
                llista_param_aux();
                break;
            default: return;
        }

    }

    private void llista_param_aux() {
        accept(Type.TIPUS_PARAM);
        accept(Type.ID);
        accept(Type.COLON);
        tipus();
        param_aux();

    }

    private void param_aux() {
        switch (lookahead.getToken()) {
            case COMA:
                accept(Type.COMA);
                llista_param_aux();
                break;
            default: return;
        }
    }

    private void tipus() {
        switch (lookahead.getToken()) {
            case TIPUS_SIMPLE:
                accept(Type.TIPUS_SIMPLE);
                break;
            case VECTOR:
                accept(Type.VECTOR);
                accept(Type.OCLAU);
                exp();
                accept(Type.DPOINT);
                exp();
                accept(Type.CCLAU);
                accept(Type.DE);
                accept(Type.TIPUS_SIMPLE);
                break;
        }
    }

    private void exp() {
        exp_simple();
        exp_aux();
    }

    private void exp_simple() {
        switch (lookahead.getToken()) {
            //FACTOR
            case SENCER_CST:
                accept(Type.SENCER_CST);
                terme_aux();
                break;
            case LOGIC_CST:
                accept(Type.LOGIC_CST);
                terme_aux();
                break;
            case CADENA:
                accept(Type.CADENA);
                terme_aux();
                break;
            case OPARENT:
                accept(Type.OPARENT);
                exp();
                accept(Type.CPARENT);
                terme_aux();
                break;
            case ID:
                accept(Type.ID);
                factor_aux();
                terme_aux();
                break;
            //EXP_SIMPLE
            case SUMA:
                accept(Type.SUMA);
                terme();
                break;
            case RESTA:
                accept(Type.RESTA);
                terme();
                break;
            case NOT:
                accept(Type.NOT);
                terme();
                break;
        }
        terme_simple();
    }

    private void exp_aux() {
        switch(lookahead.getToken()) {
            case OP_RELACIONAL:
                accept(Type.OP_RELACIONAL);
                exp_simple();
                break;
            default: return;
        }
    }

    private void llista_inst() {

    }



}
