package com.pgm.lessor.entity;

import com.pgm.lessor.crypto.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hostel_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostelAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "building_name_or_number")
    private String buildingNameOrNumber;

    @Convert(converter = EncryptedStringConverter.class)
    private String street;

    private String area;

    private String city;

    private String state;

    @Column(name = "pin_code")
    private Integer pinCode;
}
