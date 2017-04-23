package analyzer;

import taulaDeSimbols.*;
import utils.Error;
import utils.TypeError;

public class SemanticAnalyzer {
	private TaulaSimbols taulaSimbols;
	private int blocActual;
	private Error error;

	public SemanticAnalyzer(){
		blocActual = 0;
		taulaSimbols = new TaulaSimbols();
		taulaSimbols.inserirBloc(new Bloc());
		taulaSimbols.setBlocActual(blocActual);

		error = Error.getInstance();
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
		if ((boolean)data.getValue("exp.es")
				&& !((ITipus)data.getValue("exp.ts")).getNom().equals("indef")) {
			Constant constant = new Constant();
			constant.setNom((String)data.getValue("const.name"));
			constant.setValor(data.getValue("exp.vs"));
			constant.setTipus((ITipus)data.getValue("exp.ts"));
			System.out.println("DATA:" + data);
			taulaSimbols.obtenirBloc(blocActual).inserirConstant(constant);
			//TODO: inserir tipus
		} else {
			//TODO: error, no es estàtica
		}

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
			if (((TipusSimple)data.getValue("terme.th")).getNom().equals("SENCER") &&
					((TipusSimple)data.getValue("terme.ts")).getNom().equals("SENCER")) {
				int op1 = (int) data.getValue("terme.vh");
				int op2 = (int) data.getValue("terme.vs");
				int resultat = op1 * op2;
				data.setValue("terme.vs", resultat);
			} else {
				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_6);
				data.setValue("terme.ts", new TipusIndefinit("indef", 0));
			}
			data.removeAttribute("MUL");
		} else if(data.getValue("DIV") != null){
			if (((TipusSimple)data.getValue("terme.th")).getNom().equals("SENCER") &&
					((TipusSimple)data.getValue("terme.ts")).getNom().equals("SENCER")) {
				int op1 = (int) data.getValue("terme.vh");
				int op2 = (int) data.getValue("terme.vs");
				int resultat = op1 / op2;
				data.setValue("terme.vs", resultat);
			} else {
				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_6);
				data.setValue("terme.ts", new TipusIndefinit("indef", 0));
			}

			data.removeAttribute("DIV");
		} else if(data.getValue("AND") != null){
			if (((TipusSimple)data.getValue("terme.th")).getNom().equals("LOGIC") &&
					((TipusSimple)data.getValue("terme.ts")).getNom().equals("LOGIC")) {
				boolean op1 = (boolean) data.getValue("terme.vh");
				boolean op2 = (boolean) data.getValue("terme.vs");
				boolean resultat = op1 && op2;
				data.setValue("terme.vs", resultat);
			} else {
				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_7);
				data.setValue("terme.ts", new TipusIndefinit("indef", 0));
			}
			System.out.println("DATA:" + data);
			data.removeAttribute("AND");
		}
	}

	public void checkOp_aux(Data data, Data info){
		//TODO: Control d'errors
		if (data.getValue("op_aux.vs") == TypeVar.SUMA){
			System.out.println("DATA:" + data);
			System.out.println((TipusSimple)data.getValue("terme_simple.th"));

			if (((TipusSimple)data.getValue("terme_simple.th")).getNom().equals("SENCER") &&
					((TipusSimple)info.getValue("terme.ts")).getNom().equals("SENCER")) {
				int op1 = (int) data.getValue("terme_simple.vh");
				int op2 = (int) info.getValue("terme.vs");
				int res = op1 + op2;
				info.setValue("terme.vs", res);
			} else {
				//TODO: recuperació errors
                error.insertError(TypeError.ERR_SEM_6);
                data.setValue("terme.ts", new TipusIndefinit("indef", 0));
			}
			data.removeAttribute("op_aux.vs");
		} else if (data.getValue("op_aux.vs") == TypeVar.RESTA) {
			if (((TipusSimple)data.getValue("terme_simple.th")).getNom().equals("SENCER") &&
					((TipusSimple)info.getValue("terme.ts")).getNom().equals("SENCER")) {
				int op1 = (int) data.getValue("terme_simple.vh");
				int op2 = (int) info.getValue("terme.vs");
				int res = op1 - op2;
				info.setValue("terme.vs", res);
			} else {
				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_6);
				data.setValue("terme.ts", new TipusIndefinit("indef", 0));
			}
			data.removeAttribute("op_aux.vs");
		} else if (data.getValue("op_aux.vs") == TypeVar.OR) {
			if (((TipusSimple)data.getValue("terme_simple.th")).getNom().equals("LOGIC") &&
					((TipusSimple)info.getValue("terme.ts")).getNom().equals("LOGIC")) {
				boolean op1 = (boolean) data.getValue("terme_simple.vh");
				boolean op2 = (boolean) info.getValue("terme.vs");
				boolean res = op1 || op2;
				info.setValue("terme.vs", res);
			} else {
				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_7);
				data.setValue("terme.ts", new TipusIndefinit("indef", 0));
			}
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
