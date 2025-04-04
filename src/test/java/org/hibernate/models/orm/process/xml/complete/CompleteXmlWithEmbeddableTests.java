/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.xml.complete;

import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.boot.models.AttributeNature;
import org.hibernate.models.orm.process.TestingHelper;
import org.hibernate.models.orm.process.spi.AttributeMetadata;
import org.hibernate.models.orm.process.spi.CategorizedDomainModel;
import org.hibernate.models.orm.process.spi.EntityHierarchy;
import org.hibernate.models.orm.process.spi.EntityTypeMetadata;
import org.hibernate.models.spi.ClassDetails;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class CompleteXmlWithEmbeddableTests {
	@Test
	void testSimple(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				TestingHelper.metadataSources( registryScope.getRegistry() ).addResource( "mappings/complete/simple-person.xml" ),
				registryScope.getRegistry()
		);

		assertThat( domainModel.entityHierarchies() ).hasSize( 1 );
		final EntityHierarchy hierarchy = domainModel.entityHierarchies().iterator().next();
		final EntityTypeMetadata root = hierarchy.getRoot();

		assertThat( root.getClassDetails().hasDirectAnnotationUsage( DiscriminatorColumn.class ) ).isFalse();
		assertThat( root.getClassDetails().hasDirectAnnotationUsage( DiscriminatorFormula.class ) ).isFalse();
		assertThat( root.getClassDetails().hasDirectAnnotationUsage( DiscriminatorValue.class ) ).isFalse();

		assertThat( domainModel.embeddables() ).hasSize( 1 );
		final ClassDetails embeddableDetails = domainModel.embeddables().entrySet().iterator().next().getValue();
		assertThat( embeddableDetails.getClassName() ).isEqualTo( Name.class.getName() );

		final AttributeMetadata nameAttr = root.findAttribute( "name" );
		assertThat( nameAttr.nature() ).isEqualTo( AttributeNature.EMBEDDED );
		assertThat( nameAttr.member().getType().determineRawClass() ).isSameAs( embeddableDetails );
	}
}
