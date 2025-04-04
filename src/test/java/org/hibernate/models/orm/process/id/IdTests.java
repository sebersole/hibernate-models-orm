/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.id;

import java.util.Set;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.TenantId;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.models.orm.process.TestingHelper;
import org.hibernate.models.orm.process.spi.AggregatedKeyMapping;
import org.hibernate.models.orm.process.spi.AttributeMetadata;
import org.hibernate.models.orm.process.spi.BasicKeyMapping;
import org.hibernate.models.orm.process.spi.CategorizedDomainModel;
import org.hibernate.models.orm.process.spi.EntityHierarchy;
import org.hibernate.models.orm.process.spi.EntityTypeMetadata;
import org.hibernate.models.orm.process.spi.NonAggregatedKeyMapping;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Id;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.models.orm.process.TestingHelper.metadataSources;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class IdTests {
	@Test
	void testSimpleId(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				metadataSources( registryScope.getRegistry(), BasicIdEntity.class ),
				registryScope.getRegistry()
		);

		final EntityHierarchy entityHierarchy = domainModel.entityHierarchies().iterator().next();
		assertThat( entityHierarchy.getIdMapping() ).isNotNull();
		final BasicKeyMapping idMapping = (BasicKeyMapping) entityHierarchy.getIdMapping();
		assertThat( idMapping.attribute().member().hasDirectAnnotationUsage( Id.class ) ).isTrue();
		assertThat( idMapping.attribute().member().hasDirectAnnotationUsage( EmbeddedId.class ) ).isFalse();
		assertThat( idMapping.contains( entityHierarchy.getRoot().findAttribute( "id" ) ) ).isTrue();

		assertThat( entityHierarchy.getNaturalIdMapping() ).isNotNull();
		final BasicKeyMapping naturalIdMapping = (BasicKeyMapping) entityHierarchy.getNaturalIdMapping();
		assertThat( naturalIdMapping.attribute().member().hasDirectAnnotationUsage( NaturalId.class ) ).isTrue();
		assertThat( naturalIdMapping.attribute().member().hasDirectAnnotationUsage( Id.class ) ).isFalse();
		assertThat( naturalIdMapping.attribute().member().hasDirectAnnotationUsage( EmbeddedId.class ) ).isFalse();

		assertThat( entityHierarchy.getVersionAttribute() ).isNotNull();
		assertThat( entityHierarchy.getVersionAttribute().member().hasDirectAnnotationUsage( Version.class ) ).isTrue();

		assertThat( entityHierarchy.getTenantIdAttribute() ).isNotNull();
		assertThat( entityHierarchy.getTenantIdAttribute().member().hasDirectAnnotationUsage( TenantId.class ) ).isTrue();
	}

	@Test
	void testAggregatedId(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				metadataSources( registryScope.getRegistry(), AggregatedIdEntity.class ),
				registryScope.getRegistry()
		);

		final EntityHierarchy entityHierarchy = domainModel.entityHierarchies().iterator().next();

		assertThat( entityHierarchy.getIdMapping() ).isNotNull();
		final AggregatedKeyMapping idMapping = (AggregatedKeyMapping) entityHierarchy.getIdMapping();
		assertThat( idMapping.attribute().member().hasDirectAnnotationUsage( Id.class ) ).isFalse();
		assertThat( idMapping.attribute().member().hasDirectAnnotationUsage( EmbeddedId.class ) ).isTrue();

		assertThat( entityHierarchy.getNaturalIdMapping() ).isNotNull();
		assertThat( entityHierarchy.getNaturalIdMapping() ).isInstanceOf( AggregatedKeyMapping.class );
		final AggregatedKeyMapping naturalIdMapping = (AggregatedKeyMapping) entityHierarchy.getNaturalIdMapping();
		assertThat( naturalIdMapping.attribute().member().hasDirectAnnotationUsage( NaturalId.class ) ).isTrue();
		assertThat( naturalIdMapping.attribute().member().hasDirectAnnotationUsage( Id.class ) ).isFalse();
		assertThat( naturalIdMapping.attribute().member().hasDirectAnnotationUsage( EmbeddedId.class ) ).isFalse();

		assertThat( entityHierarchy.getVersionAttribute() ).isNotNull();
		assertThat( entityHierarchy.getVersionAttribute().member().hasDirectAnnotationUsage( Version.class ) ).isTrue();

		assertThat( entityHierarchy.getTenantIdAttribute() ).isNotNull();
		assertThat( entityHierarchy.getTenantIdAttribute().member().hasDirectAnnotationUsage( TenantId.class ) ).isTrue();
	}

	@Test
	void testNonAggregatedId(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				metadataSources( registryScope.getRegistry(), NonAggregatedIdEntity.class ),
				registryScope.getRegistry()
		);

		final EntityHierarchy entityHierarchy = domainModel.entityHierarchies().iterator().next();
		final EntityTypeMetadata root = entityHierarchy.getRoot();

		assertThat( entityHierarchy.getIdMapping() ).isNotNull();
		final NonAggregatedKeyMapping idMapping = (NonAggregatedKeyMapping) entityHierarchy.getIdMapping();
		assertThat( idMapping.idAttributes() ).hasSize( 2 );
		assertThat( idMapping.contains( root.findAttribute( "id1" ) ) ).isTrue();
		assertThat( idMapping.contains( root.findAttribute( "id2" ) ) ).isTrue();
		assertThat( idMapping.idClassType().getClassName() ).isEqualTo( NonAggregatedIdEntity.Pk.class.getName() );

		assertThat( entityHierarchy.getNaturalIdMapping() ).isNotNull();
		final NonAggregatedKeyMapping naturalIdMapping = (NonAggregatedKeyMapping) entityHierarchy.getNaturalIdMapping();
		assertThat( naturalIdMapping.idAttributes() ).hasSize( 2 );
		assertThat( naturalIdMapping.contains( root.findAttribute( "naturalKey1" ) ) ).isTrue();
		assertThat( naturalIdMapping.contains( root.findAttribute( "naturalKey2" ) ) ).isTrue();

		assertThat( entityHierarchy.getVersionAttribute() ).isNotNull();
		assertThat( entityHierarchy.getVersionAttribute().member().hasDirectAnnotationUsage( Version.class ) ).isTrue();

		assertThat( entityHierarchy.getTenantIdAttribute() ).isNotNull();
		assertThat( entityHierarchy.getTenantIdAttribute().member().hasDirectAnnotationUsage( TenantId.class ) ).isTrue();

		assertThat( entityHierarchy.getCacheRegion() ).isNotNull();
		assertThat( entityHierarchy.getCacheRegion().getAccessType() ).isEqualTo( AccessType.TRANSACTIONAL );

		assertThat( entityHierarchy.getInheritanceType() ).isNotNull();
		assertThat( entityHierarchy.getInheritanceType() ).isEqualTo( InheritanceType.TABLE_PER_CLASS );
	}
}
