/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.boot.internal.RootMappingDefaults;
import org.hibernate.boot.model.process.spi.ManagedResources;
import org.hibernate.boot.models.xml.spi.PersistenceUnitMetadata;
import org.hibernate.boot.models.xml.spi.XmlPreProcessingResult;
import org.hibernate.boot.models.xml.spi.XmlPreProcessor;
import org.hibernate.boot.models.xml.spi.XmlProcessingResult;
import org.hibernate.boot.models.xml.spi.XmlProcessor;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.MappingDefaults;
import org.hibernate.models.internal.MutableClassDetailsRegistry;
import org.hibernate.models.internal.jdk.JdkBuilders;
import org.hibernate.models.orm.process.internal.DomainModelCategorizationCollector;
import org.hibernate.models.orm.process.internal.ModelCategorizationContextImpl;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.ClassLoading;
import org.hibernate.models.spi.SourceModelBuildingContext;

import jakarta.persistence.SharedCacheMode;

import static org.hibernate.models.orm.process.internal.EntityHierarchyBuilder.createEntityHierarchies;
import static org.hibernate.models.orm.process.spi.ModelCategorizationLogging.MODEL_CATEGORIZATION_LOGGER;

/**
 * Processes a {@linkplain ManagedResources} (classes, mapping, etc.) and
 * produces a {@linkplain CategorizedDomainModel categorized domain model}
 *
 * @author Steve Ebersole
 */
public class ManagedResourcesCategorizer {
	/**
	 * Process the classes, xml, etc. defined by the managed resources and produce
	 * a {@linkplain CategorizedDomainModel categorized model} of the application's domain model
	 *
	 * @param managedResources The classes and mappings of the application's domain model
	 */
	public static CategorizedDomainModel categorizeManagedResources(
			ManagedResources managedResources,
			PersistenceUnitMetadata persistenceUnitMetadata,
			MappingDefaults optionDefaults,
			SharedCacheMode sharedCacheMode,
			BootstrapContext bootstrapContext) {

		final SourceModelBuildingContext modelsContext = bootstrapContext.getModelsContext();

		final XmlPreProcessingResult xmlPreProcessingResult = XmlPreProcessor.preProcessXmlResources(
				managedResources,
				persistenceUnitMetadata
		);

		primeClassDetailsRegistry( managedResources, xmlPreProcessingResult, modelsContext, bootstrapContext );

		final DomainModelCategorizationCollector modelCategorizationCollector = new DomainModelCategorizationCollector(
				modelsContext,
				bootstrapContext
		);

		final RootMappingDefaults rootMappingDefaults = new RootMappingDefaults(
				optionDefaults,
				persistenceUnitMetadata
		);

		// Apply the mapping bits of the XML.
		// The result is a collection of override (aka, non-complete) XML to be processed lastly
		final XmlProcessingResult xmlProcessingResult = XmlProcessor.processXml(
				xmlPreProcessingResult,
				persistenceUnitMetadata,
				modelCategorizationCollector::apply,
				modelsContext,
				bootstrapContext,
				rootMappingDefaults
		);

		// Apply the overrides.
		xmlProcessingResult.apply();

		// With lots of classes this could become unwieldy.
		// The alternative is to keep track of "managed classes" and just use those, but that requires some redesign
		// in XmlProcessor, etc.
		modelsContext.getClassDetailsRegistry().forEachClassDetails( modelCategorizationCollector::apply );


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//	- create entity-hierarchies
		//	- create the CategorizedDomainModel
		//
		// INPUTS:
		//		- rootEntities
		//		- mappedSuperClasses
		//  	- embeddables
		//
		// OUTPUTS:
		//		- CategorizedDomainModel

		final ModelCategorizationContextImpl mappingBuildingContext = new ModelCategorizationContextImpl(
				modelsContext,
				modelCategorizationCollector.getGlobalRegistrations(),
				sharedCacheMode
		);

		// Collect the entity hierarchies based on the set of `rootEntities`
		final Set<EntityHierarchy> entityHierarchies;
		if ( MODEL_CATEGORIZATION_LOGGER.isDebugEnabled() ) {
			final Map<String,ClassDetails> unusedMappedSuperClasses = new HashMap<>( modelCategorizationCollector.getMappedSuperclasses() );
			entityHierarchies = createEntityHierarchies(
					modelCategorizationCollector.getRootEntities(),
					(identifiableType) -> {
						if ( identifiableType instanceof MappedSuperclassTypeMetadata ) {
							unusedMappedSuperClasses.remove( identifiableType.getClassDetails().getClassName() );
						}
					},
					mappingBuildingContext
			);
			warnAboutUnusedMappedSuperclasses( unusedMappedSuperClasses );
		}
		else {
			entityHierarchies = createEntityHierarchies(
					modelCategorizationCollector.getRootEntities(),
					ManagedResourcesCategorizer::ignore,
					mappingBuildingContext
			);
		}

		return modelCategorizationCollector.createResult( entityHierarchies, persistenceUnitMetadata );
	}

	private static void primeClassDetailsRegistry(
			ManagedResources managedResources,
			XmlPreProcessingResult xmlPreProcessingResult,
			SourceModelBuildingContext modelsContext,
			BootstrapContext bootstrapContext) {
		final MutableClassDetailsRegistry classDetailsRegistry = modelsContext.getClassDetailsRegistry().as( MutableClassDetailsRegistry.class );

		managedResources.getAnnotatedClassReferences().forEach( aClass -> {
			classDetailsRegistry.resolveClassDetails(
					aClass.getName(),
					(name) -> JdkBuilders.buildClassDetailsStatic( aClass, modelsContext )
			);
		} );

		managedResources.getAnnotatedPackageNames().forEach( (packageName) -> {
			try {
				final Class<?> packageInfoClass = modelsContext.getClassLoading().classForName( packageName + ".package-info" );
				classDetailsRegistry.resolveClassDetails(
						packageInfoClass.getName(),
						(name) -> JdkBuilders.buildClassDetailsStatic( packageInfoClass, modelsContext )
				);
			}
			catch (ClassLoadingException classLoadingException) {
				// no package-info, so there can be no annotations... just skip it
			}
		} );

		managedResources.getAnnotatedClassNames().forEach( classDetailsRegistry::resolveClassDetails );

		xmlPreProcessingResult.getMappedClasses().forEach( classDetailsRegistry::resolveClassDetails );
	}

	private static void ignore(IdentifiableTypeMetadata identifiableTypeMetadata) {
	}

	private static void warnAboutUnusedMappedSuperclasses(Map<String, ClassDetails> mappedSuperClasses) {
		assert MODEL_CATEGORIZATION_LOGGER.isDebugEnabled();
		for ( Map.Entry<String, ClassDetails> entry : mappedSuperClasses.entrySet() ) {
			MODEL_CATEGORIZATION_LOGGER.debugf(
					"Encountered MappedSuperclass [%s] which was unused in any entity hierarchies",
					entry.getKey()
			);
		}
	}

}
