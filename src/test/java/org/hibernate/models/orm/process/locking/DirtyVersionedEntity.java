/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.orm.process.locking;

import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Steve Ebersole
 */
@Entity(name = "DirtyVersionedEntity")
@Table(name = "versioned3")
@OptimisticLocking(type = OptimisticLockType.DIRTY)
public class DirtyVersionedEntity {
	@Id
	private Integer id;
	private String name;
}
