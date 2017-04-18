
package taulaDeSimbols;

/**
 * <p>Clase que representa una dimensi� d'un array del
 * llenguatge Babel.</p>
 */
public class DimensioArray {

	/**<p>Tipus del l�mit de la dimensi�</p>*/
    private ITipus tipusLimit;

    /**<p>L�mit inferior de la dimensi�</p>*/
    private Object limitInferior;

    /**<p>L�mit superior de la dimensi�</p>*/
    private Object limitSuperior;

    /**<p>Contructor de la classe DimensioArray</p>*/
    public DimensioArray() {
    }
    /**
     * <p>Contructor de la classe DimensioArray</p>
     * @param (ITipus) tipusLimit
     */
    public DimensioArray(ITipus tipusLimit) {
    	this.tipusLimit = tipusLimit;
    }
    /**
     * <p>Contructor de la classe DimensioArray</p>
     * @param (ITipus) tipusLimit
     * @param (Object) limitInferior
     * @param (Object) limitSuperior
     */
    public DimensioArray(ITipus tipusLimit, Object limitInferior, Object limitSuperior) {
    	this.tipusLimit = tipusLimit;
    	this.limitInferior = limitInferior;
    	this.limitSuperior = limitSuperior;
    }

	/**
	 * <p>Obt� el tipus del l�mits de la dimensi�</p>
	 * @return ITipus
	 */
    public ITipus getTipusLimit() {        
        return tipusLimit;
    } 

	/**
	 * <p>Estableix el tipus dels l�mits de la dimensi�</p>
	 * @param (ITipus)tipusLimit 
	 */
    public void setTipusLimit(ITipus tipusLimit) {        
        this.tipusLimit = tipusLimit;
    } 

	/**
	 * <p>Obt� el l�mit inferior de la dimensi�</p>
	 * @return Object
	 */
    public Object getLimitInferior() {        
        return limitInferior;
    } 

	/**
	 * <p>Estableix el l�mit inferior de la dimensi�</p>
	 * @param (Object)limitInferior 
	 */
    public void setLimitInferior(Object limitInferior) {        
        this.limitInferior = limitInferior;
    } 

    /**
	 * <p>Obt� el l�mit inferior de la dimensi�</p>
	 * @return Object
	 */
    public Object getLimitSuperior() {        
        return limitSuperior;
    } 

	/**
	 * <p>Estableix el l�mit superior de la dimensi�</p>
	 * @param (Object)limitSuperior 
	 */
    public void setLimitSuperior(Object limitSuperior) {        
        this.limitSuperior = limitSuperior;
    } 

    /**
	 * <p>Compara l'objecte que se li pasa per paramtre amb l'objecte acual,
	 * retorna cert si s�n iguals.</p>
	 * @param (Object)obj
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		if (obj instanceof DimensioArray) {
			DimensioArray dim = (DimensioArray) obj;

			boolean equals = true;
			if (tipusLimit == null)
				equals = dim.tipusLimit == null;
			else
				equals = tipusLimit.equals(dim.tipusLimit);
			
			if (limitInferior == null)
				equals = dim.limitInferior == null;
			else
				equals = limitInferior.equals(dim.limitInferior);
			
			if (limitSuperior == null)
				equals = dim.limitSuperior == null;
			else
				equals = limitSuperior.equals(dim.limitSuperior);
			
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
        String result = "<DimensioArray";
        
        if (limitInferior != null)
        	result += " LimitInferior=\"" + limitInferior.toString() + "\"";
        else
        	result += " LimitInferior=\"null\"";
        
        if (limitSuperior != null)
        	result += " LimitSuperior=\"" + limitSuperior.toString() + "\">";
        else
        	result += " LimitSuperior=\"null\">";
        
        if (tipusLimit != null)
        	result += tipusLimit.toXml();
        result += "</DimensioArray>";
        return result;
    } 
 }
