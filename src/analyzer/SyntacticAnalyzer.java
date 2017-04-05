package analyzer;

import utils.*;
import utils.Error;

import java.io.IOException;
import java.util.Arrays;

/**
 * Analitzador sintàctic.
 * S'encarrega de demanar tokens al {@link LexicographicAnalyzer} per tal de poder parsejar el codi font
 * i construir un arbre top-down mitjançant crides recursives.
 */
public class SyntacticAnalyzer {
    private static SyntacticAnalyzer instance;
    private static LexicographicAnalyzer lexic;
    private static Token lookahead;
    private static Error error;
	private static int errorLine = 0;
	private static int nCicle = 0;	//cicle, mentre, si, percada (no té sentit controlar funció)
	private static int nMentre = 0;
	private static int nSi = 0;
	private static int nPercada = 0;

    //Si agafem el valor de la línia de l'últim token acceptat correctament aleshores encertarem la línia si
	//l'usuari s'ha deixat el SEMICOLON
	//Crec que com a paràmetre de nLina hauriem de passar:
	// - Si es tracta d'un error a accept: la línia de l'últim token correcte
	// - Si es tracta d'un error a un default (no hi ha E però s'hi ha arribat): lexic.getActualLine()
	//El primer cas és per evitar l'error en cas de que falti l'últim token mentre que el segon pel cas en que falti el primer
//    private static int errorLine = 0;

    private static Type[] cnj_var_const = {Type.SEMICOLON, Type.FUNCIO, Type.VAR, Type.CONST, Type.PROG, Type.FUNC, Type.EOF};
	private static Type[] cnj_var_const_prev = {Type.FUNCIO, Type.VAR, Type.CONST, Type.PROG, Type.FUNC, Type.EOF, };
	private static Type[] cnj_decl_func = {Type.SEMICOLON, Type.FUNCIO, Type.PROG, Type.EOF, Type.FUNC};
	private static Type[] cnj_decl_func_prev = {Type.PROG, Type.FUNCIO, Type.EOF};
	private static Type[] cnj_exp = {Type.SUMA, Type.RESTA, Type.NOT, Type.SENCER_CST, Type.LOGIC_CST, Type.CADENA, Type.ID, Type.OPARENT, Type.SEMICOLON, Type.EOF};
	private static Type[] cnj_inst = {Type.SEMICOLON, Type.ID, Type.ESCRIURE, Type.LLEGIR, Type.CICLE, Type.MENTRE, Type.SI, Type.RETORNAR, Type.PERCADA,
			Type.FIPER, Type.FISI, Type.FIMENTRE, Type.SINO, Type.FINS, Type.EOF, Type.FIPROG};
	private static Type[] cnj_inst_prev = {Type.ID, Type.ESCRIURE, Type.LLEGIR, Type.CICLE, Type.MENTRE, Type.SI, Type.RETORNAR, Type.PERCADA,
			Type.FIPER, Type.FISI, Type.FIMENTRE, Type.SINO, Type.FINS, Type.FIFUNC, Type.FIPROG, Type.EOF};
	private static Type[] cnj_param = {Type.TIPUS_PARAM, Type.COMA, Type.CPARENT, Type.SEMICOLON, Type.EOF};
	private static Type[] cnj_param_prev = {Type.TIPUS_PARAM, Type.CPARENT, Type.EOF};

	/**
	 * Mètode públic per a obtenir una instància de l'analitzador sintàctic.
	 * Com que s'utilitza el patró Singleton sempre retorna la mateixa instància.
	 * @param fileName Arxiu que conté el programa a compilar
	 * @return Instància única de {@link SyntacticAnalyzer}
	 * @throws IOException Quan no es pot obrir l'arxiu a compilar.
	 */
    public static SyntacticAnalyzer getInstance(String fileName) throws IOException {
        if (instance == null) instance = new SyntacticAnalyzer(fileName);
        return instance;
    }

    /**
	 * Constructor privat de {@link SyntacticAnalyzer}. Privat a causa del patró Singleton.
	 * @param fileName Arxiu que conté el programa a compilar
	 * @throws IOException Quan no es pot obrir l'arxiu a compilar.
	 */
    private SyntacticAnalyzer(String fileName) throws IOException {
		lexic = LexicographicAnalyzer.getInstance(fileName);
		error = Error.getInstance();
	}

	/**
	 * Mètode per a acceptar el token actual de lookahead. És a dir, comprovem que el token actual trobat al codi font
	 * ({@link SyntacticAnalyzer#lookahead}) és realment el que hauríem de trobar segons l'arbre construït ({@code type}).
	 * @param type Token esperat
	 * @throws ParseException Excepció amb el codi del tipus d'error ({@link TypeError}) donat.
	 */
    private void accept(Type type) throws ParseException{
    	System.out.println(lexic.getActualLine()+": "+ "Espera: "+ type+" - "+ "Rep: "+lookahead.getToken() +" - "+lookahead.getLexema());
		if(lookahead.getToken().equals(type)){
			errorLine = lexic.getActualLine();
			lookahead = lexic.getToken();
		}else{
			System.out.println("ERROR");
			throw new ParseException(TypeError.ERR_SIN_1);
		}
    }

	/**
	 * Mètode per a la recuperació d'errors. Aquest mètode consumeix tokens fins a trobar-ne un que estigui
	 * al conjunt de tokens rebut. D'aquesta manera aconseguim tornar a sincronitzar l'analitzador amb el codi font.
	 * @param cnj Conjunt de tokens a càrrec de la sincronització. Trobar un d'aquests tokens aquival a haver trobat un
	 *            punt del codi per on continuar analitzant.
	 * @return Booleà indicant si ha consumit algun token.
	 */
	private boolean consume(Type[] cnj) {
    	boolean flag = false;
    	do {
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

	/**
	 * Mètode públic per a iniciar la construcció de crides recursives per a construir l'arbre sintàctic.
	 */
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
		} finally {
			lexic.close();
		}

    }

    private void decl() throws ParseException {
        decl_cte_var();
        decl_func();
    }

    private void decl_cte_var() throws ParseException{
		int nlinia = lexic.getActualLine();
		if(consume(cnj_var_const_prev)){
			error.insertError(TypeError.ERR_SIN_10, nlinia);
		}
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
				try {
					accept(Type.FUNC);
				} catch (ParseException e){
					consume(cnj_inst_prev);
				}

                llista_inst();

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

		int nlinia = lexic.getActualLine();
		Type token = lookahead.getToken();
		if(consume(cnj_param)){
			error.insertError(TypeError.ERR_SIN_1, nlinia, cnj_param_prev, token);
			if (lookahead.getToken().equals(Type.COMA)){
				lookahead = lexic.getToken();
				llista_param();
			}
		}
		switch (lookahead.getToken()) {
			case TIPUS_PARAM:
				llista_param_aux();
				break;
			default:
				return;
		}
	}

	private void llista_param_aux() throws ParseException{

		int nlinia = lexic.getActualLine();
		Type token = lookahead.getToken();
		if(consume(cnj_param)){
			error.insertError(TypeError.ERR_SIN_1, nlinia, cnj_param_prev, token);
//			if (lookahead.getToken().equals(Type.COMA)) lookahead = lexic.getToken();
			param_aux();
		}else {

			try {
				accept(token = Type.TIPUS_PARAM);
				accept(token = Type.ID);
				accept(token = Type.COLON);
				tipus();
			} catch (ParseException e) {
				error.insertError(TypeError.ERR_SIN_1, nlinia, new Type[]{token}, lookahead.getToken());
				consume(cnj_param);
			}
			param_aux();
		}
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
			error.insertError(TypeError.ERR_SIN_2, errorLine, Type.SEMICOLON);
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
				//COMPROVAR SI ES TRACTA DEL FINAL D'UNA ESTRUCTURA.
				//SI N > 0 TOT BÉ, SI N == 0 LIADA. EN TEORIA NO S'HAURIA DE PODER DONAR EL N < 0.
				//També podria arribar a controlar aquelles situacions que hi ha un mentre dintre d'un cicle i es troba abans
				//el ficicle i coses d'aquestes, però ara no sé com fer-ho i em sembla massa ja
				switch (lookahead.getToken()){
					//Faig un altre switch per a mantenir una estructuració de codi consistent,
					//però realment podria ser el mateix switch
					case FIMENTRE:
						if(nMentre == 0){

							System.out.println("ERROR A MENTRE");
							lookahead = lexic.getToken();
							llista_inst();
						}
						break;
					case FISI:
						if (nSi == 0){
							System.out.println("ERROR A SI");
							lookahead = lexic.getToken();
							llista_inst();
						}
						break;
					case FIPER:
						if (nPercada == 0){
							System.out.println("ERROR A PERCADA");
							lookahead = lexic.getToken();
							llista_inst();
						}
						break;
					case FINS:
						if (nCicle == 0){
							System.out.println("ERROR A CICLE");
							lookahead = lexic.getToken();
							llista_inst();
						}
						break;
				}
//				return;
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
					nCicle++;
					llista_inst();
					accept(Type.FINS);
					exp();
					nCicle--;
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
					nMentre++;
					llista_inst();
					accept(Type.FIMENTRE);
					nMentre--;
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
					nSi++;
					llista_inst();
					fi_aux();
					accept(Type.FISI);
					nSi--;
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
					nPercada++;
					llista_inst();
					accept(Type.FIPER);
					nPercada--;
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
