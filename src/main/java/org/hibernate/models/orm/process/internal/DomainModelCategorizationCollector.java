/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.internal;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.boot.jaxb.mapping.spi.JaxbEntityListenerContainerImpl;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEntityMappingsImpl;
import org.hibernate.boot.jaxb.mapping.spi.JaxbPersistenceUnitDefaultsImpl;
import org.hibernate.boot.jaxb.mapping.spi.JaxbPersistenceUnitMetadataImpl;
import org.hibernate.boot.models.internal.GlobalRegistrationsImpl;
import org.hibernate.boot.models.xml.spi.XmlDocumentContext;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.models.orm.process.spi.CategorizedDomainModel;
import org.hibernate.models.orm.process.spi.EntityHierarchy;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.SourceModelBuildingContext;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;

/**
 * In-flight holder for various categorizations of the application's domain model.
 *
 * @see org.hibernate.models.orm.process.spi.ManagedResourcesProcessor#processManagedResources
 *
 * @author Steve Ebersole
 */
public class DomainModelCategorizationCollector {
	private final boolean areIdGeneratorsGlobal;
	private final Set<ClassDetails> rootEntities = new HashSet<>();
	private final Map<String,ClassDetails> mappedSuperclasses = new HashMap<>();
	private final Map<String,ClassDetails> embeddables = new HashMap<>();
	private final GlobalRegistrationsImpl globalRegistrations;

	private final Set<ClassDetails> processedClasses = new TreeSet<>( Comparator.comparing( ClassDetails::getName ) );

	public DomainModelCategorizationCollector(
			boolean areIdGeneratorsGlobal,
			SourceModelBuildingContext modelsContext,
			BootstrapContext bootstrapContext) {
		this.areIdGeneratorsGlobal = areIdGeneratorsGlobal;
		this.globalRegistrations = new GlobalRegistrationsImpl( modelsContext, bootstrapContext );
	}

	public Set<ClassDetails> getRootEntities() {
		return rootEntities;
	}

	public Map<String, ClassDetails> getMappedSuperclasses() {
		return mappedSuperclasses;
	}

	public Map<String, ClassDetails> getEmbeddables() {
		return embeddables;
	}

	public GlobalRegistrationsImpl getGlobalRegistrations() {
		return globalRegistrations;
	}


	public void apply(JaxbEntityMappingsImpl jaxbRoot, XmlDocumentContext xmlDocumentContext) {
		getGlobalRegistrations().collectJavaTypeRegistrations( jaxbRoot.getJavaTypeRegistrations() );
		getGlobalRegistrations().collectJdbcTypeRegistrations( jaxbRoot.getJdbcTypeRegistrations() );
		getGlobalRegistrations().collectConverterRegistrations( jaxbRoot.getConverterRegistrations() );
		getGlobalRegistrations().collectUserTypeRegistrations( jaxbRoot.getUserTypeRegistrations() );
		getGlobalRegistrations().collectCompositeUserTypeRegistrations( jaxbRoot.getCompositeUserTypeRegistrations() );
		getGlobalRegistrations().collectCollectionTypeRegistrations( jaxbRoot.getCollectionUserTypeRegistrations() );
		getGlobalRegistrations().collectEmbeddableInstantiatorRegistrations( jaxbRoot.getEmbeddableInstantiatorRegistrations() );
		getGlobalRegistrations().collectFilterDefinitions( jaxbRoot.getFilterDefinitions() );

		final JaxbPersistenceUnitMetadataImpl persistenceUnitMetadata = jaxbRoot.getPersistenceUnitMetadata();
		if ( persistenceUnitMetadata != null ) {
			final JaxbPersistenceUnitDefaultsImpl persistenceUnitDefaults = persistenceUnitMetadata.getPersistenceUnitDefaults();
			final JaxbEntityListenerContainerImpl listenerContainer = persistenceUnitDefaults.getEntityListenerContainer();
			if ( listenerContainer != null ) {
				getGlobalRegistrations().collectEntityListenerRegistrations(
						listenerContainer.getEntityListeners(),
						xmlDocumentContext.getModelBuildingContext()
				);
			}
		}

		getGlobalRegistrations().collectIdGenerators( jaxbRoot );

		// todo : named queries
		// todo : named graphs
	}

	public void apply(ClassDetails classDetails) {
		final boolean alreadyProcessed = !processedClasses.add( classDetails );
		if ( alreadyProcessed ) {
			return;
		}

		getGlobalRegistrations().collectJavaTypeRegistrations( classDetails );
		getGlobalRegistrations().collectJdbcTypeRegistrations( classDetails );
		getGlobalRegistrations().collectConverterRegistrations( classDetails );
		getGlobalRegistrations().collectUserTypeRegistrations( classDetails );
		getGlobalRegistrations().collectCompositeUserTypeRegistrations( classDetails );
		getGlobalRegistrations().collectCollectionTypeRegistrations( classDetails );
		getGlobalRegistrations().collectEmbeddableInstantiatorRegistrations( classDetails );
		getGlobalRegistrations().collectFilterDefinitions( classDetails );

		if ( areIdGeneratorsGlobal ) {
			getGlobalRegistrations().collectIdGenerators( classDetails );
		}

		// todo : named queries
		// todo : named graphs

		if ( classDetails.hasDirectAnnotationUsage( MappedSuperclass.class ) ) {
			if ( classDetails.getClassName() != null ) {
				mappedSuperclasses.put( classDetails.getClassName(), classDetails );
			}
		}
		else if ( classDetails.hasDirectAnnotationUsage( Entity.class ) ) {
			if ( EntityHierarchyBuilder.isRoot( classDetails ) ) {
				rootEntities.add( classDetails );
			}
		}
		else if ( classDetails.hasDirectAnnotationUsage( Embeddable.class ) ) {
			if ( classDetails.getClassName() != null ) {
				embeddables.put( classDetails.getClassName(), classDetails );
			}
		}

		// todo : converters?  - @Converter / AttributeConverter, as opposed to @ConverterRegistration which is already collected
	}

	/**
	 * Builder for {@linkplain CategorizedDomainModel} based on our internal state plus
	 * the incoming set of managed types.
	 *
	 * @param entityHierarchies All entity hierarchies defined in the persistence-unit, built based
	 * on {@linkplain #getRootEntities()}
	 */
	public CategorizedDomainModel createResult(Set<EntityHierarchy> entityHierarchies) {
		return new CategorizedDomainModel(
				entityHierarchies,
				mappedSuperclasses,
				embeddables,
				getGlobalRegistrations()
		);
	}
}
