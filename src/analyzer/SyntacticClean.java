package analyzer;


import taulaDeSimbols.*;

import java.io.IOException;


public class SyntacticClean {
	private static SyntacticClean instance;
	private static LexicographicAnalyzer lexic;
	private static SemanticAnalyzer semantic;
	private static Token lookahead;

	public static SyntacticClean getInstance(String fileName) throws IOException {
		if (instance == null) instance = new SyntacticClean(fileName);
		return instance;
	}

	private SyntacticClean(String fileName) throws IOException {
		lexic = LexicographicAnalyzer.getInstance(fileName);
		semantic = new SemanticAnalyzer();
	}

	private void accept(Type type){
		if(lookahead.getToken().equals(type)){
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
		accept(Type.EOF);

		lexic.close();
		System.out.println(semantic);
	}

	private void decl(){
		decl_cte_var();
		decl_func();
	}

	private void decl_cte_var(){
		Data data = new Data();
		switch(lookahead.getToken()) {
			case CONST:
				accept(Type.CONST);
				//TODO: Afegir tipus i valor a data

				data.setValue("name", lookahead.getLexema());
				accept(Type.ID);
				accept(Type.IGUAL);
				exp();
				accept(Type.SEMICOLON);

				semantic.checkConstant(data);
				break;

			case VAR:
				accept(Type.VAR);

				data.setValue("name", lookahead.getLexema());
				accept(Type.ID);
				accept(Type.COLON);
				data.setValue("type", tipus());
				accept(Type.SEMICOLON);
				semantic.checkVariable(data);
				break;

			default:
				return;
		}
		decl_cte_var();
	}

	private void decl_func(){
		switch (lookahead.getToken()) {
			case FUNCIO:
				accept(Type.FUNCIO);
				Data data = new Data();
				data.setValue("name", lookahead.getLexema());

				accept(Type.ID);

				//TODO: Afegir llista paràmetres

				semantic.checkFuncio(data);
				semantic.nextBloc();

				accept(Type.OPARENT);

				llista_param((String)data.getValue("name"));

				accept(Type.CPARENT);
				accept(Type.COLON);
				accept(Type.TIPUS_SIMPLE);
				accept(Type.SEMICOLON);
					//TODO: Afegir paràmetres a la llista de variables del bloc

				decl_cte_var();
				accept(Type.FUNC);

				llista_inst();

				accept(Type.FIFUNC);
				accept(Type.SEMICOLON);

				semantic.previousBloc();

				decl_func();
				break;

			default:
				return;
		}
	}

	private void llista_param(String idFuncio){

		switch (lookahead.getToken()) {
			case TIPUS_PARAM:
				llista_param_aux(idFuncio);
				break;

			default:
				return;
		}
	}

	private void llista_param_aux(String idFuncio){

		Data data = new Data();
		data.setValue("idFunction", idFuncio);
		data.setValue("typeParam", lookahead.getLexema());
		accept(Type.TIPUS_PARAM);
		data.setValue("name", lookahead.getLexema());
		accept(Type.ID);
		accept(Type.COLON);
		data.setValue("type", tipus());

		//TODO: Afegir el tipus al paràmetre i el tipus de paràmetre
		semantic.addParameter(data);
		param_aux(idFuncio);
	}

	private void param_aux(String idFuncio){
		switch (lookahead.getToken()) {
			case COMA:
				accept(Type.COMA);
				llista_param_aux(idFuncio);
				break;
			default: return;
		}
	}

	private ITipus tipus(){
		String tipus;
		switch (lookahead.getToken()) {
			case TIPUS_SIMPLE:
				tipus = lookahead.getLexema();
				accept(Type.TIPUS_SIMPLE);
				//TODO: Canviar mida del tipus
				TipusSimple tipusSimple = new TipusSimple(tipus, 0);
				return tipusSimple;
			case VECTOR:
				accept(Type.VECTOR);
				accept(Type.OCLAU);
//                int exp1 = exp();
				exp();
				accept(Type.DPOINT);
//                int exp2 = exp();
				exp();
				accept(Type.CCLAU);
				accept(Type.DE);
				tipus = lookahead.getLexema();
				accept(Type.TIPUS_SIMPLE);
				//TODO: Ficar el nom correcte, la mida correcta i el tamany del tipus simple correcte
				TipusArray tipusArray = new TipusArray("", 0/*exp2-exp1*/, new TipusSimple(tipus, 0));
				return tipusArray;
			default: //ERROR
				System.out.println("ERROR");
				return new TipusIndefinit();
		}
	}

	private Data exp(){
		System.out.println("BEGIN");
		Data data = exp_simple();

		data.moveBlock("exp_aux.h", "exp_simple.s");

		exp_aux(data);

		data.moveBlock("exp.s", "exp_aux.s");

		System.out.println(data);
		System.out.println("END");
		return data;
	}

	private Data exp_simple(){
		Data data = op_unari();
		terme(data);

		data.moveBlock("terme_simple.h", "terme.s");

		terme_simple(data);

		data.moveBlock("exp_simple.s", "terme_simple.s");

		return data;
	}

	private Data op_unari(){
		Data data = new Data();
		switch(lookahead.getToken()) {
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

	private void terme_simple(Data data){
		Data info;
		switch (lookahead.getToken()){
			case SUMA:
				op_aux(data);

				info = new Data();
				terme(info);

				System.out.println("| |-Info");
				System.out.println("|   |-"+info);

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

				System.out.println("| |-Info");
				System.out.println("|   |-"+info);

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
				System.out.println("| |-Info");
				System.out.println("|   |-"+info);

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

	private void op_aux(Data data){
		switch(lookahead.getToken()) {

			case SUMA:
				data.setValue("op_aux.vs",TypeVar.SUMA);
				accept(Type.SUMA);
				break;

			case RESTA:
				data.setValue("op_aux.vs",TypeVar.RESTA);
				accept(Type.RESTA);
				break;

			case OR:
				data.setValue("op_aux.vs",TypeVar.OR);
				accept(Type.OR);
				break;

			default:
				break;
		}
	}

	private void terme(Data data){
		System.out.println("|-Terme");
		System.out.println("| |-"+data);

		switch (lookahead.getToken()) {
			//FACTOR
			case SENCER_CST:
				int valor = Integer.parseInt(lookahead.getLexema());
				data.setValue("terme.vs", valor);
				data.setValue("terme.ts", new TipusSimple("SENCER", 0));
				data.setValue("terme.es", true);
				accept(Type.SENCER_CST);
				semantic.checkOp_unari(data);
				semantic.checkOp_binari(data);
				data.removeAttribute("terme.vh");
				data.removeAttribute("terme.th");
				data.removeAttribute("terme.eh");
				break;
			case LOGIC_CST:
				data.setValue("terme.vs", lookahead.getLexema().equals("CERT"));
				data.setValue("terme.ts", new TipusSimple("LOGIC", 0));
				data.setValue("terme.es", true);
				accept(Type.LOGIC_CST);
				semantic.checkOp_unari(data);
				semantic.checkOp_binari(data);
				data.removeAttribute("terme.vh");
				data.removeAttribute("terme.th");
				data.removeAttribute("terme.eh");
				break;
			case CADENA:
				data.setValue("terme.vs", lookahead.getLexema());
				data.setValue("terme.ts", new TipusSimple("CADENA", 0));
				data.setValue("terme.es", true);
				accept(Type.CADENA);
				break;
			case OPARENT:
				accept(Type.OPARENT);
				Data exp = exp();
				data.setValue("terme.vs", exp.getValue("exp.vs"));
				data.setValue("terme.ts", exp.getValue("exp.ts"));
				data.setValue("terme.es", exp.getValue("exp.es"));
				accept(Type.CPARENT);
				break;
			case ID:
				//TODO
				accept(Type.ID);
				factor_aux();
				break;
			default:
				System.out.println("ERROR");
		}

		System.out.println("|   |-"+data);
		//El case ID, que no té settejats aquests valors i llença l'excepció
		try {
			data.moveBlock("terme_aux.h", "terme.s");
		} catch (NullPointerException e){}

		System.out.println("|     |-"+data);
		terme_aux(data);

		data.moveBlock("terme.s", "terme_aux.s");

		System.out.println("|       |-"+data);

	}

	private void terme_aux(Data data){

		switch (lookahead.getToken()){
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

	private void op_binaria(){
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
				break;
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
		switch (lookahead.getToken()){
			case SUMA:
			case RESTA:
			case NOT:
			case SENCER_CST:
			case LOGIC_CST:
			case CADENA:
			case ID:
			case OPARENT:
				exp();
				llista_exp_aux();
			default:
				return;
		}

	}

	private void llista_exp_aux(){
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

	private void exp_aux(Data data) {
		switch(lookahead.getToken()) {
			case OP_RELACIONAL:
				accept(Type.OP_RELACIONAL);

				Data info = exp_simple();
				//TODO: Tractament de l'operació
				break;
			default:
				data.move("exp_aux.vs", "exp_aux.vh");
				data.move("exp_aux.ts", "exp_aux.th");
				data.move("exp_aux.es", "exp_aux.eh");
				break;
		}
	}

	private void llista_inst(){

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
				break;
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
				System.out.println("ERROR");
		}
	}

	private void igual_aux(){
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
				break;
			default:
				//ERROR
				System.out.println("ERROR");
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