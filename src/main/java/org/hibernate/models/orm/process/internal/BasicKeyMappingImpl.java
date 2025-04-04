/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.internal;

import org.hibernate.models.orm.process.spi.AttributeMetadata;
import org.hibernate.models.orm.process.spi.BasicKeyMapping;

/**
 * Standard BasicKeyMapping implementation
 *
 * @author Steve Ebersole
 */
public record BasicKeyMappingImpl(AttributeMetadata attribute) implements BasicKeyMapping {
}
