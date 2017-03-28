package Analyzer;

import utils.*;
import utils.Error;

import java.io.IOException;
import java.util.Arrays;

public class SyntacticAnalyzer {
    private static SyntacticAnalyzer instance;
    private static LexicographicAnalyzer lexic;
    private static Token lookahead;
    private static Error error;
    //Si agafem el valor de la línia de l'últim token acceptat correctament aleshores encertarem la línia si
	//l'usuari s'ha deixat el SEMICOLON
	//Crec que com a paràmetre de nLina hauriem de passar:
	// - Si es tracta d'un error a accept: la línia de l'últim token correcte
	// - Si es tracta d'un error a un default (no hi ha E però s'hi ha arribat): lexic.getActualLine()
	//El primer cas és per evitar l'error en cas de que falti l'últim token mentre que el segon pel cas en que falti el primer
//    private static int errorLine = 0;

    private static Type[] cnj_var_const = {Type.SEMICOLON, Type.FUNCIO, Type.VAR, Type.CONST, Type.PROG, Type.FUNC};
	private static Type[] cnj_var_const_prev = {Type.FUNCIO, Type.VAR, Type.CONST, Type.PROG, Type.FUNC};
	private static Type[] cnj_decl_func = {Type.SEMICOLON, Type.FUNCIO, Type.PROG};
	private static Type[] cnj_decl_func_prev = {Type.PROG, Type.FUNCIO};
	private static Type[] cnj_exp = {Type.SUMA, Type.RESTA, Type.NOT, Type.SENCER_CST, Type.LOGIC_CST, Type.CADENA, Type.ID, Type.OPARENT, Type.SEMICOLON};
	private static Type[] cnj_inst = {Type.SEMICOLON, Type.ID, Type.ESCRIURE, Type.LLEGIR, Type.CICLE, Type.MENTRE, Type.SI, Type.RETORNAR, Type.PERCADA,
			Type.FIPER, Type.FISI, Type.FIMENTRE, Type.SINO, Type.FINS};
	private static Type[] cnj_inst_prev = {Type.ID, Type.ESCRIURE, Type.LLEGIR, Type.CICLE, Type.MENTRE, Type.SI, Type.RETORNAR, Type.PERCADA,
			Type.FIPER, Type.FISI, Type.FIMENTRE, Type.SINO, Type.FINS, Type.FIFUNC, Type.FIPROG};

    public static SyntacticAnalyzer getInstance(String fileName) throws IOException {
        if (instance == null) instance = new SyntacticAnalyzer(fileName);
        return instance;
    }

    private SyntacticAnalyzer(String fileName) throws IOException {
		lexic = LexicographicAnalyzer.getInstance(fileName);
		error = Error.getInstance();
	}

    private void accept(Type type) throws ParseException{
    	System.out.println(lexic.getActualLine()+": "+ "Espera: "+ type+" - "+ "Rep: "+lookahead.getToken());
		if(lookahead.getToken().equals(type)){
//			errorLine = lexic.getActualLine();
			lookahead = lexic.getToken();
		}else{
			System.out.println("ERROR");
			throw new ParseException(TypeError.ERR_SIN_1);
		}
    }

    private boolean consume(Type[] cnj) {
    	boolean flag = false;
    	do {
    		//TODO: arreglar aquest getToken().
			//lookahead = lexic.getToken();
			if(Arrays.asList(cnj).contains(lookahead.getToken())){
				return flag;
			}
			lookahead = lexic.getToken();
			flag = true;
		} while(!lookahead.getToken().equals(Type.EOF));
    	System.out.println("FOUND EOF");
		return true;
	}

    public void programa () {
        lookahead = lexic.getToken();

        try {
        	int nlinia = lexic.getActualLine();
        	if(consume(cnj_var_const_prev)){
        		error.insertError(TypeError.ERR_SIN_10, nlinia);
			}
			decl();

			accept(Type.PROG);
			llista_inst();
			accept(Type.FIPROG);
			try{
				accept(Type.EOF);
			}catch (ParseException e){
				error.insertError(TypeError.ERR_SIN_6, lexic.getActualLine());
			}
		} catch (ParseException e) {
			error.insertError(TypeError.ERR_SIN_9, lexic.getActualLine());
		}

		lexic.close();

    }

    private void decl() throws ParseException {
        decl_cte_var();
        decl_func();
    }

    private void decl_cte_var() throws ParseException{
        switch(lookahead.getToken()) {
            case CONST:
                accept(Type.CONST);
                try {
					accept(Type.ID);
					accept(Type.IGUAL);
					exp();
					accept(Type.SEMICOLON);
				} catch (ParseException e) {
                	//Si l'exepció salta a accept(SEMICOLON) es mostra el número de línia del següent token
					error.insertError(TypeError.ERR_SIN_3, lexic.getActualLine());
//					error.insertError(TypeError.ERR_SIN_3, errorLine);
					consume(cnj_var_const);
					//Ens mengem el SEMICOLON per a començar la següent instrucció
					if(lookahead.getToken().equals(Type.SEMICOLON)) lookahead = lexic.getToken();
				}
                break;
            case VAR:
                accept(Type.VAR);
                try {
					accept(Type.ID);
					accept(Type.COLON);
					tipus();
					accept(Type.SEMICOLON);
				} catch (ParseException e) {
					//Si l'exepció salta a accept(SEMICOLON) es mostra el número de línia del següent token
					error.insertError(TypeError.ERR_SIN_4, lexic.getActualLine());
					consume(cnj_var_const);
					if(lookahead.getToken().equals(Type.SEMICOLON)) lookahead = lexic.getToken();
				}

                break;
            default: return;
        }
        decl_cte_var();
    }

    private void decl_func() throws ParseException{
		int nlinia = lexic.getActualLine();
		if(consume(cnj_decl_func_prev)){
			error.insertError(TypeError.ERR_SIN_10, nlinia);
		}

        switch (lookahead.getToken()) {
            case FUNCIO:
				accept(Type.FUNCIO);
				//Sabies que amb "sout<TAB>" s'escriu "System.out.println();" ? Ho acabo de descobrir
				try {
					accept(Type.ID);
					accept(Type.OPARENT);
					llista_param();
					accept(Type.CPARENT);
					accept(Type.COLON);
					accept(Type.TIPUS_SIMPLE);
					accept(Type.SEMICOLON);
				}catch (ParseException e){
					error.insertError(TypeError.ERR_SIN_5, lexic.getActualLine());
					consume(cnj_decl_func);
					if(lookahead.getToken().equals(Type.SEMICOLON)) lookahead = lexic.getToken();
				}
				decl_cte_var();

                accept(Type.FUNC);

                llista_inst();

                //Realment necessaria tanta merda per tant poca cosa?
				//Si volem diferenciar entre ERR_SIN_1 i ERR_SIN_2 me parece que si...
				try {
					accept(Type.FIFUNC);
					accept(Type.SEMICOLON);
				} catch (ParseException e){
					consume(cnj_decl_func);
					if(lookahead.getToken().equals(Type.SEMICOLON)) lookahead = lexic.getToken();
				}

                decl_func();
                break;
            default: return;
        }
    }


    private void llista_param() throws ParseException {
        switch (lookahead.getToken()) {
            case TIPUS_PARAM:
                llista_param_aux();
                break;
            default: return;
        }

    }

    private void llista_param_aux() throws ParseException{
        accept(Type.TIPUS_PARAM);
        accept(Type.ID);
        accept(Type.COLON);
        tipus();
        param_aux();

    }

    private void param_aux() throws ParseException {
        switch (lookahead.getToken()) {
            case COMA:
                accept(Type.COMA);
                llista_param_aux();
                break;
            default: return;
        }
    }

    private void tipus() throws ParseException {
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
            default: //ERROR
				throw new ParseException(TypeError.ERR_SIN_1);
        }
    }

    private void exp() throws ParseException {
    	try{
			exp_simple();
		}catch (ParseException e){
    		error.insertError(TypeError.ERR_SIN_8, lexic.getActualLine(), cnj_exp, lookahead.getToken());
    		consume(cnj_exp);
		}
		exp_aux();	//No salta excepció
    }

    private void exp_simple() throws ParseException {
        op_unari();
        terme();
        terme_simple();

    }

    private void op_unari() throws ParseException {
        switch(lookahead.getToken()) {
            case SUMA:
                accept(Type.SUMA);
                break;
            case RESTA:
                accept(Type.RESTA);
                break;
            case NOT:
                accept(Type.NOT);
                break;
            default:
                return;
        }
    }

	private void terme_simple() throws ParseException {
		switch (lookahead.getToken()){
            case SUMA:
            case RESTA:
            case OR:
                op_aux();
                terme();
                terme_simple();
			    break;
			default:return;
		}
	}

	private void op_aux() throws ParseException {
	    switch(lookahead.getToken()) {
            case SUMA:
                accept(Type.SUMA);
                break;
            case RESTA:
                accept(Type.RESTA);
                break;

            case OR:
                accept(Type.OR);
                break;
            default:

                return;
        }
    }

    private void terme() throws ParseException {
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
            default: //ERROR - CODI PENDENT DE REVISIÓ
				throw new ParseException(TypeError.ERR_SIN_8);
		}
		terme_aux();
	}

	private void terme_aux() throws ParseException {
    	switch (lookahead.getToken()){
            case MUL:
            case DIV:
            case AND:
                op_binaria();
                terme();
                break;
			default:
				return;
		}
	}

	private void op_binaria() throws ParseException {
	    switch(lookahead.getToken()) {
            case MUL:
                accept(Type.MUL);
                break;
            case DIV:
                accept(Type.DIV);
                break;
            case AND:
                accept(Type.AND);
                break;
            default:
                return;
        }
    }

	private void factor_aux() throws ParseException {
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

	private void llista_exp() throws ParseException {
		exp();
		switch (lookahead.getToken()){
			case COMA:
				accept(Type.COMA);
				llista_exp();
				break;
			default: //ERROR
				return;
		}
	}

	private void variable_aux() throws ParseException {
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

    private void exp_aux() throws ParseException {
        switch(lookahead.getToken()) {
            case OP_RELACIONAL:
                accept(Type.OP_RELACIONAL);
                exp_simple();
                break;
            default: return;
        }
    }

    private void llista_inst() throws ParseException{

		int nlinia = lexic.getActualLine();
		if(consume(cnj_inst_prev)){
			error.insertError(TypeError.ERR_SIN_10, nlinia);
		}
		inst();
		try{
			accept(Type.SEMICOLON);
		} catch (ParseException e){
			error.insertError(TypeError.ERR_SIN_2, lexic.getActualLine(), Type.SEMICOLON);
			consume(cnj_inst);
            if(lookahead.getToken().equals(Type.SEMICOLON)) lookahead = lexic.getToken();
		}
		nlinia = lexic.getActualLine();
		if(consume(cnj_inst_prev)){
			error.insertError(TypeError.ERR_SIN_10, nlinia);
		}
		llista_inst_aux();
    }

    private void llista_inst_aux() throws ParseException{
		int nlinia = lexic.getActualLine();
		if(consume(cnj_inst_prev)){
			error.insertError(TypeError.ERR_SIN_10, nlinia);
		}
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

	private void inst() throws ParseException{
		switch (lookahead.getToken()) {
			case ID:
				accept(Type.ID);
				try {
					variable_aux();
					accept(Type.IGUAL);
					igual_aux();
				}catch (ParseException e){
					error.insertError(TypeError.ERR_SIN_7, lexic.getActualLine(), Type.ID);
					consume(cnj_inst);
					if(lookahead.getToken().equals(Type.SEMICOLON)) lookahead = lexic.getToken();
				}
				break;
			case ESCRIURE:
				accept(Type.ESCRIURE);
				try{
					accept(Type.OPARENT);
					param_escriure();
					accept(Type.CPARENT);
				}catch (ParseException e){
					error.insertError(TypeError.ERR_SIN_7, lexic.getActualLine(), Type.ESCRIURE);
					consume(cnj_inst);
					if(lookahead.getToken().equals(Type.SEMICOLON)) lookahead = lexic.getToken();
				}
				break;
			case LLEGIR:
				accept(Type.LLEGIR);
				try{
					accept(Type.OPARENT);
					param_llegir();
					accept(Type.CPARENT);
				}catch (ParseException e){
					error.insertError(TypeError.ERR_SIN_7, lexic.getActualLine(), Type.LLEGIR);
					consume(cnj_inst);
					if(lookahead.getToken().equals(Type.SEMICOLON)) lookahead = lexic.getToken();
				}
				break;
			case CICLE:
				accept(Type.CICLE);
				try{
					llista_inst();
					accept(Type.FINS);
					exp();
				}catch (ParseException e){
					error.insertError(TypeError.ERR_SIN_7, lexic.getActualLine(), Type.CICLE);
					consume(cnj_inst);
					if(lookahead.getToken().equals(Type.SEMICOLON)) lookahead = lexic.getToken();
				}
				break;
			case MENTRE:
				accept(Type.MENTRE);
				try{
					exp();
					accept(Type.FER);
					llista_inst();
					accept(Type.FIMENTRE);
				}catch (ParseException e){
					error.insertError(TypeError.ERR_SIN_7, lexic.getActualLine(), Type.MENTRE);
					consume(cnj_inst);
					if(lookahead.getToken().equals(Type.SEMICOLON)) lookahead = lexic.getToken();
				}
				break;
			case SI:
				accept(Type.SI);
				try {
					exp();
					accept(Type.LLAVORS);
					llista_inst();
					fi_aux();
					accept(Type.FISI);
				}catch (ParseException e){
					error.insertError(TypeError.ERR_SIN_7, lexic.getActualLine(), Type.SI);
					consume(cnj_inst);
					if(lookahead.getToken().equals(Type.SEMICOLON)) lookahead = lexic.getToken();
				}
				break;
			case RETORNAR:
				accept(Type.RETORNAR);
				exp();
				break;
			case PERCADA:
				accept(Type.PERCADA);
				try{
					accept(Type.ID);
					accept(Type.EN);
					accept(Type.ID);
					accept(Type.FER);
					llista_inst();
					accept(Type.FIPER);
				}catch (ParseException e){
					error.insertError(TypeError.ERR_SIN_7, lexic.getActualLine(), Type.PERCADA);
					consume(cnj_inst);
					if(lookahead.getToken().equals(Type.SEMICOLON)) lookahead = lexic.getToken();
				}
				break;
			default:
				//ERROR
				throw new ParseException(TypeError.ERR_SIN_7);
		}
	}

	private void igual_aux() throws ParseException{
		switch (lookahead.getToken()){
			case SI:
			    accept(Type.SI);
				accept(Type.OPARENT);
				exp();
				accept(Type.CPARENT);
				accept(Type.TERNARIA);
				exp();
				accept(Type.COLON);
				exp();
				break;
            case SUMA:
            case RESTA:
            case NOT:
            case SENCER_CST:
            case LOGIC_CST:
            case CADENA:
            case ID:
            case OPARENT:
                exp();
			default:
				//ERROR
				break;
		}
	}

	private void param_escriure() throws ParseException{
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

	private void param_llegir() throws ParseException{
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

	private void fi_aux() throws ParseException{
		switch (lookahead.getToken()){
			case SINO:
				accept(Type.SINO);
				llista_inst();
			default:
				return;
		}
	}


}
