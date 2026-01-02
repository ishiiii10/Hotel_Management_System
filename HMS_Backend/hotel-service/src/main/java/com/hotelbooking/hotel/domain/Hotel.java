package com.hotelbooking.hotel.domain;

import java.time.LocalDateTime;

import com.hotelbooking.hotel.enums.City;
import com.hotelbooking.hotel.enums.HotelStatus;
import com.hotelbooking.hotel.enums.Hotel_Category;
import com.hotelbooking.hotel.enums.State;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Table(
    name = "hotels",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phone_number")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Hotel_Category category;
    
    @Column(length = 500)
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private City city;

    @NotBlank
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state;
    
    @NotBlank
    @Column(nullable = false, length = 20)
    private String country;
    
    @NotBlank
    @Column(nullable = false, length = 6)
    private String pincode;


    @Email(message = "Invalid email format")
    @NotBlank
    @Column(nullable = false, length = 150)
    private String email;

    @NotBlank
    @Pattern(
        regexp = "^[+]?[0-9]{10,15}$",
        message = "Phone number must contain 10 to 15 digits and may start with +"
    )
    @Column(name = "phone_number", nullable = false, length = 16)
    private String contactNumber;
    
    private Integer starRating;

    
    @Column(length = 500)
    private String amenities;

    private String imageUrl;

    

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HotelStatus status;
    
    private Integer totalRooms;
    private Integer availableRooms;


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.status = HotelStatus.ACTIVE;
        this.availableRooms = this.totalRooms;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}