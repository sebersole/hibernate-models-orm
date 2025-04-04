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
@DiscriminatorValue( "S2" )
public class SingleSub2 extends SingleRoot {
	private String moreData;

	public SingleSub2() {
	}

	public SingleSub2(Integer id, String name, String moreData) {
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
