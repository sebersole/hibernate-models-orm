/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.orm.process.spi;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.annotations.SQLUpdate;
import org.hibernate.jdbc.Expectation;

/**
 *  Models the information for custom SQL execution defined as part of
 *  the mapping for a primary or secondary table.
 *
 * @author Steve Ebersole
 */
public record CustomSql(String sql, boolean isCallable, Class<? extends Expectation> verify) {

	public static CustomSql from(SQLInsert sqlInsert) {
		if ( sqlInsert == null ) {
			return null;
		}
		return new CustomSql( sqlInsert.sql(), sqlInsert.callable(), sqlInsert.verify() );
	}

	public static CustomSql from(SQLUpdate sqlUpdate) {
		if ( sqlUpdate == null ) {
			return null;
		}
		return new CustomSql( sqlUpdate.sql(), sqlUpdate.callable(), sqlUpdate.verify() );
	}

	public static CustomSql from(SQLDelete sqlDelete) {
		if ( sqlDelete == null ) {
			return null;
		}
		return new CustomSql( sqlDelete.sql(), sqlDelete.callable(), sqlDelete.verify() );
	}
}
