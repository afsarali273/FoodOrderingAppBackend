package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public CustomerEntity createCustomer(CustomerEntity customerEntity) {
        entityManager.persist(customerEntity);
        return customerEntity;
    }

    /**
     * T authorization access token
     *
     * @param customerAuthEntity  new authorization will be
     *     created
     */
    public void createCustomerAuthToken(CustomerAuthEntity customerAuthEntity) {
        entityManager.persist(customerAuthEntity);
    }

    // Retrieve Customer by using AccessToken
    public CustomerAuthEntity getCustomerAuthTokenEntity(String accessToken) {
        try {
            return entityManager
                    .createNamedQuery("customerAuthTokenByAccessToken", CustomerAuthEntity.class)
                    .setParameter("accessToken", accessToken)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }

    }

    public CustomerEntity getCustomerByContactNumber(String contactNumber) {
        try {
            return entityManager
                    .createNamedQuery("customerByContactNumber", CustomerEntity.class)
                    .setParameter("contact", contactNumber)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public CustomerAuthEntity createAuthToken(final CustomerAuthEntity customerAuthEntity) {
        entityManager.persist(customerAuthEntity);
        return customerAuthEntity;
    }

    public void updateCustomer(final CustomerEntity customerEntity) {
        entityManager.merge(customerEntity);
    }

}
