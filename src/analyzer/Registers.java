package analyzer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by alexj on 11/5/2017.
 */
public class Registers {
	private HashMap<String, Boolean> regs;

	public Registers() {
		regs = new HashMap<>();
		regs.put("$t0", false);
		regs.put("$t1", false);
		regs.put("$t2", false);
		regs.put("$t3", false);
		regs.put("$t4", false);
		regs.put("$t5", false);
		regs.put("$t6", false);
		regs.put("$t7", false);
		regs.put("$t8", false);
		regs.put("$t9", false);
		regs.put("$s0", false);
		regs.put("$s1", false);
		regs.put("$s2", false);
		regs.put("$s3", false);
		regs.put("$s4", false);
		regs.put("$s5", false);
		regs.put("$s6", false);
		regs.put("$s7", false);
	}

	public String getRegister() {
		if (!regs.containsValue(false)) return null;
		Iterator it = regs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			if (!(boolean) pair.getValue()) {
				regs.put((String) pair.getKey(), true);
				return (String) pair.getKey();
			}
		}
		return null;
	}

	public void freeRegister(String key) {
		regs.put(key, false);
	}

	@Override
	public String toString() {
		return regs.toString();
	}
}
