/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.locking;

import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.mapping.BasicValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.models.orm.process.TestingHelper;
import org.hibernate.models.orm.process.inheritance.UndefinedSingleRoot;
import org.hibernate.models.orm.process.spi.CategorizedDomainModel;
import org.hibernate.models.orm.process.spi.EntityHierarchy;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class OptimisticLockingTests {
	@Test
	void testVersionAttribute(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				TestingHelper.metadataSources( registryScope.getRegistry(), VersionedEntity.class ),
				registryScope.getRegistry()
		);
		final EntityHierarchy entityHierarchy = domainModel.entityHierarchies().iterator().next();
		assertThat( entityHierarchy.getOptimisticLockStyle() ).isEqualTo( OptimisticLockStyle.VERSION );
	}

	@Test
	void testVersionAttributeWithColumn(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				TestingHelper.metadataSources( registryScope.getRegistry(), VersionedEntityWithColumn.class ),
				registryScope.getRegistry()
		);
		final EntityHierarchy entityHierarchy = domainModel.entityHierarchies().iterator().next();
		assertThat( entityHierarchy.getOptimisticLockStyle() ).isEqualTo( OptimisticLockStyle.VERSION );
	}

	@Test
	void testDirtyVersioning(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				TestingHelper.metadataSources( registryScope.getRegistry(), DirtyVersionedEntity.class ),
				registryScope.getRegistry()
		);
		final EntityHierarchy entityHierarchy = domainModel.entityHierarchies().iterator().next();
		assertThat( entityHierarchy.getOptimisticLockStyle() ).isEqualTo( OptimisticLockStyle.DIRTY );
	}

}
