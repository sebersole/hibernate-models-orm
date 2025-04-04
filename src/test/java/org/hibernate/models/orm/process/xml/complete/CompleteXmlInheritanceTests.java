/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.xml.complete;

import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.models.orm.process.TestingHelper;
import org.hibernate.models.orm.process.spi.AttributeMetadata;
import org.hibernate.models.orm.process.spi.CategorizedDomainModel;
import org.hibernate.models.orm.process.spi.EntityHierarchy;
import org.hibernate.models.orm.process.spi.EntityTypeMetadata;
import org.hibernate.models.orm.process.spi.IdentifiableTypeMetadata;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Id;

import static jakarta.persistence.InheritanceType.JOINED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class CompleteXmlInheritanceTests {
	@Test
	void testIt(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				TestingHelper.metadataSources( registryScope.getRegistry() ).addResource( "mappings/complete/simple-inherited.xml" ),
				registryScope.getRegistry()
		);

		assertThat( domainModel.entityHierarchies() ).hasSize( 1 );
		final EntityHierarchy hierarchy = domainModel.entityHierarchies().iterator().next();
		assertThat( hierarchy.getInheritanceType() ).isEqualTo( JOINED );

		final EntityTypeMetadata root = hierarchy.getRoot();
		assertThat( root.getClassDetails().getClassName() ).isEqualTo( Root.class.getName() );
		final AttributeMetadata idAttr = root.findAttribute( "id" );
		assertThat( idAttr.member().hasDirectAnnotationUsage( Id.class ) ).isTrue();

		assertThat( root.getClassDetails().hasDirectAnnotationUsage( DiscriminatorColumn.class ) ).isFalse();
		assertThat( root.getClassDetails().hasDirectAnnotationUsage( DiscriminatorFormula.class ) ).isFalse();
		assertThat( root.getClassDetails().hasDirectAnnotationUsage( DiscriminatorValue.class ) ).isFalse();

		final Iterable<IdentifiableTypeMetadata> subTypes = root.getSubTypes();
		assertThat( subTypes ).hasSize( 1 );

		final IdentifiableTypeMetadata subType = subTypes.iterator().next();
		assertThat( subType.getClassDetails().hasDirectAnnotationUsage( DiscriminatorValue.class ) ).isFalse();
	}
}
