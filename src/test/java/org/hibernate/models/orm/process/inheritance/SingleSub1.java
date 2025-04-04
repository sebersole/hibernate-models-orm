/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.orm.process.inheritance;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * @author Steve Ebersole
 */
@Entity
@DiscriminatorValue( "S1" )
public class SingleSub1 extends SingleRoot {
	private String someData;

	protected SingleSub1() {
		// for Hibernate use
	}

	public SingleSub1(Integer id, String name, String someData) {
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
