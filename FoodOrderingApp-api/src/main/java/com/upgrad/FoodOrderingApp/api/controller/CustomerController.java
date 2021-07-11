package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.LoginResponse;
import com.upgrad.FoodOrderingApp.api.model.LogoutResponse;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerResponse;
import com.upgrad.FoodOrderingApp.api.model.UpdateCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.UpdateCustomerResponse;
import com.upgrad.FoodOrderingApp.api.model.UpdatePasswordRequest;
import com.upgrad.FoodOrderingApp.service.businness.AuthenticationService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CustomerNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/")
@CrossOrigin
public class CustomerController {

    @Autowired
    CustomerService customerService;

    @Autowired
    AuthenticationService authenticationService;


    @RequestMapping(
            method = RequestMethod.POST,
            path = "/customer/signup",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(@RequestBody SignupCustomerRequest signupCustomerRequest) throws Exception {
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
                StringUtils.isEmpty(customer.getPassword()) ||
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

    @RequestMapping(
            method = RequestMethod.POST,
            path = "/customer/login",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(
            @RequestHeader("authorization") final String authorization)
            throws AuthenticationFailedException {

        byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
        String decodedText = new String(decode);

        //Check Basic username:password format is correct
        if (!decodedText.matches("^\\d+(:.*)$"))
            throw new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");

        String[] decodedArray = decodedText.split(":");
        CustomerAuthEntity customerAuthEntity =
                authenticationService.login(decodedArray[0], decodedArray[1]);

        CustomerEntity customer = customerAuthEntity.getCustomer();

        LoginResponse loginResponse =
                new LoginResponse().id(customer.getUuid()).message("LOGGED IN SUCCESSFULLY")
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .emailAddress(customer.getEmail())
                        .emailAddress(customer.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", customerAuthEntity.getAccessToken());
        return new ResponseEntity<LoginResponse>(loginResponse, headers, HttpStatus.OK);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            path = "/customer/logout",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> logout(
            @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = authenticationService.logout(authorization);
        CustomerEntity user = customerAuthEntity.getCustomer();
        LogoutResponse logoutResponse =
                new LogoutResponse().id(user.getUuid()).message("LOGGED OUT SUCCESSFULLY");
        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", customerAuthEntity.getAccessToken());
        return new ResponseEntity<LogoutResponse>(logoutResponse, headers, HttpStatus.OK);
    }

    @RequestMapping(
            method = RequestMethod.PUT,
            path = "/customer",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> update(
            @RequestHeader("authorization") final String accessToken,
            @RequestBody final UpdateCustomerRequest updateCustomerRequest)
            throws AuthorizationFailedException, UpdateCustomerException {

        if (StringUtils.isEmpty(updateCustomerRequest.getFirstName()))
            throw new UpdateCustomerException("UCR-002", "First name field should not be empty");
        //Get Values from Request Body
        CustomerEntity updatedCustomer = new CustomerEntity();
        updatedCustomer.setFirstName(updateCustomerRequest.getFirstName());
        updatedCustomer.setLastName(updateCustomerRequest.getLastName());

        //Get Update response form Dao
        CustomerEntity customer = customerService.updateCustomer(accessToken, updatedCustomer);

        //UpdateResponse Object
        UpdateCustomerResponse updateCustomerResponse = new UpdateCustomerResponse();
        updateCustomerResponse.setId(customer.getUuid());
        updateCustomerResponse.setStatus("CUSTOMER DETAILS UPDATED SUCCESSFULLY");
        return new ResponseEntity<UpdateCustomerResponse>(updateCustomerResponse, HttpStatus.OK);
    }

    @RequestMapping(
            method = RequestMethod.PUT,
            path = "/customer/password",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> changePassword(
            @RequestHeader("authorization") final String accessToken,
            @RequestBody final UpdatePasswordRequest updatePasswordRequest)
            throws AuthorizationFailedException, UpdateCustomerException {
       CustomerAuthEntity customerAuthEntity = authenticationService.authenticate(accessToken);
        CustomerEntity oldRecord = customerAuthEntity.getCustomer();

        // Check old and new Password not empty
        if (StringUtils.isEmpty(updatePasswordRequest.getOldPassword()) || StringUtils.isEmpty(updatePasswordRequest.getNewPassword()))
            throw new UpdateCustomerException("UCR-003", "No field should be empty");

        //Get Update response form Dao
        CustomerEntity customer = customerService.updateCustomer(oldRecord,updatePasswordRequest.getOldPassword(),updatePasswordRequest.getNewPassword());

        //UpdateResponse Object
        UpdateCustomerResponse updateCustomerResponse = new UpdateCustomerResponse();
        updateCustomerResponse.setId(customer.getUuid());
        updateCustomerResponse.setStatus("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");
        return new ResponseEntity<UpdateCustomerResponse>(updateCustomerResponse, HttpStatus.OK);
    }


}
