package analyzer;

/**
 * Classe pròpia del package d'analitzadors que gaurda els conjunts de sincronització utilitzats a la recuperació d'errors
 * de l'anàlisi sintàctic.
 */
class Sincronization {
	static final Type[] cnj_var_const = {Type.SEMICOLON, Type.FUNCIO, Type.VAR, Type.CONST, Type.PROG, Type.FUNC, Type.EOF};
	static final Type[] cnj_var_const_prev = {Type.FUNCIO, Type.VAR, Type.CONST, Type.PROG, Type.FUNC, Type.EOF,};
	static final Type[] cnj_decl_func = {Type.SEMICOLON, Type.FUNCIO, Type.PROG, Type.EOF, Type.FUNC};
	static final Type[] cnj_decl_func_prev = {Type.PROG, Type.FUNCIO, Type.EOF};
	static final Type[] cnj_exp = {Type.SUMA, Type.RESTA, Type.NOT, Type.SENCER_CST, Type.LOGIC_CST, Type.CADENA, Type.ID,
			Type.OPARENT, Type.SEMICOLON, Type.EOF, Type.OP_RELACIONAL};
	static final Type[] cnj_inst = {Type.SEMICOLON, Type.ID, Type.ESCRIURE, Type.LLEGIR, Type.CICLE, Type.MENTRE, Type.SI,
			Type.RETORNAR, Type.PERCADA, Type.FIPER, Type.FISI, Type.FIMENTRE, Type.SINO, Type.FINS, Type.EOF, Type.FIPROG};
	static final Type[] cnj_inst_prev = {Type.ID, Type.ESCRIURE, Type.LLEGIR, Type.CICLE, Type.MENTRE, Type.SI, Type.RETORNAR,
			Type.PERCADA, Type.FIPER, Type.FISI, Type.FIMENTRE, Type.SINO, Type.FINS, Type.FIFUNC, Type.FIPROG, Type.EOF};
	static final Type[] cnj_param = {Type.TIPUS_PARAM, Type.COMA, Type.CPARENT, Type.SEMICOLON, Type.EOF};
	static final Type[] cnj_param_prev = {Type.TIPUS_PARAM, Type.CPARENT, Type.EOF};
}
