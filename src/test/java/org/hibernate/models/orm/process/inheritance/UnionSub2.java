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
public class UnionSub2 extends UnionRoot {
	private String moreData;

	public UnionSub2() {
	}

	public UnionSub2(Integer id, String name, String moreData) {
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
