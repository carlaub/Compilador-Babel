package analyzer;

import taulaDeSimbols.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Generador de codi.
 * S'encarrega de interpretar la informació llegida del codi font un cop aquesta és correcta i en genera el codi en
 * el llenguatge d'ensamblador MIPS per a una màquina RISC.<br>
 * Totes les crides a aquesta classe es fan des de l'analitzador semàntic ({@link SemanticAnalyzer}) perquè
 * conceptualment sempre es genera codi des d'un punt on s'ha fet una revisió semàntica. <br>
 * Per a facilitar aquesta quarta fase, suposem que no hi ha errors sintàctics ni semàntics.
 */
public class CodeGenerator {
	private static final int N_REGISTERS = 25 - 8 + 1;
	private static final int REGISTER_SIZE = 4; //Bytes
	private static final int REGISTERS_SIZE = N_REGISTERS * REGISTER_SIZE; //Bytes
	private Registers registers;
	private Labels labels;
	private BufferedWriter bwGC;
	private int des;
	private int des_func;
	private boolean isFunction;

	/**
	 * Constructor del generador de codi.
	 *
	 * @param filename Nom del fitxer que contindrà el codi generat
	 */
	public CodeGenerator(String filename) {
		registers = new Registers();
		labels = new Labels();

		des = 0;
		des_func = 12;
		isFunction = false;
		File err = new File(filename.split(Pattern.quote("."))[0] + ".s");

		try {
			bwGC = new BufferedWriter(new FileWriter(err));
			writeDefaultData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Escriptura de les cadenes predefinides per defecte. Per exemple, els missatges d'errors en temps d'execució
	 */
	private void writeDefaultData() {
		gc("\n.data");
		gc("_ecert: .asciiz \"cert\"");
		gc("_efals: .asciiz \"fals\"");
		gc("_ejump: .asciiz \"\\n\"");

		// error out of bounds
		gc("_err_out_of_bounds: .asciiz \"\n\nAccés invàlid al vector\n\n\"");

		gc("\n.text\n");
		gc_eti("_errorAccess:");
		gc("li\t$v0,\t4");
		gc("la\t$a0,\t_err_out_of_bounds");
		gc("syscall");
		gc("li\t$v0,\t4");
		gc("la\t$a0,\t_ejump");
		gc("syscall");
		gc("b\t_end");
	}

	/**
	 * Funció per a escriure un comentari al punt actual del codi que es genera.
	 *
	 * @param code Comentari a escriure
	 */
	public void debug(String code) {
		gc("#DEBUG -> " + code);
	}

	@Override
	public String toString() {
		return registers.toString();
	}

	/**
	 * Retorna el desplaçament al punt actual del codi i actualitza el desplaçament per la següent variable.
	 *
	 * @param type Tipus de variable per a la que es demana espai
	 * @return El desplaçament que ha de tenir la variable respecte $gp o $fp
	 */
	public int getDes(ITipus type) {
		int desp = des;
		if (isFunction) {
			desp = des_func;
			des_func += type.getTamany();
		} else
			des += type.getTamany();
		return desp;
	}

	/**
	 * Escriu el fragment de codi que rep com a paràmetre al fitxer generat amb tabulat d'instrucció.
	 *
	 * @param code Fragment de codi a escriure
	 */
	private void gc(String code) {
		try {
			bwGC.write("\t" + code + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Escriu una etiqueta amb el tabulat d'etiqueta.
	 *
	 * @param code Etiqueta a escriure
	 */
	private void gc_eti(String code) {
		try {
			bwGC.write(code + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Finalitza la generació de codi.
	 */
	public void closeBuffer() {
		// Escriure la finalitzacio del programa
		System.out.println(registers);
		try {
			bwGC.write("\n_end:\n");
			bwGC.write("\tli\t$ra,\t0x00400018\n");
			bwGC.write("\tjr\t$ra");
			bwGC.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Carrega una variable a un registre i el retorna.
	 *
	 * @param variable Variable a moure a un registre
	 * @return Registre que conté el valor de la variable
	 */
	public String loadWord(Variable variable) {
		String reg = registers.getRegister();
		gc("lw\t" + reg + ",\t-" + variable.getDesplacament() + (variable.getIsGlobal() ? "($gp)" : "($fp)"));
		return reg;
	}

	/**
	 * Genera el codi d'assignació a una variable.
	 *
	 * @param data Conté la informació de la variable a assignar
	 * @param info Conté la informació de la expressió a assignar
	 */
	public void assignate(Data data, Data info) {
		String reg_data = (String) data.getValue("dirs");
		String reg_info = (String) info.getValue("regs");
		if (reg_info == null) reg_info = (String) info.getValue("regs1");

		if ((boolean) info.getValue("exp.es")) {
			//Demanem registre (la intrucció li es entre un li y un reg, no una @)
			String regValueEs = registers.getRegister();

			if (((ITipus) info.getValue("exp.ts")).getNom().equals("LOGIC")) {
				//Guardem el valor estic al registre auxiliar
				gc("li\t" + regValueEs + ",\t" + (((boolean) info.getValue("exp.vs")) ? "0x01" : "0x00"));

			} else {
				System.out.println("LLEGA -> " + data);
				System.out.println("LLEGA -> " + info);
				gc("li\t" + regValueEs + ",\t" + info.getValue("exp.vs"));
			}
			//Pasem el valor del registre auxiliar a l'adreça de la variable en questio
			gc("sw\t" + regValueEs + ",\t" + reg_data);
			//Alliberem el registre demanat
			registers.freeRegister(regValueEs);
		} else {
			gc("sw\t" + reg_info + ",\t" + reg_data);
			LexicographicAnalyzer lexic = LexicographicAnalyzer.getInstance();
			System.out.println(lexic.getActualLine() + " - reg_info: " + reg_info);
			if (reg_info != null)
				registers.freeRegister(reg_info);
			else
				System.out.println("NULL REGISTER AT " + lexic.getActualLine());
			System.out.println("LIBERAMEEE------------------");
			System.out.println("|" + reg_data + "|");
			System.out.println(registers);
		}
		if (reg_data.charAt(0) == '0') {
			System.out.println("LIBERAO------------------");
			registers.freeRegister(reg_data.substring(2, 5));
			System.out.println("|" + reg_data.substring(2, 5) + "|");
			System.out.println(registers);
		}

	}

	/**
	 * Rep una variable i en retorna el seu accés en memòria.
	 *
	 * @param variable Variable de la que es vol obtenir l'adreça a memòria
	 * @return Direcció de memòria
	 */
	public String getDirs(Variable variable) {
		System.out.println("VARIABLE GLOBAL -> " + variable);
		return "-" + variable.getDesplacament() + (variable.getIsGlobal() ? "($gp)" : "($fp)");
	}

	/**
	 * Genera el codi per a sumar dos termes.
	 *
	 * @param data Informació del primer terme
	 * @param info Informació del segon terme
	 */
	public void suma(Data data, Data info) {
		System.out.println("DATA: " + data);
		System.out.println("INFO: " + info);
		if (!(boolean) data.getValue("terme_simple.eh")) {
			String reg1 = (String) data.getValue("regs");
			if ((boolean) info.getValue("terme.es")) {
				System.out.println(reg1);
				gc("add\t" + reg1 + ",\t" + reg1 + ",\t" + info.getValue("terme.vs"));
			} else {
				String reg2 = (String) info.getValue("regs");
				gc("add\t" + reg1 + ",\t" + reg1 + ",\t" + reg2);
				registers.freeRegister(reg2);
			}
			data.setValue("regs", reg1);

		} else {
			if (!(boolean) info.getValue("terme.es")) {
				String reg2 = (String) info.getValue("regs");
				gc("add\t" + reg2 + ",\t" + reg2 + ",\t" + data.getValue("terme_simple.vh"));
				data.setValue("regs", reg2);
			}
		}
	}

	/**
	 * Genera el codi per a restar dos termes.
	 *
	 * @param data Informació del primer terme
	 * @param info Informació del segon terme
	 */
	public void resta(Data data, Data info) {
		System.out.println("DATA: " + data);
		System.out.println("INFO: " + info);
		if (!(boolean) data.getValue("terme_simple.eh")) {
			if ((boolean) info.getValue("terme.es")) {
				String reg1 = (String) data.getValue("regs");
				System.out.println(reg1);
				gc("sub\t" + reg1 + ",\t" + reg1 + ",\t" + info.getValue("terme.vs"));
			} else {
				String reg1 = (String) data.getValue("regs");
				String reg2 = (String) info.getValue("regs");
				gc("sub\t" + reg1 + ",\t" + reg1 + ",\t" + reg2);
				registers.freeRegister(reg2);
			}
		} else {
			if (!(boolean) info.getValue("terme.es")) {
				String reg2 = (String) info.getValue("regs");
				System.out.println(reg2);
				gc("sub\t" + reg2 + ",\t" + reg2 + ",\t" + data.getValue("terme_simple.vh"));
				data.setValue("regs", reg2);
			}
		}

	}

	/**
	 * Genera el codi per a multiplicar dos termes.
	 *
	 * @param data Informació dels termes a multiplicar
	 */
	public void mul(Data data) {

		System.out.println("DATA -------------------------: " + data);
		if (!(boolean) data.getValue("terme.eh")) {
			if ((boolean) data.getValue("terme.es")) {
				String reg1 = (String) data.getValue("regs");
				System.out.println(reg1);
				gc("mul\t" + reg1 + ",\t" + reg1 + ",\t" + data.getValue("terme.vs"));
			} else {
				String reg1 = (String) data.getValue("regs1");
				String reg2 = (String) data.getValue("regs2");
				gc("mul\t" + reg1 + ",\t" + reg1 + ",\t" + reg2);
				data.move("regs", "regs1");
				registers.freeRegister(reg2);
			}
		} else {
			if (!(boolean) data.getValue("terme.es")) {
				String reg2 = (String) data.getValue("regs");
				System.out.println(reg2);
				gc("mul\t" + reg2 + ",\t" + reg2 + ",\t" + data.getValue("terme.vh"));
				System.out.println("**************************DATA " + data);
				data.setValue("regs", reg2);
			}
		}
	}

	/**
	 * Genera el codi per a dividir dos termes.
	 *
	 * @param data Informació dels termes a multiplicar
	 */
	public void div(Data data) {

		if (!(boolean) data.getValue("terme.eh")) {
			if ((boolean) data.getValue("terme.es")) {
				String reg1 = (String) data.getValue("regs");
				System.out.println("Reg 1: " + reg1);

				// Movem el valor a un registre
				String reg2 = registers.getRegister();
				gc("li\t" + reg2 + ",\t" + data.getValue("terme.vs"));
				//El resultat ho guardem a reg1
				gc("div\t" + reg1 + ",\t" + reg1 + ",\t" + reg2);
				registers.freeRegister(reg2);

			} else {
				String reg1 = (String) data.getValue("regs1");
				String reg2 = (String) data.getValue("regs2");
				//El resultat ho guardem a reg1
				gc("div\t" + reg1 + ",\t" + reg1 + ",\t" + reg2);
				data.move("regs", "regs1");
				registers.freeRegister(reg2);
			}
		} else {
			if (!(boolean) data.getValue("terme.es")) {
				String reg2 = (String) data.getValue("regs");
				System.out.println(reg2);

				String reg = registers.getRegister();
				gc("li\t" + reg + ",\t" + data.getValue("terme.vh"));
				gc("div\t" + reg2 + ",\t" + reg + ",\t" + reg2);

				registers.freeRegister(reg);
			}
		}
	}

	/**
	 * Genera el codi per a multiplicar lògicament dos termes.
	 *
	 * @param data Informació dels termes a multiplicar
	 */
	public void and(Data data) {

		if (!(boolean) data.getValue("terme.eh")) {
			if ((boolean) data.getValue("terme.es")) {
				String reg1 = (String) data.getValue("regs");
				System.out.println("Reg 1: " + reg1);

				if ((boolean) data.getValue("terme.vs")) {
					// Cas valor estatic CERT 0x1
					gc("andi\t" + reg1 + ",\t" + reg1 + ",\t0x1");
				} else {
					// Cas valor estatic FALS 0x0
					gc("andi\t" + reg1 + ",\t" + reg1 + ",\t0x0");
				}
			} else {
				// Tots dos termes no son estatics
				String reg1 = (String) data.getValue("regs1");
				String reg2 = (String) data.getValue("regs2");
				//El resultat ho guardem a reg1
				System.out.println("ASDF -> " + data);
				gc("and\t" + reg1 + ",\t" + reg1 + ",\t" + reg2);
				data.move("regs", "regs1");
				registers.freeRegister(reg2);
			}

		} else {
			if (!(boolean) data.getValue("terme.es")) {
				String reg2 = (String) data.getValue("regs");

				if ((boolean) data.getValue("terme.vh")) {
					gc("andi\t" + reg2 + ",\t" + reg2 + ",\t0x1");
				} else {
					gc("andi\t" + reg2 + ",\t" + reg2 + ",\t0x0");
				}
				data.setValue("regs", reg2);
			}
		}
	}

	/**
	 * Genera el codi per a sumar lògicament dos termes.
	 *
	 * @param data Informació dels termes a sumar
	 */
	public void or(Data data, Data info) {
		if (!(boolean) data.getValue("terme_simple.eh")) {
			String reg1 = (String) data.getValue("regs");
			if ((boolean) info.getValue("terme.es")) {

				if ((boolean) info.getValue("terme.vs")) {
					// Terme estatic cert
					gc("ori\t" + reg1 + ",\t" + reg1 + ",\t0x1");
				} else {
					// Terme estatic fals
					gc("ori\t" + reg1 + ",\t" + reg1 + ",\t0x0");
				}
			} else {
				String reg2 = (String) info.getValue("regs");
				gc("or\t" + reg1 + ",\t" + reg1 + ",\t" + reg2);
				registers.freeRegister(reg2);
			}
			data.setValue("regs", reg1);

		} else {
			if (!(boolean) info.getValue("terme.es")) {
				String reg2 = (String) info.getValue("regs");
				if ((boolean) data.getValue("terme_simple.vh")) {
					// Terme estatic cert
					gc("ori\t" + reg2 + ",\t" + reg2 + ",\t0x1");
				} else {
					// Terme estatic fdals
					gc("ori\t" + reg2 + ",\t" + reg2 + ",\t0x0");
				}
				data.setValue("regs", reg2);
			}
		}
	}

	/**
	 * Genera el codi per a negar un terme.
	 *
	 * @param data Informació del terme a negar
	 */
	public void opUnariResta(Data data) {
		gc("#Negacio");
		gc("neg\t" + data.getValue("regs") + ",\t" + data.getValue("regs"));
	}

	/**
	 * Genera el codi per a negar logicament un terme.<br>
	 * Fa una negació lògica i hi aplica una màscara per a mantenir tots els bits sense significat a 0
	 * (només utilitzem l'últim bit per a representar els dos possibles valors).<br>
	 * 0x00000001 -> cert<br>
	 * 0x00000000 -> fals
	 *
	 * @param data Informació del terme a negar
	 */
	public void opUnariNot(Data data) {
		gc("#NOT");
		gc("not\t" + data.getValue("regs") + ",\t" + data.getValue("regs"));
		gc("andi\t" + data.getValue("regs") + ",\t" + data.getValue("regs") + ",\t 0x00000001");
	}

	/**
	 * Gestiona la generació de codi dels operadors relacionals (==, <, <=, >, >=, <>).
	 *
	 * @param data Informació del terme de l'esquerra
	 * @param info Informació del terme de la dreta
	 */
	public void opRelacionals(Data data, Data info) {
		System.out.println("CI DATA " + data);
		System.out.println("CI INFO " + info);

		gc("#OP_REL " + data.getValue("op_relacional.vs"));
		switch ((String) data.getValue("op_relacional.vs")) {
			case "==":
				opRelacional(data, info, "seq");
				break;
			case ">=":
				opRelacional(data, info, "sge");
				break;
			case ">":
				opRelacional(data, info, "sgt");
				break;
			case "<=":
				opRelacional(data, info, "sle");
				break;
			case "<":
				opRelacional(data, info, "slt");
				break;
			case "<>":
				opRelacional(data, info, "sne");
				break;
		}
	}

	/**
	 * Genera el codi per a comparar dos valors sencers segons l'operació rebuda.
	 *
	 * @param data Informació del terme de l'esquerra
	 * @param info Informació del terme de la dreta
	 * @param op   Operació a realitzar
	 */
	private void opRelacional(Data data, Data info, String op) {
		if (!(boolean) data.getValue("exp_aux.eh")) {
			String reg1 = (String) data.getValue("regs");
			if ((boolean) info.getValue("exp_simple.es")) {

				gc(op + "\t" + reg1 + ",\t" + reg1 + ",\t" + info.getValue("exp_simple.vs"));

			} else {
				String reg2 = (String) info.getValue("regs");
				gc(op + "\t" + reg1 + ",\t" + reg1 + ",\t" + reg2);
				registers.freeRegister(reg2);
			}
			data.setValue("regs", reg1);

		} else {
			if (!(boolean) info.getValue("exp_simple.es")) {
				String reg2 = (String) info.getValue("regs");
				gc(op + "\t" + reg2 + ",\t" + reg2 + ",\t" + data.getValue("exp_aux.vh"));
				data.setValue("regs", reg2);
			}
		}
	}

	/**
	 * Genera el codi per a escriure al terminal.
	 *
	 * @param data Informació de l'expressió a mostrar
	 */
	public void write(Data data) {
		ITipus tipus = (ITipus) data.getValue("exp.ts");

		if (tipus instanceof TipusSimple) {
			if (tipus.getNom().equals("SENCER")) {
				// Cas variable sencera

				// Configuracio print_int
				gc("li\t$v0,\t1");
				if ((boolean) data.getValue("exp.es"))
					gc("li\t$a0,\t" + data.getValue("exp.vs"));
				else {
					gc("move\t$a0,\t" + data.getValue("regs"));
					registers.freeRegister((String) data.getValue("regs"));
				}
				gc("syscall");
			} else {
				// Cas variable logica

				//Generam codi per escriure
				String eti1 = labels.getLabel();
				String eti2 = labels.getLabel();

				System.out.println("PRINT BOOLEAN: " + data);
				if ((boolean) data.getValue("exp.es")) {
					if (!(boolean) data.getValue("exp.vs"))
						gc("b\t" + eti1);
				} else {
					gc("beqz\t" + data.getValue("regs") + ",\t" + eti1);
					registers.freeRegister((String) data.getValue("regs"));
				}

				// Cert
				gc("li\t$v0,\t4");
				gc("la\t$a0,\t_ecert");
				gc("b\t" + eti2);

				//Fals
				gc("\n" + eti1 + ":");
				gc("li\t$v0,\t4");
				gc("la\t$a0,\t_efals");

				gc("\n" + eti2 + ":");
				gc("syscall");
			}
			System.out.println("WRITE ---------------");
			System.out.println(registers);
		} else if (tipus instanceof TipusCadena) {
			//Introduim la cadena a l'apartat de .data
			// Cal una nova etiqueda per la cadena
			String eti = labels.getLabel();
			gc("\n" + ".data");
			gc(eti + ": .asciiz \"" + data.getValue("exp.vs") + "\"");
			gc("\n" + ".text");
			//Generem el codi per mostrar la cadena
			gc("li\t$v0,\t4");
			gc("la\t$a0,\t" + eti);
			gc("syscall");

		}

	}

	/**
	 * Genera el codi per a escriure a una posició de memòria.
	 *
	 * @param dir Posició de memòria a escriure
	 */
	public void read(String dir) {
		gc("#read");
		gc("li\t$v0,\t5");
		gc("syscall");
		String reg = registers.getRegister();
		gc("move\t" + reg + ",\t$v0");
		gc("sw\t" + reg + ",\t" + dir);
		registers.freeRegister(reg);
		registers.freeRegister(dir.substring(2, 5));
	}

	/**
	 * Genera el codi per a escriure a una posició de memòria.
	 *
	 * @param variable Variable sobre la que s'escriu
	 */
	public void read(Variable variable) {
		gc("#read");
		gc("li\t$v0,\t5");
		gc("syscall");
		String reg = registers.getRegister();
		gc("move\t" + reg + ",\t$v0");
		if (variable.getIsGlobal()) {
			gc("sw\t" + reg + ",\t-" + variable.getDesplacament() + "($gp)");
		} else {
			gc("sw\t" + reg + ",\t-" + variable.getDesplacament() + "($sp)");
		}

		registers.freeRegister(reg);
	}

	/**
	 * Genera el codi prèvi a una crida a una funció. Equival al DORa.
	 */
	public void initFunction() {
		gc("#Init funció");
		gc("addi\t$sp,\t$sp,\t" + -REGISTERS_SIZE);
		gc("sw\t$fp,\t0($sp)");
		gc("addi\t$sp,\t$sp,\t-12");
	}

	/**
	 * Genera el codi per a passar les expressions de la crida a la pila de crides. Estem al final del DORa.
	 *
	 * @param info     Informació de l'expressió
	 * @param isGlobal Booleà indicant si és una variable global o no
	 */
	public void addParamFunction(Data info, boolean isGlobal) {

		// Mirem si és estàtic
		if ((boolean) info.getValue("exp.es")) {
			String reg = registers.getRegister();
			gc("#PARAM FUNC");
			Object value = info.getValue("exp.vs");
			if (value instanceof Boolean)
				gc("li\t" + reg + ",\t" + ((boolean) value ? 0x01 : 0x00));
			else
				gc("li\t" + reg + ",\t" + value);

			gc("sw\t" + reg + ",\t0($sp)");
			gc("addi\t$sp,\t$sp,\t-4");
			registers.freeRegister(reg);
		} else {
			System.out.println("INFOOO PARAM" + info);
			gc("#PARAM FUNC");
			ITipus type = (ITipus) info.getValue("exp.ts");
			if (type instanceof TipusArray) {
				int li = (int) ((TipusArray) type).obtenirDimensio(0).getLimitInferior();
				int ls = (int) ((TipusArray) type).obtenirDimensio(0).getLimitSuperior();
				String reg = (String) info.getValue("regs");
				int desp = ((Variable) info.getValue("exp.vs")).getDesplacament();
				for (int i = 1; i <= ls - li + 1; i++) {
					gc("lw\t" + reg + ",\t-" + desp + (isGlobal ? "($gp)" : "($fp)"));
					gc("sw\t" + reg + ",\t0($sp)");
					desp += ((TipusArray) type).getTipusElements().getTamany();
					gc("addi\t$sp,\t$sp,\t-4");
				}
				registers.freeRegister(reg);

			} else {
				String reg = (String) info.getValue("regs");
				gc("sw\t" + reg + ",\t0($sp)");
				gc("addi\t$sp,\t$sp,\t-4");
				registers.freeRegister(reg);
			}
		}
	}

	/**
	 * Genera el codi de salt a una funció. Equival al DORs.
	 *
	 * @param desp     Desplaçament a causa dels paràmetres
	 * @param etiqueta Etiqueta de la funció a la que saltem
	 */
	public void saltInvocador(int desp, String etiqueta) {
		gc("move\t$fp,\t$sp");
		gc("addi\t$fp,\t$fp,\t" + desp);
		gc("jal\t" + etiqueta);
	}

	/**
	 * Genera el codi per a actualitzar el valor d'aquelles variables passades per referència. Equival al DORd.
	 *
	 * @param funcio Funció cridada
	 * @param exps   Llista d'expressions usada per a cridar la funció
	 * @return Registre amb el valor de retorn de la funció
	 */
	public String retornInvocador(Funcio funcio, ArrayList<Data> exps) {

		String reg = registers.getRegister();

		for (int i = 0; i < funcio.getNumeroParametres(); i++) {
			Parametre parametre = funcio.obtenirParametre(i);
			if (parametre.getTipusPasParametre().toString().equals("PERREF")) {
				Data data = exps.get(i);
				Object object = data.getValue("exp.vs");

				if (object instanceof Variable) {
					Variable variable = (Variable) object;
					if (variable.getTipus() instanceof TipusArray) {
						int des = variable.getDesplacament();
						int des_param = parametre.getDesplacament();
						int li = (int) ((TipusArray) variable.getTipus()).obtenirDimensio(0).getLimitInferior();
						int ls = (int) ((TipusArray) variable.getTipus()).obtenirDimensio(0).getLimitSuperior();
						for (int j = 0; j < ls - li + 1; j++) {
							gc("lw\t" + reg + ",\t-" + (des_param + j * 4) + "($sp)");
							gc("sw\t" + reg + ",\t-" + (des + j * 4) + (variable.getIsGlobal() ? "($gp)" : "($fp)"));

						}
					} else {
						gc("lw\t" + reg + ",\t-" + parametre.getDesplacament() + "($sp)");
						gc("sw\t" + reg + ",\t-" + variable.getDesplacament() + (variable.getIsGlobal() ? "($gp)" : "($fp)"));
					}
				}
			}
		}

		gc("lw\t" + reg + ",\t-8($sp)");
		gc("addi\t$sp,\t$sp,\t72");
		return reg;
	}

	/**
	 * Genera el codi per a obtenir un registre amb el desplaçament d'una posició d'un vector.
	 *
	 * @param desp          Desplaçament de la primera posició del vector
	 * @param limitInferior Límit inferior del vector
	 * @param limitSuperior Límit superior del vector
	 * @param value         Informació del valor d'accés
	 * @param isGlobal      Booleà indicant si és una variable global o no
	 * @return Retorna un registre amb el desplaçament d'una posició del vector
	 */
	public String initVector(int desp, int limitInferior, int limitSuperior, Object value, boolean isGlobal) {
		String reg = registers.getRegister();
		String r1 = registers.getRegister();
		String r2 = registers.getRegister();
		String r3 = registers.getRegister();
		gc("#Init vector");
		debug("INIT VECTOR REGS: " + registers.toString());
		gc("la\t" + reg + ",\t-" + desp + (isGlobal ? "($gp)" : "($fp)"));
		gc("li\t" + r1 + ",\t" + limitInferior);
		gc("li\t" + r3 + ",\t" + limitSuperior);
		if (value instanceof Integer)
			gc("li\t" + r2 + ",\t" + value);
		else {
			gc("move\t" + r2 + ",\t" + value.toString());
			registers.freeRegister(value.toString());
		}

		gc("bgt\t" + r2 + ",\t" + r3 + ",\t_errorAccess");
		gc("blt\t" + r2 + ",\t" + r1 + ",\t_errorAccess");

		gc("sub\t" + r2 + ",\t" + r1 + ",\t" + r2);
		gc("li\t" + r1 + ",\t4");
		gc("mul\t" + r2 + ",\t" + r2 + ",\t" + r1);
		gc("add\t" + reg + ",\t" + reg + ",\t" + r2);
		registers.freeRegister(r1);
		registers.freeRegister(r2);
		registers.freeRegister(r3);
		return reg;
	}

	/**
	 * Genera el codi per a la part inicial d'un condicional.
	 *
	 * @param data Informació sobre l'expressió
	 */
	public void initConditional(Data data) {
		String label_1 = labels.getLabel();
		String label_2 = labels.getLabel();

		gc("#Condicional");
		if ((boolean) data.getValue("exp.es")) {
			if (!(boolean) data.getValue("exp.vs"))
				gc("b\t" + label_1);
		} else
			gc("beqz\t" + data.getValue("regs") + ",\t" + label_1);

		data.setValue("label_1", label_1);
		data.setValue("label_2", label_2);
	}

	/**
	 * Genera el codi de l'else d'un condicional.
	 *
	 * @param data Informació sobre l'expressió
	 */
	public void elseConditional(Data data) {
		gc("b\t" + data.getValue("label_2"));
		gc_eti(data.getValue("label_1") + ":");
	}

	/**
	 * Genera el codi final d'un condicional.
	 *
	 * @param data Informació sobre l'expressió
	 */
	public void endConditional(Data data) {
		gc_eti(data.getValue("label_2") + ":");
	}

	/**
	 * Genera el codi per a la part inicial d'un cicle.
	 *
	 * @return Etiqueta on inicia el cicle
	 */
	public String initCicle() {
		String label = labels.getLabel();
		gc("#Cicle");
		gc_eti(label + ":");
		return label;
	}

	/**
	 * Genera el codi per a la part final d'un cicle.
	 *
	 * @param data  Informació de l'expressió
	 * @param label Etiqueta on s'inicia el cicle
	 */
	public void endCicle(Data data, String label) {
		if ((boolean) data.getValue("exp.es")) {
			if (!(boolean) data.getValue("exp.vs"))
				gc("b\t" + label);
		} else
			gc("beqz\t" + data.getValue("regs") + ",\t" + label);
	}

	/**
	 * Genera el codi de la part inicial del mentre.
	 *
	 * @param data Informació de l'expressió
	 */
	public void initWhile(Data data) {
		String eti1 = labels.getLabel();
		String eti2 = labels.getLabel();

		gc("#While");
		gc_eti(eti2 + ":");

		data.setValue("eti1", eti1);
		data.setValue("eti2", eti2);
	}

	/**
	 * Genera el codi de l'avaluació de l'expressió del mentre.
	 *
	 * @param data Informació de l'etiqueta
	 * @param info Informació de l'expressió
	 */
	public void iterationConditionWhile(Data data, Data info) {
		if ((boolean) info.getValue("exp.es")) {
			if (!(boolean) info.getValue("exp.vs"))
				gc("b\t" + data.getValue("eti1"));
		} else
			gc("beqz\t" + info.getValue("regs") + ",\t" + data.getValue("eti1"));
	}

	/**
	 * Genera el codi de la part final del mentre.
	 *
	 * @param data Informació de l'etiqueta
	 */
	public void endWhile(Data data) {
		gc("b\t" + data.getValue("eti2"));
		gc_eti(data.getValue("eti1") + ":");
	}

	/**
	 * Mostra l'estat actual dels registres per consola.
	 */
	public void printRegs() {
		System.out.println(registers);
	}

	/**
	 * Genera el codi per moure el valor d'una posició de memòria a un registre.
	 * Si es tracta d'una posició a memòria d'un vector, també allibera el registre que emmagatzema el seu desplaçament.
	 *
	 * @param dirs Direcció de memòria
	 * @return Registre amb el valor de la direcció de memòria
	 */
	public String moveToReg(String dirs) {
		String reg = registers.getRegister();
		System.out.println("MOVE TO REG: " + registers);
		gc("#ACCÉS VECTOR");
		gc("lw\t" + reg + ",\t" + dirs);
		//Guarradilla
		if (dirs.charAt(0) == '0') {
			registers.freeRegister(dirs.substring(2, 5));
		}
		return reg;
	}

	/**
	 * Allibera un registre.
	 *
	 * @param regs Registre a alliberar
	 */
	public void free(String regs) {
		registers.freeRegister(regs);
	}

	/**
	 * Genera el codi de l'inici de la declaració d'una funció. Equival al DOe.
	 *
	 * @param funcio Funció a iniciar
	 */
	public void declaracioFuncio(Funcio funcio) {
		gc_eti(funcio.getEtiqueta() + ":");
		isFunction = true;

		// Guardem $ra a la pila d'execució
		gc("sw\t$ra,\t-4($fp)");
		// Guardem els registres
		for (int i = 0; i < 18; i++) {
			gc("sw\t$" + (i + 8) + ",\t" + (4 + 4 * i) + "($fp)");
		}
		// Reserva espai variables locals
		gc("addi\t$sp,\t$sp,\t-" + des_func);
	}

	/**
	 * Genera el codi d'inici del programa.
	 */
	public void initProgram() {
		gc_eti("\nmain:");
		gc("move\t$fp,\t$sp\n");
	}

	/**
	 * Mètode per a indicar que ha finalitzat la declaració d'una funció.
	 */
	public void endFunctionDeclaration() {
		isFunction = false;
	}

	/**
	 * Mètode per a inicilitzar el desplaçament de les variables d'una funció.
	 *
	 * @param bloc Bloc actual de la funció
	 */
	public void initFunctionVars(Bloc bloc) {
		int n = bloc.getNumParams();
		if (n > 0) {
			Parametre parametre = bloc.getLastParametre();
			des_func = parametre.getDesplacament() + parametre.getTipus().getTamany();
		} else des_func = 12;
	}

	/**
	 * Genera el codi de final de la declaració d'una funció. Equival al DOs.
	 *
	 * @param data Informació de l'expressió de retorn
	 */
	public void endFunction(Data data) {
		System.out.println("END FUNCTION DATA: " + data);
		String reg;
		if ((boolean) data.getValue("exp.es")) {
			reg = registers.getRegister();
			if (((ITipus) data.getValue("exp.ts")).getNom().equals("LOGIC"))
				gc("li\t" + reg + ",\t" + (((boolean) data.getValue("exp.vs")) ? "0x01" : "0x00"));
			else
				gc("li\t" + reg + ",\t" + data.getValue("exp.vs"));
		} else {
			reg = (String) data.getValue("regs");
		}
		gc("sw\t" + reg + ",\t-8($fp)");
		registers.freeRegister(reg);


		// Lliberar el frame
		gc("move\t$sp,\t$fp");
		gc("lw\t$fp,\t0($fp)");

		// Restaurar registres
		for (int i = 0; i < 18; i++) {
			gc("lw\t$" + (i + 8) + ",\t" + (4 + 4 * i) + "($sp)");
		}

		// Saltar a l'invocador
		gc("lw\t$ra,\t-4($sp)");
		gc("jr\t$ra");
	}
}
