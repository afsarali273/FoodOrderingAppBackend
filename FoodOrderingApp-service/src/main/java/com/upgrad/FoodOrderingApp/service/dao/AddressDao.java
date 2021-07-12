package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AddressDao {

    @PersistenceContext
    private EntityManager entityManager;

    //Creates Address
    public AddressEntity createAddress(AddressEntity addressEntity) {
        entityManager.persist(addressEntity);
        return addressEntity;
    }

    //Updating existing address
    public void updateAddress(final AddressEntity addressEntity) {
        entityManager.merge(addressEntity);
    }

    // Get All Address
    public List<AddressEntity> getAllAddress() {
        return entityManager.createNamedQuery("allAddress", AddressEntity.class).getResultList();
    }

    public AddressEntity findAddressById(String uuid){
        return entityManager.createNamedQuery("getAddressById", AddressEntity.class)
                            .setParameter("uuid",uuid)
                            .getSingleResult();
    }

    public AddressEntity deleteAddress(String Id){
        AddressEntity addressEntity = findAddressById(Id);
        entityManager.remove(addressEntity);
        return addressEntity;
    }

    public CustomerEntity getCustomerByAddressId(String addressUuid){
      CustomerAddressEntity customerAddressEntity =   entityManager.
              createNamedQuery("getCustomerAddressByAddressId", CustomerAddressEntity.class)
                .setParameter("addressUuid",addressUuid)
                .getSingleResult();

       return customerAddressEntity.getCustomerEntity();
    }
}
