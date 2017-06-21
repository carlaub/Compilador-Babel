package analyzer;


import taulaDeSimbols.*;

import java.io.IOException;

/**
 * Analitzador sintàctic.
 * S'encarrega de demanar tokens al {@link LexicographicAnalyzer} per tal de poder parsejar el codi font
 * i construir un arbre top-down mitjançant crides recursives.
 */
public class SyntacticClean {
	private static SyntacticClean instance;
	private static LexicographicAnalyzer lexic;
	private static SemanticAnalyzer semantic;
	private static Token lookahead;

	/**
	 * Mètode públic per a obtenir una instància de l'analitzador sintàctic.
	 * Com que s'utilitza el patró Singleton sempre retorna la mateixa instància.
	 *
	 * @param fileName Arxiu que conté el programa a compilar
	 * @return Instància única de {@link SyntacticClean}
	 * @throws IOException Quan no es pot obrir l'arxiu a compilar.
	 */
	public static SyntacticClean getInstance(String fileName) throws IOException {
		if (instance == null) instance = new SyntacticClean(fileName);
		return instance;
	}

	/**
	 * Constructor privat de {@link SyntacticClean}. Privat a causa del patró Singleton.
	 *
	 * @param fileName Arxiu que conté el programa a compilar
	 * @throws IOException Quan no es pot obrir l'arxiu a compilar.
	 */
	private SyntacticClean(String fileName) throws IOException {
		lexic = LexicographicAnalyzer.getInstance(fileName);
		semantic = new SemanticAnalyzer(fileName);
	}

	/**
	 * Mètode per a acceptar el token actual de lookahead. És a dir, comprovem que el token actual trobat al codi font
	 * ({@link SyntacticClean#lookahead}) és realment el que hauríem de trobar segons l'arbre construït ({@code type}).
	 *
	 * @param type Token esperat
	 */
	private void accept(Type type) {
		if (lookahead.getToken().equals(type)) {
			lookahead = lexic.getToken();
		} else {
			System.out.println("ERROR - accept()");
			System.out.println(lexic.getActualLine() + " - " + lookahead.getLexema() + " -> " + type);
		}
	}

	/**
	 * Mètode públic per a iniciar la construcció de crides recursives per a construir l'arbre sintàctic.
	 */
	public void programa() {
		lookahead = lexic.getToken();

		decl();

		accept(Type.PROG);
		llista_inst();
		accept(Type.FIPROG);
		accept(Type.EOF);

		System.out.println(semantic);

		lexic.close();
		semantic.close();
	}

	public void end() {
		lexic.close();
		semantic.close();
	}

	private void decl() {
		decl_cte_var();
		decl_func();
	}

	private void decl_cte_var() {
		Data data = new Data();
		switch (lookahead.getToken()) {
			case CONST:
				accept(Type.CONST);

				String const_name = lookahead.getLexema();

				data.setValue("name", lookahead.getLexema());
				accept(Type.ID);
				accept(Type.IGUAL);
				data = exp();

				//Fem aquesta comprovació abans que el SEMICOLON per si hi trobem un error i loggejem una línia, que ens doni la correcta
				data.setValue("const.name", const_name);
				semantic.checkConstant(data);

				accept(Type.SEMICOLON);

				break;


			case VAR:
				accept(Type.VAR);

				data.setValue("var.name", lookahead.getLexema());
				accept(Type.ID);
				accept(Type.COLON);
				ITipus tipus = tipus();
				data.setValue("var.type", tipus);
				semantic.checkVariable(data);
				accept(Type.SEMICOLON);
				break;

			default:
				return;
		}
		decl_cte_var();
	}

	private void decl_func() {
		switch (lookahead.getToken()) {
			case FUNCIO:
				accept(Type.FUNCIO);
				Data data = new Data();
				String id = lookahead.getLexema();
				data.setValue("func.name", id);

				accept(Type.ID);

				id = semantic.checkFuncio(data);
				semantic.nextBloc();

				accept(Type.OPARENT);

				llista_param(id);

				accept(Type.CPARENT);
				accept(Type.COLON);
				String tipus = lookahead.getLexema();
				semantic.setTipusFuncio(id, tipus);
				accept(Type.TIPUS_SIMPLE);
				accept(Type.SEMICOLON);
				decl_cte_var();
				accept(Type.FUNC);

				boolean ret = llista_inst();
				semantic.checkCamiReturn(ret);
				accept(Type.FIFUNC);
				accept(Type.SEMICOLON);

				semantic.previousBloc();

				decl_func();
				break;

			default:
				return;
		}
	}

	private void llista_param(String idFuncio) {

		switch (lookahead.getToken()) {
			case TIPUS_PARAM:
				llista_param_aux(idFuncio, 12);
				break;

			default:
				return;
		}
	}

	private void llista_param_aux(String idFuncio, int desp) {

		Data data = new Data();
		data.setValue("func.name", idFuncio);
		data.setValue("param.typeParam", lookahead.getLexema());
		accept(Type.TIPUS_PARAM);
		data.setValue("param.name", lookahead.getLexema());
		accept(Type.ID);
		accept(Type.COLON);
		ITipus type = tipus();
		data.setValue("param.type", type);
		data.setValue("param.desp", desp);

		semantic.addParameter(data);

		param_aux(idFuncio, (int) data.getValue("param.desp"));
	}

	private void param_aux(String idFuncio, int desp) {
		switch (lookahead.getToken()) {
			case COMA:
				accept(Type.COMA);
				llista_param_aux(idFuncio, desp);
				break;
			default:
				return;
		}
	}

	private ITipus tipus() {
		String tipus;
		switch (lookahead.getToken()) {
			case TIPUS_SIMPLE:
				tipus = lookahead.getLexema();
				accept(Type.TIPUS_SIMPLE);
				return new TipusSimple(tipus);

			case VECTOR:
				accept(Type.VECTOR);
				accept(Type.OCLAU);
				Data exp1 = exp();
				accept(Type.DPOINT);
				Data exp2 = exp();
				accept(Type.CCLAU);
				accept(Type.DE);
				tipus = lookahead.getLexema();
				accept(Type.TIPUS_SIMPLE);
				return semantic.checkVector(tipus, exp1, exp2);

			default:
				System.out.println("ERROR - tipus()");
				return new TipusIndefinit("indef", 0);
		}
	}

	private Data exp() {

		Data data = exp_simple();

		data.moveBlock("exp_aux.h", "exp_simple.s");

		exp_aux(data);
		data.moveBlock("exp.s", "exp_aux.s");

		return data;
	}

	private Data exp_simple() {
		Data data = op_unari();
		terme(data);

		data.moveBlock("terme_simple.h", "terme.s");


		terme_simple(data);

		data.moveBlock("exp_simple.s", "terme_simple.s");


		return data;
	}

	private Data op_unari() {
		Data data = new Data();
		switch (lookahead.getToken()) {
			case SUMA:
				data.setValue("op_unari.vs", TypeVar.SUMA);
				accept(Type.SUMA);
				break;

			case RESTA:
				data.setValue("op_unari.vs", TypeVar.RESTA);
				accept(Type.RESTA);
				break;

			case NOT:
				data.setValue("op_unari.vs", TypeVar.NOT);
				accept(Type.NOT);
				break;

			default:
				data.setValue("op_unari.vs", false);
				break;
		}
		return data;
	}

	private void terme_simple(Data data) {
		Data info;
		switch (lookahead.getToken()) {
			case SUMA:
				op_aux(data);

				info = new Data();
				terme(info);

				semantic.checkOp_aux(data, info);

				//No es pot fer amb move perquè són de Data diferents
				data.setValue("terme_simple.vh", info.getValue("terme.vs"));
				info.removeAttribute("terme.vs");
				data.setValue("terme_simple.th", info.getValue("terme.ts"));
				info.removeAttribute("terme.ts");
				data.setValue("terme_simple.eh", info.getValue("terme.es"));
				info.removeAttribute("terme.es");


				terme_simple(data);


				break;

			case RESTA:
				op_aux(data);

				info = new Data();
				terme(info);

				semantic.checkOp_aux(data, info);

				//No es pot fer amb move perquè són de Data diferents
				data.setValue("terme_simple.vh", info.getValue("terme.vs"));
				info.removeAttribute("terme.vs");
				data.setValue("terme_simple.th", info.getValue("terme.ts"));
				info.removeAttribute("terme.ts");
				data.setValue("terme_simple.eh", info.getValue("terme.es"));
				info.removeAttribute("terme.es");

				terme_simple(data);
				break;

			case OR:
				op_aux(data);

				info = new Data();
				terme(info);

				semantic.checkOp_aux(data, info);

				//No es pot fer amb move perquè són de Data diferents
				data.setValue("terme_simple.vh", info.getValue("terme.vs"));
				info.removeAttribute("terme.vs");
				data.setValue("terme_simple.th", info.getValue("terme.ts"));
				info.removeAttribute("terme.ts");
				data.setValue("terme_simple.eh", info.getValue("terme.es"));
				info.removeAttribute("terme.es");

				terme_simple(data);
				break;

			default:
				data.moveBlock("terme_simple.s", "terme_simple.h");
				return;
		}
	}

	private void op_aux(Data data) {
		switch (lookahead.getToken()) {

			case SUMA:
				data.setValue("op_aux.vs", TypeVar.SUMA);
				accept(Type.SUMA);
				break;

			case RESTA:
				data.setValue("op_aux.vs", TypeVar.RESTA);
				accept(Type.RESTA);
				break;

			case OR:
				data.setValue("op_aux.vs", TypeVar.OR);
				accept(Type.OR);
				break;

			default:
				break;
		}
	}

	private void terme(Data data) {

		switch (lookahead.getToken()) {
			//FACTOR
			case SENCER_CST:
				int valor = Integer.parseInt(lookahead.getLexema());
				data.setBlock("terme.s", valor, new TipusSimple("SENCER"), true);
				accept(Type.SENCER_CST);
				semantic.checkOp_unari(data);
				semantic.checkOp_binari(data);
				data.removeBlock("terme.h");
				break;

			case LOGIC_CST:
				data.setBlock("terme.s", lookahead.getLexema().equals("CERT"), new TipusSimple("LOGIC"), true);
				accept(Type.LOGIC_CST);
				semantic.checkOp_unari(data);
				semantic.checkOp_binari(data);
				data.removeBlock("terme.h");
				break;

			case CADENA:
				data.setBlock("terme.s",
						lookahead.getLexema().substring(1, lookahead.getLexema().length() - 1),
						new TipusCadena("CADENA", lookahead.getLexema().length(), lookahead.getLexema().length()),
						true);
				accept(Type.CADENA);
				break;

			case OPARENT:
				accept(Type.OPARENT);
				Data exp = exp();

				data.setBlock("terme.s", exp.getValue("exp.vs"), exp.getValue("exp.ts"), exp.getValue("exp.es"));

				// TODO: solució a la excepció de donava dissabte a la tarda relacionada amb l'atribut regs
				if (!(boolean) data.getValue("terme.es"))
					data.setValue("regs", exp.getValue("regs"));

				accept(Type.CPARENT);
				semantic.checkOp_unari(data);
				semantic.checkOp_binari(data);
				break;

			case ID:
				data.setValue("id.name", lookahead.getLexema());
				semantic.checkID(data);
				accept(Type.ID);

				semantic.checkOp_unari(data);

				semantic.checkOp_binari(data);
				data.moveBlock("factor_aux.h", "terme.s");
				factor_aux(data);
				data.moveBlock("terme.s", "factor_aux.s");


				break;
			default:
				System.out.println("ERROR - terme()");
		}

		data.moveBlock("terme_aux.h", "terme.s");

		terme_aux(data);
		data.moveBlock("terme.s", "terme_aux.s");
	}

	private void terme_aux(Data data) {

		switch (lookahead.getToken()) {
			case MUL:
				op_binaria();
				data.moveBlock("terme.h", "terme_aux.h");

				data.setValue("MUL", true);


				terme(data);

				data.moveBlock("terme_aux.s", "terme.s");
				break;

			case DIV:
				op_binaria();
				data.moveBlock("terme.h", "terme_aux.h");

				data.setValue("DIV", true);
				terme(data);
				data.moveBlock("terme_aux.s", "terme.s");
				break;

			case AND:
				op_binaria();
				data.moveBlock("terme.h", "terme_aux.h");

				data.setValue("AND", true);
				terme(data);

				data.moveBlock("terme_aux.s", "terme.s");
				break;

			default:
				data.moveBlock("terme_aux.s", "terme_aux.h");
				break;
		}
	}

	private void op_binaria() {
		switch (lookahead.getToken()) {
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
				break;
		}
	}

	private void factor_aux(Data data) {
		switch (lookahead.getToken()) {
			case OPARENT:
				accept(Type.OPARENT);
				semantic.initFuncio(data);
				llista_exp(data);

				Funcio funcio = (Funcio) data.getValue("llista_exp.vs");

				data.move("factor_aux.vs", "llista_exp.vs");
				data.setValue("factor_aux.ts", funcio.getTipus());
				data.setValue("factor_aux.es", false);
				data.setValue("op", true);

				accept(Type.CPARENT);
				break;

			case OCLAU:
				data.moveBlock("variable_aux.h", "factor_aux.h");
				variable_aux(data);
				semantic.moveToReg(data);
				data.moveBlock("factor_aux.s", "variable_aux.s");
				break;

			default:
				data.moveBlock("variable_aux.h", "factor_aux.h");
				variable_aux(data);
				data.moveBlock("factor_aux.s", "variable_aux.s");
				break;

		}
	}

	private void llista_exp(Data data) {
		switch (lookahead.getToken()) {
			case SUMA:
			case RESTA:
			case NOT:
			case SENCER_CST:
			case LOGIC_CST:
			case CADENA:
			case ID:
			case OPARENT:
				Data info = exp();

				semantic.checkParam(data, info);

				data.move("llista_exp_aux.vh", "llista_exp.vh");
				llista_exp_aux(data);
				data.move("llista_exp.vs", "llista_exp_aux.vs");
				break;
			default:

				data.move("llista_exp.vs", "llista_exp.vh");
				return;
		}

	}

	private void llista_exp_aux(Data data) {
		switch (lookahead.getToken()) {
			case COMA:
				accept(Type.COMA);
				data.move("llista_exp.vh", "llista_exp_aux.vh");
				llista_exp(data);
				data.move("llista_exp_aux.vs", "llista_exp.vs");
				break;
			default:
				semantic.checkParamNext(data);
				data.move("llista_exp_aux.vs", "llista_exp_aux.vh");
				return;
		}
	}

	private void variable_aux(Data data) {
		switch (lookahead.getToken()) {
			case OCLAU:
				accept(Type.OCLAU);
				Data info = exp();
				semantic.checkVectorAccess(data, info);
				accept(Type.CCLAU);
				data.moveBlock("variable_aux.s", "variable_aux.h");
				break;

			default:
				semantic.checkVariableAux(data);
				data.moveBlock("variable_aux.s", "variable_aux.h");
				return;
		}
	}

	private void exp_aux(Data data) {

		switch (lookahead.getToken()) {
			case OP_RELACIONAL:
				data.setValue("op_relacional.vs", lookahead.getLexema());
				accept(Type.OP_RELACIONAL);
				Data info = exp_simple();

				semantic.checkOp_relacional(data, info);
				data.removeBlock("exp_aux.h");
				data.remove("op_relacional.vs");
				break;

			default:
				data.moveBlock("exp_aux.s", "exp_aux.h");
				break;
		}
	}

	private boolean llista_inst() {

		boolean ret = inst();
		accept(Type.SEMICOLON);
		ret = ret | llista_inst_aux(ret);
		return ret;
	}

	private boolean llista_inst_aux(boolean ret) {

		switch (lookahead.getToken()) {
			case ID:
			case ESCRIURE:
			case LLEGIR:
			case CICLE:
			case MENTRE:
			case SI:
			case RETORNAR:
			case PERCADA:
				semantic.checkCodiReturn(ret);
				return llista_inst();

			default:
				return false;
		}
	}

	private boolean inst() {

		switch (lookahead.getToken()) {
			case ID:
				Data data = semantic.initAssignation(lookahead.getLexema());

				accept(Type.ID);
				variable_aux(data);
				accept(Type.IGUAL);
				data.moveBlock("igual_aux.h", "variable_aux.s");
				igual_aux(data);
				System.out.println("IGUAL DATA -> " + data);
				return false;

			case ESCRIURE:
				accept(Type.ESCRIURE);
				accept(Type.OPARENT);
				param_escriure();
				accept(Type.CPARENT);
				return false;

			case LLEGIR:
				accept(Type.LLEGIR);
				accept(Type.OPARENT);
				param_llegir();
				accept(Type.CPARENT);
				return false;

			case CICLE:
				accept(Type.CICLE);
				boolean ret_cicle = llista_inst();
				accept(Type.FINS);
				Data info_cicle = exp();
				semantic.checkLogic(info_cicle);
				return ret_cicle;

			case MENTRE:
				accept(Type.MENTRE);
				Data info_mentre = exp();
				semantic.checkLogic(info_mentre);
				accept(Type.FER);
				boolean ret_mentre = llista_inst();
				accept(Type.FIMENTRE);
				return ret_mentre;

			case SI:
				accept(Type.SI);
				Data exp_si = exp();
				semantic.checkLogic(exp_si);

				accept(Type.LLAVORS);
				boolean ret_si = llista_inst();
				ret_si = ret_si & fi_aux();
				accept(Type.FISI);
				return ret_si;

			case RETORNAR:
				accept(Type.RETORNAR);
				Data info = exp();
				semantic.checkReturn(info);
				return true;

			case PERCADA:
				accept(Type.PERCADA);
				accept(Type.ID);
				accept(Type.EN);
				accept(Type.ID);
				accept(Type.FER);
				boolean ret_foreach = llista_inst();
				accept(Type.FIPER);
				return ret_foreach;

			default:
				System.out.println("ERROR - inst()");
				return false;
		}
	}

	private void igual_aux(Data data) {

		switch (lookahead.getToken()) {
			case SI:
				//No es fa anàlisi semàntic a la ternària
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
				Data info = exp();
				System.out.println("REGISTERS ASSIG " + lexic.getActualLine());
				semantic.printRegs();
				semantic.checkAssignation(data, info);

				break;

			default:
				System.out.println("ERROR - igual_aux()");
				break;
		}
	}

	private void param_escriure() {

		Data data = exp();

		semantic.checkEscriure(data);

		switch (lookahead.getToken()) {
			case COMA:
				accept(Type.COMA);
				param_escriure();
				break;

			default:
				return;
		}
	}

	private void param_llegir() {

		Data data = semantic.initLlegir(lookahead.getLexema());
		accept(Type.ID);
		variable_aux(data);
		semantic.checkLlegir(data);

		switch (lookahead.getToken()) {
			case COMA:
				accept(Type.COMA);
				param_llegir();
				break;

			default:
				return;
		}
	}

	private boolean fi_aux() {

		switch (lookahead.getToken()) {
			case SINO:
				accept(Type.SINO);
				return llista_inst();

			default:
				return false;
		}
	}

}
