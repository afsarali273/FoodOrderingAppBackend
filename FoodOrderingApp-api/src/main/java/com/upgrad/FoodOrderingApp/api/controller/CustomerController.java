package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.SignupCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerResponse;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CustomerNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/")
public class CustomerController {

    @Autowired
    CustomerService customerService;

    @RequestMapping(
            method = RequestMethod.POST,
            path = "/customer/signup",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup( @RequestBody SignupCustomerRequest signupCustomerRequest) throws Exception {
        CustomerEntity customer = new CustomerEntity();
        customer.setUuid(UUID.randomUUID().toString());
        customer.setFirstName(signupCustomerRequest.getFirstName());
        customer.setLastName(signupCustomerRequest.getLastName());
        customer.setPassword(signupCustomerRequest.getPassword());
        customer.setContactNumber(signupCustomerRequest.getContactNumber());
        customer.setEmail(signupCustomerRequest.getEmailAddress());
        customer.setSalt("123abc");

        // Check all fields are Non-Empty except last name
        if (StringUtils.isEmpty(customer.getFirstName()) ||
                StringUtils.isEmpty(customer.getPassword())||
                StringUtils.isEmpty(customer.getEmail()) ||
                StringUtils.isEmpty(customer.getContactNumber()))
            throw new SignUpRestrictedException(
                    "SGR-005", "Except last name all fields should be filled");

        CustomerEntity createdCustomer = customerService.saveCustomer(customer);
        SignupCustomerResponse customerResponse =
                new SignupCustomerResponse()
                        .id(createdCustomer.getUuid())
                        .status("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupCustomerResponse>(customerResponse, HttpStatus.CREATED);
    }


}
