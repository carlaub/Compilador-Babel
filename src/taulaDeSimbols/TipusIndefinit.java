
package taulaDeSimbols;

/**
 * <p>Clase que representa el tipus indefinit del
 * llenguatge Babel.</p>
 */
public class TipusIndefinit extends ITipus {
	
	/**<p>Creador del tipus indefinit</p>*/
	public TipusIndefinit() {
		nom = "";
	}
	
	/**
	 * <p>Creador del tipus indefinit</p>
	 * @param nom
	 * @param tamany
	 */
	public TipusIndefinit(String nom, int tamany) {
		super.nom = nom;
		super.tamany = tamany;
	}

	/**
	 * <p>Creador del tipus indefinit</p>
	 * @param nom
	 */
	public TipusIndefinit(String nom) {
		super.nom = nom;
		super.tamany = 0;
	}

	/**
	 * <p>Obt� tota la informaci� del objecte en format XML</p>
	 * @return String
	 */
	public String toXml() {
		String str = "<TipusIndefinit Nom=\"" + nom +
				"\" Tamany=\"" + tamany + "\"></TipusIndefinit>";
		return str;
	}

	@Override
	public String toString() {
		String str = "<TipusIndefinit Nom=\"" + nom +
				"\" Tamany=\"" + tamany + "\"></TipusIndefinit>";
		return str;
	}
 }
