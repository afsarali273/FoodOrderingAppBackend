package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

@Repository
public class OrderDao {
  @PersistenceContext private EntityManager entityManager;

  /**
   *
   *
   * @param customerUUID
   * @return list of orders made by customer.
   */
  public List<OrderEntity> getOrdersByCustomers(String customerUUID) {
    List<OrderEntity> ordersByCustomer =
        entityManager
            .createNamedQuery("getOrdersByCustomer", OrderEntity.class)
            .setParameter("customerUUID", customerUUID)
            .getResultList();
    if (ordersByCustomer != null) {
      return ordersByCustomer;
    }
    return Collections.emptyList();
  }

  /**
   *
   *
   * @param order
   * @return Persisted Order.
   */
  public OrderEntity saveOrder(OrderEntity order) {
    entityManager.persist(order);
    return order;
  }

  /**
   * s
   *
   * @param orderItemEntity
   * @return persisted order item.
   */
  public OrderItemEntity saveOrderItem(OrderItemEntity orderItemEntity) {
    entityManager.persist(orderItemEntity);
    return orderItemEntity;
  }
}
