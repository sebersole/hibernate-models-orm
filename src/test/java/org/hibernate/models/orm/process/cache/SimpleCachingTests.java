/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.cache;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.models.orm.process.TestingHelper;
import org.hibernate.models.orm.process.spi.CategorizedDomainModel;
import org.hibernate.models.orm.process.spi.EntityHierarchy;
import org.hibernate.models.orm.process.spi.EntityTypeMetadata;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;

import static jakarta.persistence.InheritanceType.JOINED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;
import static org.hibernate.models.orm.process.TestingHelper.metadataSources;

/**
 * @author Steve Ebersole
 */
public class SimpleCachingTests {
	@Test
	@ServiceRegistry
	void simpleTest(ServiceRegistryScope registryScope) {
		final CategorizedDomainModel domainModel = TestingHelper.categorizeDomainModel(
				metadataSources( registryScope.getRegistry(), CacheableEntity.class, CacheableEntitySub.class ),
				registryScope.getRegistry()
		);

		assertThat( domainModel.entityHierarchies() ).hasSize( 1 );
		final EntityHierarchy hierarchy = domainModel.entityHierarchies().iterator().next();
		assertThat( hierarchy.getCacheRegion() ).isNotNull();
		assertThat( hierarchy.getCacheRegion().getRegionName() ).isEqualTo( "org.hibernate.testing.entity" );
		assertThat( hierarchy.getCacheRegion().getAccessType() ).isEqualTo( AccessType.READ_WRITE );
		assertThat( hierarchy.getNaturalIdCacheRegion() ).isNotNull();
		assertThat( hierarchy.getNaturalIdCacheRegion().getRegionName() ).isEqualTo( "org.hibernate.testing.natural-id" );

		final EntityTypeMetadata root = hierarchy.getRoot();
		assertThat( root.getClassDetails().getClassName() ).isEqualTo( CacheableEntity.class.getName() );
		assertThat( root.isCacheable() ).isTrue();

		assertThat( root.getSubTypes() ).hasSize( 1 );
		final EntityTypeMetadata sub = (EntityTypeMetadata) root.getSubTypes().iterator().next();
		assertThat( sub.isCacheable() ).isFalse();
	}

	@Entity(name="CacheableEntity")
	@Table(name="CacheableEntity")
	@Cacheable
	@Cache( region = "org.hibernate.testing.entity", usage = READ_WRITE)
	@NaturalIdCache( region = "org.hibernate.testing.natural-id" )
	@Inheritance(strategy = JOINED)
	public static class CacheableEntity {
		@Id
		private Integer id;
		private String name;
	}

	@Entity(name="CacheableEntitySub")
	@Table(name="CacheableEntitySub")
	@Cacheable(false)
	public static class CacheableEntitySub extends CacheableEntity {
		private String someText;
	}
}
