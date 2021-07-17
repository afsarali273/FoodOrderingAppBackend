package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class StateDao {

    @PersistenceContext
    private EntityManager entityManager;

    //Search State
    public StateEntity getStateByStateName(String stateName) {
        try {
            return entityManager
                    .createNamedQuery("stateByStateName", StateEntity.class)
                    .setParameter("state", stateName)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    //Search State
    public StateEntity getStateByUUID(String uuId) {
        try {
            return entityManager
                    .createNamedQuery("stateByUuid", StateEntity.class)
                    .setParameter("uuid", uuId)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    //Get All States
    public List<StateEntity> getAllStates() {
        try {
            return entityManager
                    .createNamedQuery("getAllStates", StateEntity.class)
                    .getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
