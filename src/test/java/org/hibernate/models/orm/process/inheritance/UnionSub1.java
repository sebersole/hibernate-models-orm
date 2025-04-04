/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.orm.process.inheritance;

import jakarta.persistence.Entity;

/**
 * @author Steve Ebersole
 */
@Entity
public class UnionSub1 extends UnionRoot {
	private String someData;

	public UnionSub1() {
	}

	public UnionSub1(Integer id, String name, String someData) {
		super( id, name );
		this.someData = someData;
	}

	public String getSomeData() {
		return someData;
	}

	public void setSomeData(String someData) {
		this.someData = someData;
	}
}
