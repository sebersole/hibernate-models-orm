/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.model.process.spi.ManagedResources;
import org.hibernate.boot.model.process.spi.MetadataBuildingProcess;
import org.hibernate.boot.models.xml.internal.PersistenceUnitMetadataImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.models.orm.process.spi.CategorizedDomainModel;
import org.hibernate.models.orm.process.spi.ManagedResourcesCategorizer;

import org.hibernate.testing.boot.BootstrapContextImpl;

import jakarta.persistence.SharedCacheMode;

/**
 * @author Steve Ebersole
 */
public class TestingHelper {
	public static MetadataSources metadataSources(StandardServiceRegistry serviceRegistry, Class<?>... classes) {
		return new MetadataSources( serviceRegistry ).addAnnotatedClasses( classes );
	}

	public static CategorizedDomainModel categorizeDomainModel(
			MetadataSources modelSources,
			StandardServiceRegistry serviceRegistry) {
		return categorizeDomainModel( modelSources, SharedCacheMode.UNSPECIFIED, serviceRegistry );
	}

	public static CategorizedDomainModel categorizeDomainModel(
			MetadataSources modelSources,
			SharedCacheMode sharedCacheMode,
			StandardServiceRegistry serviceRegistry) {
		final BootstrapContextImpl bootstrapContext = new BootstrapContextImpl( serviceRegistry );
		final ManagedResources managedResources = MetadataBuildingProcess.prepare( modelSources, bootstrapContext );
		return ManagedResourcesCategorizer.categorizeManagedResources(
				managedResources,
				new PersistenceUnitMetadataImpl(),
				new MetadataBuilderImpl.MappingDefaultsImpl( serviceRegistry ),
				sharedCacheMode,
				bootstrapContext
		);
	}

}
