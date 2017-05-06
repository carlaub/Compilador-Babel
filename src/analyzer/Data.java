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

	public void move(String atrib_dest, String atrib_orig){
		setValue(atrib_dest, getValue(atrib_orig));
		removeAttribute(atrib_orig);
	}

	public void copy(String atrib_dest, String atrib_orig){
		setValue(atrib_dest, getValue(atrib_orig));
	}

	public void remove(String atrib){
		removeAttribute(atrib);
	}


	/**
	 * Mètode per a realitzar un 
	 * @param block_dest
	 * @param block_orig
	 */
	public void moveBlock(String block_dest, String block_orig){
		String [] info_dest = block_dest.split("\\.");
		String [] info_orig = block_orig.split("\\.");

		move(info_dest[0]+".v"+info_dest[1], info_orig[0]+".v"+info_orig[1]);
		move(info_dest[0]+".t"+info_dest[1], info_orig[0]+".t"+info_orig[1]);
		move(info_dest[0]+".e"+info_dest[1], info_orig[0]+".e"+info_orig[1]);
	}

	public void copyBlock(String block_dest, String block_orig){
		String [] info_dest = block_dest.split("\\.");
		String [] info_orig = block_orig.split("\\.");

		copy(info_dest[0]+".v"+info_dest[1], info_orig[0]+".v"+info_orig[1]);
		copy(info_dest[0]+".t"+info_dest[1], info_orig[0]+".t"+info_orig[1]);
		copy(info_dest[0]+".e"+info_dest[1], info_orig[0]+".e"+info_orig[1]);
	}

	public void removeBlock(String block){
		String [] info = block.split("\\.");

		remove(info[0]+".v"+info[1]);
		remove(info[0]+".t"+info[1]);
		remove(info[0]+".e"+info[1]);
	}

	public void setBloc(String block, Object v, Object t, Object e){
		String [] info = block.split("\\.");

		setValue(info[0]+".v"+info[1], v);
		setValue(info[0]+".t"+info[1], t);
		setValue(info[0]+".e"+info[1], e);
	}

	@Override
	public String toString(){
		return attributes.toString();
	}


}