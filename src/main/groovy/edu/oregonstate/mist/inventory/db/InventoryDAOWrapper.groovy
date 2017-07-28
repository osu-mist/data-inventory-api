package edu.oregonstate.mist.inventory.db

import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.inventory.core.ConsumingEntity
import edu.oregonstate.mist.inventory.core.DataSource
import edu.oregonstate.mist.inventory.core.Field
import edu.oregonstate.mist.inventory.core.Inventory
import org.skife.jdbi.v2.sqlobject.Transaction

import javax.ws.rs.core.UriBuilder

class InventoryDAOWrapper {
    private InventoryDAO inventoryDAO
    private URI selfLinkBase

    private final String QUERY_DB_TYPE = "Query"
    private final String PROVIDED_DATA_DB_TYPE = "Provided Data"

    InventoryDAOWrapper(InventoryDAO inventoryDao, URI selfLinkBase) {
        this.inventoryDAO = inventoryDao
        this.selfLinkBase = selfLinkBase
    }

    /**
     * Get a single inventory object by ID.
     * @param inventoryID
     * @param selfLinkBase
     * @return
     */
    public ResourceObject getInventoryById(String inventoryID) {
        Inventory inventory = inventoryDAO.getInventoryByID(inventoryID)
        ResourceObject completeInventory

        if (inventory) {
            completeInventory = inventoryBuilder(inventory)
        }

        completeInventory
    }

    /**
     * Get list of ResourceObjects from a list of inventory objects
     * @param selfLinkBase
     * @return
     */
    public List<ResourceObject> getAllInventories() {
        List<Inventory> baseInventories = inventoryDAO.getInventories()
        List<ResourceObject> completeInventories = []

        baseInventories.each {
            completeInventories += inventoryBuilder(it)
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
    private ResourceObject inventoryBuilder(Inventory inventory) {
        inventory.apiQueryParams = inventoryDAO.getFields(
                QUERY_DB_TYPE,
                inventory.id,
                inventory.id
        )

        inventory.consumingEntities = inventoryDAO.getConsumingEntities(inventory.id)
        inventory.providedData = inventoryDAO.getProvidedData(inventory.id)

        inventory.providedData.each {
            it.fields = inventoryDAO.getFields(
                    PROVIDED_DATA_DB_TYPE,
                    it.sourceID,
                    inventory.id
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

    @Transaction
    public void createInventory(Inventory inventory) {
        inventoryDAO.createInventory(inventory)

        inventory.apiQueryParams.each { queryParam ->
            inventoryDAO.createField((Field) queryParam, inventory.id, QUERY_DB_TYPE, inventory.id)
        }

        inventory.consumingEntities.each { consumingEntity ->
            inventoryDAO.createConsumingEntity((ConsumingEntity) consumingEntity, inventory.id)
        }

        inventory.providedData.each { dataSource ->
            inventoryDAO.createProvidedData((DataSource) dataSource, inventory.id)

            dataSource.fields.each { field ->
                inventoryDAO.createField(
                        (Field) field,
                        dataSource.sourceID,
                        PROVIDED_DATA_DB_TYPE,
                        inventory.id
                )
            }
        }
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
