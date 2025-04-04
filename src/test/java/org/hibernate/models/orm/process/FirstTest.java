/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.orm.process;

import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.model.process.spi.ManagedResources;
import org.hibernate.boot.models.AttributeNature;
import org.hibernate.boot.models.xml.internal.PersistenceUnitMetadataImpl;
import org.hibernate.boot.models.xml.spi.PersistenceUnitMetadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.MappingDefaults;
import org.hibernate.models.orm.process.spi.AttributeMetadata;
import org.hibernate.models.orm.process.spi.CategorizedDomainModel;
import org.hibernate.models.orm.process.spi.EntityHierarchy;
import org.hibernate.models.orm.process.spi.ManagedResourcesProcessor;

import org.hibernate.testing.boot.BootstrapContextImpl;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.Table;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
public class FirstTest {
	@Test
	@ServiceRegistry
	void fireItUp(ServiceRegistryScope registryScope) {
		final ManagedResources managedResources = new ManagedResourcesImpl();
		managedResources.getAnnotatedClassReferences().add( FirstOne.class );

		final PersistenceUnitMetadata persistenceUnitMetadata = new PersistenceUnitMetadataImpl();

		final MappingDefaults mappingDefaults = new MetadataBuilderImpl.MappingDefaultsImpl( registryScope.getRegistry() );

		final BootstrapContext bootstrapContext = new BootstrapContextImpl();

		final CategorizedDomainModel categorizedDomainModel = ManagedResourcesProcessor.processManagedResources(
				managedResources,
				persistenceUnitMetadata,
				mappingDefaults,
				SharedCacheMode.UNSPECIFIED,
				bootstrapContext
		);

		assertThat( categorizedDomainModel.entityHierarchies() ).hasSize( 1 );
		final EntityHierarchy entityHierarchy = categorizedDomainModel.entityHierarchies().iterator().next();
		assertThat( entityHierarchy.getRoot().getEntityName() ).isEqualTo( FirstOne.class.getName() );
		assertThat( entityHierarchy.getRoot() ).isSameAs( entityHierarchy.getAbsoluteRoot() );
		assertThat( entityHierarchy.getRoot().getAttributes() ).hasSize( 2 );

		final AttributeMetadata idAttr = entityHierarchy.getRoot().findAttribute( "id" );
		final AttributeMetadata nameAttr = entityHierarchy.getRoot().findAttribute( "name" );

		assertThat( idAttr.nature() ).isEqualTo( AttributeNature.BASIC );
		assertThat( nameAttr.nature() ).isEqualTo( AttributeNature.BASIC );

		assertThat( entityHierarchy.getIdMapping().contains( idAttr ) ).isTrue();
	}

	@Entity(name="FirstOne")
	@Table(name="FirstOne")
	public static class FirstOne {
		@Id
		private Integer id;
		private String name;
	}
}
