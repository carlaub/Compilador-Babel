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
		//Comprovació de si té tipus
		if (((ITipus)data.getValue("exp.ts")).getNom().equals("indef")){
			//L'error ja l'haurem mostrat allà on hem detectat que els tipus no són coincidents
			Constant constant = new Constant(
					(String)data.getValue("const.name"),
					new TipusIndefinit("indef", 0),
					false );
			taulaSimbols.obtenirBloc(blocActual).inserirConstant(constant);
			return;
		}
		//Comprovació de si és estàtic
		if (!(boolean)data.getValue("exp.es")){
			error.insertError(TypeError.ERR_SEM_20);
			Constant constant = new Constant(
					(String)data.getValue("const.name"),
					new TipusIndefinit("indef", 0),
					false );
			taulaSimbols.obtenirBloc(blocActual).inserirConstant(constant);
			return;
		}
		String const_name = (String)data.getValue("const.name");
		//Comprovació de si ha estat declarat com a constant prèviament
		if (taulaSimbols.obtenirBloc(blocActual).existeixConstant(const_name)){
			error.insertError(TypeError.ERR_SEM_1, const_name);
			return;
		}
		//Comprovació de si ha estat declarat com a variable o funció prèviament
		if (taulaSimbols.obtenirBloc(blocActual).existeixID(const_name)){
			//TODO: Decidir codi d'error per aquest cas.
			//Si ens decantem per ERR_SEM_1, el condicional anterior perd tot el sentit
			error.insertError(TypeError.ERR_SEM_1, const_name);
			return;
		}
		Constant constant = new Constant(
				const_name,
				(ITipus)data.getValue("exp.ts"),
				data.getValue("exp.vs")
		);
		taulaSimbols.obtenirBloc(blocActual).inserirConstant(constant);
	}

	public void checkVariable(Data data) {
		//TODO: Comprovació de la informació de variable
		//TODO: Modificar el desplaçament
		ITipus type = (ITipus)data.getValue("var.type");
		if (type.getNom().equals("indef")){
			Variable variable = new Variable(
					(String)data.getValue("var.name"),
					new TipusIndefinit("indef", 0),
					0);
			taulaSimbols.obtenirBloc(blocActual).inserirVariable(variable);
			return;
		}
		String var_name = (String)data.getValue("var.name");
		//Comprovació de si ha estat declarat com a variable prèviament
		if (taulaSimbols.obtenirBloc(blocActual).existeixVariable(var_name)){
			error.insertError(TypeError.ERR_SEM_2, var_name);
			return;
		}
		//Comprovació de si ha estat declarat com a constant o funció prèviament
		if (taulaSimbols.obtenirBloc(blocActual).existeixID(var_name)){
			//TODO: Decidir codi d'error per aquest cas.
			//Si ens decantem per ERR_SEM_2, el condicional anterior perd tot el sentit
			error.insertError(TypeError.ERR_SEM_2, var_name);
			return;
		}
		Variable variable = new Variable(
				var_name,
				(ITipus) data.getValue("var.type"),
				0
		);
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
			if (((ITipus)data.getValue("terme.th")).getNom().equals("SENCER") &&
					((ITipus)data.getValue("terme.ts")).getNom().equals("SENCER")) {
				int op1 = (int) data.getValue("terme.vh");
				int op2 = (int) data.getValue("terme.vs");
				int resultat = op1 * op2;
				data.setValue("terme.vs", resultat);
				//Actualització de si és estàtic
				boolean e1 = (boolean) data.getValue("terme.eh");
				boolean e2 = (boolean) data.getValue("terme.es");
				boolean e_res = e1 && e2;
				data.setValue("terme.es", e_res);

			} else {
				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_6);
				data.setValue("terme.ts", new TipusIndefinit("indef", 0));
			}
			data.removeAttribute("MUL");
		} else if(data.getValue("DIV") != null){
			if (((ITipus)data.getValue("terme.th")).getNom().equals("SENCER") &&
					((ITipus)data.getValue("terme.ts")).getNom().equals("SENCER")) {
				int op1 = (int) data.getValue("terme.vh");
				int op2 = (int) data.getValue("terme.vs");
				int resultat = op1 / op2;
				data.setValue("terme.vs", resultat);
				//Actualització de si és estàtic
				boolean e1 = (boolean) data.getValue("terme.eh");
				boolean e2 = (boolean) data.getValue("terme.es");
				boolean e_res = e1 && e2;
				data.setValue("terme.es", e_res);
			} else {
				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_6);
				data.setValue("terme.ts", new TipusIndefinit("indef", 0));
			}

			data.removeAttribute("DIV");
		} else if(data.getValue("AND") != null){
			if (((ITipus)data.getValue("terme.th")).getNom().equals("LOGIC") &&
					((ITipus)data.getValue("terme.ts")).getNom().equals("LOGIC")) {
				boolean op1 = (boolean) data.getValue("terme.vh");
				boolean op2 = (boolean) data.getValue("terme.vs");
				boolean resultat = op1 && op2;
				data.setValue("terme.vs", resultat);
				//Actualització de si és estàtic
				boolean e1 = (boolean) data.getValue("terme.eh");
				boolean e2 = (boolean) data.getValue("terme.es");
				boolean e_res = e1 && e2;
				data.setValue("terme.es", e_res);
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
			System.out.println(data.getValue("terme_simple.th"));

			if (((ITipus)data.getValue("terme_simple.th")).getNom().equals("SENCER") &&
					((ITipus)info.getValue("terme.ts")).getNom().equals("SENCER")) {
				int op1 = (int) data.getValue("terme_simple.vh");
				int op2 = (int) info.getValue("terme.vs");
				int res = op1 + op2;
				info.setValue("terme.vs", res);
				//Actualització de si és estàtic
				boolean e1 = (boolean) data.getValue("terme_simple.eh");
				boolean e2 = (boolean) info.getValue("terme.es");
				boolean e_res = e1 && e2;
				info.setValue("terme.es", e_res);
			} else {
				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_6);
				info.setValue("terme.ts", new TipusIndefinit("indef", 0));
			}
			data.removeAttribute("op_aux.vs");
		} else if (data.getValue("op_aux.vs") == TypeVar.RESTA) {
			if (((ITipus)data.getValue("terme_simple.th")).getNom().equals("SENCER") &&
					((ITipus)info.getValue("terme.ts")).getNom().equals("SENCER")) {
				int op1 = (int) data.getValue("terme_simple.vh");
				int op2 = (int) info.getValue("terme.vs");
				int res = op1 - op2;
				info.setValue("terme.vs", res);
				//Actualització de si és estàtic
				boolean e1 = (boolean) data.getValue("terme_simple.eh");
				boolean e2 = (boolean) info.getValue("terme.es");
				boolean e_res = e1 && e2;
				info.setValue("terme.es", e_res);
			} else {
				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_6);
				info.setValue("terme.ts", new TipusIndefinit("indef", 0));
			}
			data.removeAttribute("op_aux.vs");
		} else if (data.getValue("op_aux.vs") == TypeVar.OR) {
			if (((ITipus)data.getValue("terme_simple.th")).getNom().equals("LOGIC") &&
					((ITipus)info.getValue("terme.ts")).getNom().equals("LOGIC")) {
				boolean op1 = (boolean) data.getValue("terme_simple.vh");
				boolean op2 = (boolean) info.getValue("terme.vs");
				boolean res = op1 || op2;
				info.setValue("terme.vs", res);
				//Actualització de si és estàtic
				boolean e1 = (boolean) data.getValue("terme_simple.eh");
				boolean e2 = (boolean) info.getValue("terme.es");
				boolean e_res = e1 && e2;
				info.setValue("terme.es", e_res);
			} else {
				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_7);
				info.setValue("terme.ts", new TipusIndefinit("indef", 0));
			}
			data.removeAttribute("op_aux.vs");
		}
	}

	public void checkOp_unari(Data data) {

        //TODO: tamanys

        if (data.getValue("op_unari.vs") == TypeVar.RESTA){

            if (((ITipus)data.getValue("terme.ts")).getNom().equals("SENCER")) {
                data.setValue("terme.vs", -(int)data.getValue("terme.vs"));

            } else {
                error.insertError(TypeError.ERR_SEM_6);
                data.setValue("terme.vs", 0);
                data.setValue("terme.ts", new TipusIndefinit("indef", 0));
            }

		} else if (data.getValue("op_unari.vs") == TypeVar.SUMA){
            if (((ITipus)data.getValue("terme.ts")).getNom().equals("SENCER")) {

            } else {
                error.insertError(TypeError.ERR_SEM_6);
                data.setValue("terme.vs", 0);
                data.setValue("terme.ts", new TipusIndefinit("indef", 0));
            }

		} else if (data.getValue("op_unari.vs") == TypeVar.NOT){
            if (((ITipus)data.getValue("terme.ts")).getNom().equals("LOGIC")) {
                data.setValue("terme.vs", !(boolean)data.getValue("terme.vs"));

            } else {
                error.insertError(TypeError.ERR_SEM_7);
                data.setValue("terme.vs", 0);
                data.setValue("terme.ts", new TipusIndefinit("indef", 0));
            }
		}else{

		}
		data.removeAttribute("op_unari.vs");
	}

	public void checkOp_relacional(Data data, Data info) {

        //Comprovem que els tipus a evaluar son tots dos sencer, en cas que no sigui aixi, error
        if (!((ITipus)data.getValue("exp_aux.th")).getNom().equals("SENCER") ||
                !((ITipus)info.getValue("exp_simple.ts")).getNom().equals("SENCER")) {
            error.insertError(TypeError.ERR_SEM_6);
            //TODO: inserir indefinit, recuperació d'errors

            data.setValue("exp_aux.vs", 0);
            data.setValue("exp_aux.ts", new TipusIndefinit("indef", 0));
            data.setValue("exp_aux.es", true);
        } else {
            //TODO tamany?
            //TODO estatic?

            if (data.getValue("op_relacional.vs").equals(">")) {
                data.setValue("exp_aux.vs", (int)data.getValue("exp_aux.vh") > (int)info.getValue("exp_simple.vs")) ;
                data.setValue("exp_aux.ts", new TipusSimple("LOGIC", 0));
                data.setValue("exp_aux.es", true);

            } else  if (data.getValue("op_relacional.vs").equals("<")) {
                data.setValue("exp_aux.vs", (int) data.getValue("exp_aux.vh") < (int) info.getValue("exp_simple.vs"));
                data.setValue("exp_aux.ts", new TipusSimple("LOGIC", 0));
                data.setValue("exp_aux.es", true);

            } else  if (data.getValue("op_relacional.vs").equals("<=")) {
                data.setValue("exp_aux.vs", (int) data.getValue("exp_aux.vh") <= (int) info.getValue("exp_simple.vs"));
                data.setValue("exp_aux.ts", new TipusSimple("LOGIC", 0));
                data.setValue("exp_aux.es", true);

            } else if (data.getValue("op_relacional.vs").equals(">=")) {
                data.setValue("exp_aux.vs", (int) data.getValue("exp_aux.vh") >= (int) info.getValue("exp_simple.vs"));
                data.setValue("exp_aux.ts", new TipusSimple("LOGIC", 0));
                data.setValue("exp_aux.es", true);

            } else if (data.getValue("op_relacional.vs").equals("==")) {
                data.setValue("exp_aux.vs", (int) data.getValue("exp_aux.vh") == (int) info.getValue("exp_simple.vs"));
                data.setValue("exp_aux.ts", new TipusSimple("LOGIC", 0));
                data.setValue("exp_aux.es", true);

            } else if (data.getValue("op_relacional.vs").equals("<>")) {
                data.setValue("exp_aux.vs", (int) data.getValue("exp_aux.vh") != (int) info.getValue("exp_simple.vs"));
                data.setValue("exp_aux.ts", new TipusSimple("LOGIC", 0));
                data.setValue("exp_aux.es", true);
            }
        }
    }
}
