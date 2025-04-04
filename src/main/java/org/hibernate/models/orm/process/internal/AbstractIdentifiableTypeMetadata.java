/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.hibernate.boot.models.JpaEventListenerStyle;
import org.hibernate.boot.models.spi.JpaEventListener;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.models.orm.process.spi.EntityHierarchy;
import org.hibernate.models.orm.process.spi.IdentifiableTypeMetadata;
import org.hibernate.models.orm.process.spi.ModelCategorizationContext;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.ClassDetailsRegistry;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.ExcludeDefaultListeners;
import jakarta.persistence.ExcludeSuperclassListeners;


/**
 * @author Steve Ebersole
 */
public abstract class AbstractIdentifiableTypeMetadata
		extends AbstractManagedTypeMetadata
		implements IdentifiableTypeMetadata {
	private final EntityHierarchy hierarchy;
	private final AbstractIdentifiableTypeMetadata superType;
	private final Set<IdentifiableTypeMetadata> subTypes = new HashSet<>();
	private final AccessType accessType;

	/**
	 * Used when creating the hierarchy root-root
	 *
	 * @param accessType This is the hierarchy default
	 */
	public AbstractIdentifiableTypeMetadata(
			ClassDetails classDetails,
			EntityHierarchy hierarchy,
			AccessType accessType,
			ModelCategorizationContext categorizationContext) {
		super( classDetails, categorizationContext );

		this.hierarchy = hierarchy;
		this.superType = null;

		this.accessType = determineAccessType( accessType );
	}


	public AbstractIdentifiableTypeMetadata(
			ClassDetails classDetails,
			EntityHierarchy hierarchy,
			AbstractIdentifiableTypeMetadata superType,
			ModelCategorizationContext processingContext) {
		super( classDetails, processingContext );

		assert superType != null;

		this.hierarchy = hierarchy;
		this.superType = superType;

		this.accessType = determineAccessType( superType.getAccessType() );
	}

	protected void postInstantiate(HierarchyTypeConsumer typeConsumer) {
		typeConsumer.acceptType( this );

		// now we can effectively walk subs
		walkSubclasses( typeConsumer );

		// the idea here is to collect up class-level annotations and to apply
		// the maps from supers
		collectConversionInfo();
		collectAttributeOverrides();
		collectAssociationOverrides();
	}

	private void walkSubclasses(HierarchyTypeConsumer typeConsumer) {
		walkSubclasses( getClassDetails(), typeConsumer );
	}

	private void walkSubclasses(ClassDetails base, HierarchyTypeConsumer typeConsumer) {
		final ClassDetailsRegistry classDetailsRegistry = getCategorizationContext().getClassDetailsRegistry();
		classDetailsRegistry.forEachDirectSubType( base.getName(), (subClassDetails) -> {
			final AbstractIdentifiableTypeMetadata subTypeMetadata;
			if ( CategorizationHelper.isEntity( subClassDetails ) ) {
				subTypeMetadata = new EntityTypeMetadataImpl(
						subClassDetails,
						getHierarchy(),
						this,
						typeConsumer,
						getCategorizationContext()
				);
				addSubclass( subTypeMetadata );
			}
			else if ( CategorizationHelper.isMappedSuperclass( subClassDetails ) ) {
				subTypeMetadata = new MappedSuperclassTypeMetadataImpl(
						subClassDetails,
						getHierarchy(),
						this,
						typeConsumer,
						getCategorizationContext()
				);
				addSubclass( subTypeMetadata );
			}
			else {
				// skip over "intermediate" sub-types
				walkSubclasses( subClassDetails, typeConsumer );
			}
		} );

	}

	private AccessType determineAccessType(AccessType defaultAccessType) {
		final Access annotation = getClassDetails().getDirectAnnotationUsage( Access.class );
		if ( annotation != null ) {
			return annotation.value();
		}

		return defaultAccessType;
	}

	private void addSubclass(IdentifiableTypeMetadata subclass) {
		subTypes.add( subclass );
	}

	@Override
	public EntityHierarchy getHierarchy() {
		return hierarchy;
	}

	@Override
	public IdentifiableTypeMetadata getSuperType() {
		return superType;
	}

	@Override
	public boolean isAbstract() {
		return getClassDetails().isAbstract();
	}

	@Override
	public boolean hasSubTypes() {
		// assume this is called only after its constructor is complete
		return !subTypes.isEmpty();
	}

	@Override
	public int getNumberOfSubTypes() {
		return subTypes.size();
	}

	@Override
	public void forEachSubType(Consumer<IdentifiableTypeMetadata> consumer) {
		// assume this is called only after its constructor is complete
		subTypes.forEach( consumer );
	}

	@Override
	public Iterable<IdentifiableTypeMetadata> getSubTypes() {
		// assume this is called only after its constructor is complete
		return subTypes;
	}

	@Override
	public AccessType getAccessType() {
		return accessType;
	}

	protected void collectConversionInfo() {
		// we only need to do this on root
	}

	protected void collectAttributeOverrides() {
		// we only need to do this on root
	}

	protected void collectAssociationOverrides() {
		// we only need to do this on root
	}

	protected List<JpaEventListener> collectHierarchyEventListeners(JpaEventListener localCallback) {
		final ClassDetails classDetails = getClassDetails();

		final List<JpaEventListener> combined = new ArrayList<>();

		if ( !classDetails.hasDirectAnnotationUsage( ExcludeSuperclassListeners.class ) ) {
			final IdentifiableTypeMetadata superType = getSuperType();
			if ( superType != null ) {
				combined.addAll( superType.getHierarchyJpaEventListeners() );
			}
		}

		applyLocalEventListeners( combined::add );

		if ( localCallback != null ) {
			combined.add( localCallback );
		}

		return combined;
	}

	private void applyLocalEventListeners(Consumer<JpaEventListener> consumer) {
		final ClassDetails classDetails = getClassDetails();

		final EntityListeners entityListenersAnnotation = classDetails.getDirectAnnotationUsage( EntityListeners.class );
		if ( entityListenersAnnotation == null ) {
			return;
		}

		final Class<?>[] listenerClasses = entityListenersAnnotation.value();
		if ( CollectionHelper.isEmpty( listenerClasses ) ) {
			return;
		}

		final ClassDetailsRegistry classDetailsRegistry = getCategorizationContext().getClassDetailsRegistry();
		for ( Class<?> listenerClass : listenerClasses ) {
			final ClassDetails listenerClassDetails = classDetailsRegistry.resolveClassDetails( listenerClass.getName() );
			consumer.accept( JpaEventListener.from( JpaEventListenerStyle.LISTENER, listenerClassDetails ) );
		}
	}

	protected List<JpaEventListener> collectCompleteEventListeners(ModelCategorizationContext modelContext) {
		final ClassDetails classDetails = getClassDetails();
		if ( classDetails.hasDirectAnnotationUsage( ExcludeDefaultListeners.class ) ) {
			return getHierarchyJpaEventListeners();
		}

		final List<JpaEventListener> combined = new ArrayList<>();
		combined.addAll( modelContext.getDefaultEventListeners() );
		combined.addAll( getHierarchyJpaEventListeners() );
		return combined;
	}
}
