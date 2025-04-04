/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.orm.process.inheritance;

import org.hibernate.models.orm.process.TestingHelper;
import org.hibernate.models.orm.process.spi.CategorizedDomainModel;
import org.hibernate.models.orm.process.spi.EntityHierarchy;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.InheritanceType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class InheritanceTests {
	@Test
	void testNoInheritance(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				TestingHelper.metadataSources( registryScope.getRegistry(), UndefinedSingleRoot.class ),
				registryScope.getRegistry()
		);
		assertThat( domainModel.entityHierarchies() ).hasSize( 1 );
		final EntityHierarchy entityHierarchy = domainModel.entityHierarchies().iterator().next();
		assertThat( entityHierarchy.getInheritanceType() ).isEqualTo( InheritanceType.SINGLE_TABLE );
	}

	@Test
	void testSingleTableInheritance(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				TestingHelper.metadataSources( registryScope.getRegistry(), SingleRoot.class, SingleSub1.class, SingleSub2.class ),
				registryScope.getRegistry()
		);
		assertThat( domainModel.entityHierarchies() ).hasSize( 1 );
		final EntityHierarchy entityHierarchy = domainModel.entityHierarchies().iterator().next();
		assertThat( entityHierarchy.getInheritanceType() ).isEqualTo( InheritanceType.SINGLE_TABLE );
		assertThat( entityHierarchy.getRoot().getSubTypes() ).hasSize( 2 );
	}

	@Test
	void testJoinedInheritance(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				TestingHelper.metadataSources( registryScope.getRegistry(), JoinedRoot.class, JoinedSub1.class, JoinedSub2.class ),
				registryScope.getRegistry()
		);
		assertThat( domainModel.entityHierarchies() ).hasSize( 1 );
		final EntityHierarchy entityHierarchy = domainModel.entityHierarchies().iterator().next();
		assertThat( entityHierarchy.getInheritanceType() ).isEqualTo( InheritanceType.JOINED );
		assertThat( entityHierarchy.getRoot().getSubTypes() ).hasSize( 2 );
	}

	@Test
	void testUnionInheritance(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				TestingHelper.metadataSources( registryScope.getRegistry(), UnionRoot.class, UnionSub1.class, UnionSub2.class ),
				registryScope.getRegistry()
		);
		assertThat( domainModel.entityHierarchies() ).hasSize( 1 );
		final EntityHierarchy entityHierarchy = domainModel.entityHierarchies().iterator().next();
		assertThat( entityHierarchy.getInheritanceType() ).isEqualTo( InheritanceType.TABLE_PER_CLASS );
		assertThat( entityHierarchy.getRoot().getSubTypes() ).hasSize( 2 );
	}
}
