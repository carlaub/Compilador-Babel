
package taulaDeSimbols;

/**
 * <p>Classe que representa una variable del llenguatge Babel</p>
 */
public class Variable {

	/**
	 * <p>nom de la variable</p>
	 */
	private String nom;

	/**
	 * <p>tipus de la variable</p>
	 */
	private ITipus tipus;

	/**
	 * <p>desla�ament de la variable</p>
	 */
	private int desplacament;

	private boolean isGlobal;

	/**
	 * <p>Creador de la clase Variable</p>
	 */
	public Variable() {
	}

	/**
	 * <p>Creador de la clase Variable</p>
	 *
	 * @param nom
	 * @param tipus
	 * @param desplacament
	 */
	public Variable(String nom, ITipus tipus, int desplacament, boolean isGlobal) {
		this.nom = nom;
		this.tipus = tipus;
		this.desplacament = desplacament;
		this.isGlobal = isGlobal;
	}

	/**
	 * <p>Obt� el nom de la variable</p>
	 *
	 * @return String
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * <p>Estableix el nom de la variable</p>
	 *
	 * @param nom
	 */
	public void setNom(String nom) {
		this.nom = nom;
	}

	/**
	 * <p>Obt� el tipus de la variable</p>
	 *
	 * @return ITipus
	 */
	public ITipus getTipus() {
		return tipus;
	}

	/**
	 * <p>Estableix el tipus de la variable</p>
	 *
	 * @param tipus
	 */
	public void setTipus(ITipus tipus) {
		this.tipus = tipus;
	}

	/**
	 * <p>Obt� el despla�ament de la variable</p>
	 *
	 * @return int
	 */
	public int getDesplacament() {
		return desplacament;
	}

	/**
	 * <p>Estableix el despla�ament de la variable</p>
	 *
	 * @param despla�ament
	 */
	public void setDesplacament(int desplacament) {
		this.desplacament = desplacament;
	}

	/**
	 * <p>Obt� un flag que indica si la variable es parametre o no<p>
	 *
	 * @return (boolean) si es parametre
	 */
	public boolean getEsParametre() {
		return false;
	}

	/**
	 * <p>Obt� tota la informaci� del objecte en format XML</p>
	 *
	 * @return String
	 */
	public String toXml() {
		String result = "<Variable Nom=\"" + nom + "\" Desplacament=\"" + desplacament + "\" isGlobal=\""+isGlobal+"\">";
		if (tipus != null) result += tipus.toXml();
		result += "</Variable>";
		return result;
	}

	public boolean getIsGlobal() {
		return isGlobal;
	}

	@Override
	public String toString() {
		return toXml();
	}
}
