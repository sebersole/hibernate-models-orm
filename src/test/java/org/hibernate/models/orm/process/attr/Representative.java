/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.orm.process.attr;

import java.util.Set;

import org.hibernate.annotations.Any;
import org.hibernate.annotations.ManyToAny;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

/**
 * @author Steve Ebersole
 */
@Entity
public class Representative {
	@Id
	private Integer id;
	private String name;
	private Status status;
	@Embedded
	private Component component;
	@OneToOne
	private Representative anotherOne;
	@ManyToOne
	private Representative another;
	@Any
	private Representative other;
	@ElementCollection
	private Set<Status> statuses;
	@ElementCollection
	private Set<Component> components;
	@OneToMany
	private Set<Representative> others;
	@ManyToMany
	private Set<Representative> manyOthers;
	@ManyToAny
	private Set<Representative> anyOthers;
}
