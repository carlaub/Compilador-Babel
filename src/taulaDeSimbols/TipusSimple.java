package taulaDeSimbols;

/**
 * <p>Clase que representa un tipus simple del llenguatge Babel, com per exemple un
 * sencer, caracter, real, ...</p>
 */
public class TipusSimple extends ITipus {

	/**<p>Valor m�nim que pot assolir el tipus</p>*/
	private Object minim;
	
	/**<p>Valor m�xim que pot assolir el tipus</p>*/
	private Object maxim;

	/**<p>Constructor de TipusSimple</p>*/
	public TipusSimple() {
	}
	
	/**
	 * <p>Constructor de TipusSimple</p>
	 * @param nom del tipus
	 * @param tamany que ocupa el tipus
	 */
	public TipusSimple(String nom, int tamany) {
		this.nom = nom;
		this.tamany = tamany;
		this.setLimits(nom);
	}

	public TipusSimple(String nom) {
		this.nom = nom;
		this.setLimits(nom);
	}

	private void setLimits(String nom) {
		minim = nom.equals("SENCER")? Integer.MIN_VALUE:0;
		maxim = nom.equals("SENCER")? Integer.MAX_VALUE:1;
		tamany = nom.equals("SENCER")? 4:1;
	}

	/**
	 * <p>Constructor de TipusSimple</p>
	 * @param (Object) valor m�nim que pot assolir aquest tipus
	 * @param (Object) valor m�xim que pot assolir aquest tipus
	 */
	public TipusSimple(Object minim, Object maxim) {
		this.minim = minim;
		this.maxim = maxim;
	}
	/**
	 * <p>Constructor de TipusSimple</p>
	 * @param (String) nom del tipus
	 * @param (int) tamany que ocupa el tipus
	 * @param (Object) valor m�nim que pot assolir aquest tipus
	 * @param (Object) valor m�xim que pot assolir aquest tipus
	 */
	public TipusSimple(String nom, int tamany, Object minim, Object maxim) {
		this.nom = nom;
		this.tamany = tamany;
		this.minim = minim;
		this.maxim = maxim;
	}
	
	/**
	 * <p>Obt� el valor m�nim que pot assolir el tipus simple</p>
	 * @return Object
	 */
	public Object getMinim() {
		return minim;
	}

	/**
	 * <p>Estableix el valor m�nim que pot assolir el tipus simple</p>
	 * @param (Object) minim
	 */
	public void setMinim(Object minim) {
		this.minim = minim;
	}
	
	/**
	 * <p>Obt� el valor m�xim que pot assolir el tipus simple</p>
	 * @return Object
	 */
	public Object getMaxim() {
		return maxim;
	}

	/**
	 * <p>Estableix el valor m�xim que pot assolir el tipus simple</p>
	 * @param (Object) maxim
	 */
	public void setMaxim(Object maxim) {
		this.maxim = maxim;
	}
	
	/**
	 * <p>Compara l'objecte que se li pasa per paramtre amb l'objecte acual,
	 * retorna cert si s�n iguals.</p>
	 * @param (Object)obj
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		if (obj instanceof TipusSimple) {
			TipusSimple tipus = (TipusSimple) obj;
			boolean equals = super.equals(tipus);
			if (maxim == null)
				equals &= tipus.maxim == null;
			else
				equals &= maxim.equals(tipus.maxim);
			
			if (minim == null)
				equals &= tipus.minim == null;
			else
				equals &= minim.equals(tipus.minim);
			
			return equals;			
		} else {
			return false;
		}
	}
	
	 /**
	 * <p>Obt� tota la informaci� del objecte en format XML</p>
	 * @return String
	 */
	public String toXml() {
		String str = "<TipusSimple Nom=\"" + nom + "\"" + 
			" Tamany=\"" + tamany + "\"";
		if (minim != null)
			str += " Mínim=\"" + minim.toString() + "\"";
		else
			str += " Mínim=\"null\"";
		
		if (maxim != null)
			str += " Màxim=\"" + maxim.toString() + "\">";
		else
			str += " Màxim=\"null\">";
		
		str += "</TipusSimple>";
		return str;
	}
	public String toString() {
		String str = "<TipusSimple Nom=\"" + nom + "\"" +
				" Tamany=\"" + tamany + "\"";

		return "<TipusSimple Nom=\"" + nom + "\"/>";
	}
}
