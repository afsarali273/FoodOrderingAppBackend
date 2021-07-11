package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CustomerNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class CustomerService {

    @Autowired
    CustomerDao customerDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(final String authorization)
            throws AuthorizationFailedException, CustomerNotFoundException {
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthTokenEntity(authorization);

        // Check user is signed in
        if (customerAuthEntity == null)
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");

        // Check User is Signed Out
        if (customerAuthEntity.getLogoutAt() != null)
            throw new AuthorizationFailedException(
                    "ATHR-002", "User is signed out.Sign in first to post a question");
        return customerAuthEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity saveCustomer(CustomerEntity customerEntity)  throws SignUpRestrictedException{

        // Check if contact number already used
        if (customerDao.getCustomerByContactNumber(customerEntity.getContactNumber()) != null)
            throw new SignUpRestrictedException(
                    "SGR-001", "This contact number is already registered! Try other contact number.");

        //Check Email Id format
        if(!customerEntity.getEmail().matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$"))
            throw new SignUpRestrictedException(
                    "SGR-002", "Invalid email-id format!");

        // Check for Valid contact number
        if(!customerEntity.getContactNumber().matches("^\\d{10}$"))
            throw new SignUpRestrictedException(
                    "SGR-003", "Invalid contact number!");

        //Check for password weakness
        if(!customerEntity.getPassword().matches("^(?=.*[A-Z])(?=.*[#@$%&*!^])(?=.*[0-9])(?=.*[a-z]).{8,20}$"))
            throw new SignUpRestrictedException(
                    "SGR-004", "Weak password!");

        // Encryption of Password before persisting to DB
        String[] encryptedText = passwordCryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);
        return customerDao.createCustomer(customerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomer(
            final String accessToken, CustomerEntity updatedCustomer)
            throws AuthorizationFailedException {

        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthTokenEntity(accessToken);
        if (customerAuthEntity == null)
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");

        if (customerAuthEntity.getLogoutAt() != null)
            throw new AuthorizationFailedException(
                    "ATHR-002", "Customer is logged out. Log in again to access this endpoint.");

        //If Current time is exceeding 8 hours of Login then ,Session expired
        if(ZonedDateTime.now().compareTo(customerAuthEntity.getExpiresAt()) > 0)
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");

        CustomerEntity customer = customerAuthEntity.getCustomer();
        customer.setFirstName(updatedCustomer.getFirstName());
        customer.setLastName(updatedCustomer.getLastName());
        customerDao.updateCustomer(customer);
        return customer;
    }
}
