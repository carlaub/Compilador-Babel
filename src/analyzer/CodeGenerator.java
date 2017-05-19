package analyzer;

import com.sun.org.apache.bcel.internal.classfile.Code;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import taulaDeSimbols.ITipus;
import taulaDeSimbols.Variable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by alexj on 11/5/2017.
 */
public class CodeGenerator {
	private Registers registers;
	private BufferedWriter bwGC;
	private int des;

	public CodeGenerator(String filename) {
		registers = new Registers();
		des = 0;
		File err = new File(filename.split(Pattern.quote("."))[0] + ".s");

		try {
			bwGC = new BufferedWriter(new FileWriter(err));
			bwGC.write("\t.text\n");
			bwGC.write("main:\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			if (((ITipus) info.getValue("exp.ts")).getNom().equals("LOGIC")) {
				//Ara ho he escrit així, però està obert a canvis, segurament al final sigui algo com "set"
				gc("sw\t" + (((boolean) info.getValue("exp.vs")) ? "0xFFFFFFFF" : "0x000000000") + ",\t" + reg_data);
			} else {
				//En hex
//			gc("sw 0x" + Integer.toHexString((int) info.getValue("exp.vs")) + ", " + reg_data);
				gc("sw\t" + info.getValue("exp.vs") + ",\t" + reg_data);
			}
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
			if ((boolean) info.getValue("terme.es")) {
				String reg1 = (String) data.getValue("regs");
				System.out.println(reg1);
				gc("add\t"+reg1+",\t"+reg1+",\t"+info.getValue("terme.vs"));
			} else {
				String reg1 = (String) data.getValue("regs");
				String reg2 = (String) info.getValue("regs");
				gc("add\t"+reg1+",\t"+reg1+",\t"+reg2);
				System.out.println("reg2: "+reg2);
				registers.freeRegister(reg2);
			}
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
				gc("li\t"+reg2+"\t"+data.getValue("terme.vs"));
				//El resultat ho guardem a reg1
				gc("div\t"+reg1 +"\t"+reg1+",\t"+reg2);
				registers.freeRegister(reg2);
			} else {
				String reg1 = (String) data.getValue("regs1");
				String reg2 = (String) data.getValue("regs2");
				//El resultat ho guardem a reg1
				gc("div\t"+reg1+"\t"+reg1+",\t"+reg2);
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
}
