package edu.oregonstate.mist.inventory.db

import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.inventory.core.Inventory
import org.skife.jdbi.v2.sqlobject.Transaction

import javax.ws.rs.core.UriBuilder

class InventoryDAOWrapper {
    private InventoryDAO inventoryDAO

    private final String QUERY_DB_TYPE = "Query"
    private final String PROVIDED_DATA_DB_TYPE = "Provided Data"

    InventoryDAOWrapper(InventoryDAO inventoryDao) {
        this.inventoryDAO = inventoryDao
    }

    /**
     * Get a single inventory object by ID.
     * @param inventoryID
     * @param selfLinkBase
     * @return
     */
    public ResourceObject getInventoryById(String inventoryID, URI selfLinkBase) {
        Inventory inventory = inventoryDAO.getInventoryByID(inventoryID)
        ResourceObject completeInventory

        if (inventory) {
            completeInventory = inventoryBuilder(inventory, selfLinkBase)
        }

        completeInventory
    }

    /**
     * Get list of ResourceObjects from a list of inventory objects
     * @param selfLinkBase
     * @return
     */
    public List<ResourceObject> getAllInventories(URI selfLinkBase) {
        List<Inventory> baseInventories = inventoryDAO.getInventories()
        List<ResourceObject> completeInventories = []

        baseInventories.each {
            completeInventories += inventoryBuilder(it, selfLinkBase)
        }

        completeInventories
    }

    /**
     * Create a single ResourceObject from an inventory object.
     * Calls DAO methods to get objects associated with inventory object.
     * @param inventory
     * @param selfLinkBase
     * @return
     */
    private ResourceObject inventoryBuilder(Inventory inventory, URI selfLinkBase) {
        inventory.apiQueryParams = inventoryDAO.getFields(QUERY_DB_TYPE, inventory.id)
        inventory.consumingEntities = inventoryDAO.getConsumingEntities(inventory.id)
        inventory.providedData = inventoryDAO.getProvidedData(inventory.id)

        inventory.providedData.each {
            it.fields = inventoryDAO.getFields(
                    PROVIDED_DATA_DB_TYPE, it.internalID
            )
        }

        inventory

        def addSelfLink = { String id ->
            [
                    'self': UriBuilder.fromUri(selfLinkBase).path(id).build()
            ]
        }

        new ResourceObject(
                id: inventory.id,
                type: "inventory",
                attributes: inventory,
                links: addSelfLink(inventory.id)
        )
    }

    /**
     * Call DAO methods to delete inventory object
     * and its associated objects.
     * @param inventoryID
     */
    @Transaction
    public void deleteInventory(String inventoryID) {
        inventoryDAO.deleteInventory(inventoryID)
        inventoryDAO.deleteFields(inventoryID)
        inventoryDAO.deleteConsumingEntities(inventoryID)
        inventoryDAO.deleteProvidedData(inventoryID)
    }
}
