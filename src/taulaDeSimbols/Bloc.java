
package taulaDeSimbols;

import java.util.Hashtable;

/**
 * <p>Classe que representa un bloc del llenguatge Babel</p>
 */
public class Bloc {

	/**
	 * <p>Llista de constants del bloc</p>
	 */
	private Hashtable llistaConstants = new Hashtable();

	/**
	 * <p>Llista de variables del bloc</p>
	 */
	private Hashtable llistaVariables = new Hashtable();

	/**
	 * <p>Llista de procediments del bloc</p>
	 */
	private Hashtable llistaProcediments = new Hashtable();

	private Parametre parametre;

	/**
	 * <p>Constructor de la classe Bloc</p>
	 */
	public Bloc() {
	}

	/**
	 * <p>Constructor de la classe Bloc. Aquest constructor
	 * insereix tots els par�metres del procediment com a variables</p>
	 *
	 * @param procediment
	 */
	public Bloc(Procediment procediment) {
		for (int i = 0; i < procediment.getNumeroParametres(); i++)
			inserirVariable(procediment.obtenirParametre(i));
	}

	public int getNumParams() {
		int n = 0, max = 0;
		for (int i = 0; i < llistaVariables.size(); i++) {
			Variable v = (Variable) llistaVariables.values().toArray()[i];
			if (v instanceof Parametre) {
				n++;
				if (v.getDesplacament() > max){
					max = v.getDesplacament();
					parametre = (Parametre) v;
				}
			}
		}
		return n;
	}

	/**
	 * <p>Insereix la constant en la llista de constants</p>
	 *
	 * @param (Constant) constant
	 */
	public void inserirConstant(Constant constant) {
		llistaConstants.put(constant.getNom(), constant);
	}

	/**
	 * <p>Obt� la constant a partir del nom</p>
	 *
	 * @param (String) nom
	 * @return Constant
	 */
	public Constant obtenirConstant(String nom) {
		return (Constant) llistaConstants.get(nom);
	}

	public boolean existeixConstant(String nom) {
		return obtenirConstant(nom) != null;
	}

	/**
	 * <p>Insereix la variable en la llista de variables</p>
	 *
	 * @param (Variable) variable
	 */
	public void inserirVariable(Variable variable) {
		llistaVariables.put(variable.getNom(), variable);
	}

	/**
	 * <p>Obt� la variable a partir del nom</p>
	 *
	 * @param (String) nom
	 * @return Variable
	 */
	public Variable obtenirVariable(String nom) {
		return (Variable) llistaVariables.get(nom);
	}

	public boolean existeixVariable(String nom) {
		return obtenirVariable(nom) != null;
	}

	/**
	 * <p>Insereix el procediment en la llista de procediments</p>
	 *
	 * @param (Procediment) funcio
	 */
	public void inserirProcediment(Procediment funcio) {
		llistaProcediments.put(funcio.getNom(), funcio);
	}

	/**
	 * <p>Obt� el procediment a partir del nom</p>
	 *
	 * @param (String) nom
	 * @return Constant
	 */
	public Procediment obtenirProcediment(String nom) {
		return (Procediment) llistaProcediments.get(nom);
	}

	public boolean existeixProcediment(String nom) {
		return obtenirProcediment(nom) != null;
	}

	public boolean existeixID(String nom) {
		return existeixVariable(nom) || existeixConstant(nom) || existeixProcediment(nom);
	}

	/**
	 * <p>Obt� tota la informaci� del objecte en format XML</p>
	 *
	 * @return String
	 */
	public String toXml() {
		String result = "<Bloc>";

		result += "<Constants>";
		for (int i = 0; i < llistaConstants.size(); i++)
			result += ((Constant) llistaConstants.values().toArray()[i]).toXml();
		result += "</Constants>";

		result += "<Variables>";
		for (int i = 0; i < llistaVariables.size(); i++)
			result += ((Variable) llistaVariables.values().toArray()[i]).toXml();
		result += "</Variables>";

		result += "<Procediments>";
		for (int i = 0; i < llistaProcediments.size(); i++)
			result += ((Procediment) llistaProcediments.values().toArray()[i]).toXml();
		result += "</Procediments>";

		result += "</Bloc>";
		return result;
	}

	@Override
	public String toString() {
		return toXml();
	}

	public Parametre getLastParametre() {
		return parametre;
	}
}
