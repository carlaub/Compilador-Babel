package Analyzer;

import java.io.IOException;

import static Analyzer.Type.SUMA;

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

    private void accept(Type type) {
    	System.out.println(lexic.getActualLine()+": "+type+" - "+lookahead.getToken());
		if(lookahead.getToken() == type){
			lookahead = lexic.getToken();

		}else{
			System.out.println("ERROR");
		}
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
			//TERME
			case SENCER_CST:
			case LOGIC_CST:
			case CADENA:
			case OPARENT:
			case ID:
				terme();
				break;
            //EXP_SIMPLE
            case SUMA:
                accept(SUMA);
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

	private void terme_simple(){
		switch (lookahead.getToken()){
			case SUMA:
				accept(Type.SUMA);
				terme();
				terme_simple();
				break;
			case RESTA:
				accept(Type.RESTA);
				terme();
				terme_simple();
				break;
			case OR:
				accept(Type.OR);
				terme();
				terme_simple();
				break;
			default:return;
		}
	}

    private void terme(){
		switch (lookahead.getToken()) {
			//FACTOR
			case SENCER_CST:
				accept(Type.SENCER_CST);
				break;
			case LOGIC_CST:
				accept(Type.LOGIC_CST);
				break;
			case CADENA:
				accept(Type.CADENA);
				break;
			case OPARENT:
				accept(Type.OPARENT);
				exp();
				accept(Type.CPARENT);
				break;
			case ID:
				accept(Type.ID);
				factor_aux();
				break;
		}
		terme_aux();
	}

	private void terme_aux(){
    	switch (lookahead.getToken()){
			case MUL:
				accept(Type.MUL);
				terme();
				break;
			case DIV:
				accept(Type.DIV);
				terme();
				break;
			case AND:
				accept(Type.AND);
				terme();
				break;
			default:
				return;
		}
	}

	private void factor_aux(){
    	switch (lookahead.getToken()){
			case OPARENT:
				accept(Type.OPARENT);
				llista_exp();
				accept(Type.CPARENT);
				break;
			case OCLAU:
			default:
				variable_aux();
				break;

    	}
	}

	private void llista_exp(){
		exp();
		switch (lookahead.getToken()){
			case COMA:
				accept(Type.COMA);
				llista_exp();
				break;
			default:
				return;
		}
	}

	private void variable_aux(){
		switch (lookahead.getToken()){
			case OCLAU:
				accept(Type.OCLAU);
				exp();
				accept(Type.CCLAU);
				break;
			default:
				return;
		}
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
		inst();

		accept(Type.SEMICOLON);
		llista_inst_aux();
    }

    private void llista_inst_aux(){
		switch (lookahead.getToken()){
			case ID:
			case ESCRIURE:
			case LLEGIR:
			case CICLE:
			case MENTRE:
			case SI:
			case RETORNAR:
			case PERCADA:
				llista_inst();
				break;
			default:
				return;
		}
	}

	private void inst(){
		switch (lookahead.getToken()) {
			case ID:
				accept(Type.ID);
				variable_aux();
				accept(Type.IGUAL);
				igual_aux();
				break;
			case ESCRIURE:
				accept(Type.ESCRIURE);
				accept(Type.OPARENT);
				param_escriure();
				accept(Type.CPARENT);
				break;
			case LLEGIR:
				accept(Type.LLEGIR);
				accept(Type.OPARENT);
				param_llegir();
				accept(Type.CPARENT);
				break;
			case CICLE:
				accept(Type.CICLE);
				llista_inst();
				accept(Type.FINS);
				exp();
				break;
			case MENTRE:
				accept(Type.MENTRE);
				exp();
				accept(Type.FER);
				llista_inst();
				accept(Type.FIMENTRE);
				break;
			case SI:
				accept(Type.SI);
				exp();
				accept(Type.LLAVORS);
				llista_inst();
				fi_aux();
				accept(Type.FISI);
				break;
			case RETORNAR:
				accept(Type.RETORNAR);
				exp();
				break;
			case PERCADA:
				accept(Type.PERCADA);
				accept(Type.ID);
				accept(Type.EN);
				accept(Type.ID);
				accept(Type.FER);
				llista_inst();
				accept(Type.FIPER);
				break;
			default:
				//ERROR
				return;
		}
	}

	private void igual_aux(){
		switch (lookahead.getToken()){
			case OPARENT:
				accept(Type.OPARENT);
				exp();
				accept(Type.CPARENT);
				accept(Type.TERNARIA);
				exp();
				accept(Type.COLON);
				exp();
				break;
			default:
				exp();
				break;
		}
	}

	private void param_escriure(){
		exp();
		switch (lookahead.getToken()){
			case COMA:
				accept(Type.COMA);
				param_escriure();
				break;
			default:
				return;
		}
	}

	private void param_llegir(){
		accept(Type.ID);
		variable_aux();
		switch (lookahead.getToken()){
			case COMA:
				accept(Type.COMA);
				param_llegir();
				break;
			default:
				return;
		}
	}

	private void fi_aux(){
		switch (lookahead.getToken()){
			case SINO:
				accept(Type.SINO);
				llista_inst();
			default:
				return;
		}
	}


}
