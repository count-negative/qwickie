package org.qwickie.test.project.panel;

import java.io.Serializable;
import java.util.Date;

public class Customer implements Serializable {
	private static final long serialVersionUID = 1L;
	private String firstname;
	private String lastname;
	private Date birthday;

	/**
	 * 
	 */
	public Customer() {
	}

	public Customer(final String vorname, final String nachname, final Date geburtsdatum) {
		super();
		this.firstname = vorname;
		this.lastname = nachname;
		this.birthday = geburtsdatum;
	}

	public String getVorname() {
		return firstname;
	}

	public void setVorname(final String vorname) {
		this.firstname = vorname;
	}

	public String getNachname() {
		return lastname;
	}

	public void setNachname(final String nachname) {
		this.lastname = nachname;
	}

	public Date getGeburtsdatum() {
		return birthday;
	}

	public void setGeburtsdatum(final Date geburtsdatum) {
		this.birthday = geburtsdatum;
	}

	@Override
	public String toString() {
		return firstname + " " + lastname;
	}
}
