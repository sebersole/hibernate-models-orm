/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.xml.complete;

import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.boot.models.AttributeNature;
import org.hibernate.models.orm.process.TestingHelper;
import org.hibernate.models.orm.process.spi.AttributeMetadata;
import org.hibernate.models.orm.process.spi.CategorizedDomainModel;
import org.hibernate.models.orm.process.spi.EntityHierarchy;
import org.hibernate.models.orm.process.spi.EntityTypeMetadata;
import org.hibernate.models.orm.process.xml.SimpleEntity;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Id;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class CompleteXmlTests {
	@Test
	void testSimpleCompleteEntity(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				TestingHelper.metadataSources( registryScope.getRegistry() ).addResource( "mappings/complete/simple-complete.xml" ),
				registryScope.getRegistry()
		);

		assertThat( domainModel.entityHierarchies() ).hasSize( 1 );

		final EntityHierarchy hierarchy = domainModel.entityHierarchies().iterator().next();

		final EntityTypeMetadata root = hierarchy.getRoot();
		assertThat( root.getClassDetails().getClassName() ).isEqualTo( SimpleEntity.class.getName() );

		assertThat( root.getClassDetails().hasDirectAnnotationUsage( DiscriminatorColumn.class ) ).isFalse();
		assertThat( root.getClassDetails().hasDirectAnnotationUsage( DiscriminatorFormula.class ) ).isFalse();
		assertThat( root.getClassDetails().hasDirectAnnotationUsage( DiscriminatorValue.class ) ).isFalse();

		assertThat( root.getNumberOfAttributes() ).isEqualTo( 2 );

		final AttributeMetadata idAttribute = root.findAttribute( "id" );
		assertThat( idAttribute.nature() ).isEqualTo( AttributeNature.BASIC );
		assertThat( idAttribute.member().hasDirectAnnotationUsage( Basic.class ) ).isTrue();
		assertThat( idAttribute.member().hasDirectAnnotationUsage( Id.class ) ).isTrue();
		final Column idColumnAnn = idAttribute.member().getDirectAnnotationUsage( Column.class );
		assertThat( idColumnAnn ).isNotNull();
		assertThat( idColumnAnn.name() ).isEqualTo( "pk" );

		final AttributeMetadata nameAttribute = root.findAttribute( "name" );
		assertThat( nameAttribute.nature() ).isEqualTo( AttributeNature.BASIC );
		assertThat( nameAttribute.member().hasDirectAnnotationUsage( Basic.class ) ).isTrue();
		final Column nameColumnAnn = nameAttribute.member().getDirectAnnotationUsage( Column.class );
		assertThat( nameColumnAnn ).isNotNull();
		assertThat( nameColumnAnn.name() ).isEqualTo( "description" );

		final SQLRestriction sqlRestriction = root.getClassDetails().getDirectAnnotationUsage( SQLRestriction.class );
		assertThat( sqlRestriction ).isNotNull();
		assertThat( sqlRestriction.value() ).isEqualTo( "name is not null" );

		validateSqlInsert( root.getClassDetails().getDirectAnnotationUsage( SQLInsert.class ));

		validateFilterUsage( root.getClassDetails().getDirectAnnotationUsage( Filters.class ) );
	}

	private void validateFilterUsage(Filters filters) {
		assertThat( filters ).isNotNull();
		assertThat( filters.value() ).hasSize( 1 );
		final Filter filter = filters.value()[0];
		assertThat( filter.name() ).isEqualTo( "name_filter" );
		assertThat( filter.condition() ).isEqualTo( "{t}.name = :name" );
		assertThat( filter.aliases() ).hasSize( 1 );
		assertThat( filter.aliases()[0].alias() ).isEqualTo( "t" );
		assertThat( filter.aliases()[0].table() ).isEqualTo( "simple_entity" );
		assertThat( filter.aliases()[0].entity() ).isEqualTo( SimpleEntity.class );
	}

	private void validateSqlInsert(SQLInsert sqlInsert) {
		assertThat( sqlInsert ).isNotNull();
		assertThat( sqlInsert.sql() ).isEqualTo( "insert into simple_entity(name) values(?)" );
		assertThat( sqlInsert.table() ).isEqualTo( "simple_entity" );
		assertThat( sqlInsert.callable() ).isTrue();
		assertThat( sqlInsert.check() ).isEqualTo( ResultCheckStyle.COUNT );
	}
}
