/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.callbacks;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.models.orm.process.TestingHelper;
import org.hibernate.models.orm.process.spi.CategorizedDomainModel;
import org.hibernate.models.orm.process.spi.EntityHierarchy;
import org.hibernate.models.orm.process.spi.EntityTypeMetadata;
import org.hibernate.models.orm.process.spi.IdentifiableTypeMetadata;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.models.orm.process.TestingHelper.metadataSources;

/**
 * @author Steve Ebersole
 */
public class CallbackTests {
	@Test
	@ServiceRegistry
	void testMappedSuper(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				metadataSources( registryScope.getRegistry(), HierarchySuper.class, HierarchyRoot.class ),
				registryScope.getRegistry()
		);

		assertThat( domainModel.entityHierarchies() ).hasSize( 1 );
		final EntityHierarchy hierarchy = domainModel.entityHierarchies().iterator().next();
		final EntityTypeMetadata root = hierarchy.getRoot();
		assertThat( root ).isNotSameAs( hierarchy.getAbsoluteRoot() );
		assertThat( domainModel.mappedSuperclasses() ).containsValue( hierarchy.getAbsoluteRoot().getClassDetails() );

		assertThat( root.getCompleteJpaEventListeners() ).hasSize( 3 );
		assertThat( root.getHierarchyJpaEventListeners() ).hasSize( 3 );

		final List<String> listenerClassNames = root.getHierarchyJpaEventListeners()
				.stream()
				.map( listener -> listener.getCallbackClass().getClassName() )
				.collect( Collectors.toList() );
		assertThat( listenerClassNames ).containsExactly(
				Listener1.class.getName(),
				Listener2.class.getName(),
				HierarchyRoot.class.getName()
		);

		final IdentifiableTypeMetadata superMapping = root.getSuperType();
		assertThat( superMapping.getCompleteJpaEventListeners() ).hasSize( 1 );
		assertThat( superMapping.getHierarchyJpaEventListeners() ).hasSize( 1 );
		final String callbackClassName = superMapping.getHierarchyJpaEventListeners()
				.get( 0 )
				.getCallbackClass()
				.getClassName();
		assertThat( callbackClassName ).isEqualTo( Listener1.class.getName() );
	}
}
