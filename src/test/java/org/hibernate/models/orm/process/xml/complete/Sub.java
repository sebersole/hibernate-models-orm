/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.xml.complete;

/**
 * @author Steve Ebersole
 */
public class Sub extends Root {
	private String subName;

	protected Sub() {
		// for Hibernate use
	}

	public Sub(Integer id, String name, String subName) {
		super( id, name );
		this.subName = subName;
	}

	public String getSubName() {
		return subName;
	}

	public void setSubName(String subName) {
		this.subName = subName;
	}
}
