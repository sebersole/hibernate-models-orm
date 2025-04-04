/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.internal;

import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.annotations.SQLUpdate;
import org.hibernate.annotations.Synchronize;
import org.hibernate.boot.model.naming.EntityNaming;
import org.hibernate.boot.models.JpaAnnotations;
import org.hibernate.boot.models.spi.JpaEventListener;
import org.hibernate.models.orm.process.spi.AttributeMetadata;
import org.hibernate.models.orm.process.spi.CustomSql;
import org.hibernate.models.orm.process.spi.EntityHierarchy;
import org.hibernate.models.orm.process.spi.EntityTypeMetadata;
import org.hibernate.models.orm.process.spi.ModelCategorizationContext;
import org.hibernate.models.spi.ClassDetails;

import jakarta.persistence.AccessType;
import jakarta.persistence.Cacheable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import static org.hibernate.internal.util.StringHelper.EMPTY_STRINGS;
import static org.hibernate.internal.util.StringHelper.isNotEmpty;
import static org.hibernate.internal.util.StringHelper.unqualify;

/**
 * @author Steve Ebersole
 */
public class EntityTypeMetadataImpl
		extends AbstractIdentifiableTypeMetadata
		implements EntityTypeMetadata, EntityNaming {
	private final String entityName;
	private final String jpaEntityName;

	private final List<AttributeMetadata> attributeList;

	private final boolean mutable;
	private final boolean cacheable;
	private final boolean isLazy;
	private final String proxy;
	private final int batchSize;
	private final String discriminatorMatchValue;
	private final boolean isDynamicInsert;
	private final boolean isDynamicUpdate;
	private final CustomSql customInsert;
	private final CustomSql customUpdate;
	private final CustomSql customDelete;
	private final String[] synchronizedTableNames;

	private List<JpaEventListener> hierarchyEventListeners;
	private List<JpaEventListener> completeEventListeners;

	public EntityTypeMetadataImpl(
			ClassDetails classDetails,
			EntityHierarchy hierarchy,
			AccessType defaultAccessType,
			HierarchyTypeConsumer typeConsumer,
			ModelCategorizationContext categorizationContext) {
		super( classDetails, hierarchy, defaultAccessType, categorizationContext );

		// NOTE: There is no annotation for `entity-name` - it comes exclusively from XML
		// 		mappings.  By default, the `entityName` is simply the entity class name.
		// 		`ClassDetails#getName` already handles this all for us
		this.entityName = getClassDetails().getName();

		final Entity entityAnnotation = classDetails.getDirectAnnotationUsage( Entity.class );
		this.jpaEntityName = determineJpaEntityName( entityAnnotation, entityName );

		final LifecycleCallbackCollector lifecycleCallbackCollector = new LifecycleCallbackCollector( classDetails );
		this.attributeList = resolveAttributes( lifecycleCallbackCollector );
		this.hierarchyEventListeners = collectHierarchyEventListeners( lifecycleCallbackCollector.resolve() );
		this.completeEventListeners = collectCompleteEventListeners( categorizationContext );

		this.mutable = determineMutability( classDetails, categorizationContext );
		this.cacheable = determineCacheability( classDetails, categorizationContext );
		this.synchronizedTableNames = determineSynchronizedTableNames( categorizationContext );
		this.batchSize = determineBatchSize( categorizationContext );
		this.isDynamicInsert = decodeDynamicInsert( categorizationContext );
		this.isDynamicUpdate = decodeDynamicUpdate( categorizationContext );

		this.customInsert = CustomSql.from( classDetails.getAnnotationUsage( SQLInsert.class, categorizationContext.getModelsContext() ) );
		this.customUpdate = CustomSql.from( classDetails.getAnnotationUsage( SQLUpdate.class, categorizationContext.getModelsContext() ) );
		this.customDelete = CustomSql.from( classDetails.getAnnotationUsage( SQLDelete.class, categorizationContext.getModelsContext() ) );


		// defaults are that it is lazy and that the class itself is the proxy class
		this.isLazy = true;
		this.proxy = getEntityName();

		final DiscriminatorValue discriminatorValueAnn = classDetails.getDirectAnnotationUsage( DiscriminatorValue.class );
		if ( discriminatorValueAnn != null ) {
			this.discriminatorMatchValue = discriminatorValueAnn.value();
		}
		else {
			this.discriminatorMatchValue = null;
		}

		postInstantiate( typeConsumer );
	}

	public EntityTypeMetadataImpl(
			ClassDetails classDetails,
			EntityHierarchy hierarchy,
			AbstractIdentifiableTypeMetadata superType,
			HierarchyTypeConsumer typeConsumer,
			ModelCategorizationContext categorizationContext) {
		super( classDetails, hierarchy, superType, categorizationContext );

		// NOTE: There is no annotation for `entity-name` - it comes exclusively from XML
		// 		mappings.  By default, the `entityName` is simply the entity class name.
		// 		`ClassDetails#getName` already handles this all for us
		this.entityName = getClassDetails().getName();

		final Entity entityAnnotation = classDetails.getDirectAnnotationUsage( JpaAnnotations.ENTITY );
		this.jpaEntityName = determineJpaEntityName( entityAnnotation, entityName );

		final LifecycleCallbackCollector lifecycleCallbackCollector = new LifecycleCallbackCollector( classDetails );
		this.attributeList = resolveAttributes( lifecycleCallbackCollector );
		this.hierarchyEventListeners = collectHierarchyEventListeners( lifecycleCallbackCollector.resolve() );
		this.completeEventListeners = collectCompleteEventListeners( categorizationContext );

		this.mutable = determineMutability( classDetails, categorizationContext );
		this.cacheable = determineCacheability( classDetails, categorizationContext );
		this.synchronizedTableNames = determineSynchronizedTableNames( categorizationContext );
		this.batchSize = determineBatchSize( categorizationContext );
		this.isDynamicInsert = decodeDynamicInsert( categorizationContext );
		this.isDynamicUpdate = decodeDynamicUpdate( categorizationContext );

		this.customInsert = CustomSql.from( classDetails.getAnnotationUsage( SQLInsert.class, categorizationContext.getModelsContext() ) );
		this.customUpdate = CustomSql.from( classDetails.getAnnotationUsage( SQLUpdate.class, categorizationContext.getModelsContext() ) );
		this.customDelete = CustomSql.from( classDetails.getAnnotationUsage( SQLDelete.class, categorizationContext.getModelsContext() ) );

		this.isLazy = true;
		this.proxy = getEntityName();

		final DiscriminatorValue discriminatorValueAnn = classDetails.getDirectAnnotationUsage( DiscriminatorValue.class );
		if ( discriminatorValueAnn != null ) {
			this.discriminatorMatchValue = discriminatorValueAnn.value();
		}
		else {
			this.discriminatorMatchValue = null;
		}

		postInstantiate( typeConsumer );
	}

	@Override
	protected List<AttributeMetadata> attributeList() {
		return attributeList;
	}

	@Override
	public String getEntityName() {
		return entityName;
	}

	@Override
	public String getJpaEntityName() {
		return jpaEntityName;
	}

	@Override
	public String getClassName() {
		return getClassDetails().getClassName();
	}

	@Override
	public boolean isMutable() {
		return mutable;
	}

	@Override
	public boolean isCacheable() {
		return cacheable;
	}

	@Override
	public String[] getSynchronizedTableNames() {
		return synchronizedTableNames;
	}

	@Override
	public int getBatchSize() {
		return batchSize;
	}

	@Override
	public boolean isDynamicInsert() {
		return isDynamicInsert;
	}

	@Override
	public boolean isDynamicUpdate() {
		return isDynamicUpdate;
	}

	@Override
	public CustomSql getCustomInsert() {
		return customInsert;
	}

	@Override
	public CustomSql getCustomUpdate() {
		return customUpdate;
	}

	@Override
	public CustomSql getCustomDelete() {
		return customDelete;
	}

	public String getDiscriminatorMatchValue() {
		return discriminatorMatchValue;
	}

	public boolean isLazy() {
		return isLazy;
	}

	public String getProxy() {
		return proxy;
	}

	@Override
	public List<JpaEventListener> getHierarchyJpaEventListeners() {
		return hierarchyEventListeners;
	}

	@Override
	public List<JpaEventListener> getCompleteJpaEventListeners() {
		return completeEventListeners;
	}


	private String determineJpaEntityName(Entity entityAnnotation, String entityName) {
		final String name = entityAnnotation.name();
		if ( isNotEmpty( name ) ) {
			return name;
		}
		return unqualify( entityName );
	}

	private boolean determineMutability(ClassDetails classDetails, ModelCategorizationContext categorizationContext) {
		final Immutable immutableAnn = classDetails.getAnnotationUsage( Immutable.class, categorizationContext.getModelsContext() );
		return immutableAnn == null;
	}

	private boolean determineCacheability(
			ClassDetails classDetails,
			ModelCategorizationContext categorizationContext) {
		final Cacheable cacheableAnn = classDetails.getAnnotationUsage( Cacheable.class, categorizationContext.getModelsContext() );
		switch ( categorizationContext.getSharedCacheMode() ) {
			case NONE: {
				return false;
			}
			case ALL: {
				return true;
			}
			case DISABLE_SELECTIVE: {
				// Disable caching for all `@Cacheable(false)`, enabled otherwise (including no annotation)
				//noinspection RedundantIfStatement
				if ( cacheableAnn == null || cacheableAnn.value() ) {
					// not disabled
					return true;
				}
				else {
					// disable, there was an explicit `@Cacheable(false)`
					return false;
				}
			}
			default: {
				// ENABLE_SELECTIVE
				// UNSPECIFIED

				// Enable caching for all `@Cacheable(true)`, disable otherwise (including no annotation)
				//noinspection RedundantIfStatement
				if ( cacheableAnn != null && cacheableAnn.value() ) {
					// enable, there was an explicit `@Cacheable(true)`
					return true;
				}
				else {
					return false;
				}
			}
		}
	}

	private String[] determineSynchronizedTableNames(ModelCategorizationContext categorizationContext) {
		final Synchronize synchronizeAnnotation = getClassDetails().getAnnotationUsage( Synchronize.class, categorizationContext.getModelsContext() );
		if ( synchronizeAnnotation != null ) {
			return synchronizeAnnotation.value();
		}
		return EMPTY_STRINGS;
	}

	private int determineBatchSize(ModelCategorizationContext categorizationContext) {
		final BatchSize batchSizeAnnotation = getClassDetails().getAnnotationUsage(
				BatchSize.class,
				categorizationContext.getModelsContext()
		);
		if ( batchSizeAnnotation != null ) {
			return batchSizeAnnotation.size();
		}
		return -1;
	}

	private boolean decodeDynamicInsert(ModelCategorizationContext categorizationContext) {
		return getClassDetails().hasAnnotationUsage(
				DynamicInsert.class,
				categorizationContext.getModelsContext()
		);
	}

	private boolean decodeDynamicUpdate(ModelCategorizationContext categorizationContext) {
		return getClassDetails().hasAnnotationUsage(
				DynamicUpdate.class,
				categorizationContext.getModelsContext()
		);
	}
}
