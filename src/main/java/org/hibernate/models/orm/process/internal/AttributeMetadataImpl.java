/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.internal;

import org.hibernate.boot.models.AttributeNature;
import org.hibernate.models.orm.process.spi.AttributeMetadata;
import org.hibernate.models.spi.MemberDetails;

/**
 * Standard AttributeMetadata implementation
 *
 * @author Steve Ebersole
 */
public record AttributeMetadataImpl(String name, AttributeNature nature, MemberDetails member)
		implements AttributeMetadata {

	@Override
	public String toString() {
		return "AttributeMetadata(`" + name + "`)";
	}
}
