/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.spi;

import java.util.Map;
import java.util.Set;

import org.hibernate.boot.models.spi.GlobalRegistrations;
import org.hibernate.internal.util.IndexedConsumer;
import org.hibernate.internal.util.KeyedConsumer;
import org.hibernate.models.spi.ClassDetails;

/**
 * The application's domain model, understood at a very rudimentary level - we know
 * a class is an entity, a mapped-superclass, ...  And we know about persistent attributes,
 * but again on a very rudimentary level.
 * <p/>
 * We also know about all {@linkplain #globalRegistrations() global registrations} -
 * sequence-generators, named-queries, ...
 *
 * @author Steve Ebersole
 */
public record CategorizedDomainModel(
		Set<EntityHierarchy> entityHierarchies,
		Map<String, ClassDetails> mappedSuperclasses,
		Map<String, ClassDetails> embeddables,
		GlobalRegistrations globalRegistrations) {

	/**
	 * Iteration over the {@linkplain #entityHierarchies() entity hierarchies}
	 */
	public void forEachEntityHierarchy(IndexedConsumer<EntityHierarchy> hierarchyConsumer) {
		final Set<EntityHierarchy> entityHierarchies = entityHierarchies();
		if ( entityHierarchies.isEmpty() ) {
			return;
		}

		int pos = 0;
		for ( EntityHierarchy entityHierarchy : entityHierarchies ) {
			hierarchyConsumer.accept( pos, entityHierarchy );
			pos++;
		}
	}

	/**
	 * Iteration over the {@linkplain #mappedSuperclasses() mapped superclasses}
	 */
	public void forEachMappedSuperclass(KeyedConsumer<String, ClassDetails> consumer) {
		final Map<String, ClassDetails> mappedSuperclasses = mappedSuperclasses();
		if ( mappedSuperclasses.isEmpty() ) {
			return;
		}

		mappedSuperclasses.forEach( consumer::accept );
	}

	/**
	 * Iteration over the {@linkplain #embeddables() embeddables}
	 */

	public void forEachEmbeddable(KeyedConsumer<String, ClassDetails> consumer) {
		final Map<String, ClassDetails> embeddables = embeddables();
		if ( embeddables.isEmpty() ) {
			return;
		}

		embeddables.forEach( consumer::accept );
	}
}
