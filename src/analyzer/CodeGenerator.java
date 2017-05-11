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
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			bwGC.write(code + "\n");
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

		gc("lw " + reg + ", -" + variable.getDesplacament() + "($gp)");

		return reg;
	}

	public void assignate(Data data, Data info) {
		String reg_data = (String) data.getValue("dirs");
		String reg_info = (String)info.getValue("regs");
//		String reg_info = "$t5";
		
		if ((boolean) info.getValue("exp.es")) {
			if (((ITipus) info.getValue("exp.ts")).getNom().equals("LOGIC")){
				//Ara ho he escrit així, però està obert a canvis, segurament al final sigui algo com "set"
				gc("sw " + (((boolean)info.getValue("exp.vs"))?"0xFFFFFFFF":"0x000000000") + ", " + reg_data);
			}
			else {
				//En hex
//			gc("sw 0x" + Integer.toHexString((int) info.getValue("exp.vs")) + ", " + reg_data);
				gc("sw " + info.getValue("exp.vs") + ", " + reg_data);
			}
		} else {
			gc("sw " + reg_info + ", " + reg_data);
			registers.freeRegister(reg_info);
		}

	}

	public String getDirs(Variable value) {
		return "-" + value.getDesplacament() + "($gp)";
	}
}
