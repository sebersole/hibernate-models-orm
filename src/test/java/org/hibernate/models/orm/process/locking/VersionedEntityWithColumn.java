/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.orm.process.locking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * @author Steve Ebersole
 */
@Entity(name = "VersionedEntity")
@Table(name = "versioned2")
public class VersionedEntityWithColumn {
	@Id
	private Integer id;
	private String name;
	@Version
	@Column(name = "revision")
	private int version;
}
