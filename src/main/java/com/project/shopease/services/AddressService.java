package com.project.shopease.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import com.project.shopease.auth.entities.User;
import com.project.shopease.dto.AddressRequest;
import com.project.shopease.entities.Address;
import com.project.shopease.repositories.AddressRepository;

import java.security.Principal;
import java.util.UUID;

@Service
public class AddressService {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AddressRepository addressRepository;

    // ✅ Create Address
    public Address createAddress(AddressRequest addressRequest, Principal principal) {
        User user = (User) userDetailsService.loadUserByUsername(principal.getName());

        Address address = Address.builder()
                .name(addressRequest.getName())
                .street(addressRequest.getStreet())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .country(addressRequest.getCountry())
                .pincode(addressRequest.getPincode())
                .phoneNumber(addressRequest.getPhoneNumber())
                .user(user)
                .build();

        user.getAddressList().add(address);
        return addressRepository.save(address);
    }

    // ✅ Delete Address
    public void deleteAddress(UUID id) {
        addressRepository.deleteById(id);
    }

    // ✅ Update Address
    public Address updateAddress(UUID id, AddressRequest addressRequest, Principal principal) {
        User user = (User) userDetailsService.loadUserByUsername(principal.getName());

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Security check: make sure this address belongs to logged-in user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to update this address");
        }

        // Update fields
        address.setName(addressRequest.getName());
        address.setStreet(addressRequest.getStreet());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setCountry(addressRequest.getCountry());
        address.setPincode(addressRequest.getPincode());
        address.setPhoneNumber(addressRequest.getPhoneNumber());

        return addressRepository.save(address);
    }
}
