/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.spi;

import java.util.List;

import org.hibernate.boot.models.spi.JpaEventListener;
import org.hibernate.models.orm.process.internal.StandardPersistentAttributeMemberResolver;
import org.hibernate.models.spi.ClassDetailsRegistry;
import org.hibernate.models.spi.SourceModelBuildingContext;

import jakarta.persistence.SharedCacheMode;

/**
 * Contextual information used while building {@linkplain ManagedTypeMetadata} and friends.
 *
 * @author Steve Ebersole
 */
public interface ModelCategorizationContext {
	SourceModelBuildingContext getModelsContext();

	default ClassDetailsRegistry getClassDetailsRegistry() {
		return getModelsContext().getClassDetailsRegistry();
	}

	SharedCacheMode getSharedCacheMode();

	default PersistentAttributeMemberResolver getPersistentAttributeMemberResolver() {
		return StandardPersistentAttributeMemberResolver.INSTANCE;
	}

	List<JpaEventListener> getDefaultEventListeners();
}
