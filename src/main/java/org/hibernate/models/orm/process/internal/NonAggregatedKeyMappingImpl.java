/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.internal;

import java.util.List;

import org.hibernate.models.orm.process.spi.AttributeConsumer;
import org.hibernate.models.orm.process.spi.AttributeMetadata;
import org.hibernate.models.orm.process.spi.NonAggregatedKeyMapping;
import org.hibernate.models.spi.ClassDetails;

/**
 * Standard NonAggregatedKeyMapping implementation
 *
 * @author Steve Ebersole
 */
public record NonAggregatedKeyMappingImpl(List<AttributeMetadata> idAttributes, ClassDetails idClassType)
		implements NonAggregatedKeyMapping {

	@Override
	public ClassDetails getKeyType() {
		return idClassType;
	}

	@Override
	public void forEachAttribute(AttributeConsumer consumer) {
		for ( int i = 0; i < idAttributes.size(); i++ ) {
			consumer.accept( i, idAttributes.get( i ) );
		}
	}

	@Override
	public boolean contains(AttributeMetadata attributeMetadata) {
		for ( int i = 0; i < idAttributes.size(); i++ ) {
			if ( idAttributes.get( i ) == attributeMetadata ) {
				return true;
			}
		}
		return false;
	}
}
