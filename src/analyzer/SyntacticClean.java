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
		System.out.println(semantic);

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

	private void exp(){
		exp_simple();
		exp_aux();	//No salta excepció
	}

	private void exp_simple(){
		op_unari();
		terme();
		terme_simple();
	}

	private void op_unari(){
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

	private void terme_simple(){
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

	private void op_aux(){
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
				break;
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
			default: //ERROR
				System.out.println("ERROR");
		}
		terme_aux();
	}

	private void terme_aux(){
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

	private void exp_aux() {
		switch(lookahead.getToken()) {
			case OP_RELACIONAL:
				accept(Type.OP_RELACIONAL);
				exp_simple();
				break;
			default: return;
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
