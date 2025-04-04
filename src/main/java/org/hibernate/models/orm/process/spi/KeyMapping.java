/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.spi;

import org.hibernate.models.spi.ClassDetails;

/**
 * Describes a relational key mapping
 *
 * @author Steve Ebersole
 */
public interface KeyMapping {
	/**
	 * The domain type of the key.
	 */
	ClassDetails getKeyType();

	/**
	 * Visit each attribute that is part of the key.
	 */
	void forEachAttribute(AttributeConsumer consumer);

	/**
	 * Checks whether the given {@code attribute} part of the key.
	 */
	boolean contains(AttributeMetadata attribute);
}
