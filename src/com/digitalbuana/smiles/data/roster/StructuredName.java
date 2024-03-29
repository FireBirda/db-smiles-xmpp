package com.digitalbuana.smiles.data.roster;

/**
 * vCard-based structured name.
 * 
 * @author alexander.ivanov
 * 
 */
public class StructuredName {

	private final String nickName;
	private final String formattedName;
	private final String firstName;
	private final String middleName;
	private final String lastName;
	private final String bestName;

	public StructuredName(String nickName, String formattedName,
			String firstName, String middleName, String lastName) {
		super();
		this.nickName = nickName == null ? "" : nickName;
		this.formattedName = formattedName == null ? "" : formattedName;
		this.firstName = firstName == null ? "" : firstName;
		this.middleName = middleName == null ? "" : middleName;
		this.lastName = lastName == null ? "" : lastName;
		if (!"".equals(this.nickName))
			bestName = this.nickName;
		else if (!"".equals(this.formattedName))
			bestName = this.formattedName;
		else
			bestName = "";
	}

	/**
	 * @return the nick name.
	 */
	public String getNickName() {
		return nickName;
	}

	/**
	 * @return the formatted name.
	 */
	public String getFormattedName() {
		return formattedName;
	}

	/**
	 * @return the first name.
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @return the middle name.
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * @return the last name.
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @return the nick name or formatted name.
	 */
	public String getBestName() {
		return bestName;
	}

}