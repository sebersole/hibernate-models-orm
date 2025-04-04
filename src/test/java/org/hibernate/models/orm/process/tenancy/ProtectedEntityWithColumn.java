/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.orm.process.tenancy;

import org.hibernate.annotations.TenantId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Steve Ebersole
 */
@Entity(name = "ProtectedEntity")
@Table(name = "protected_entity")
public class ProtectedEntityWithColumn {
	@Id
	private Integer id;
	private String name;
	@TenantId
	@Enumerated(EnumType.STRING)
	@Column(name = "customer")
	private Tenant tenant;
}
