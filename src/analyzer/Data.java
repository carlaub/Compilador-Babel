package analyzer;

/**
 * This type was created in VisualAge.
 */

import java.util.Hashtable;

public class Data {
	private Hashtable attributes = new Hashtable();
	/**
	 * Data constructor comment.
	 */
	public Data() {
		super();
	}
	/**
	 * This method was created in VisualAge.
	 * @return java.util.Hashtable
	 */
	public java.util.Hashtable getAttributes() {
		return attributes;
	}
	/**
	 * This method was created in VisualAge.
	 * @return java.lang.Object
	 * @param attributeID java.lang.Object
	 */
	public Object getValue(java.lang.Object attributeID) {
		return getAttributes().get(attributeID);
	}
	/**
	 * This method was created in VisualAge.
	 */
	public void removeAll() {
		getAttributes().clear();
	}
	/**
	 * This method was created in VisualAge.
	 * @param attributeID java.lang.Object
	 */
	public void removeAttribute(java.lang.Object attributeID) {
		getAttributes().remove(attributeID);
	}
	/**
	 * This method was created in VisualAge.
	 * @param newValue java.util.Hashtable
	 */
	public void setAttributes(java.util.Hashtable newValue) {
		this.attributes = newValue;
	}
	/**
	 * This method was created in VisualAge.
	 * @param attributeID java.lang.Object
	 * @param attributeValue java.lang.Object
	 */
	public void setValue(java.lang.Object attributeID, java.lang.Object attributeValue) {
		getAttributes().put(attributeID,attributeValue);
	}

	@Override
	public String toString(){
		return attributes.toString();
	}


}