/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.internal;

import java.util.List;

import org.hibernate.boot.models.spi.GlobalRegistrations;
import org.hibernate.boot.models.spi.JpaEventListener;
import org.hibernate.models.orm.process.spi.ModelCategorizationContext;
import org.hibernate.models.spi.SourceModelBuildingContext;

import jakarta.persistence.SharedCacheMode;

/**
 * @author Steve Ebersole
 */
public class ModelCategorizationContextImpl implements ModelCategorizationContext {
	private final SourceModelBuildingContext modelsContext;
	private final GlobalRegistrations globalRegistrations;
	private final SharedCacheMode sharedCacheMode;

	public ModelCategorizationContextImpl(
			SourceModelBuildingContext modelsContext,
			GlobalRegistrations globalRegistrations,
			SharedCacheMode sharedCacheMode) {
		this.modelsContext = modelsContext;
		this.globalRegistrations = globalRegistrations;
		this.sharedCacheMode = sharedCacheMode;
	}

	@Override
	public SourceModelBuildingContext getModelsContext() {
		return modelsContext;
	}

	public GlobalRegistrations getGlobalRegistrations() {
		return globalRegistrations;
	}

	@Override
	public SharedCacheMode getSharedCacheMode() {
		return sharedCacheMode;
	}

	@Override
	public List<JpaEventListener> getDefaultEventListeners() {
		return getGlobalRegistrations().getEntityListenerRegistrations();
	}
}
