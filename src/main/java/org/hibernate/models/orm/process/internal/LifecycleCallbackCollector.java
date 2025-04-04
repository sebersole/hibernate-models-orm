/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.internal;

import java.lang.annotation.Annotation;
import java.util.Locale;

import org.hibernate.boot.models.JpaEventListenerStyle;
import org.hibernate.boot.models.spi.JpaEventListener;
import org.hibernate.models.ModelsException;
import org.hibernate.models.orm.process.spi.AllMemberConsumer;
import org.hibernate.models.orm.process.spi.ModelCategorizationContext;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.MemberDetails;
import org.hibernate.models.spi.MethodDetails;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

import static org.hibernate.boot.models.spi.JpaEventListener.matchesSignature;

/**
 * AllMemberConsumer implementation for collecting method-details for
 * JPA callback methods.
 *
 * @author Steve Ebersole
 */
public class LifecycleCallbackCollector implements AllMemberConsumer {
	private final ClassDetails managedTypeDetails;

	private MethodDetails prePersist;
	private MethodDetails postPersist;
	private MethodDetails preUpdate;
	private MethodDetails postUpdate;
	private MethodDetails preRemove;
	private MethodDetails postRemove;
	private MethodDetails postLoad;

	public LifecycleCallbackCollector(ClassDetails managedTypeDetails) {
		this.managedTypeDetails = managedTypeDetails;
	}

	@Override
	public void acceptMember(MemberDetails memberDetails) {
		if ( memberDetails.isField() ) {
			return;
		}

		final MethodDetails methodDetails = (MethodDetails) memberDetails;

		if ( methodDetails.hasDirectAnnotationUsage( PrePersist.class )
				&& matchesSignature( JpaEventListenerStyle.CALLBACK, methodDetails ) ) {
			prePersist = apply( methodDetails, PrePersist.class, managedTypeDetails, prePersist );
		}
		else if ( methodDetails.hasDirectAnnotationUsage( PostPersist.class )
				&& matchesSignature( JpaEventListenerStyle.CALLBACK, methodDetails ) ) {
			postPersist = apply( methodDetails, PostPersist.class, managedTypeDetails, postPersist );
		}
		else if ( methodDetails.hasDirectAnnotationUsage( PreRemove.class )
				&& matchesSignature( JpaEventListenerStyle.CALLBACK, methodDetails ) ) {
			preRemove = apply( methodDetails, PreRemove.class, managedTypeDetails, preRemove );
		}
		else if ( methodDetails.hasDirectAnnotationUsage( PostRemove.class )
				&& matchesSignature( JpaEventListenerStyle.CALLBACK, methodDetails ) ) {
			postRemove = apply( methodDetails, PostRemove.class, managedTypeDetails, postRemove );
		}
		else if ( methodDetails.hasDirectAnnotationUsage( PreUpdate.class )
				&& matchesSignature( JpaEventListenerStyle.CALLBACK, methodDetails ) ) {
			preUpdate = apply( methodDetails, PreUpdate.class, managedTypeDetails, preUpdate );
		}
		else if ( methodDetails.hasDirectAnnotationUsage( PostUpdate.class )
				&& matchesSignature( JpaEventListenerStyle.CALLBACK, methodDetails ) ) {
			postUpdate = apply( methodDetails, PostUpdate.class, managedTypeDetails, postUpdate );
		}
		else if ( methodDetails.hasDirectAnnotationUsage( PostLoad.class )
				&& matchesSignature( JpaEventListenerStyle.CALLBACK, methodDetails ) ) {
			postLoad = apply( methodDetails, PostLoad.class, managedTypeDetails, postLoad );
		}
	}

	private static <A extends Annotation> MethodDetails apply(
			MethodDetails incomingValue,
			Class<A> annotationType,
			ClassDetails managedTypeDetails,
			MethodDetails currentValue) {
		if ( currentValue != null ) {
			throw new ModelsException(
					String.format(
							Locale.ROOT,
							"Encountered multiple @%s methods [%s] - %s, %s",
							annotationType.getSimpleName(),
							managedTypeDetails.getClassName(),
							currentValue.getName(),
							incomingValue.getName()
					)
			);
		}
		return incomingValue;
	}

	public JpaEventListener resolve() {
		if ( prePersist != null
				|| postPersist != null
				|| preUpdate != null
				|| postUpdate != null
				|| preRemove != null
				|| postRemove != null
				|| postLoad != null ) {
			return new JpaEventListener(
					JpaEventListenerStyle.CALLBACK,
					managedTypeDetails,
					prePersist,
					postPersist,
					preRemove,
					postRemove,
					preUpdate,
					postUpdate,
					postLoad
			);
		}
		return null;
	}
}
