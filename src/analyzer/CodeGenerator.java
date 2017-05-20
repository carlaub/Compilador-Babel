package analyzer;

import com.sun.media.sound.SimpleSoundbank;
import com.sun.org.apache.bcel.internal.classfile.Code;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;
import taulaDeSimbols.ITipus;
import taulaDeSimbols.Variable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedMap;
import java.util.regex.Pattern;

public class CodeGenerator {
	private Registers registers;
	private Labels labels;
	private BufferedWriter bwGC;
	private int des;

	public CodeGenerator(String filename) {
		registers = new Registers();
		labels = new Labels();

		des = 0;
		File err = new File(filename.split(Pattern.quote("."))[0] + ".s");

		try {
			bwGC = new BufferedWriter(new FileWriter(err));
			writeDefaultData();
			bwGC.write("\t.text\n");
			bwGC.write("main:\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Escriptura de les cadenes predefinides per defecte. Per exemple, els
	 * error en temps d'execucio
	 */
	public void writeDefaultData(){
		gc(".data");
		gc("ecert: .asciiz \"cert\"");
		gc("efals: .asciiz \"fals\"");
		//TODO: Segurament hi ha una forma millor de fer el salt de linia als "escriure"
		gc("ejump: .asciiz \"\\n\"");

		// error out of bounds
		gc("err_out_of_bounds: .asciiz \"Accés invàlid al vector\"");
	}

	@Override
	public String toString(){
		return registers.toString();
	}
	public String getReg() {
		return registers.getRegister();
	}

	public void saveWord(Variable var) {

	}

	public int getDes(ITipus type) {
		int desp = des;
		des += type.getTamany();
		return desp;
	}

	private void gc(String code) {
		try {
			bwGC.write("\t"+code + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeBuffer() {
		// Escriure la finalitzacio del programa
		gc("jr $ra");
		System.out.println(registers);
		try {
			bwGC.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String loadWord(Variable variable) {
		String reg = registers.getRegister();
		gc("lw\t" + reg + ",\t-" + variable.getDesplacament() + "($gp)");

		return reg;
	}

	public void assignate(Data data, Data info) {
		String reg_data = (String) data.getValue("dirs");
		String reg_info = (String) info.getValue("regs");
		if (reg_info == null) reg_info = (String) info.getValue("regs1");
//		String reg_info = "$t5";

		if ((boolean) info.getValue("exp.es")) {
			//Demanem registre (la intrucció li es entre un li y un reg, no una @)
			String regValueEs = registers.getRegister();

			if (((ITipus) info.getValue("exp.ts")).getNom().equals("LOGIC")) {
				//Guardem el valor estic al registre auxiliar
				gc("li\t" + regValueEs+ ",\t" + (((boolean) info.getValue("exp.vs")) ? "0x1" : "0x0"));

			} else {
				//En hex
//			gc("sw 0x" + Integer.toHexString((int) info.getValue("exp.vs")) + ", " + reg_data);
				gc("li\t" + regValueEs + ",\t" + info.getValue("exp.vs"));
			}
			//Pasem el valor del registre auxiliar a l'adreça de la variable en questio
			gc("sw\t" + regValueEs + ",\t" + reg_data);
			//Alliberem el registre demanat
			registers.freeRegister(regValueEs);
		} else {
			gc("sw\t" + reg_info + ",\t" + reg_data);
			System.out.println("reg_info: " +reg_info);
			registers.freeRegister(reg_info);
		}

	}


	public String getDirs(Variable value) {
		return "-" + value.getDesplacament() + "($gp)";
	}

	public void suma(Data data, Data info) {
		System.out.println("DATA: " +data);
		System.out.println("INFO: "+info);
		if (!(boolean) data.getValue("terme_simple.eh")) {
			String reg1 = (String) data.getValue("regs");
			if ((boolean) info.getValue("terme.es")) {
				System.out.println(reg1);
				gc("add\t"+reg1+",\t"+reg1+",\t"+info.getValue("terme.vs"));
			} else {
				String reg2 = (String) info.getValue("regs");
				gc("add\t"+reg1+",\t"+reg1+",\t"+reg2);
				System.out.println("reg2: "+reg2);
				registers.freeRegister(reg2);
			}
			data.setValue("regs", reg1);

			//TODO: Hem de lliberar registres!! (Aqui no)
		} else {
			if (!(boolean) info.getValue("terme.es")) {
				String reg2 = (String) info.getValue("regs");
				System.out.println(reg2);
				gc("add\t"+reg2+",\t"+reg2+",\t"+data.getValue("terme_simple.vh"));
				data.setValue("regs", reg2);


			}
		}

	}

	public void resta(Data data, Data info) {
		System.out.println("DATA: " +data);
		System.out.println("INFO: "+info);
		if (!(boolean) data.getValue("terme_simple.eh")) {
			if ((boolean) info.getValue("terme.es")) {
				String reg1 = (String) data.getValue("regs");
				System.out.println(reg1);
				gc("sub\t"+reg1+",\t"+reg1+",\t"+info.getValue("terme.vs"));
			} else {
				String reg1 = (String) data.getValue("regs");
				String reg2 = (String) info.getValue("regs");
				gc("sub\t"+reg1+",\t"+reg1+",\t"+reg2);
				registers.freeRegister(reg2);
			}
		} else {
			if (!(boolean) info.getValue("terme.es")) {
				String reg2 = (String) info.getValue("regs");
				System.out.println(reg2);
				gc("sub\t"+reg2+",\t"+reg2+",\t"+data.getValue("terme_simple.vh"));
				data.setValue("regs", reg2);
			}
		}

	}

	public void mul(Data data) {

		System.out.println("DATA -------------------------: " +data);
		if (!(boolean) data.getValue("terme.eh")) {
			if ((boolean) data.getValue("terme.es")) {
				String reg1 = (String) data.getValue("regs");
				System.out.println(reg1);
				gc("mul\t"+reg1+",\t"+reg1+",\t"+data.getValue("terme.vs"));
			} else {
				String reg1 = (String) data.getValue("regs1");
				String reg2 = (String) data.getValue("regs2");
				gc("mul\t"+reg1+",\t"+reg1+",\t"+reg2);
				data.move("regs", "regs1");
				registers.freeRegister(reg2);
			}
		} else {
			if (!(boolean) data.getValue("terme.es")) {
				String reg2 = (String) data.getValue("regs");
				System.out.println(reg2);
				gc("mul\t"+reg2+",\t"+reg2+",\t"+data.getValue("terme.vh"));
				System.out.println("**************************DATA " + data);
				data.setValue("regs", reg2);
			}
		}
	}
	public void div(Data data) {

		System.out.println("DATA -------------------------: " +data);
		if (!(boolean) data.getValue("terme.eh")) {
			if ((boolean) data.getValue("terme.es")) {
				String reg1 = (String) data.getValue("regs");
				System.out.println("Reg 1: " + reg1);

				// Movem el valor a un registre
				String reg2 = registers.getRegister();
				gc("li\t"+reg2+",\t"+data.getValue("terme.vs"));
				//El resultat ho guardem a reg1
				gc("div\t"+reg1 +",\t"+reg1+",\t"+reg2);
				registers.freeRegister(reg2);

			} else {
				String reg1 = (String) data.getValue("regs1");
				String reg2 = (String) data.getValue("regs2");
				//El resultat ho guardem a reg1
				gc("div\t"+reg1+",\t"+reg1+",\t"+reg2);
				data.move("regs", "regs1");
				registers.freeRegister(reg2);
			}
		} else {
			if (!(boolean) data.getValue("terme.es")) {
				String reg2 = (String) data.getValue("regs");
				System.out.println(reg2);
				gc("div\t"+data.getValue("terme.vh")+",\t"+reg2);
				data.setValue("regs", reg2);
			}
		}
	}

	/**
	 * Funcio encarregada de mostrar per pantalla.
	 * @param data
	 */
	public void write(Data data) {
		ITipus tipus = (ITipus) data.getValue("exp.ts");
		//Comentari per aclarir el codi en assembler
		gc("#Escriure");

		//TODO: Cas escriure vector

		if (tipus.getNom().equals("SENCER")) {
			// Cas variable sencera

			// Configuracio print_int
			gc("li\t$v0,\t1");
			gc("move\t$a0,\t" + data.getValue("regs"));
			gc("syscall");
		} else {
			// Cas variable logica

			//Generam codi per escriure
			String eti1 = labels.getLabel();
			String eti2 = labels.getLabel();

			gc("beqz\t" + data.getValue("regs") + ",\t" + eti1);

			// Cert
			gc("li\t$v0,\t4");
			gc("la\t$a0,\tecert");
			gc("b\t" + eti2);

			//Fals
			gc(eti1 + ":");
			gc("li\t$v0,\t4");
			gc("la\t$a0,\tefals");

			gc(eti2 + ":");
			gc("syscall");
		}

		// Generem codi pel salt de linia
		gc("li\t$v0,\t11");
		gc("la\t$a0,\tejump");
		gc("syscall");
	}
}
