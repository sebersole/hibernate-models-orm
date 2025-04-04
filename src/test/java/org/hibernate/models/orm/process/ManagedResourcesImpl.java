/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.orm.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.jaxb.spi.JaxbBindableMappingDescriptor;
import org.hibernate.boot.model.convert.spi.ConverterDescriptor;
import org.hibernate.boot.model.process.spi.ManagedResources;

import jakarta.persistence.AttributeConverter;

/**
 * @author Steve Ebersole
 */
public class ManagedResourcesImpl implements ManagedResources {
	private final Map<Class<? extends AttributeConverter<?,?>>, ConverterDescriptor> attributeConverterDescriptorMap = new HashMap<>();
	private final Set<Class<?>> annotatedClassReferences = new LinkedHashSet<>();
	private final Set<String> annotatedClassNames = new LinkedHashSet<>();
	private final Set<String> annotatedPackageNames = new LinkedHashSet<>();
	private final List<Binding<JaxbBindableMappingDescriptor>> mappingFileBindings = new ArrayList<>();
	private final Map<String, Class<?>> extraQueryImports = new HashMap<>();

	public Map<Class<? extends AttributeConverter<?, ?>>, ConverterDescriptor> getAttributeConverterDescriptorMap() {
		return attributeConverterDescriptorMap;
	}

	@Override
	public Collection<ConverterDescriptor> getAttributeConverterDescriptors() {
		return getAttributeConverterDescriptorMap().values();
	}

	@Override
	public Set<Class<?>> getAnnotatedClassReferences() {
		return annotatedClassReferences;
	}

	@Override
	public Set<String> getAnnotatedClassNames() {
		return annotatedClassNames;
	}

	@Override
	public Set<String> getAnnotatedPackageNames() {
		return annotatedPackageNames;
	}

	@Override
	public Collection<Binding<JaxbBindableMappingDescriptor>> getXmlMappingBindings() {
		return mappingFileBindings;
	}

	@Override
	public Map<String, Class<?>> getExtraQueryImports() {
		return extraQueryImports;
	}
}
