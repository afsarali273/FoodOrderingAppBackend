package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAddressDao;
import com.upgrad.FoodOrderingApp.service.dao.OrderDao;
import com.upgrad.FoodOrderingApp.service.dao.OrdersDao;
import com.upgrad.FoodOrderingApp.service.dao.StateDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressService {

    @Autowired
    AddressDao addressDao;

    @Autowired
    private CustomerAddressDao customerAddressDao;
    @Autowired StateDao stateDao;

    @Autowired
    OrdersDao ordersDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(AddressEntity addressEntity,CustomerEntity customer) throws SaveAddressException, AddressNotFoundException {

        //Check Empty Address fields
        if (StringUtils.isEmpty(addressEntity.getCity()) ||
                StringUtils.isEmpty(addressEntity.getFlatBuilNumber()) ||
                StringUtils.isEmpty(addressEntity.getLocality()) ||
                StringUtils.isEmpty(addressEntity.getPinCode()))
            throw new SaveAddressException("SAR-001", "No field can be empty");

        // Check Pin code entered is valid
        if(!addressEntity.getPinCode().matches("^[0-9]{6}$"))
            throw new SaveAddressException("SAR-002","Invalid pincode");

        //Check State Uuid
        StateEntity state =  stateDao.getStateByUUID(addressEntity.getStateEntity().getUuid());
        if(state != null)
            throw new AddressNotFoundException("ANF-002","No state by this id");
         AddressEntity createdAddress = addressDao.createAddress(addressEntity);

         // Create Customer address
        CustomerAddressEntity createdCustomerAddressEntity = new CustomerAddressEntity();
        createdCustomerAddressEntity.setCustomerEntity(customer);
        createdCustomerAddressEntity.setAddressEntity(createdAddress);
        customerAddressDao.createCustomerAddress(createdCustomerAddressEntity);

        return createdAddress;
    }

    /**
     *
     * @param customerEntity
     * @return List<AddressEntity>
     */
    public List<AddressEntity> getAllAddress(final CustomerEntity customerEntity) {
        List<AddressEntity> addressEntityList = new ArrayList<>();
        List<CustomerAddressEntity> customerAddressEntityList =
                addressDao.customerAddressByCustomer(customerEntity);
        if (customerAddressEntityList != null || !customerAddressEntityList.isEmpty()) {
            customerAddressEntityList.forEach(
                    customerAddressEntity -> addressEntityList.add(customerAddressEntity.getAddressEntity()));
        }
        return addressEntityList;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<StateEntity> getAllStates()  {
        return stateDao.getAllStates();
    }


    /**
     * @param addressEntity Address to delete.
     * @return AddressEntity type object.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity deleteAddress(final AddressEntity addressEntity) {
        final List<OrderEntity> orders = ordersDao.getAllOrdersByAddress(addressEntity);
        if (orders == null || orders.isEmpty()) {
            return addressDao.deleteAddress(addressEntity.getUuid());
        }
        addressEntity.setActive(false);
        return addressDao.updateAddress(addressEntity);
    }


    /**
     * @param addressId
     * @param customerEntity
     * @return AddressEntity object.
     * @throws AddressNotFoundException
     * @throws AuthorizationFailedException
     */
    public AddressEntity getAddressByUUID(final String addressId, final CustomerEntity customerEntity)
            throws AuthorizationFailedException, AddressNotFoundException {
        AddressEntity addressEntity = addressDao.findAddressById(addressId);
        if (addressEntity == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }
        if (addressId.isEmpty()) {
            throw new AddressNotFoundException("ANF-005", "Address id can not be empty");
        }

        CustomerAddressEntity customerAddressEntity =
                customerAddressDao.customerAddressByAddress(addressEntity);
        if (!customerAddressEntity.getCustomerEntity().getUuid().equals(customerEntity.getUuid())) {
            throw new AuthorizationFailedException(
                    "ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }
        return addressEntity;
    }

    /**
     *
     *
     * @param stateUuid
     * @return StateEntity object.
     * @throws AddressNotFoundException
     */
    public StateEntity getStateByUUID(final String stateUuid) throws AddressNotFoundException {
        if (stateDao.getStateByUUID(stateUuid) == null) {
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        }
        return stateDao.getStateByUUID(stateUuid);
    }
}
