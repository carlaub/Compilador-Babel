package analyzer;

import javafx.beans.binding.ObjectBinding;
import taulaDeSimbols.*;
import utils.Error;
import utils.TypeError;

public class SemanticAnalyzer {
	private TaulaSimbols taulaSimbols;
	private int blocActual;
	private Error error;
	private static final int INDEF = -1;

	public SemanticAnalyzer() {
		blocActual = 0;
		taulaSimbols = new TaulaSimbols();
		taulaSimbols.inserirBloc(new Bloc());
		taulaSimbols.setBlocActual(blocActual);

		error = Error.getInstance();
	}

	@Override
	public String toString() {
		return taulaSimbols.toXml();
	}

	public void nextBloc() {
		//Personalment preferiria posar blocActual++;
		blocActual = 1;
		taulaSimbols.inserirBloc(new Bloc());
	}

	public void previousBloc() {
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
		if (((ITipus) data.getValue("exp.ts")).getNom().equals("indef") ||
				((ITipus) data.getValue("exp.ts")).getTamany() == INDEF) {
			//L'error ja l'haurem mostrat allà on hem detectat que els tipus no són coincidents
			Constant constant = new Constant(
					(String) data.getValue("const.name"),
					new TipusIndefinit("indef", 0),
					false);
			taulaSimbols.obtenirBloc(blocActual).inserirConstant(constant);
			return;
		}
		//Comprovació de si és estàtic
		if (!(boolean) data.getValue("exp.es")) {
			error.insertError(TypeError.ERR_SEM_20);
			Constant constant = new Constant(
					(String) data.getValue("const.name"),
					new TipusIndefinit("indef", 0),
					false);
			taulaSimbols.obtenirBloc(blocActual).inserirConstant(constant);
			return;
		}
		String const_name = (String) data.getValue("const.name");
		//Comprovació de si ha estat declarat com a constant prèviament
		if (taulaSimbols.obtenirBloc(blocActual).existeixConstant(const_name)) {
			error.insertError(TypeError.ERR_SEM_1, const_name);
			return;
		}
		//Comprovació de si ha estat declarat com a variable o funció prèviament
		if (taulaSimbols.obtenirBloc(blocActual).existeixID(const_name)) {
			//TODO: Decidir codi d'error per aquest cas.
			//Si ens decantem per ERR_SEM_1, el condicional anterior perd tot el sentit
			error.insertError(TypeError.ERR_SEM_1, const_name);
			return;
		}
		Constant constant = new Constant(
				const_name,
				(ITipus) data.getValue("exp.ts"),
				data.getValue("exp.vs")
		);
		taulaSimbols.obtenirBloc(blocActual).inserirConstant(constant);
	}

	public void checkVariable(Data data) {
		//TODO: Comprovació de la informació de variable
		//TODO: Modificar el desplaçament
		ITipus type = (ITipus) data.getValue("var.type");
		if (type.getNom().equals("indef")) {
			Variable variable = new Variable(
					(String) data.getValue("var.name"),
					new TipusIndefinit("indef", 0),
					0);
			taulaSimbols.obtenirBloc(blocActual).inserirVariable(variable);
			return;
		}
		String var_name = (String) data.getValue("var.name");
		//Comprovació de si ha estat declarat com a variable prèviament
		if (taulaSimbols.obtenirBloc(blocActual).existeixVariable(var_name)) {
			error.insertError(TypeError.ERR_SEM_2, var_name);
			return;
		}
		//Comprovació de si ha estat declarat com a constant o funció prèviament
		if (taulaSimbols.obtenirBloc(blocActual).existeixID(var_name)) {
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

	public void checkID(Data data) {
		String id = (String) data.getValue("id.name");
		data.removeAttribute("id.name");
		if (taulaSimbols.obtenirBloc(blocActual).existeixConstant(id)) {
			Constant constant = taulaSimbols.obtenirBloc(blocActual).obtenirConstant(id);
			System.out.println("CONST: " + constant.toXml());
			data.setValue("terme.vs", constant);
			data.setValue("terme.ts", constant.getTipus());
			data.setValue("terme.es", true);
		} else if (taulaSimbols.obtenirBloc(blocActual).existeixVariable(id)) {
			Variable variable = taulaSimbols.obtenirBloc(blocActual).obtenirVariable(id);
			System.out.println("VAR: " + variable.toXml());
			data.setValue("terme.vs", variable);
			data.setValue("terme.ts", variable.getTipus());
			data.setValue("terme.es", false);
		} else if (taulaSimbols.obtenirBloc(0).existeixConstant(id)) {
			Constant constant = taulaSimbols.obtenirBloc(0).obtenirConstant(id);
			System.out.println("CONST: " + constant.toXml());
			data.setValue("terme.vs", constant);
			data.setValue("terme.ts", constant.getTipus());
			data.setValue("terme.es", true);
		}  else if (taulaSimbols.obtenirBloc(0).existeixVariable(id)) {
			Variable variable = taulaSimbols.obtenirBloc(0).obtenirVariable(id);
			System.out.println("VAR: " + variable.toXml());
			data.setValue("terme.vs", variable);
			if (variable.getTipus() instanceof TipusArray){
				data.setValue("terme.ts", ((TipusArray) variable.getTipus()).getTipusElements());
			} else data.setValue("terme.ts", variable.getTipus());
			data.setValue("terme.es", false);
		} else if (taulaSimbols.obtenirBloc(blocActual).existeixProcediment(id)) {
			Procediment funcio = taulaSimbols.obtenirBloc(blocActual).obtenirProcediment(id);
			data.setValue("terme.vs", funcio);
			data.setValue("terme.ts", false);
			data.setValue("terme.es", false);
		} else {
			error.insertError(TypeError.ERR_SEM_9, id);
			System.out.println("ERR_SEM_9");
			data.setValue("terme.vs", id);
			data.setValue("terme.ts", new TipusIndefinit("indef", 0));
			data.setValue("terme.es", false);
		}
	}

	public void checkFuncio(Data data) {
		//Aquí també seria millor utilitzar blocActual
		Funcio funcio = new Funcio();
		funcio.setNom((String) data.getValue("name"));
		taulaSimbols.obtenirBloc(0).inserirProcediment(funcio);
	}

	public void addParameter(Data data) {

		Parametre parametre = new Parametre();
		parametre.setNom((String) data.getValue("name"));
		parametre.setTipus((ITipus) data.getValue("type"));
		parametre.setTipusPasParametre(new TipusPasParametre((String) data.getValue("typeParam")));
		if (taulaSimbols.obtenirBloc(1).obtenirVariable(parametre.getNom()) == null) {
			taulaSimbols.obtenirBloc(0)
					.obtenirProcediment((String) data.getValue("idFunction"))
					.inserirParametre(parametre);
			taulaSimbols.obtenirBloc(1).inserirVariable(parametre);
		} else {
			error.insertError(TypeError.ERR_SEM_4, parametre.getNom());
		}

	}

	public void checkOp_binari(Data data) {
		if (data.getValue("MUL") != null) {
			if (!(data.getValue("terme.th") instanceof TipusIndefinit)
					&& ((ITipus) data.getValue("terme.th")).getNom().equals("SENCER") &&
					((ITipus) data.getValue("terme.ts")).getNom().equals("SENCER")) {
				if (((ITipus) data.getValue("terme.th")).getTamany() != INDEF &&
						((ITipus) data.getValue("terme.th")).getTamany() != INDEF) {

					if ((boolean) data.getValue("terme.eh") && (boolean) data.getValue("terme.es")) {
						int op1 = (int) data.getValue("terme.vh");
						int op2 = (int) data.getValue("terme.vs");
						int resultat = op1 * op2;
						data.setValue("terme.vs", resultat);
						data.setValue("terme.es", true);
					} else {
						data.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
						data.setValue("terme.vs", 0);
						data.setValue("op", true);

					}
				} else {
					data.setValue("terme.es", false);
					data.setValue("terme.vs", 0);
				}

			} else {
				error.insertError(TypeError.ERR_SEM_6);
				data.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
				data.setValue("terme.vs", 0);
			}
			data.removeAttribute("MUL");
		} else if (data.getValue("DIV") != null) {
			if (!(data.getValue("terme.th") instanceof TipusIndefinit)
					&& ((ITipus) data.getValue("terme.th")).getNom().equals("SENCER") &&
					((ITipus) data.getValue("terme.ts")).getNom().equals("SENCER")) {
				if (((ITipus) data.getValue("terme.th")).getTamany() != INDEF &&
						((ITipus) data.getValue("terme.th")).getTamany() != INDEF) {

					if ((boolean) data.getValue("terme.eh") && (boolean) data.getValue("terme.es")) {
						int op1 = (int) data.getValue("terme.vh");
						int op2 = (int) data.getValue("terme.vs");
						if (op2 != 0) {
							int resultat = op1 / op2;
							data.setValue("terme.vs", resultat);

							data.setValue("terme.es", true);
						} else {
							error.insertError(TypeError.ERR_SEM_21);
							data.setValue("terme.vs", 0);
							data.setValue("terme.es", true);
						}
					} else {
						data.setValue("terme.es", false);
						data.setValue("terme.vs", 0);
						data.setValue("op", true);

					}


				} else {
					data.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
					data.setValue("terme.vs", 0);
				}
			} else {
				error.insertError(TypeError.ERR_SEM_6);
				data.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
				data.setValue("terme.vs", 0);
			}

			data.removeAttribute("DIV");
		} else if (data.getValue("AND") != null) {
			if (!(data.getValue("terme.th") instanceof TipusIndefinit)
					&& ((ITipus) data.getValue("terme.th")).getNom().equals("LOGIC") &&
					((ITipus) data.getValue("terme.ts")).getNom().equals("LOGIC")) {
				if (((ITipus) data.getValue("terme.th")).getTamany() != INDEF &&
						((ITipus) data.getValue("terme.th")).getTamany() != INDEF) {
					if ((boolean) data.getValue("terme.eh") && (boolean) data.getValue("terme.es")) {
						boolean op1 = (boolean) data.getValue("terme.vh");
						boolean op2 = (boolean) data.getValue("terme.vs");
						boolean resultat = op1 && op2;
						data.setValue("terme.vs", resultat);
						data.setValue("terme.es", true);
					} else {
						data.setValue("terme.es", false);
						data.setValue("terme.vs", 0);
						data.setValue("op", true);
					}

				} else {
					data.setValue("terme.ts", new TipusSimple("LOGIC", INDEF));
					data.setValue("terme.vs", 0);
				}
			} else {
				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_7);
				data.setValue("terme.ts", new TipusSimple("LOGIC", INDEF));
				data.setValue("terme.vs", 0);
			}
			System.out.println("DATA:" + data);
			data.removeAttribute("AND");
		}
	}

	public void checkOp_aux(Data data, Data info) {
		//TODO: Control d'errors

		if (data.getValue("op_aux.vs") == TypeVar.SUMA) {
			System.out.println("DATA:" + data);
			System.out.println(data.getValue("terme_simple.th"));
			System.out.println(data);
			if (!(data.getValue("terme_simple.th") instanceof TipusIndefinit)
					&& ((ITipus) data.getValue("terme_simple.th")).getNom().equals("SENCER") &&
					((ITipus) info.getValue("terme.ts")).getNom().equals("SENCER")) {
				if (((ITipus) data.getValue("terme_simple.th")).getTamany() != INDEF &&
						((ITipus) info.getValue("terme.ts")).getTamany() != INDEF) {

					if (!((boolean) data.getValue("terme_simple.eh") && (boolean) info.getValue("terme.es"))) {
						info.setValue("terme.es", false);
						info.setValue("terme.vs", 0);
						info.setValue("terme.ts", new TipusSimple("SENCER", 0));
						data.setValue("op", true);
					} else {
						int op1 = (int) data.getValue("terme_simple.vh");
						int op2 = (int) info.getValue("terme.vs");
						int res = op1 + op2;
						info.setValue("terme.vs", res);
						info.setValue("terme.es", true);
					}
				} else {
					info.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
					info.setValue("terme.vs", 0);
				}
			} else {

				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_6);
				//Si el tamany és negatiu, vol dir que hi ha hagut un error al tipus
				info.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
				info.setValue("terme.vs", 0);
			}
			data.removeAttribute("op_aux.vs");
		} else if (data.getValue("op_aux.vs") == TypeVar.RESTA) {
			if (((ITipus) data.getValue("terme_simple.th")).getNom().equals("SENCER") &&
					((ITipus) info.getValue("terme.ts")).getNom().equals("SENCER")) {
				if (((ITipus) data.getValue("terme_simple.th")).getTamany() != INDEF &&
						((ITipus) info.getValue("terme.ts")).getTamany() != INDEF) {

					if (!((boolean) data.getValue("terme_simple.eh") && (boolean) info.getValue("terme.es"))) {
						info.setValue("terme.es", false);
						info.setValue("terme.vs", 0);
						info.setValue("terme.ts", new TipusSimple("SENCER", 0));
						data.setValue("op", true);
					} else {
						int op1 = (int) data.getValue("terme_simple.vh");
						int op2 = (int) info.getValue("terme.vs");
						int res = op1 - op2;
						info.setValue("terme.vs", res);
						info.setValue("terme.es", true);
					}
				} else {
					info.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
					info.setValue("terme.vs", 0);
				}

			} else {
				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_6);
				info.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
				info.setValue("terme.vs", 0);
			}
			data.removeAttribute("op_aux.vs");
		} else if (data.getValue("op_aux.vs") == TypeVar.OR) {
			if (((ITipus) data.getValue("terme_simple.th")).getNom().equals("LOGIC") &&
					((ITipus) info.getValue("terme.ts")).getNom().equals("LOGIC")) {
				if (((ITipus) data.getValue("terme_simple.th")).getTamany() != INDEF &&
						((ITipus) info.getValue("terme.ts")).getTamany() != INDEF) {

					if (!((boolean) data.getValue("terme_simple.eh") && (boolean) info.getValue("terme.es"))) {
						info.setValue("terme.es", false);
						info.setValue("terme.vs", 0);
						info.setValue("terme.ts", new TipusSimple("LOGIC", 0));
						data.setValue("op", true);
					} else {
						boolean op1 = (boolean) data.getValue("terme_simple.vh");
						boolean op2 = (boolean) info.getValue("terme.vs");
						boolean res = op1 || op2;
						info.setValue("terme.vs", res);
						info.setValue("terme.es", true);
					}
				} else {
					info.setValue("terme.ts", new TipusSimple("LOGIC", INDEF));
					info.setValue("terme.vs", 0);
				}
			} else {
				//TODO: recuperació errors
				error.insertError(TypeError.ERR_SEM_7);
				info.setValue("terme.ts", new TipusSimple("LOGIC", INDEF));
				info.setValue("terme.vs", 0);
			}
			data.removeAttribute("op_aux.vs");
		}
	}

	public void checkOp_unari(Data data) {

		//TODO: tamanys

		if (data.getValue("op_unari.vs") == TypeVar.RESTA) {

			if (((ITipus) data.getValue("terme.ts")).getNom().equals("SENCER")) {
				data.setValue("terme.vs", -(int) data.getValue("terme.vs"));

			} else {
				error.insertError(TypeError.ERR_SEM_6);
				data.setValue("terme.vs", 0);
				data.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
			}

		} else if (data.getValue("op_unari.vs") == TypeVar.SUMA) {
			if (!((ITipus) data.getValue("terme.ts")).getNom().equals("SENCER")) {
				error.insertError(TypeError.ERR_SEM_6);
				data.setValue("terme.vs", 0);
				data.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
			}

		} else if (data.getValue("op_unari.vs") == TypeVar.NOT) {
			if (((ITipus) data.getValue("terme.ts")).getNom().equals("LOGIC")) {
				data.setValue("terme.vs", !(boolean) data.getValue("terme.vs"));

			} else {
				error.insertError(TypeError.ERR_SEM_7);
				data.setValue("terme.vs", 0);
				data.setValue("terme.ts", new TipusSimple("LOGIC", INDEF));
			}
		}
		data.removeAttribute("op_unari.vs");
	}

	public void checkOp_relacional(Data data, Data info) {

		//Comprovem que els tipus a evaluar son tots dos sencer, en cas que no sigui aixi, error
		if (!((ITipus) data.getValue("exp_aux.th")).getNom().equals("SENCER") ||
				!((ITipus) info.getValue("exp_simple.ts")).getNom().equals("SENCER")) {
			error.insertError(TypeError.ERR_SEM_6);

			data.setValue("exp_aux.vs", 0);
			data.setValue("exp_aux.ts", new TipusIndefinit("indef", 0));
			data.setValue("exp_aux.es", true);
		} else if (((ITipus) data.getValue("exp_aux.th")).getTamany() == INDEF ||
				((ITipus) info.getValue("exp_simple.ts")).getTamany() == INDEF) {
			data.setValue("exp_aux.vs", 0);
			data.setValue("exp_aux.ts", new TipusIndefinit("indef", 0));
			data.setValue("exp_aux.es", true);

		} else if (!(boolean) data.getValue("exp_aux.es") || !(boolean) data.getValue("exp_simple.es")) {
			data.setValue("exp_aux.ts", new TipusSimple("LOGIC", 0));
			data.setValue("exp_aux.es", false);
			data.setValue("exp_aux.vs", false);
		} else {
			//TODO tamany?
			//TODO estatic?
			if (data.getValue("op_relacional.vs").equals(">")) {
				data.setValue("exp_aux.vs", (int) data.getValue("exp_aux.vh") > (int) info.getValue("exp_simple.vs"));
				data.setValue("exp_aux.ts", new TipusSimple("LOGIC", 0));
				data.setValue("exp_aux.es", true);

			} else if (data.getValue("op_relacional.vs").equals("<")) {
				data.setValue("exp_aux.vs", (int) data.getValue("exp_aux.vh") < (int) info.getValue("exp_simple.vs"));
				data.setValue("exp_aux.ts", new TipusSimple("LOGIC", 0));
				data.setValue("exp_aux.es", true);

			} else if (data.getValue("op_relacional.vs").equals("<=")) {
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

	public void initFuncio(Data data) {
		Funcio funcio;
		if (data.getValue("factor_aux.vh") instanceof Funcio) {
			funcio = (Funcio) data.getValue("factor_aux.vh");
			data.move("llista_exp.vh", "factor_aux.vh");
		} else {
			funcio = new Funcio();
			data.setValue("llista_exp.vh", funcio);
		}
		data.setValue("param.index", 0);
		data.setValue("param.num", funcio.getNumeroParametres());
	}

	public void checkParam(Data data, Data info) {

		ITipus exp_ts = (ITipus) info.getValue("exp.ts");
		Funcio funcio = (Funcio) data.getValue("llista_exp.vh");
		int param_index = (int) data.getValue("param.index") + 1;
		data.setValue("param.index", param_index);
		if (param_index <= (int) data.getValue("param.num")) {
			Parametre parametre = funcio.obtenirParametre(param_index - 1);

			System.out.println("CHECK PARAM: " + data);
			System.out.println("CHECK PARAM INFO: " + info);

			if (parametre.getTipusPasParametre().toString().equals("PERREF") &&
					(boolean) info.getValue("exp.es") || info.getValue("op") != null) {
				error.insertError(TypeError.ERR_SEM_17, param_index);
			}

			if (!(info.getValue("exp.ts") instanceof TipusIndefinit)
					&& !exp_ts.getNom().equals(parametre.getTipus().getNom())) {
				error.insertError(TypeError.ERR_SEM_16, param_index, parametre.getTipus().getNom());
			}
		}
	}

	public void checkParamNext(Data data) {
		if ((int) data.getValue("param.index") != (int) data.getValue("param.num")) {
			error.insertError(TypeError.ERR_SEM_15, (int) data.getValue("param.index"), (int) data.getValue("param.num"));
		}
	}

	public void checkErrSem22(Data data) {
		if (data.getValue("variable_aux.vh") instanceof Funcio) {
			error.insertError(TypeError.ERR_SEM_22, ((Funcio) data.getValue("variable_aux.vh")).getNom());
		}
	}


	public void checkVector(Data exp1, Data exp2) {
		//TODO: Afegir valors a retornar per a la declaració del vector
		if (((ITipus) exp1.getValue("exp.ts")).getNom().equals("SENCER") &&
				((ITipus) exp2.getValue("exp.ts")).getNom().equals("SENCER")) {
			if ((boolean) exp1.getValue("exp.es") && (boolean) exp2.getValue("exp.es")) {
				if ((int) exp1.getValue("exp.vs") > (int) exp2.getValue("exp.vs")) {
					error.insertError(TypeError.ERR_SEM_5);
				} else return;
			} else {
				error.insertError(TypeError.ERR_SEM_20);
			}

		} else {
			error.insertError(TypeError.ERR_SEM_6);
		}
		exp1.setValue("exp.vs", 0);
		exp2.setValue("exp.vs", 0);
	}

	public void checkVectorAccess(Data data, Data info) {
		Object id = data.getValue("variable_aux.vh");
		Object type = data.getValue("variable_aux.th");
		System.out.println("ID: " + data.getValue("variable_aux.th"));
		if (!(id instanceof Variable && type instanceof TipusArray)) {
			error.insertError(TypeError.ERR_SEM_23, getNomId(id));
		} else {
			data.setValue("variable_aux.th", ((TipusArray) type).getTipusElements());
			if (!((ITipus) info.getValue("exp.ts")).getNom().equals("SENCER")) {
				error.insertError(TypeError.ERR_SEM_13, getNomId(id));
			} else {
				int index = (int) info.getValue("exp.vs");
				if ((boolean)info.getValue("exp.es") && (
						(int)((TipusArray) type).obtenirDimensio(0).getLimitInferior() > index ||
						(int)((TipusArray) type).obtenirDimensio(0).getLimitSuperior() < index)){
					error.insertError(TypeError.ERR_SEM_24, getNomId(id));
				}
			}
		}
	}

	private String getNomId(Object id) {
		if (id instanceof Variable) return ((Variable) id).getNom();
		if (id instanceof Constant) return ((Constant) id).getNom();
		if (id instanceof Funcio) return ((Funcio) id).getNom();
		return (String) id;
	}

	public Data initAssignation(String lexema) {
		Data data = new Data();

		if (taulaSimbols.obtenirBloc(blocActual).existeixVariable(lexema)){
			Variable variable = taulaSimbols.obtenirBloc(blocActual).obtenirVariable(lexema);
			System.out.println("Variable: "+variable);
			data.setValue("variable_aux.vh", variable);
			data.setValue("variable_aux.th", variable.getTipus());
			data.setValue("variable_aux.eh", false);
		} else if (taulaSimbols.obtenirBloc(0).existeixVariable(lexema)){
			Variable variable = taulaSimbols.obtenirBloc(0).obtenirVariable(lexema);
			data.setValue("variable_aux.vh", variable);
			data.setValue("variable_aux.th", variable.getTipus());
			data.setValue("variable_aux.eh", false);
		} else {
			error.insertError(TypeError.ERR_SEM_11, lexema);
			data.setValue("variable_aux.vh", new Variable(lexema, new TipusIndefinit("indef", 0), 0));
			data.setValue("variable_aux.th", new TipusIndefinit("indef", 0));
			data.setValue("variable_aux.eh", false);
		}
		return data;
	}

	public void setTipusFuncio(String id, String tipus) {
		((Funcio)taulaSimbols.obtenirBloc(0).obtenirProcediment(id)).setTipus(new TipusSimple(tipus));
	}

	public void checkAssignation(Data data, Data info) {
		if (data.getValue("igual_aux.th") instanceof TipusSimple) {
			if (!((TipusSimple) data.getValue("igual_aux.th")).getNom().equals(
					((ITipus) info.getValue("exp.ts")).getNom())) {
				error.insertError(TypeError.ERR_SEM_12, ((Variable) data.getValue("igual_aux.vh")).getNom(),
						((Variable) data.getValue("igual_aux.vh")).getTipus().getNom(),
						((ITipus) info.getValue("exp.ts")).getNom());
			}
		} else if (data.getValue("igual_aux.th") instanceof TipusArray)
			error.insertError(TypeError.ERR_SEM_12, ((Variable) data.getValue("igual_aux.vh")).getNom(),
					"VECTOR DE " + ((TipusArray) data.getValue("igual_aux.th")).getTipusElements().getNom(),
					((ITipus) info.getValue("exp.ts")).getNom());

	}

	public void checkEscriure(Data info) {
		ITipus tipus = (ITipus) info.getValue("exp.ts");
		if (!(tipus instanceof TipusSimple || tipus instanceof TipusCadena)){
			error.insertError(TypeError.ERR_SEM_14);
		}
	}

	public Data initLlegir(String lexema) {
		Data data = new Data();

		if (taulaSimbols.obtenirBloc(blocActual).existeixVariable(lexema)){
			Variable variable = taulaSimbols.obtenirBloc(blocActual).obtenirVariable(lexema);
			System.out.println("Variable: "+variable);
			data.setValue("variable_aux.vh", variable);
			data.setValue("variable_aux.th", variable.getTipus());
			data.setValue("variable_aux.eh", false);
		} else if (taulaSimbols.obtenirBloc(0).existeixVariable(lexema)){
			Variable variable = taulaSimbols.obtenirBloc(0).obtenirVariable(lexema);
			data.setValue("variable_aux.vh", variable);
			data.setValue("variable_aux.th", variable.getTipus());
			data.setValue("variable_aux.eh", false);
		} else if (taulaSimbols.obtenirBloc(blocActual).existeixID(lexema) ||
				taulaSimbols.obtenirBloc(0).existeixID(lexema)){
			data.setValue("llegir.id", lexema);
		} else {
			error.insertError(TypeError.ERR_SEM_9, lexema);
			data.setValue("llegir.id", lexema);
			data.setValue("variable_aux.vh", new Variable(lexema, new TipusIndefinit("indef", 0), 0));
			data.setValue("variable_aux.th", new TipusIndefinit("indef", 0));
			data.setValue("variable_aux.eh", false);
		}
		return data;
	}

	public void checkLlegir(Data data) {
		ITipus tipus = (ITipus) data.getValue("variable_aux.ts");
		if (!(tipus instanceof TipusIndefinit) && !(tipus instanceof TipusSimple)){
			if (tipus instanceof TipusArray)
				error.insertError(TypeError.ERR_SEM_10, ((Variable)data.getValue("variable_aux.vs")).getNom());
			else
				error.insertError(TypeError.ERR_SEM_10, (String)data.getValue("llegir.id"));
		}
	}

}
