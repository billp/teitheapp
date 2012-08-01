package org.teitheapp.classes;

import java.io.Serializable;

public class Teacher implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4060884025693107320L;
	
	private String name, surname, role, phone, email;

	public Teacher(String surname, String name, String roles, String phone,
			String email) {
		super();
		this.name = name;
		this.surname = surname;
		this.role = roles;
		this.phone = phone;
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getRole() {
		return role;
	}

	public void setRoles(String roles) {
		this.role = roles;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
