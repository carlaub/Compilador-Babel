
package taulaDeSimbols;

/**
 * <p>Classe que representa un par�metre d'una funci� o procediment</p>
 */
public class Parametre extends Variable {

	/**<p>tipus del pas de par�metre que se li fa a aquest parametre</p>*/
    private TipusPasParametre tipusPas;

    /** <p>Creador de la clase Variable</p>*/
    public Parametre() {
    }
    
    /**
     * <p>Creador de la clase Variable</p>
     * @param nom
     * @param tipus
     * @param desplacament
     */
    public Parametre(String nom, ITipus tipus, int desplacament) {
    	super(nom, tipus, desplacament);
    }
    
    /**
     * <p>Creador de la clase Variable</p>
     * @param nom
     * @param tipus
     * @param desplacament
     * @param tipusPas
     */
    public Parametre(String nom, ITipus tipus, int desplacament, TipusPasParametre tipusPas) {
    	super(nom, tipus, desplacament);
    	this.tipusPas = tipusPas;
    }
    
	/**
	 * <p>Obt� el tipus de pas de par�metre que se li fa a aquest parametre</p>
	 * @return TipusPasParametre
	 */
    public TipusPasParametre getTipusPasParametre() {        
        return tipusPas;
    } 

	/**
	 * <p>Estableix el pas de par�metre que se li fa a aquest parametre</p>
	 * @param tipusPas
	 */
    public void setTipusPasParametre(TipusPasParametre tipusPas) {        
        this.tipusPas = tipusPas;
    } 
    
    /**
     * <p>Obt� un flag que indica si la variable es parametre o no<p>
     * @return (boolean) si es parametre
     */
    public boolean getEsParametre() {
    	return true;
    }

    /**
	 * <p>Obt� tota la informaci� del objecte en format XML</p>
	 * @return String
	 */
    public String toXml() {
    	String result = "";
		result = "<Parametre Nom=\"" + getNom() +
				"\" Desplacament=\"" + getDesplacament();
    	if (tipusPas != null) result += "\" TipusPasParametre=\"" + tipusPas.toString() + "\">";
    	if (getTipus() != null) result += getTipus().toXml();
    	result += "</Parametre>";
        return result;
    } 
 }
