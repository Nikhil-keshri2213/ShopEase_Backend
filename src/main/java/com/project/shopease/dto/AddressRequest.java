package com.project.shopease.dto;

import java.util.UUID;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressRequest {
    private UUID id;
    private String name;
    private String street;
    private String city;
    private String state;    
    private String country;
    private String pincode;
    private String phoneNumber;
}