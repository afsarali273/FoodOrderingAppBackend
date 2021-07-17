package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class CustomerService {

    @Autowired
    CustomerDao customerDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    /**
     *
     * @param username customers contactnumber will be the username.
     * @param password customers password.
     * @return CustomerAuthEntity object.
     * @throws AuthenticationFailedException if any of the validation fails.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(String username, String password)
            throws AuthenticationFailedException {
        CustomerEntity customerEntity = customerDao.getCustomerByContactNumber(username);
        if (customerEntity == null) {
            throw new AuthenticationFailedException(
                    "ATH-001", "This contact number has not been registered!");
        }
        final String encryptedPassword =
                PasswordCryptographyProvider.encrypt(password, customerEntity.getSalt());
        if (!encryptedPassword.equals(customerEntity.getPassword())) {
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");
        }
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
        CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
        customerAuthEntity.setCustomerEntity(customerEntity);
        customerAuthEntity.setUuId(UUID.randomUUID().toString());
        final ZonedDateTime dayT = ZonedDateTime.now();
        final ZonedDateTime expiresAt = dayT.plusHours(8);
        customerAuthEntity.setLoginAt(dayT);
        customerAuthEntity.setExpiresAt(expiresAt);
        String accessToken = jwtTokenProvider.generateToken(customerEntity.getUuid(), dayT, expiresAt);
        customerAuthEntity.setAccessToken(accessToken);
        customerDao.createCustomerAuthToken(customerAuthEntity);
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
    public CustomerEntity updateCustomer(CustomerEntity updatedCustomer) {
        customerDao.updateCustomer(updatedCustomer);
        return updatedCustomer;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomerPassword(String oldPassword , String newPassword,CustomerEntity oldCustomer) throws UpdateCustomerException {

        //Check for password weakness
        if(!newPassword.matches("^(?=.*[A-Z])(?=.*[#@$%&*!^])(?=.*[0-9])(?=.*[a-z]).{8,20}$"))
            throw new UpdateCustomerException("UCR-001", "Weak password!");

        //Check Old password is correct
        final String encryptedPassword = passwordCryptographyProvider.encrypt(oldPassword, oldCustomer.getSalt());
        if(!encryptedPassword.equals(oldCustomer.getPassword()))
            throw new UpdateCustomerException("UCR-004","Incorrect old password!");

        //Setting new Password to existing customer
        oldCustomer.setPassword(newPassword);
        customerDao.updateCustomer(oldCustomer);
        return oldCustomer;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity logout(final String accessToken) throws AuthorizationFailedException {

        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthTokenEntity(accessToken);

        //Check if customer is logged in
        if (customerAuthEntity == null)
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");

        //Check if Customer is logged out
        if(customerAuthEntity.getLogoutAt() !=null)
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");

        //If Current time is exceeding 8 hours of Login then ,Session expired
        if(ZonedDateTime.now().compareTo(customerAuthEntity.getExpiresAt()) > 0)
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");

        final ZonedDateTime now = ZonedDateTime.now();
        customerAuthEntity.setLogoutAt(now); //Set Logout At to Now
        return customerAuthEntity;
    }

    /**
     * This method checks if the token is valid.
     *
     * @param authorization for authorisation
     * @return customer information return
     * @throws AuthorizationFailedException exception in case customer token invalidated
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity getCustomer(final String authorization)
            throws AuthorizationFailedException{

        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthTokenEntity(authorization);

        //Check if customer is logged in
        if (customerAuthEntity == null)
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");

        //Check if Customer is logged out
        if(customerAuthEntity.getLogoutAt() !=null)
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");

        //If Current time is exceeding 8 hours of Login then ,Session expired
        if(ZonedDateTime.now().compareTo(customerAuthEntity.getExpiresAt()) > 0)
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");

        return customerAuthEntity.getCustomer();
    }
}
