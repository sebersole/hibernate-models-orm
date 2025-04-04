/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.internal;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.OptimisticLocking;
import org.hibernate.annotations.TenantId;
import org.hibernate.boot.models.AttributeNature;
import org.hibernate.models.ModelsException;
import org.hibernate.models.orm.process.spi.AttributeMetadata;
import org.hibernate.models.orm.process.spi.EntityHierarchy;
import org.hibernate.models.orm.process.spi.EntityTypeMetadata;
import org.hibernate.models.orm.process.spi.IdentifiableTypeMetadata;
import org.hibernate.models.orm.process.spi.KeyMapping;
import org.hibernate.models.orm.process.spi.ModelCategorizationContext;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.MemberDetails;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Version;

import static org.hibernate.models.orm.process.spi.ModelCategorizationLogging.MODEL_CATEGORIZATION_LOGGER;


/**
 * Used to collect useful details about a hierarchy as we build its metadata
 *
 * @implNote The HierarchyTypeConsumer is called from root down.  We make use
 * of that detail in a number of places here in the code.
 *
 * @author Steve Ebersole
 */
public class HierarchyMetadataCollector implements HierarchyTypeConsumer {
	private final EntityHierarchy entityHierarchy;
	private final ClassDetails rootEntityClassDetails;
	private final HierarchyTypeConsumer delegateConsumer;
	private final ModelCategorizationContext categorizationContext;

	private boolean belowRootEntity;

	private EntityTypeMetadata rootEntityMetadata;
	private Inheritance inheritanceAnnotation;
	private OptimisticLocking optimisticLockingAnnotation;
	private Cache cacheAnnotation;
	private NaturalIdCache naturalIdCacheAnnotation;

	private KeyMapping idMapping;
	private AttributeMetadata versionAttribute;
	private AttributeMetadata tenantIdAttribute;

	private IdClass idClassAnnotation;
	private Object collectedIdAttributes;
	private Object collectedNaturalIdAttributes;

	public HierarchyMetadataCollector(
			EntityHierarchy entityHierarchy,
			ClassDetails rootEntityClassDetails,
			HierarchyTypeConsumer delegateConsumer,
			ModelCategorizationContext categorizationContext) {
		this.entityHierarchy = entityHierarchy;
		this.rootEntityClassDetails = rootEntityClassDetails;
		this.delegateConsumer = delegateConsumer;
		this.categorizationContext = categorizationContext;
	}

	public EntityTypeMetadata getRootEntityMetadata() {
		return rootEntityMetadata;
	}

	public KeyMapping getIdMapping() {
		if ( idMapping == null ) {
			idMapping = buildIdMapping();
		}

		return idMapping;
	}

	public Inheritance getInheritanceAnnotation() {
		return inheritanceAnnotation;
	}

	public AttributeMetadata getVersionAttribute() {
		return versionAttribute;
	}

	public AttributeMetadata getTenantIdAttribute() {
		return tenantIdAttribute;
	}

	public OptimisticLocking getOptimisticLockingAnnotation() {
		return optimisticLockingAnnotation;
	}

	public Cache getCacheAnnotation() {
		return cacheAnnotation;
	}

	public NaturalIdCache getNaturalIdCacheAnnotation() {
		return naturalIdCacheAnnotation;
	}

	private KeyMapping buildIdMapping() {
		if ( collectedIdAttributes instanceof List ) {
			//noinspection unchecked
			final List<AttributeMetadata> idAttributes = (List<AttributeMetadata>) collectedIdAttributes;
			final ClassDetails idClassDetails;
			if ( idClassAnnotation == null ) {
				idClassDetails = null;
			}
			else {
				final Class<?> idClass = idClassAnnotation.value();
				idClassDetails = categorizationContext.getClassDetailsRegistry().resolveClassDetails( idClass.getName() );
			}
			return new NonAggregatedKeyMappingImpl( idAttributes, idClassDetails );
		}

		final AttributeMetadata idAttribute = (AttributeMetadata) collectedIdAttributes;

		if ( idAttribute.nature() == AttributeNature.BASIC ) {
			return new BasicKeyMappingImpl( idAttribute );
		}

		if ( idAttribute.nature() == AttributeNature.EMBEDDED ) {
			return new AggregatedKeyMappingImpl( idAttribute );
		}

		throw new ModelsException(
				String.format(
						Locale.ROOT,
						"Unexpected attribute nature [%s] - %s",
						idAttribute.nature(),
						entityHierarchy.getRoot().getEntityName()
				)
		);
	}

	public KeyMapping getNaturalIdMapping() {
		if ( collectedNaturalIdAttributes == null ) {
			return null;
		}

		if ( collectedNaturalIdAttributes instanceof List ) {
			//noinspection unchecked
			final List<AttributeMetadata> attributes = (List<AttributeMetadata>) collectedNaturalIdAttributes;
			return new NonAggregatedKeyMappingImpl( attributes, null );
		}

		final AttributeMetadata attribute = (AttributeMetadata) collectedNaturalIdAttributes;

		if ( attribute.nature() == AttributeNature.BASIC ) {
			return new BasicKeyMappingImpl( attribute );
		}

		if ( attribute.nature() == AttributeNature.EMBEDDED ) {
			return new AggregatedKeyMappingImpl( attribute );
		}

		throw new ModelsException(
				String.format(
						Locale.ROOT,
						"Unexpected attribute nature [%s] - %s",
						attribute.nature(),
						entityHierarchy.getRoot().getEntityName()
				)
		);
	}

	@Override
	public void acceptType(IdentifiableTypeMetadata typeMetadata) {
		if ( delegateConsumer != null ) {
			delegateConsumer.acceptType( typeMetadata );
		}

		if ( belowRootEntity ) {
			return;
		}

		final ClassDetails classDetails = typeMetadata.getClassDetails();

		if ( classDetails == rootEntityClassDetails ) {
			rootEntityMetadata = (EntityTypeMetadata) typeMetadata;
			belowRootEntity = true;
		}

		inheritanceAnnotation = applyLocalAnnotation( Inheritance.class, classDetails, inheritanceAnnotation );
		optimisticLockingAnnotation = applyLocalAnnotation( OptimisticLocking.class, classDetails, optimisticLockingAnnotation );
		cacheAnnotation = applyLocalAnnotation( Cache.class, classDetails, cacheAnnotation );
		naturalIdCacheAnnotation = applyLocalAnnotation( NaturalIdCache.class, classDetails, naturalIdCacheAnnotation );
		idClassAnnotation = applyLocalAnnotation( IdClass.class, classDetails, idClassAnnotation );

		final boolean collectIds = collectedIdAttributes == null;
		if ( collectIds || versionAttribute == null || tenantIdAttribute == null ) {
			// walk the attributes
			typeMetadata.forEachAttribute( (index, attributeMetadata) -> {
				final MemberDetails attributeMember = attributeMetadata.member();

				if ( collectIds ) {
					final EmbeddedId eIdAnn = attributeMember.getDirectAnnotationUsage( EmbeddedId.class );
					if ( eIdAnn != null ) {
						collectIdAttribute( attributeMetadata );
					}

					final Id idAnn = attributeMember.getDirectAnnotationUsage( Id.class );
					if ( idAnn != null ) {
						collectIdAttribute( attributeMetadata );
					}
				}

				if ( attributeMember.hasDirectAnnotationUsage( NaturalId.class ) ) {
					collectNaturalIdAttribute( attributeMetadata );
				}

				if ( versionAttribute == null ) {
					if ( attributeMember.hasDirectAnnotationUsage( Version.class ) ) {
						versionAttribute = attributeMetadata;
					}
				}

				if ( tenantIdAttribute == null ) {
					if ( attributeMember.hasDirectAnnotationUsage( TenantId.class ) ) {
						tenantIdAttribute = attributeMetadata;
					}
				}
			} );
		}
	}

	private <A extends Annotation> A applyLocalAnnotation(Class<A> annotationType, ClassDetails classDetails, A currentValue) {
		final A localAnnotation = classDetails.getDirectAnnotationUsage( annotationType );
		if ( localAnnotation != null ) {
			if ( currentValue != null ) {
				MODEL_CATEGORIZATION_LOGGER.debugf(
						"Ignoring @%s from %s in favor of usage from %s",
						annotationType.getSimpleName(),
						classDetails.getName(),
						currentValue.annotationType().getName()
				);
			}

			// the one "closest" to the root-entity should win
			return localAnnotation;
		}

		return currentValue;
	}

	public void collectIdAttribute(AttributeMetadata member) {
		assert member != null;

		if ( collectedIdAttributes == null ) {
			collectedIdAttributes = member;
		}
		else if ( collectedIdAttributes instanceof List ) {
			//noinspection unchecked,rawtypes
			final List<AttributeMetadata> membersList = (List) collectedIdAttributes;
			membersList.add( member );
		}
		else if ( collectedIdAttributes instanceof AttributeMetadata ) {
			final ArrayList<AttributeMetadata> combined = new ArrayList<>();
			combined.add( (AttributeMetadata) collectedIdAttributes );
			combined.add( member );
			collectedIdAttributes = combined;
		}
	}

	public void collectNaturalIdAttribute(AttributeMetadata member) {
		assert member != null;

		if ( collectedNaturalIdAttributes == null ) {
			collectedNaturalIdAttributes = member;
		}
		else if ( collectedNaturalIdAttributes instanceof List ) {
			//noinspection unchecked,rawtypes
			final List<AttributeMetadata> membersList = (List) collectedNaturalIdAttributes;
			membersList.add( member );
		}
		else if ( collectedNaturalIdAttributes instanceof AttributeMetadata ) {
			final ArrayList<AttributeMetadata> combined = new ArrayList<>();
			combined.add( (AttributeMetadata) collectedNaturalIdAttributes );
			combined.add( member );
			collectedNaturalIdAttributes = combined;
		}
	}
}
