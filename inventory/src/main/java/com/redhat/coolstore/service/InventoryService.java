package com.redhat.coolstore.service;

import com.redhat.coolstore.model.Inventory;
import java.util.List;

import javax.inject.Inject;
import javax.enterprise.context.Dependent;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Dependent
public class InventoryService {

    @Inject
    EntityManager entityManager;
   
    public InventoryService() {

    }

    public boolean isAlive() {
        return entityManager.createQuery("select 1 from Inventory i")
                .setMaxResults(1)
                .getResultList().size() == 1;
    }
    public Inventory getInventory(String itemId) {
        return entityManager.find(Inventory.class, itemId);
    }

    public List<Inventory> getAllInventory() {
        Query query = entityManager.createQuery("SELECT i FROM Inventory i");
        return query.getResultList();
    }
}
