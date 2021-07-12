package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.StateDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    AddressDao addressDao;
    @Autowired
    StateDao stateDao;
    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;


    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(AddressEntity addressEntity) throws SaveAddressException, AddressNotFoundException {

        // Check Pin code entered is valid
        if(!addressEntity.getPinCode().matches("^[0-9]{6}$"))
            throw new SaveAddressException("SAR-002","Invalid pincode");

        //Check State Uuid
        StateEntity state =  stateDao.getStateByStateUuid(addressEntity.getStateEntity().getUuid());
        if(state != null)
            throw new AddressNotFoundException("ANF-002","No state by this id");

        return addressDao.createAddress(addressEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<AddressEntity> getAllAddress()  {
        return addressDao.getAllAddress();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<StateEntity> getAllStates()  {
        return stateDao.getAllStates();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity deleteAddress(CustomerAuthEntity authEntity, String addressId) throws AddressNotFoundException, AuthorizationFailedException {

        if(StringUtils.isEmpty(addressId))
            throw new AddressNotFoundException("ANF-005","Address id can not be empty");

        AddressEntity address = addressDao.findAddressById(addressId);
        if(address ==null)
            throw new AddressNotFoundException("ANF-003","No address by this id");

        // Check user has right to update/delete
        CustomerEntity originalUser =  addressDao.getCustomerByAddressId(addressId);
        CustomerEntity loggedInUser = authEntity.getCustomer();

        if(!originalUser.getUuid().equals(loggedInUser.getUuid()))
            throw new AuthorizationFailedException("ATHR-004","You are not authorized to view/update/delete any one else's address");

        return addressDao.deleteAddress(addressId);
    }

}
