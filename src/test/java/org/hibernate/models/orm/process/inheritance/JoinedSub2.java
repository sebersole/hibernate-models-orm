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
public class JoinedSub2 extends JoinedRoot {
	private String moreData;

	public JoinedSub2() {
	}

	public JoinedSub2(Integer id, String name, String moreData) {
		super( id, name );
		this.moreData = moreData;
	}

	public String getMoreData() {
		return moreData;
	}

	public void setMoreData(String moreData) {
		this.moreData = moreData;
	}
}
