package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.AddressList;
import com.upgrad.FoodOrderingApp.api.model.AddressListResponse;
import com.upgrad.FoodOrderingApp.api.model.AddressListState;
import com.upgrad.FoodOrderingApp.api.model.DeleteAddressResponse;
import com.upgrad.FoodOrderingApp.api.model.SaveAddressRequest;
import com.upgrad.FoodOrderingApp.api.model.SaveAddressResponse;
import com.upgrad.FoodOrderingApp.api.model.StatesList;
import com.upgrad.FoodOrderingApp.api.model.StatesListResponse;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.upgrad.FoodOrderingApp.service.common.Utils.getTokenFromAuthorization;

@RestController
@RequestMapping("/")
@CrossOrigin
public class AddressController {

    @Autowired
    CustomerService customerService;

    @Autowired
    AddressService addressService;

    @RequestMapping(
            method = RequestMethod.POST,
            path = "/address",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(
            @RequestHeader("authorization") final String authorization, @RequestBody(required = false) SaveAddressRequest saveAddressRequest) throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {
        String accessToken = getTokenFromAuthorization(authorization);
        //Check user credentials
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        // Get the State available in DB
        StateEntity stateEntity = addressService.getStateByUUID(saveAddressRequest.getStateUuid());

        //Create New address
        AddressEntity address = new AddressEntity();
        address.setUuid(UUID.randomUUID().toString());
        address.setCity(saveAddressRequest.getCity());
        address.setFlatBuilNumber(saveAddressRequest.getFlatBuildingName());
        address.setLocality(saveAddressRequest.getLocality());
        address.setUuid(saveAddressRequest.getStateUuid());
        address.setPinCode(saveAddressRequest.getPincode());
        address.setActive(true);
        address.setStateEntity(stateEntity);

        AddressEntity addressEntity = addressService.saveAddress(address, customerEntity);
        SaveAddressResponse saveAddressResponse =
                new SaveAddressResponse().id(addressEntity.getUuid()).status("ADDRESS SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SaveAddressResponse>(saveAddressResponse, HttpStatus.CREATED);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/address/customer",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getAllAddress(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {
        String accessToken = getTokenFromAuthorization(authorization);
        //Check user credentials
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        //Get All Addresses
        List<AddressEntity> addressEntities = addressService.getAllAddress(customerEntity);
        List<AddressList> addressList = new ArrayList<AddressList>();

        // Map Address Object with Address List
        addressEntities.forEach(address -> {

            //Get State
            AddressListState addressListState = new AddressListState();
            addressListState.setId(UUID.fromString(address.getStateEntity().getUuid()));
            addressListState.setStateName(address.getStateEntity().getStateName());

            // Get Address
            AddressList add = new AddressList();
            add.setCity(address.getCity());
            add.setId(UUID.fromString(address.getUuid()));
            add.setLocality(address.getLocality());
            add.setPincode(address.getPinCode());
            add.setFlatBuildingName(address.getFlatBuilNumber());
            add.setState(addressListState);
            addressList.add(add);
        });

        AddressListResponse addressListResponse = new AddressListResponse();
        addressListResponse.setAddresses(addressList);

        return new ResponseEntity<AddressListResponse>(addressListResponse, HttpStatus.OK);
    }

    @RequestMapping(
            method = RequestMethod.DELETE,
            path = "/address/{address_id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> deleteAddress(
            @PathVariable("address_id") final String addressId,
            @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AddressNotFoundException {

        //Get Token
        String accessToken = getTokenFromAuthorization(authorization);
        //Check user credentials
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        AddressEntity addressEntity = addressService.getAddressByUUID(addressId, customerEntity);
        AddressEntity deletedAddress = addressService.deleteAddress(addressEntity);

        DeleteAddressResponse deleteAddressResponse =
                new DeleteAddressResponse().id(UUID.fromString(deletedAddress.getUuid())).status("ADDRESS DELETED SUCCESSFULLY");
        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/states",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StatesListResponse> getAllStates() {
        //Get All Addresses
        List<StateEntity> states = addressService.getAllStates();

        // Save statesList Object to response Entity
        StatesListResponse statesListResponse = new StatesListResponse();

        // Map Address Object with Address List
        states.forEach(stateEntity -> {
            //Get State
            StatesList statesList = new StatesList();
            statesList.setId(UUID.fromString(stateEntity.getUuid()));
            statesList.setStateName(stateEntity.getStateName());
            statesListResponse.addStatesItem(statesList);
        });
        return new ResponseEntity<StatesListResponse>(statesListResponse, HttpStatus.OK);
    }

}
