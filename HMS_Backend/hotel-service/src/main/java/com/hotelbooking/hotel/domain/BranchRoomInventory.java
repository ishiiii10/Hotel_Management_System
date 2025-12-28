package com.hotelbooking.hotel.domain;

import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Entity
@Table(name = "branch_room_inventory", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "branch_id", "room_category_id" }) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchRoomInventory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "branch_id")
	private HotelBranch branch;

	@ManyToOne(optional = false)
	@JoinColumn(name = "room_category_id")
	private RoomCategory roomCategory;

	@Column(nullable = false)
	private int totalRooms;

	@Column(nullable = true)
	private BigDecimal priceOverride;

	@Column(nullable = false)
	private Long lastUpdatedBy;
}