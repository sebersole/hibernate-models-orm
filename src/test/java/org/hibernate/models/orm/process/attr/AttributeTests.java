/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.orm.process.attr;

import org.hibernate.boot.models.AttributeNature;
import org.hibernate.models.orm.process.TestingHelper;
import org.hibernate.models.orm.process.spi.AttributeMetadata;
import org.hibernate.models.orm.process.spi.CategorizedDomainModel;
import org.hibernate.models.orm.process.spi.EntityHierarchy;
import org.hibernate.models.orm.process.spi.EntityTypeMetadata;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.models.orm.process.TestingHelper.metadataSources;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class AttributeTests {
	@Test
	void testSimpleEntity(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				metadataSources( registryScope.getRegistry(), Representative.class ),
				registryScope.getRegistry()
		);

		final EntityHierarchy entityHierarchy = domainModel.entityHierarchies().iterator().next();
		final EntityTypeMetadata root = entityHierarchy.getRoot();

		final AttributeMetadata name = root.findAttribute( "name" );
		assertThat( name.nature() ).isEqualTo( AttributeNature.BASIC );

		final AttributeMetadata status = root.findAttribute( "status" );
		assertThat( status.nature() ).isEqualTo( AttributeNature.BASIC );

		final AttributeMetadata component = root.findAttribute( "component" );
		assertThat( component.nature() ).isEqualTo( AttributeNature.EMBEDDED );

		final AttributeMetadata oneToOne = root.findAttribute( "anotherOne" );
		assertThat( oneToOne.nature() ).isEqualTo( AttributeNature.TO_ONE );

		final AttributeMetadata manyToOne = root.findAttribute( "another" );
		assertThat( manyToOne.nature() ).isEqualTo( AttributeNature.TO_ONE );

		final AttributeMetadata any = root.findAttribute( "other" );
		assertThat( any.nature() ).isEqualTo( AttributeNature.ANY );

		final AttributeMetadata statuses = root.findAttribute( "statuses" );
		assertThat( statuses.nature() ).isEqualTo( AttributeNature.ELEMENT_COLLECTION );

		final AttributeMetadata components = root.findAttribute( "components" );
		assertThat( components.nature() ).isEqualTo( AttributeNature.ELEMENT_COLLECTION );

		final AttributeMetadata oneToMany = root.findAttribute( "others" );
		assertThat( oneToMany.nature() ).isEqualTo( AttributeNature.ONE_TO_MANY );

		final AttributeMetadata manyToMany = root.findAttribute( "manyOthers" );
		assertThat( manyToMany.nature() ).isEqualTo( AttributeNature.MANY_TO_MANY );

		final AttributeMetadata pluralAny = root.findAttribute( "anyOthers" );
		assertThat( pluralAny.nature() ).isEqualTo( AttributeNature.MANY_TO_ANY );

	}
}
