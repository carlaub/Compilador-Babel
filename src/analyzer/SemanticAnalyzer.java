package analyzer;

import taulaDeSimbols.*;

public class SemanticAnalyzer {
	private TaulaSimbols taulaSimbols;
	private int blocActual;

	public SemanticAnalyzer(){
		blocActual = 0;
		taulaSimbols = new TaulaSimbols();
		taulaSimbols.inserirBloc(new Bloc());
		taulaSimbols.setBlocActual(blocActual);
	}

	@Override
	public String toString(){
		return taulaSimbols.toXml();
	}

	public void nextBloc(){
		//Personalment preferiria posar blocActual++;
		blocActual = 1;
		taulaSimbols.inserirBloc(new Bloc());
	}

	public void previousBloc(){
		//Personalment preferiria posar blocActual--;
		//La següent línia ha d'anar descomentada, fer-ho en els següents casos:
//			-Estem en producció i ja no necessitem mirar el bloc
//			-Tenim més d'una única funció declarada

//		taulaSimbols.esborrarBloc(1);
		blocActual = 0;
	}

	public void checkConstant(Data data) {
		//TODO: Comprovació de la informació de constant

		Constant constant = new Constant();
		constant.setNom((String)data.getValue("name"));
		taulaSimbols.obtenirBloc(blocActual).inserirConstant(constant);
	}

	public void checkVariable(Data data) {
		//TODO: Comprovació de la informació de variable

		Variable variable = new Variable();
		variable.setNom((String) data.getValue("name"));
		variable.setTipus((ITipus) data.getValue("type"));
		taulaSimbols.obtenirBloc(blocActual).inserirVariable(variable);
	}

	public void checkFuncio(Data data){
		//Aquí també seria millor utilitzar blocActual
		Funcio funcio = new Funcio();
		funcio.setNom((String)data.getValue("name"));
		taulaSimbols.obtenirBloc(0).inserirProcediment(funcio);
	}

	public void addParameter(Data data) {

		Parametre parametre = new Parametre();
		parametre.setNom((String) data.getValue("name"));
		parametre.setTipus((ITipus) data.getValue("type"));
		parametre.setTipusPasParametre(new TipusPasParametre((String)data.getValue("typeParam")));
		taulaSimbols.obtenirBloc(0)
				.obtenirProcediment((String)data.getValue("idFunction"))
				.inserirParametre(parametre);
		taulaSimbols.obtenirBloc(1).inserirVariable(parametre);
	}

	public void checkOp_binari(Data data){
		//TODO: Afegir comprovacions
		if (data.getValue("MUL") != null ){
			int op1 = (int)data.getValue("terme.vh");
			int op2 = (int)data.getValue("terme.vs");
			int resultat = op1 * op2;
			data.setValue("terme.vs", resultat);
			data.removeAttribute("MUL");
		} else if(data.getValue("DIV") != null){
			int op1 = (int)data.getValue("terme.vh");
			int op2 = (int)data.getValue("terme.vs");
			int resultat = op1 / op2;
			data.setValue("terme.vs", resultat);
			data.removeAttribute("DIV");
		} else if(data.getValue("AND") != null){
			boolean op1 = (boolean)data.getValue("terme.vh");
			boolean op2 = (boolean)data.getValue("terme.vs");
			boolean resultat = op1 && op2;
			data.setValue("terme.vs", resultat);
			data.removeAttribute("AND");
		}
	}

	public void checkOp_aux(Data data, Data info){
		//TODO: Control d'errors
		if (data.getValue("op_aux.vs") == TypeVar.SUMA){
			int op1 = (int) data.getValue("terme_simple.vh");
			int op2 = (int) info.getValue("terme.vs");
			int res = op1 + op2;
			info.setValue("terme.vs", res);
			data.removeAttribute("op_aux.vs");
		} else if (data.getValue("op_aux.vs") == TypeVar.RESTA) {
			int op1 = (int) data.getValue("terme_simple.vh");
			int op2 = (int) info.getValue("terme.vs");
			int res = op1 - op2;
			info.setValue("terme.vs", res);
			data.removeAttribute("op_aux.vs");
		} else if (data.getValue("op_aux.vs") == TypeVar.OR) {
			boolean op1 = (boolean) data.getValue("terme_simple.vh");
			boolean op2 = (boolean) info.getValue("terme.vs");
			boolean res = op1 || op2;
			info.setValue("terme.vs", res);
			data.removeAttribute("op_aux.vs");
		}
	}

	public void checkOp_unari(Data data) {
		if (data.getValue("op_unari.vs") == TypeVar.RESTA){
			data.setValue("terme.vs", -(int)data.getValue("terme.vs"));
		} else if (data.getValue("op_unari.vs") == TypeVar.SUMA){

		} else if (data.getValue("op_unari.vs") == TypeVar.NOT){
			data.setValue("terme.vs", !(boolean)data.getValue("terme.vs"));
		}else{

		}
		data.removeAttribute("op_unari.vs");
	}
}
