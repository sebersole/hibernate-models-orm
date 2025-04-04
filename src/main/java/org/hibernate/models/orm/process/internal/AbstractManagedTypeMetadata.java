/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.process.internal;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyDiscriminator;
import org.hibernate.annotations.AnyDiscriminatorValue;
import org.hibernate.annotations.AnyDiscriminatorValues;
import org.hibernate.annotations.AnyKeyJavaClass;
import org.hibernate.annotations.AnyKeyJavaType;
import org.hibernate.annotations.AnyKeyJdbcType;
import org.hibernate.annotations.AnyKeyJdbcTypeCode;
import org.hibernate.annotations.CompositeType;
import org.hibernate.annotations.EmbeddableInstantiator;
import org.hibernate.annotations.EmbeddedColumnNaming;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JavaType;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.TimeZoneColumn;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.Type;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.boot.models.AttributeNature;
import org.hibernate.boot.models.HibernateAnnotations;
import org.hibernate.boot.models.JpaAnnotations;
import org.hibernate.boot.models.MultipleAttributeNaturesException;
import org.hibernate.internal.util.IndexedConsumer;
import org.hibernate.models.orm.process.spi.AllMemberConsumer;
import org.hibernate.models.orm.process.spi.AttributeMetadata;
import org.hibernate.models.orm.process.spi.ManagedTypeMetadata;
import org.hibernate.models.orm.process.spi.ModelCategorizationContext;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.MemberDetails;

import jakarta.persistence.Basic;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.Version;

import static org.hibernate.internal.util.collections.CollectionHelper.arrayList;
import static org.hibernate.models.orm.process.spi.ModelCategorizationLogging.MODEL_CATEGORIZATION_LOGGER;

/**
 * Models metadata about a JPA {@linkplain jakarta.persistence.metamodel.ManagedType managed-type}.
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 * @author Brett Meyer
 */
public abstract class AbstractManagedTypeMetadata implements ManagedTypeMetadata {
	private final ClassDetails classDetails;
	private final ModelCategorizationContext categorizationContext;

	private final AttributePath attributePathBase;
	private final AttributeRole attributeRoleBase;

	/**
	 * This form is intended for construction of the root of an entity hierarchy
	 * and its mapped-superclasses
	 */
	public AbstractManagedTypeMetadata(ClassDetails classDetails, ModelCategorizationContext categorizationContext) {
		this.classDetails = classDetails;
		this.categorizationContext = categorizationContext;
		this.attributeRoleBase = new AttributeRole( classDetails.getName() );
		this.attributePathBase = new AttributePath();
	}

	/**
	 * This form is used to create Embedded references
	 *
	 * @param classDetails The Embeddable descriptor
	 * @param attributeRoleBase The base for the roles of attributes created *from* here
	 * @param attributePathBase The base for the paths of attributes created *from* here
	 */
	public AbstractManagedTypeMetadata(
			ClassDetails classDetails,
			AttributeRole attributeRoleBase,
			AttributePath attributePathBase,
			ModelCategorizationContext categorizationContext) {
		this.classDetails = classDetails;
		this.categorizationContext = categorizationContext;
		this.attributeRoleBase = attributeRoleBase;
		this.attributePathBase = attributePathBase;
	}

	public ClassDetails getClassDetails() {
		return classDetails;
	}

	public ModelCategorizationContext getCategorizationContext() {
		return categorizationContext;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		AbstractManagedTypeMetadata that = (AbstractManagedTypeMetadata) o;
		return Objects.equals( classDetails.getName(), that.classDetails.getName() );
	}

	@Override
	public int hashCode() {
		return Objects.hash( classDetails );
	}

	@Override
	public String toString() {
		return "ManagedTypeMetadata(" + classDetails.getName() + ")";
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// attribute handling

	protected abstract List<AttributeMetadata> attributeList();

	@Override
	public int getNumberOfAttributes() {
		return attributeList().size();
	}

	@Override
	public Collection<AttributeMetadata> getAttributes() {
		return attributeList();
	}

	@Override
	public AttributeMetadata findAttribute(String name) {
		final List<AttributeMetadata> attributeList = attributeList();
		for ( int i = 0; i < attributeList.size(); i++ ) {
			final AttributeMetadata attribute = attributeList.get( i );
			if ( attribute.name().equals( name ) ) {
				return attribute;
			}
		}
		return null;
	}

	@Override
	public void forEachAttribute(IndexedConsumer<AttributeMetadata> consumer) {
		for ( int i = 0; i < attributeList().size(); i++ ) {
			consumer.accept( i, attributeList().get( i ) );
		}
	}

	protected List<AttributeMetadata> resolveAttributes(AllMemberConsumer memberConsumer) {
		final List<MemberDetails> backingMembers = getCategorizationContext()
				.getPersistentAttributeMemberResolver()
				.resolveAttributesMembers( classDetails, getAccessType(), memberConsumer, categorizationContext );

		final List<AttributeMetadata> attributeList = arrayList( backingMembers.size() );

		for ( MemberDetails backingMember : backingMembers ) {
			final AttributeMetadata attribute = new AttributeMetadataImpl(
					backingMember.resolveAttributeName(),
					determineAttributeNature( backingMember ),
					backingMember
			);
			attributeList.add( attribute );
		}

		return attributeList;
	}

	/**
	 * Determine the attribute's nature - is it a basic mapping, an embeddable, ...?
	 *
	 * Also performs some simple validation around multiple natures being indicated
	 */
	private AttributeNature determineAttributeNature(MemberDetails backingMember) {
		final EnumSet<AttributeNature> natures = EnumSet.noneOf( AttributeNature.class );

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// first, look for explicit nature annotations

		final Any any = backingMember.getDirectAnnotationUsage( HibernateAnnotations.ANY );
		final Basic basic = backingMember.getDirectAnnotationUsage( JpaAnnotations.BASIC );
		final ElementCollection elementCollection = backingMember.getDirectAnnotationUsage( JpaAnnotations.ELEMENT_COLLECTION );
		final Embedded embedded = backingMember.getDirectAnnotationUsage( JpaAnnotations.EMBEDDED );
		final EmbeddedId embeddedId = backingMember.getDirectAnnotationUsage( JpaAnnotations.EMBEDDED_ID );
		final ManyToAny manyToAny = backingMember.getDirectAnnotationUsage( HibernateAnnotations.MANY_TO_ANY );
		final ManyToMany manyToMany = backingMember.getDirectAnnotationUsage( JpaAnnotations.MANY_TO_MANY );
		final ManyToOne manyToOne = backingMember.getDirectAnnotationUsage( JpaAnnotations.MANY_TO_ONE );
		final OneToMany oneToMany = backingMember.getDirectAnnotationUsage( JpaAnnotations.ONE_TO_MANY );
		final OneToOne oneToOne = backingMember.getDirectAnnotationUsage( JpaAnnotations.ONE_TO_ONE );

		if ( basic != null ) {
			natures.add( AttributeNature.BASIC );
		}

		if ( embedded != null
				|| embeddedId != null
				|| ( backingMember.getType() != null && backingMember.getType().determineRawClass().hasDirectAnnotationUsage( Embeddable.class ) ) ) {
			natures.add( AttributeNature.EMBEDDED );
		}

		if ( any != null ) {
			natures.add( AttributeNature.ANY );
		}

		if ( oneToOne != null
				|| manyToOne != null ) {
			natures.add( AttributeNature.TO_ONE );
		}

		if ( oneToMany != null ) {
			natures.add( AttributeNature.ONE_TO_MANY );
		}

		if ( manyToMany != null ) {
			natures.add( AttributeNature.MANY_TO_MANY );
		}

		if ( elementCollection != null ) {
			natures.add( AttributeNature.ELEMENT_COLLECTION );
		}

		if ( manyToAny != null ) {
			natures.add( AttributeNature.MANY_TO_ANY );
		}

		final boolean plural = oneToMany != null
				|| manyToMany != null
				|| elementCollection != null
				|| manyToAny != null;

		// look at annotations that imply a nature
		//		NOTE : these could apply to the element or index of collection, so
		//		only do these if it is not a collection

		if ( !plural ) {
			// first implicit basic nature
			if ( backingMember.hasDirectAnnotationUsage( Temporal.class )
					|| backingMember.hasDirectAnnotationUsage( Lob.class )
					|| backingMember.hasDirectAnnotationUsage( Enumerated.class )
					|| backingMember.hasDirectAnnotationUsage( Convert.class )
					|| backingMember.hasDirectAnnotationUsage( Version.class )
					|| backingMember.hasDirectAnnotationUsage( Generated.class )
					|| backingMember.hasDirectAnnotationUsage( Nationalized.class )
					|| backingMember.hasDirectAnnotationUsage( TimeZoneColumn.class )
					|| backingMember.hasDirectAnnotationUsage( TimeZoneStorage.class )
					|| backingMember.hasDirectAnnotationUsage( Type.class )
					|| backingMember.hasDirectAnnotationUsage( TenantId.class )
					|| backingMember.hasDirectAnnotationUsage( JavaType.class )
					|| backingMember.hasDirectAnnotationUsage( JdbcTypeCode.class )
					|| backingMember.hasDirectAnnotationUsage( JdbcType.class ) ) {
				natures.add( AttributeNature.BASIC );
			}

			// then embedded
			if ( backingMember.hasDirectAnnotationUsage( EmbeddableInstantiator.class )
					|| backingMember.hasDirectAnnotationUsage( EmbeddedColumnNaming.class )
					|| backingMember.hasDirectAnnotationUsage( CompositeType.class ) ) {
				natures.add( AttributeNature.EMBEDDED );
			}

			// and any
			if ( backingMember.hasDirectAnnotationUsage( AnyDiscriminator.class )
					|| backingMember.hasDirectAnnotationUsage( AnyDiscriminatorValue.class )
					|| backingMember.hasDirectAnnotationUsage( AnyDiscriminatorValues.class )
					|| backingMember.hasDirectAnnotationUsage( AnyKeyJavaType.class )
					|| backingMember.hasDirectAnnotationUsage( AnyKeyJavaClass.class )
					|| backingMember.hasDirectAnnotationUsage( AnyKeyJdbcType.class )
					|| backingMember.hasDirectAnnotationUsage( AnyKeyJdbcTypeCode.class ) ) {
				natures.add( AttributeNature.ANY );
			}
		}

		int size = natures.size();
		return switch ( size ) {
			case 0 -> {
				MODEL_CATEGORIZATION_LOGGER.debugf(
						"Implicitly interpreting attribute `%s` as BASIC",
						backingMember.resolveAttributeName()
				);
				yield AttributeNature.BASIC;
			}
			case 1 -> natures.iterator().next();
			default -> throw new MultipleAttributeNaturesException( backingMember.resolveAttributeName(), natures );
		};
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Stuff affecting attributes built from this managed type.

	public boolean canAttributesBeInsertable() {
		return true;
	}

	public boolean canAttributesBeUpdatable() {
		return true;
	}

	public NaturalIdMutability getContainerNaturalIdMutability() {
		return NaturalIdMutability.NOT_NATURAL_ID;
	}
}
