package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class AuthenticationService {

  @Autowired private CustomerDao customerDao;

  @Autowired private PasswordCryptographyProvider CryptographyProvider;

  @Transactional(propagation = Propagation.REQUIRED)
  public CustomerAuthEntity login(final String username, final String password) throws AuthenticationFailedException {
    CustomerEntity customerEntity = customerDao.getCustomerByContactNumber(username);
    if (customerEntity == null) {
      throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
    }

    final String encryptedPassword = CryptographyProvider.encrypt(password, customerEntity.getSalt());
    if (encryptedPassword.equals(customerEntity.getPassword())) {
      JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
      CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
      customerAuthEntity.setCustomerEntity(customerEntity);
      customerAuthEntity.setUuId(customerEntity.getUuid());
      final ZonedDateTime now = ZonedDateTime.now();
      final ZonedDateTime expiresAt = now.plusHours(8);

      customerAuthEntity.setAccessToken(
          jwtTokenProvider.generateToken(customerEntity.getUuid(), now, expiresAt));

      customerAuthEntity.setLoginAt(now);
      customerAuthEntity.setExpiresAt(expiresAt);

      customerDao.createAuthToken(customerAuthEntity);
      customerDao.updateCustomer(customerEntity);
      return customerAuthEntity;
    } else {
      throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public CustomerAuthEntity logout(final String accessToken) throws AuthorizationFailedException {

    CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthTokenEntity(accessToken);

    //Check if customer is logged in
    if (customerAuthEntity == null)
      throw new AuthorizationFailedException("SGR-001", "Customer is not Logged in.");

    //Check if Customer is logged out
    if(customerAuthEntity.getLogoutAt() !=null)
      throw new AuthorizationFailedException("SGR-002", "Customer is logged out. Log in again to access this endpoint.");

      //If Current time is exceeding 8 hours of Login then ,Session expired
      if(ZonedDateTime.now().compareTo(customerAuthEntity.getExpiresAt()) > 0)
        throw new AuthorizationFailedException("SGR-003", "Your session is expired. Log in again to access this endpoint.");

    final ZonedDateTime now = ZonedDateTime.now();
    customerAuthEntity.setLogoutAt(now); //Set Logout At to Now
    return customerAuthEntity;
  }
//
//  @Transactional(propagation = Propagation.REQUIRED)
//  public UserAuthTokenEntity userProfile(String uuId, final String accessToken)
//      throws AuthorizationFailedException, UserNotFoundException {
//    UserAuthTokenEntity userEntity = userDao.getUserAuthTokenEntity(accessToken);
//
//    // Check user is signed in
//    if (userEntity == null)
//      throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
//
//    // Check User is Signed Out
//    if (userEntity.getLogoutAt() != null)
//      throw new AuthorizationFailedException(
//          "ATHR-002", "User is signed out.Sign in first to get user details");
//
//    // Check userId is correct/exist in DB
//    if (userDao.getUserByUuid(uuId) == null)
//      throw new UserNotFoundException("USR-001", "User with entered uuid does not exist");
//
//    return userEntity;
//  }

  //    public UserAuthTokenEntity getUserAuthTokenEntity(String authorization) {
  //        try {
  //            byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
  //            String decodedText = new String(decode);
  //            String[] decodedArray = decodedText.split(":");
  //            return userDao.getUserAuthTokenEntityByUserName(decodedArray[0]);
  //        } catch (Exception exc) {
  //            return null;
  //        }
  //    }
}
