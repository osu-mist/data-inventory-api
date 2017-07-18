package edu.oregonstate.mist.inventory.health

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result
import edu.oregonstate.mist.inventory.db.InventoryDAO

class InventoryHealthCheck extends HealthCheck {
    private InventoryDAO inventoryDAO

    InventoryHealthCheck(InventoryDAO inventoryDAO) {
        this.inventoryDAO = inventoryDAO
    }

    @Override
    protected Result check() {
        try {
            String status = inventoryDAO.checkHealth()

            if (status != null) {
                return Result.healthy()
            }
            Result.unhealthy("status: ${status}")
        } catch(Exception e) {
            Result.unhealthy(e.message)
        }
    }
}
