package analyzer;

import taulaDeSimbols.*;
import utils.Error;
import utils.TypeError;

/**
 * Analitzador semàntic.
 * S'encarrega de comprovar que la informació llegida del codi font és correcta pel que fa al significat (semàntica).
 * Aquesta classe és cridada des de l'analitzador sintàctic quan aquest té prou informació com per a poder realitzar
 * una comprovació semàntica. <br>
 * Per a facilitar la fase, suposem que no hi ha errors sintàctics, així que utilitzem
 * {@link SyntacticClean} enlloc de {@link SyntacticAnalyzer}.
 */
public class SemanticAnalyzer {
	private TaulaSimbols taulaSimbols;
	private int blocActual;
	private String idFuncio;
	private Error error;
	private static final int INDEF = -1;
	private CodeGenerator generator;

	/**
	 * Constructor de l'analitzador semàntic.
	 */
	public SemanticAnalyzer(String filename) {
		blocActual = 0;
		taulaSimbols = new TaulaSimbols();
		taulaSimbols.inserirBloc(new Bloc());
		taulaSimbols.setBlocActual(blocActual);
		idFuncio = "";
		generator = new CodeGenerator(filename);

		error = Error.getInstance();
	}

	@Override
	public String toString() {
		return taulaSimbols.toXml();
	}

	/**
	 * Mètode per a passar al següent bloc. S'utilitza quan es detecta la declaració d'una funció.
	 */
	public void nextBloc() {
		blocActual = 1;
		taulaSimbols.inserirBloc(new Bloc());
	}

	/**
	 * Mètode per a tornar al bloc anterior i esborrar l'actual.
	 * S'utilitza quan es detecta la fi de declaració d'una funció.
	 */
	public void previousBloc() {
		taulaSimbols.esborrarBloc(1);
		idFuncio = null;
		blocActual = 0;
	}

	/**
	 * Mètode per a comprovar la informació d'una constant i afegir-la al bloc actual de la taula de símbols.
	 *
	 * @param data Conté la informació de la constant
	 */
	public void checkConstant(Data data) {

		ITipus exp_ts = (ITipus) data.getValue("exp.ts");
		String const_name = (String) data.getValue("const.name");

		//Comprovació de si té tipus
		if (exp_ts.getNom().equals("indef") || exp_ts.getTamany() == INDEF) {
			//L'error ja l'haurem mostrat allà on hem detectat que els tipus no són coincidents
			Constant constant = new Constant(
					const_name,
					new TipusIndefinit("indef", 0),
					false);
			taulaSimbols.obtenirBloc(blocActual).inserirConstant(constant);
			return;
		}
		//Comprovació de si és estàtic
		if (!(boolean) data.getValue("exp.es")) {
			error.insertError(TypeError.ERR_SEM_20);
			Constant constant = new Constant(
					const_name,
					new TipusIndefinit("indef", 0),
					false);
			taulaSimbols.obtenirBloc(blocActual).inserirConstant(constant);
			return;
		}
		//Comprovació de si ha estat declarat prèviament com a variable
		if (taulaSimbols.obtenirBloc(blocActual).existeixConstant(const_name)) {
			error.insertError(TypeError.ERR_SEM_1, const_name);
			return;
		}
		//Comprovació de si ha estat declarat prèviament
		if (taulaSimbols.obtenirBloc(blocActual).existeixID(const_name)) {
			error.insertError(TypeError.ERR_SEM_21, const_name);
			return;
		}
		Constant constant = new Constant(
				const_name,
				exp_ts,
				data.getValue("exp.vs")
		);
		taulaSimbols.obtenirBloc(blocActual).inserirConstant(constant);
	}

	/**
	 * Mètode per a comprovar la informació d'una variable i afegir-la al bloc actual de la taula de símbols.
	 *
	 * @param data Conté la informació de la variable
	 */
	public void checkVariable(Data data) {

		ITipus type = (ITipus) data.getValue("var.type");
		String var_name = (String) data.getValue("var.name");

		if (type.getNom().equals("indef")) {
			Variable variable = new Variable(
					var_name,
					new TipusIndefinit("indef"),
					0);
			taulaSimbols.obtenirBloc(blocActual).inserirVariable(variable);
			return;
		}
		//Comprovació de si ha estat declarat prèviament
		if (taulaSimbols.obtenirBloc(blocActual).existeixVariable(var_name)) {
			error.insertError(TypeError.ERR_SEM_2, var_name);
			return;
		}
		//Comprovació de si ha estat declarat prèviament
		if (taulaSimbols.obtenirBloc(blocActual).existeixID(var_name)) {
			error.insertError(TypeError.ERR_SEM_21, var_name);
			return;
		}
		Variable variable = new Variable(
				var_name,
				type,
				generator.getDes(type)
		);


		taulaSimbols.obtenirBloc(blocActual).inserirVariable(variable);
	}

	/**
	 * Mètode per a comprovar un identificador quan apareix a una expressió.
	 *
	 * @param data Informació de l'identificador
	 */
	public void checkID(Data data) {
		String id = (String) data.getValue("id.name");
		System.out.println("ID: " + id);
		data.removeAttribute("id.name");

		if (taulaSimbols.obtenirBloc(blocActual).existeixConstant(id)) {

			Constant constant = taulaSimbols.obtenirBloc(blocActual).obtenirConstant(id);
			data.setBlock("terme.s", constant.getValor(), constant.getTipus(), true);

		} else if (taulaSimbols.obtenirBloc(blocActual).existeixVariable(id)) {

			Variable variable = taulaSimbols.obtenirBloc(blocActual).obtenirVariable(id);

			data.setBlock("terme.s", variable, variable.getTipus(), false);
			System.out.println("REGISTERS: " + generator);
			System.out.println(data);
			if (data.getValue("regs") != null) {
				data.move("regs1", "regs");
				data.setValue("regs2", generator.loadWord(variable, blocActual == 0));
			} else {
				LexicographicAnalyzer lexic = LexicographicAnalyzer.getInstance();
				System.out.println(lexic.getActualLine() + " - VAR: " + variable);
				data.setValue("regs", generator.loadWord(variable, blocActual == 0));
			}
			System.out.println("data -> " + data);
		} else if (taulaSimbols.obtenirBloc(0).existeixConstant(id)) {

			Constant constant = taulaSimbols.obtenirBloc(0).obtenirConstant(id);
			data.setBlock("terme.s", constant.getValor(), constant.getTipus(), true);

		} else if (taulaSimbols.obtenirBloc(0).existeixVariable(id)) {

			Variable variable = taulaSimbols.obtenirBloc(0).obtenirVariable(id);
			data.setBlock("terme.s",
					variable,
					variable.getTipus() instanceof TipusArray ?
							((TipusArray) variable.getTipus()).getTipusElements() :
							variable.getTipus(),
					false);
			if (data.getValue("regs") != null) {
				data.move("regs1", "regs");
				data.setValue("regs2", generator.loadWord(variable, false));
			} else {
				data.setValue("regs", generator.loadWord(variable, false));
			}

			System.out.println("data -> " + data);
		} else if (taulaSimbols.obtenirBloc(0).existeixProcediment(id)) {
			Funcio funcio = (Funcio) taulaSimbols.obtenirBloc(0).obtenirProcediment(id);
			data.setBlock("terme.s", funcio, funcio.getTipus(), false);
		} else {
			error.insertError(TypeError.ERR_SEM_9, id);
			taulaSimbols.obtenirBloc(blocActual).inserirVariable(
					new Variable(
							id,
							new TipusIndefinit("indef"),
							0));
			data.setBlock("terme.s", id, new TipusIndefinit("indef"), false);
		}
	}

	/**
	 * Mètode per a comprovar la informació d'una funció i afegir-la al bloc actual de la taula de símbols.
	 *
	 * @param data Conté la informació de la funció
	 */
	public String checkFuncio(Data data) {
		Funcio funcio = new Funcio();
		funcio.setNom((String) data.getValue("func.name"));
		funcio.setEtiqueta("_" + funcio.getNom() + "_");
		generator.declaracioFuncio(funcio);

		if (taulaSimbols.obtenirBloc(0).existeixID(funcio.getNom())) {

			if (taulaSimbols.obtenirBloc(0).existeixProcediment(funcio.getNom()))
				error.insertError(TypeError.ERR_SEM_3, funcio.getNom());
			else
				error.insertError(TypeError.ERR_SEM_21, funcio.getNom());

			funcio.setNom("!" + funcio.getNom());
			taulaSimbols.obtenirBloc(0).inserirProcediment(funcio);

		} else {
			taulaSimbols.obtenirBloc(0).inserirProcediment(funcio);
		}

		idFuncio = funcio.getNom();
		return idFuncio;
	}

	/**
	 * Mètode per a inserir un paràmetre a una funció que s'està declarant.
	 *
	 * @param data Informació del paràmetre i la funció
	 */
	public void addParameter(Data data) {
		ITipus type = (ITipus) data.getValue("param.type");
		Parametre parametre = new Parametre(
				(String) data.getValue("param.name"),
				type,
				(int) data.getValue("param.desp"),
				new TipusPasParametre((String) data.getValue("param.typeParam"))
		);

		data.setValue("param.desp", type.getTamany() + parametre.getDesplacament());

		if (taulaSimbols.obtenirBloc(1).obtenirVariable(parametre.getNom()) == null) {
			taulaSimbols.obtenirBloc(0)
					.obtenirProcediment((String) data.getValue("func.name"))
					.inserirParametre(parametre);
			taulaSimbols.obtenirBloc(1).inserirVariable(parametre);
		} else {
			error.insertError(TypeError.ERR_SEM_4, parametre.getNom());
		}

	}

	/**
	 * Mètode per a comprovar i realitzar una operació binaria d'alta prioritat.
	 *
	 * @param data Informació de l'expressió
	 */
	public void checkOp_binari(Data data) {

		ITipus terme_th = (ITipus) data.getValue("terme.th");

		System.out.println("MUL: " + data);
		if (data.getValue("MUL") != null) {

			if (!(terme_th instanceof TipusIndefinit) && terme_th.getNom().equals("SENCER") &&
					((ITipus) data.getValue("terme.ts")).getNom().equals("SENCER")) {

				if (terme_th.getTamany() != INDEF) {

					if ((boolean) data.getValue("terme.eh") && (boolean) data.getValue("terme.es")) {

						int op1 = (int) data.getValue("terme.vh");
						int op2 = (int) data.getValue("terme.vs");
						data.setValue("terme.vs", op1 * op2);
						data.setValue("terme.es", true);

					} else {
						generator.mul(data);
						//TODO: Tenir en compte si afecta en algo
						data.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
						data.setValue("terme.vs", 0);
						data.setValue("terme.es", false);
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

			if (!(terme_th instanceof TipusIndefinit)
					&& terme_th.getNom().equals("SENCER") &&
					((ITipus) data.getValue("terme.ts")).getNom().equals("SENCER")) {

				if (terme_th.getTamany() != INDEF) {

					if ((boolean) data.getValue("terme.eh") && (boolean) data.getValue("terme.es")) {

						int op1 = (int) data.getValue("terme.vh");
						int op2 = (int) data.getValue("terme.vs");
						if (op2 != 0) {
							data.setValue("terme.vs", op1 / op2);
							data.setValue("terme.es", true);
						} else {
							error.insertError(TypeError.WAR_OPC_1);
							data.setValue("terme.vs", 0);
							data.setValue("terme.es", true);
						}
					} else {
						generator.div(data);
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

			if (!(terme_th instanceof TipusIndefinit) && terme_th.getNom().equals("LOGIC") &&
					((ITipus) data.getValue("terme.ts")).getNom().equals("LOGIC")) {

				if (terme_th.getTamany() != INDEF) {

					if ((boolean) data.getValue("terme.eh") && (boolean) data.getValue("terme.es")) {

						boolean op1 = (boolean) data.getValue("terme.vh");
						boolean op2 = (boolean) data.getValue("terme.vs");
						data.setValue("terme.vs", op1 && op2);
						data.setValue("terme.es", true);
					} else {
						// Generació codi AND
						generator.and(data);
						data.setValue("terme.es", false);
						data.setValue("terme.vs", 0);
						data.setValue("op", true);
					}

				} else {
					data.setValue("terme.ts", new TipusSimple("LOGIC", INDEF));
					data.setValue("terme.vs", 0);
				}
			} else {
				error.insertError(TypeError.ERR_SEM_7);
				data.setValue("terme.ts", new TipusSimple("LOGIC", INDEF));
				data.setValue("terme.vs", 0);
			}
			data.removeAttribute("AND");
		}
	}

	/**
	 * Mètode per a comprovar i realitzar una operació binaria de baixa prioritat.
	 *
	 * @param data Informació de la primera expressió
	 * @param info Informació de la segona expressió
	 */
	public void checkOp_aux(Data data, Data info) {

		ITipus data_terme_simple_th = (ITipus) data.getValue("terme_simple.th");
		ITipus info_terme_ts = (ITipus) info.getValue("terme.ts");

		if (data.getValue("op_aux.vs") == TypeVar.SUMA) {

			if (!(data_terme_simple_th instanceof TipusIndefinit) && data_terme_simple_th.getNom().equals("SENCER") &&
					info_terme_ts.getNom().equals("SENCER")) {

				if (data_terme_simple_th.getTamany() != INDEF && info_terme_ts.getTamany() != INDEF) {

					if (!((boolean) data.getValue("terme_simple.eh") && (boolean) info.getValue("terme.es"))) {
						generator.suma(data, info);
						info.setBlock("terme.s", 0, new TipusSimple("SENCER"), false);
						data.setValue("op", true);
					} else {
						int op1 = (int) data.getValue("terme_simple.vh");
						int op2 = (int) info.getValue("terme.vs");
						info.setValue("terme.vs", op1 + op2);
						info.setValue("terme.es", true);
					}
				} else {
					info.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
					info.setValue("terme.vs", 0);
				}
			} else {

				error.insertError(TypeError.ERR_SEM_6);
				//Si el tamany és negatiu, vol dir que hi ha hagut un error al tipus
				info.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
				info.setValue("terme.vs", 0);
			}
			data.removeAttribute("op_aux.vs");

		} else if (data.getValue("op_aux.vs") == TypeVar.RESTA) {

			if (data_terme_simple_th.getNom().equals("SENCER") && info_terme_ts.getNom().equals("SENCER")) {

				if (data_terme_simple_th.getTamany() != INDEF && info_terme_ts.getTamany() != INDEF) {

					if (!((boolean) data.getValue("terme_simple.eh") && (boolean) info.getValue("terme.es"))) {
						generator.resta(data, info);
						info.setBlock("terme.s", 0, new TipusSimple("SENCER"), false);
						data.setValue("op", true);
					} else {
						int op1 = (int) data.getValue("terme_simple.vh");
						int op2 = (int) info.getValue("terme.vs");
						info.setValue("terme.vs", op1 - op2);
						info.setValue("terme.es", true);
					}
				} else {
					info.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
					info.setValue("terme.vs", 0);
				}

			} else {
				error.insertError(TypeError.ERR_SEM_6);
				info.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
				info.setValue("terme.vs", 0);
			}
			data.removeAttribute("op_aux.vs");

		} else if (data.getValue("op_aux.vs") == TypeVar.OR) {

			if (data_terme_simple_th.getNom().equals("LOGIC") && info_terme_ts.getNom().equals("LOGIC")) {
				if (data_terme_simple_th.getTamany() != INDEF && info_terme_ts.getTamany() != INDEF) {

					if (!((boolean) data.getValue("terme_simple.eh") && (boolean) info.getValue("terme.es"))) {
						// Generacio codi OR
						generator.or(data, info);
						info.setBlock("terme.s", 0, new TipusSimple("LOGIC"), false);
						data.setValue("op", true);
					} else {
						boolean op1 = (boolean) data.getValue("terme_simple.vh");
						boolean op2 = (boolean) info.getValue("terme.vs");
						info.setValue("terme.vs", op1 || op2);
						info.setValue("terme.es", true);
					}
				} else {
					info.setValue("terme.ts", new TipusSimple("LOGIC", INDEF));
					info.setValue("terme.vs", 0);
				}
			} else {
				error.insertError(TypeError.ERR_SEM_7);
				info.setValue("terme.ts", new TipusSimple("LOGIC", INDEF));
				info.setValue("terme.vs", 0);
			}
			data.removeAttribute("op_aux.vs");
		}
	}

	/**
	 * Mètode per a comprovar i realitzar una operació unària.
	 *
	 * @param data Informació del factor i de l'operador
	 */
	public void checkOp_unari(Data data) {

		Object op_unari = data.getValue("op_unari.vs");
		ITipus terme_ts = (ITipus) data.getValue("terme.ts");

		System.out.println("OPERADOR UNARI " + data);
		if (op_unari == TypeVar.RESTA) {

			if (terme_ts.getNom().equals("SENCER")) {
				if ((boolean) data.getValue("terme.es")) {
					System.out.println(data);
					data.setValue("terme.vs", -(int) data.getValue("terme.vs"));
					System.out.println(data);
				} else {
					generator.opUnariResta(data);
				}
			} else {
				error.insertError(TypeError.ERR_SEM_6);
				data.setValue("terme.vs", 0);
				data.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
			}

		} else if (op_unari == TypeVar.SUMA) {

			if (!terme_ts.getNom().equals("SENCER")) {

				error.insertError(TypeError.ERR_SEM_6);
				data.setValue("terme.vs", 0);
				data.setValue("terme.ts", new TipusSimple("SENCER", INDEF));
			}

		} else if (op_unari == TypeVar.NOT) {

			if (terme_ts.getNom().equals("LOGIC")) {
				if ((boolean) data.getValue("terme.es")) {
					data.setValue("terme.vs", !(boolean) data.getValue("terme.vs"));
				} else {
					generator.opUnariNot(data);
				}
			} else {
				error.insertError(TypeError.ERR_SEM_7);
				data.setValue("terme.vs", 0);
				data.setValue("terme.ts", new TipusSimple("LOGIC", INDEF));
			}
		}
		data.removeAttribute("op_unari.vs");
	}

	/**
	 * Mètode per a comprovar i realitzar una operació relacional.
	 *
	 * @param data Informació de la primera expressió
	 * @param info Informació de la segona expressió
	 */
	public void checkOp_relacional(Data data, Data info) {

		ITipus data_exp_aux_th = (ITipus) data.getValue("exp_aux.th");
		ITipus info_exp_simple_ts = (ITipus) info.getValue("exp_simple.ts");

		//Comprovem que els tipus a evaluar son tots dos sencer, en cas que no sigui aixi, error
		if (!data_exp_aux_th.getNom().equals("SENCER") || !info_exp_simple_ts.getNom().equals("SENCER")) {

			error.insertError(TypeError.ERR_SEM_6);
			data.setBlock("exp_aux.s", 0, new TipusIndefinit("indef"), true);

		} else if (data_exp_aux_th.getTamany() == INDEF || info_exp_simple_ts.getTamany() == INDEF) {

			data.setBlock("exp_aux.s", 0, new TipusIndefinit("indef"), true);

		} else if (!(boolean) data.getValue("exp_aux.eh")
				|| !(boolean) info.getValue("exp_simple.es")) {
			// Generem codi per la operacio amb operadors relacions. A dins de la funció es realitzara la
			// classificacions segons "op_relacional.vs"
			generator.opRelacionals(data, info);
			data.setBlock("exp_aux.s", false, new TipusSimple("LOGIC"), false);

		} else {
			int exp1 = (int) data.getValue("exp_aux.vh");
			int exp2 = (int) info.getValue("exp_simple.vs");

			switch ((String) data.getValue("op_relacional.vs")) {
				case ">":
					data.setBlock("exp_aux.s", exp1 > exp2, new TipusSimple("LOGIC"), true);
					break;
				case "<":
					data.setBlock("exp_aux.s", exp1 < exp2, new TipusSimple("LOGIC"), true);
					break;
				case "<=":
					data.setBlock("exp_aux.s", exp1 <= exp2, new TipusSimple("LOGIC"), true);
					break;
				case ">=":
					data.setBlock("exp_aux.s", exp1 >= exp2, new TipusSimple("LOGIC"), true);
					break;
				case "==":
					data.setBlock("exp_aux.s", exp1 == exp2, new TipusSimple("LOGIC"), true);
					break;
				case "<>":
					data.setBlock("exp_aux.s", exp1 != exp2, new TipusSimple("LOGIC"), true);
					break;
			}
		}
	}

	/**
	 * Mètode per a inicialitzar el tractament de la crida d'una funció.
	 *
	 * @param data Informació de la funció
	 */
	public void initFuncio(Data data) {

		Funcio funcio;

		if (data.getValue("factor_aux.vh") instanceof Funcio) {

			funcio = (Funcio) data.getValue("factor_aux.vh");
			data.move("llista_exp.vh", "factor_aux.vh");

		} else {
			funcio = new Funcio();
			funcio.setTipus(new TipusIndefinit());
			data.setValue("llista_exp.vh", funcio);
			data.setValue("llista_exp.th", funcio.getTipus());
		}
		data.setValue("param.index", 0);
		data.setValue("param.num", funcio.getNumeroParametres());
		System.out.println("FUNCIÓ --> " + funcio);
		generator.initFunction();
	}

	/**
	 * Mètode per a comprovar i inserir un paràmetre a la crida d'una funció
	 *
	 * @param data Informació de la funció
	 * @param info Informació de l'expressió
	 */
	public void checkParam(Data data, Data info) {

		ITipus exp_ts = (ITipus) info.getValue("exp.ts");
		Funcio funcio = (Funcio) data.getValue("llista_exp.vh");
		int param_index = (int) data.getValue("param.index") + 1;
		data.setValue("param.index", param_index);


		if (param_index <= (int) data.getValue("param.num")) {
			Parametre parametre = funcio.obtenirParametre(param_index - 1);

			if (parametre.getTipusPasParametre().toString().equals("PERREF") &&
					(boolean) info.getValue("exp.es") || info.getValue("op") != null) {
				error.insertError(TypeError.ERR_SEM_17, param_index);
			}

			if (!(info.getValue("exp.ts") instanceof TipusIndefinit)
					&& exp_ts.getNom().equals(parametre.getTipus().getNom())) {


				Object vs = info.getValue("exp.vs");

				if ((vs instanceof Variable && ((Variable) vs).getTipus() instanceof TipusArray &&
						((Variable) vs).getTipus().getNom().equals(parametre.getTipus().getNom()))) {
					if (parametre.getTipus() instanceof TipusArray) {

						DimensioArray dimensioArray = ((TipusArray) parametre.getTipus()).obtenirDimensio(0);
						error.insertError(TypeError.ERR_SEM_16, param_index,
								"VECTOR [" + dimensioArray.getLimitInferior() + ".." + dimensioArray.getLimitSuperior() +
										"] DE " + ((TipusArray) parametre.getTipus()).getTipusElements().getNom());

					} else {
						error.insertError(TypeError.ERR_SEM_16, param_index, parametre.getTipus().getNom());
					}
				}
			}
		}
		generator.addParamFunction(data, info, blocActual == 0);
	}

	/**
	 * Mètode per a comprovar que una funció ha estat cridat amb el nombre de paràmetres correcte.
	 *
	 * @param data Informació de la funció
	 */
	public void checkParamNext(Data data) {

		int param_index = (int) data.getValue("param.index");
		int param_num = (int) data.getValue("param.num");

		if (param_index != param_num) {
			error.insertError(TypeError.ERR_SEM_15, param_index, param_num);
		}
	}

	/**
	 * Mètode per a comprovar si s'està invocant una funció.
	 *
	 * @param data Informació de l'identificador
	 */
	public void checkVariableAux(Data data) {
		Object value = data.getValue("variable_aux.vh");
		if (value instanceof Funcio) {
			error.insertError(TypeError.ERR_SEM_22, ((Funcio) data.getValue("variable_aux.vh")).getNom());
		} else if (value instanceof Variable) {
			data.setValue("dirs", generator.getDirs((Variable) value, blocActual == 0));
		}
	}

	/**
	 * Mètode per a comprovar la declaració d'un vector tant com a variable com a paràmetre.
	 *
	 * @param tipus Tipus simple del vector
	 * @param exp1  Informació de l'expressió amb el límit inferior del vector
	 * @param exp2  Informació de l'expressió amb el límit superior del vector
	 * @return Tipus de vector instanciat
	 */
	public TipusArray checkVector(String tipus, Data exp1, Data exp2) {

		int lower_limit = (int) exp1.getValue("exp.vs");
		int upper_limit = (int) exp2.getValue("exp.vs");

		if (((ITipus) exp1.getValue("exp.ts")).getNom().equals("SENCER") &&
				((ITipus) exp2.getValue("exp.ts")).getNom().equals("SENCER")) {

			if ((boolean) exp1.getValue("exp.es") && (boolean) exp2.getValue("exp.es")) {

				if ((int) exp1.getValue("exp.vs") > (int) exp2.getValue("exp.vs")) {
					error.insertError(TypeError.ERR_SEM_5);
					lower_limit = upper_limit = 0;
				}

			} else {
				error.insertError(TypeError.ERR_SEM_20);
				lower_limit = upper_limit = 0;
			}

		} else {
			error.insertError(TypeError.ERR_SEM_6);
			lower_limit = upper_limit = 0;
		}

		TipusSimple tipusSimple = new TipusSimple(tipus);
		TipusArray tipusArray = new TipusArray("V_" + lower_limit + "_" + upper_limit + "_" + tipusSimple.getNom(),
				(upper_limit - lower_limit + 1) * tipusSimple.getTamany(), tipusSimple);
		DimensioArray dimensioArray = new DimensioArray(new TipusSimple("SENCER"), lower_limit, upper_limit);
		tipusArray.inserirDimensio(dimensioArray);

		return tipusArray;
	}

	/**
	 * Mètode per a comprovar que l'accés a un vector s'està realitzant correctament.
	 *
	 * @param data Informació del vector
	 * @param info Informació de l'expressió d'accés al vector
	 */
	public void checkVectorAccess(Data data, Data info) {

		Object id = data.getValue("variable_aux.vh");
		Object type = data.getValue("variable_aux.th");

		if (!(id instanceof Variable && type instanceof TipusArray)) {

			error.insertError(TypeError.ERR_SEM_23, getNomId(id));

		} else {

			data.setValue("variable_aux.th", ((TipusArray) type).getTipusElements());

			if (!((ITipus) info.getValue("exp.ts")).getNom().equals("SENCER")) {

				error.insertError(TypeError.ERR_SEM_13, getNomId(id));

			} else {
				int li = (int) ((TipusArray) type).obtenirDimensio(0).getLimitInferior();
				int ls = (int) ((TipusArray) type).obtenirDimensio(0).getLimitSuperior();

				if (info.getValue("exp.vs") instanceof Integer) {
					System.out.println("VECTOR ACCESS: " + info);
//					if (data.getValue("regs") != null) generator.free((String)data.getValue("regs"));

					String register = generator.initVector(((Variable) id).getDesplacament(), li, ls, (int) info.getValue("exp.vs"), blocActual == 0);
					data.setValue("dirs", "0(" + register + ")");
					int index = (int) info.getValue("exp.vs");
					if ((boolean) info.getValue("exp.es") && (
							(int) ((TipusArray) type).obtenirDimensio(0).getLimitInferior() > index ||
									(int) ((TipusArray) type).obtenirDimensio(0).getLimitSuperior() < index)) {
						error.insertError(TypeError.WAR_OPC_2, getNomId(id));
					}
				} else if (info.getValue("exp.vs") instanceof Variable) {
					generator.debug(info.toString());
					System.out.println("VECTOR ACCESS: " + info);

					String register = generator.initVectorVar(((Variable) id).getDesplacament(), li, ls, (String) info.getValue("regs"), blocActual == 0);
					data.setValue("dirs", "0(" + register + ")");

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

	/**
	 * Mètode per a inicialitzar una operació d'assignació.
	 *
	 * @param lexema Identificador de la variable
	 * @return Informació de l'identificador
	 */
	public Data initAssignation(String lexema) {

		Data data = new Data();

		if (taulaSimbols.obtenirBloc(blocActual).existeixVariable(lexema)) {

			Variable variable = taulaSimbols.obtenirBloc(blocActual).obtenirVariable(lexema);
			data.setBlock("variable_aux.h", variable, variable.getTipus(), false);

		} else if (taulaSimbols.obtenirBloc(0).existeixVariable(lexema)) {

			Variable variable = taulaSimbols.obtenirBloc(0).obtenirVariable(lexema);
			data.setBlock("variable_aux.h", variable, variable.getTipus(), false);

		} else {

			if (taulaSimbols.obtenirBloc(blocActual).existeixID(lexema) ||
					taulaSimbols.obtenirBloc(0).existeixID(lexema)) {
				error.insertError(TypeError.ERR_SEM_11, lexema);
			} else {
				error.insertError(TypeError.ERR_SEM_9, lexema);
				taulaSimbols.obtenirBloc(blocActual).inserirVariable(
						new Variable(
								lexema,
								new TipusIndefinit("indef"),
								0));
			}
			data.setBlock("variable_aux.h", new Variable(lexema, new TipusIndefinit("indef"), 0),
					new TipusIndefinit("indef"), false);
		}
		return data;
	}

	/**
	 * Mètode per a assignar el tipus de retorn a una funció.
	 *
	 * @param id    Identificador de la funció
	 * @param tipus Tipus de retorn de la funció
	 */
	public void setTipusFuncio(String id, String tipus) {
		((Funcio) taulaSimbols.obtenirBloc(0).obtenirProcediment(id)).setTipus(new TipusSimple(tipus));
	}

	/**
	 * Mètode per a comprovar l'assignació.
	 *
	 * @param data Informació del receptor de l'assignació
	 * @param info Informació de l'expressió a assignar
	 */
	public void checkAssignation(Data data, Data info) {
		ITipus igual_aux_th = (ITipus) data.getValue("igual_aux.th");
		ITipus exp_ts = (ITipus) info.getValue("exp.ts");

		if (igual_aux_th instanceof TipusSimple) {

			if (!igual_aux_th.getNom().equals(exp_ts.getNom())) {

				error.insertError(TypeError.ERR_SEM_12, ((Variable) data.getValue("igual_aux.vh")).getNom(),
						igual_aux_th.getNom(),
						exp_ts instanceof TipusArray ?
								"VECTOR DE " + ((TipusArray) exp_ts).getTipusElements().getNom()
								:
								exp_ts.getNom());
				return;
			}

		} else if (igual_aux_th instanceof TipusArray) {

			error.insertError(TypeError.ERR_SEM_12, ((Variable) data.getValue("igual_aux.vh")).getNom(),
					"VECTOR DE " + ((TipusArray) igual_aux_th).getTipusElements().getNom(),
					exp_ts.getNom());
			return;
		}
		generator.assignate(data, info);
	}

	/**
	 * Mètode per a comprovar l'expressió d'un paràmetre de l'instrucció escriure.
	 *
	 * @param data Informació de l'expressió
	 */
	public void checkEscriure(Data data) {

		ITipus tipus = (ITipus) data.getValue("exp.ts");

		if (!(tipus instanceof TipusSimple || tipus instanceof TipusCadena)) {
			error.insertError(TypeError.ERR_SEM_14);
		} else {
			// Generació del codi necessari per mostrar per pantalla
			generator.write(data);
		}
	}

	/**
	 * Mètode per a iniciar la comprovació d'un paràmentre de l'instrucció LLEGIR
	 *
	 * @param lexema Identificador que rep llegir com a paràmetre
	 * @return Informació del paràmetre
	 */
	public Data initLlegir(String lexema) {

		Data data = new Data();

		if (taulaSimbols.obtenirBloc(blocActual).existeixVariable(lexema)) {

			Variable variable = taulaSimbols.obtenirBloc(blocActual).obtenirVariable(lexema);
			data.setBlock("variable_aux.h", variable, variable.getTipus(), false);
			System.out.println("LLEGIR VARIABLE -> " + variable);
			if (variable.getTipus().getNom().equals("SENCER"))
				generator.read(variable.getDesplacament(), blocActual == 0);

		} else if (taulaSimbols.obtenirBloc(0).existeixVariable(lexema)) {

			Variable variable = taulaSimbols.obtenirBloc(0).obtenirVariable(lexema);
			data.setBlock("variable_aux.h", variable, variable.getTipus(), false);
			if (variable.getTipus().getNom().equals("SENCER")) generator.read(variable.getDesplacament(), true);


		} else if (taulaSimbols.obtenirBloc(blocActual).existeixID(lexema) ||
				taulaSimbols.obtenirBloc(0).existeixID(lexema)) {

			data.setValue("llegir.id", lexema);
			error.insertError(TypeError.ERR_SEM_10, lexema);
			data.setBlock("variable_aux.h", new Variable(lexema, new TipusIndefinit("indef"), 0),
					new TipusIndefinit("indef"), false);

		} else {
			error.insertError(TypeError.ERR_SEM_9, lexema);
			taulaSimbols.obtenirBloc(blocActual).inserirVariable(
					new Variable(
							lexema,
							new TipusIndefinit("indef"),
							0));
			data.setValue("llegir.id", lexema);
			data.setBlock("variable_aux.h", new Variable(lexema, new TipusIndefinit("indef"), 0),
					new TipusIndefinit("indef"), false);
		}
		return data;
	}

	/**
	 * Mètode per a comprovar un paràmetre de l'instrucció LLEGIR
	 *
	 * @param data Informació del paràmetre
	 */
	public void checkLlegir(Data data) {

		ITipus tipus = (ITipus) data.getValue("variable_aux.ts");

		if (!(tipus instanceof TipusIndefinit) && !(tipus instanceof TipusSimple)) {

			if (tipus instanceof TipusArray)

				error.insertError(TypeError.ERR_SEM_10, ((Variable) data.getValue("variable_aux.vs")).getNom());

			else
				error.insertError(TypeError.ERR_SEM_10, (String) data.getValue("llegir.id"));
		}
	}

	/**
	 * Mètode per comprovar si una expressió és lògica
	 *
	 * @param data Informació de l'expressió
	 */
	public void checkLogic(Data data, Type type) {

		if (!((ITipus) data.getValue("exp.ts")).getNom().equals("LOGIC")) {
			error.insertError(TypeError.ERR_SEM_7);
		} else {
			if ((boolean) data.getValue("exp.es")) {
				if ((boolean) data.getValue("exp.vs")) {
					if (type.equals(Type.CICLE)) error.insertError(TypeError.WAR_OPC_7);
					else error.insertError(TypeError.WAR_OPC_4);
				} else {
					if (type.equals(Type.CICLE)) error.insertError(TypeError.WAR_OPC_6);
					else error.insertError(TypeError.WAR_OPC_5);
				}
			}
		}
		//TODO: Revisar aquest super-parche de la parra
		if (data.getValue("regs") != null)
			generator.free((String) data.getValue("regs"));
		else System.out.println("NULL REGISTER AT " + type);
	}

	/**
	 * Mètode per a comprovar si hi ha algun camí sense RETORNAR a una funció
	 *
	 * @param ret Booleà indicador de si hi ha un RETORNAR
	 */
	public void checkCamiReturn(boolean ret) {
		if (!ret) error.insertError(TypeError.ERR_SEM_24);
		generator.endFunctionDeclaration();
	}

	/**
	 * Mètode per a comprovar la instrucció RETORNAR
	 *
	 * @param data Informació de l'expressió de retorn
	 */
	public void checkReturn(Data data) {
		if (blocActual == 0) {
			error.insertError(TypeError.ERR_SEM_19);

		} else {
			ITipus type = (ITipus) data.getValue("exp.ts");

			if (!type.getNom().equals(
					((Funcio) taulaSimbols.obtenirBloc(0).obtenirProcediment(idFuncio)).getTipus().getNom())) {

				if (taulaSimbols.obtenirBloc(0).existeixProcediment(idFuncio))

					error.insertError(TypeError.ERR_SEM_18, idFuncio,
							((Funcio) taulaSimbols.obtenirBloc(0).obtenirProcediment(idFuncio)).getTipus().getNom(),
							type.getNom());
			} else {
				generator.endFunction(data);
			}
		}
	}

	/**
	 * Condicional - inicialització
	 *
	 * @param exp_si
	 */

	public void initConditional(Data exp_si) {
		generator.initConditional(exp_si);
	}

	/**
	 * Condicional - else
	 * @param exp_si
	 */
	public void elseConditional(Data exp_si) {
		generator.elseConditional(exp_si);
	}

	/**
	 * Condicional - end
	 *
	 * @param exp_si
	 */
	public void endConditional(Data exp_si) {
		generator.endConditional(exp_si);
	}

	/**
	 * Mentre - inicialització
	 */
	public String initCicle() {
		return generator.initCicle();
	}

	public void endCicle(Data info_mentre, String label) {
		generator.endCicle(info_mentre, label);
	}

	public void checkCodiReturn(boolean ret) {
		if (ret)
			error.insertError(TypeError.WAR_OPC_3);
	}

	public void close() {
		generator.closeBuffer();
	}

	public void printRegs() {
		generator.printRegs();
	}

	public void moveToReg(Data data) {
		String reg = generator.moveToReg((String) data.getValue("dirs"));
		data.setValue("regs", reg);
	}

	public String cridaInvocador(Funcio funcio) {
		if(funcio.getNumeroParametres() == 0) {
			generator.saltInvocador(12, funcio.getEtiqueta());

		} else {
			Parametre parametre = funcio.obtenirParametre(funcio.getNumeroParametres() - 1);
			generator.saltInvocador(parametre.getDesplacament() + parametre.getTipus().getTamany(), funcio.getEtiqueta());
		}
		return generator.retornInvocador();
	}

	public void initProgram() {
		generator.initProgram();
	}

	public String showBloc() {
		return taulaSimbols.obtenirBloc(blocActual).toString();
	}

	public void initFuncio() {
		generator.initFunctionVars(taulaSimbols.obtenirBloc(blocActual));
	}

}
