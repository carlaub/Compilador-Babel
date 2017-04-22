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
			default: return;
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
			default: return;
		}
	}

	private void llista_param(String idFuncio){

		int nlinia = lexic.getActualLine();
		Type token = lookahead.getToken();
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
		Data data = exp_simple();
		exp_aux(data);	//No salta excepció
		return data;
	}

	private Data exp_simple(){
		System.out.println("BEGIN");
		Data data = op_unari();
		terme(data);

		if(data.getValue("terme.vs") != null){
			data.setValue("terme_simple.vh", data.getValue("terme.vs"));
			data.removeAttribute("terme.vs");
			data.setValue("terme_simple.th", data.getValue("terme.ts"));
			data.removeAttribute("terme.ts");
			data.setValue("terme_simple.eh", data.getValue("terme.es"));
			data.removeAttribute("terme.es");
		}
		terme_simple(data);
		System.out.println(data);
		if (data.getValue("terme_simple.vs") != null)System.out.println((int)data.getValue("terme_simple.vs"));
		System.out.println("END");
		return null;
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
				break;
		}
		return data;
	}

	private void terme_simple(Data data){
		switch (lookahead.getToken()){
			case SUMA:
				op_aux(data);
				terme(data);
				System.out.println(data);
				int op1;
				if (data.getValue("terme_simple.vh") != null){
					if (data.getValue("op_unari.vs") == TypeVar.RESTA){
						data.removeAttribute("op_unari.vs");
						op1 = -Integer.parseInt((String)data.getValue("terme_simple.vh"));
					}
					else op1 = Integer.parseInt((String)data.getValue("terme_simple.vh"));
					//TODO: Canviar es i ts
					data.setValue("terme_simple.ts", new TipusSimple("SENCER", 0));
					data.setValue("terme_simple.es", true);
					int op2 = Integer.parseInt((String) data.getValue("terme.vs"));
					int res = op1 + op2;
					data.setValue("terme_simple.vs", res);
				}
				if (data.getValue("terme_simple.vs") != null){
					data.setValue("terme_simple.vh", Integer.toString((int)data.getValue("terme_simple.vs")));
					data.setValue("terme_simple.th", data.getValue("terme_simple.ts"));
					data.setValue("terme_simple.eh", data.getValue("terme_simple.es"));
				}

				terme_simple(data);
				break;
			case RESTA:
				op_aux(data);
				terme(data);
				System.out.println(data);
				int op1r;
				if (data.getValue("terme_simple.vh") != null){
					if (data.getValue("op_unari.vs") == TypeVar.RESTA){
						data.removeAttribute("op_unari.vs");
						op1r = -Integer.parseInt((String)data.getValue("terme_simple.vh"));
					}
					else op1r = Integer.parseInt((String)data.getValue("terme_simple.vh"));
					//TODO: Canviar es i ts
					data.setValue("terme_simple.ts", new TipusSimple("SENCER", 0));
					data.setValue("terme_simple.es", true);
					int op2 = Integer.parseInt((String) data.getValue("terme.vs"));
					int res = op1r - op2;
					data.setValue("terme_simple.vs", res);
				}
				if (data.getValue("terme_simple.vs") != null){
					data.setValue("terme_simple.vh", Integer.toString((int)data.getValue("terme_simple.vs")));
					data.setValue("terme_simple.th", data.getValue("terme_simple.ts"));
					data.setValue("terme_simple.eh", data.getValue("terme_simple.es"));
				}

				terme_simple(data);
				break;
			case OR:
				op_aux(data);
				terme(data);

				data.setValue("terme_simple.vh", data.getValue("terme.vs"));
				data.setValue("terme_simple.th", data.getValue("terme.ts"));
				data.setValue("terme_simple.eh", data.getValue("terme.es"));

				terme_simple(data);
				break;
			default:
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
		TipusSimple tipus = new TipusSimple();
		switch (lookahead.getToken()) {
			//FACTOR
			case SENCER_CST:
				//TODO: Afegir tamany, min i max
				tipus.setNom("SENCER");
				data.setValue("terme.vs", lookahead.getLexema());
				data.setValue("terme.ts", tipus);
				data.setValue("terme.es", true);
				accept(Type.SENCER_CST);
				semantic.checkOp_binari(data);
				break;
			case LOGIC_CST:
				tipus.setNom("LOGIC");
				data.setValue("terme.vs", lookahead.getLexema());
				data.setValue("terme.ts", tipus);
				data.setValue("terme.es", true);
				accept(Type.LOGIC_CST);
				break;
			case CADENA:
				tipus.setNom("CADENA");
				data.setValue("terme.vs", lookahead.getLexema());
				data.setValue("terme.ts", tipus);
				data.setValue("terme.es", true);
				accept(Type.CADENA);
				break;
			case OPARENT:
				accept(Type.OPARENT);
				//TODO
				exp();
				accept(Type.CPARENT);
				break;
			case ID:
				//TODO
				accept(Type.ID);
				factor_aux();
				break;
			default: //ERROR
				System.out.println("ERROR");
		}
		try{
			data.setValue("terme_aux.vh", data.getValue("terme.vs"));
			data.setValue("terme_aux.th", data.getValue("terme.ts"));
			data.setValue("terme_aux.eh", data.getValue("terme.es"));
		}catch (NullPointerException e){

		}
		terme_aux(data);
	}

	private Data terme_aux(Data data){

		switch (lookahead.getToken()){
			case MUL:
				op_binaria();
				data.setValue("terme.vh", data.getValue("terme_aux.vh"));
				data.removeAttribute("terme_aux.vh");
				data.setValue("terme.th", data.getValue("terme_aux.th"));
				data.removeAttribute("terme_aux.th");
				data.setValue("terme.eh", data.getValue("terme_aux.eh"));
				data.removeAttribute("terme_aux.eh");
				data.setValue("MUL", true);
				terme(data);
				break;
			case DIV:
				op_binaria();
				data.setValue("terme.vh", data.getValue("terme.vs"));
				data.removeAttribute("terme.vs");
				data.setValue("terme.th", data.getValue("terme.ts"));
				data.removeAttribute("terme.ts");
				data.setValue("terme.eh", data.getValue("terme.es"));
				data.removeAttribute("terme.es");
				data.setValue("DIV", true);
				terme(data);
				break;
			case AND:
				op_binaria();
				terme(data);
				break;
			default:
				return data;
		}
		return data;
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

	private Data exp_aux(Data data) {
		switch(lookahead.getToken()) {
			case OP_RELACIONAL:
				accept(Type.OP_RELACIONAL);
				exp_simple();
				break;
			default:
				break;
		}
		return data;
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
