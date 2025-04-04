/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.orm.process.inheritance;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

/**
 * @author Steve Ebersole
 */
@Entity
@PrimaryKeyJoinColumn(name="root_fk")
public class JoinedSub1 extends JoinedRoot {
	private String someData;

	public JoinedSub1() {
	}

	public JoinedSub1(Integer id, String name, String someData) {
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
