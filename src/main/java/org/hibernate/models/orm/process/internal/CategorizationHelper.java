/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.internal;

import org.hibernate.models.spi.ClassDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;

/**
 * @author Steve Ebersole
 */
public class CategorizationHelper {
	public static boolean isMappedSuperclass(ClassDetails classDetails) {
		return classDetails.hasDirectAnnotationUsage( MappedSuperclass.class );
	}

	public static boolean isEntity(ClassDetails classDetails) {
		return classDetails.hasDirectAnnotationUsage( Entity.class );
	}

	public static boolean isIdentifiable(ClassDetails classDetails) {
		return isEntity( classDetails ) || isMappedSuperclass( classDetails );
	}
}
