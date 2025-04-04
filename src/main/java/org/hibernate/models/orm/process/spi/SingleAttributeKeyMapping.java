/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.spi;

import org.hibernate.models.spi.ClassDetails;

/**
 * @author Steve Ebersole
 */
public interface SingleAttributeKeyMapping extends KeyMapping {
	AttributeMetadata attribute();

	default String getAttributeName() {
		return attribute().name();
	}

	default ClassDetails getKeyType() {
		return attribute().member().getType().determineRawClass();
	}

	@Override
	default void forEachAttribute(AttributeConsumer consumer) {
		consumer.accept( 0, attribute() );
	}

	@Override
	default boolean contains(AttributeMetadata attributeMetadata) {
		return attributeMetadata == attribute();
	}
}
