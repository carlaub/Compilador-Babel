package analyzer;

import com.sun.org.apache.bcel.internal.classfile.Code;
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

	public CodeGenerator(String filename){
		registers = new Registers();
		des = 0;
		File err = new File (filename.split(Pattern.quote("."))[0]+".s");

		try {
			bwGC = new BufferedWriter(new FileWriter(err));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void saveWord(Variable var){

	}

	public int getDes(ITipus type) {
		int desp = des;
		des += type.getTamany();
		gc(registers.getRegister());
		return desp;
	}

	private void gc(String code){
		try {
			bwGC.write(code+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeBuffer(){
		System.out.println(registers);
		try {
			bwGC.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
