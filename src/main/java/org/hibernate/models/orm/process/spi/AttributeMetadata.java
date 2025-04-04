/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.spi;


import org.hibernate.boot.models.AttributeNature;
import org.hibernate.models.spi.MemberDetails;

/**
 * Metadata about a persistent attribute
 *
 * @author Steve Ebersole
 */
public interface AttributeMetadata extends TableOwner {
	/**
	 * The attribute name
	 */
	String name();

	/**
	 * The persistent nature of the attribute
	 */
	AttributeNature nature();

	/**
	 * The backing member
	 */
	MemberDetails member();
}
