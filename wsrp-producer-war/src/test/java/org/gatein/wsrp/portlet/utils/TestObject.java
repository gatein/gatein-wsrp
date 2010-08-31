package org.gatein.wsrp.portlet.utils;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TestObject implements Serializable {

	private static final long serialVersionUID = 1L;

	private String firstName;
	private String lastName;
	private String username;
	private Integer employeeNumber;
	private String email;

	public TestObject() {
	}

	public TestObject(String firstName, String lastName, String username,
			Integer employeeNumber, String email) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.employeeNumber = employeeNumber;
		this.email = email;
	}

	public TestObject(TestObject object) {
		this.firstName = object.getFirstName();
		this.lastName = object.getLastName();
		this.username = object.getUsername();
		this.employeeNumber = object.getEmployeeNumber();
		this.email = object.getEmail();
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getEmployeeNumber() {
		return employeeNumber;
	}

	public void setEmployeeNumber(Integer employeeNumber) {
		this.employeeNumber = employeeNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TestObject) {
			TestObject object = (TestObject) obj;
			if (this.getUsername().equals(object.getUsername()))
				return true;
		}
		return false;

	}

	@Override
	public int hashCode() {
		return super.hashCode() + this.getUsername().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestObject: ");
		builder.append(firstName);
		builder.append(", ");
		builder.append(lastName);
		builder.append(", ");
		builder.append(username);
		builder.append(", ");
		builder.append(employeeNumber);
		builder.append(", ");
		builder.append(email);
		builder.append(".");
		return builder.toString();
	}
}
