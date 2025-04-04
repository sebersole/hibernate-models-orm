/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.tenancy;

import org.hibernate.models.orm.process.TestingHelper;
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
public class SimpleTenancyTests {
	@Test
	void testSimpleTenancy(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				TestingHelper.metadataSources( registryScope.getRegistry(), ProtectedEntity.class ),
				registryScope.getRegistry()
		);
		final EntityHierarchy entityHierarchy = domainModel.entityHierarchies().iterator().next();
		assertThat( entityHierarchy.getTenantIdAttribute() ).isNotNull();
		assertThat( entityHierarchy.getTenantIdAttribute().name() ).isEqualTo( "tenant" );
	}

	@Test
	void testTenancyWithColumn(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				TestingHelper.metadataSources( registryScope.getRegistry(), ProtectedEntity.class ),
				registryScope.getRegistry()
		);
		final EntityHierarchy entityHierarchy = domainModel.entityHierarchies().iterator().next();
		assertThat( entityHierarchy.getTenantIdAttribute() ).isNotNull();
		assertThat( entityHierarchy.getTenantIdAttribute().name() ).isEqualTo( "tenant" );
	}

}
