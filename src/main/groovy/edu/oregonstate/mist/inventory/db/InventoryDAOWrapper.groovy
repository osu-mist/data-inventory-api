package edu.oregonstate.mist.inventory.db

import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.inventory.core.ConsumingEntity
import edu.oregonstate.mist.inventory.core.DataSource
import edu.oregonstate.mist.inventory.core.Field
import edu.oregonstate.mist.inventory.core.Inventory
import org.skife.jdbi.v2.sqlobject.Transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.core.UriBuilder

class InventoryDAOWrapper {
    private InventoryDAO inventoryDAO
    private URI selfLinkBase

    private final String QUERY_DB_TYPE = "Query"
    private final String PROVIDED_DATA_DB_TYPE = "Provided Data"

    Logger logger = LoggerFactory.getLogger(InventoryDAOWrapper.class)

    /**
     * Get a single inventory object by ID.
     * @param inventoryID
     * @param selfLinkBase
     * @return
     */
    public ResourceObject getInventoryById(String inventoryID) {
        Inventory inventory = inventoryDAO.getInventoryByID(inventoryID)

        if (inventory) {
            inventoryBuilder(inventory)
        } else {
            null
        }
    }

    /**
     * Get list of ResourceObjects from a list of inventory objects
     * @param selfLinkBase
     * @return
     */
    public List<ResourceObject> getAllInventories() {
        inventoryDAO.getInventories().collect { inventoryBuilder(it) }
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
     * Create a single inventory object
     * @param inventory
     */
    @Transaction
    public void createInventory(Inventory inventory) {
        inventory.trimNameAndDescription()

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
                        dataSource.sourceID.toString(),
                        PROVIDED_DATA_DB_TYPE,
                        inventory.id
                )
            }
        }
    }

    /**
     * Update a single inventory object by ID
     * @param inventory
     * @param inventoryID
     */
    @Transaction
    public void updateInventory(Inventory inventory, String inventoryID) {
        //Update top level inventory attributes
        inventory.trimNameAndDescription()
        inventoryDAO.updateInventory(inventory, inventoryID)

        //Update api query parameters
        createUpdateDeleteFields(inventory.apiQueryParams, inventoryID, inventoryID, QUERY_DB_TYPE)

        //Update consuming entities
        createUpdateDeleteConsumingEntities(inventory.consumingEntities, inventoryID)

        //Update provided data
        createUpdateDeleteProvideData(inventory.providedData, inventoryID)
    }

    /**
     * Create, update, or delete field objects.
     * @param fields
     * @param inventoryID
     * @param parentID
     * @param type
     */
    private void createUpdateDeleteFields(List<Field> fields, String inventoryID,
                                          String parentID,
                                          String type) {
        List<Field> currentFields = inventoryDAO.getFields(
                type, parentID, inventoryID)

        def currentFieldIDs = currentFields.collect { it.fieldID }

        fields.each { field ->
            if (currentFieldIDs.contains(field.fieldID)) {
                inventoryDAO.updateField(
                        (Field) field, parentID, type, inventoryID)
            } else {
                inventoryDAO.createField(
                        (Field) field, parentID, type, inventoryID)
            }
        }

        def fieldIDsToDelete = currentFieldIDs -
                fields.collect { it.fieldID }

        fieldIDsToDelete.each {
            inventoryDAO.deleteFields(inventoryID, it, parentID, type)
        }
    }

    /**
     * Create, update, or delete consuming entity objects
     * @param consumingEntities
     * @param inventoryID
     */
    private void createUpdateDeleteConsumingEntities(List<ConsumingEntity> consumingEntities,
                                                     String inventoryID) {
        List<ConsumingEntity> currentConsumingEntities =
                inventoryDAO.getConsumingEntities(inventoryID)

        def currentConsumingEntityIDs = currentConsumingEntities.collect { it.entityID }

        consumingEntities.each { consumingEntity ->
            if (currentConsumingEntityIDs.contains(consumingEntity.entityID)) {
                inventoryDAO.updateConsumingEntity((ConsumingEntity) consumingEntity, inventoryID)
            } else {
                inventoryDAO.createConsumingEntity((ConsumingEntity) consumingEntity, inventoryID)
            }
        }

        def consumingEntityIDsToDelete = currentConsumingEntityIDs -
                consumingEntities.collect { it.entityID }

        consumingEntityIDsToDelete.each {
            inventoryDAO.deleteConsumingEntities(inventoryID, it)
        }
    }

    /**
     * Create, update, or delete data source objects
     * @param providedData
     * @param inventoryID
     */
    private void createUpdateDeleteProvideData(List<DataSource> providedData,
                                               String inventoryID) {
        List<DataSource> currentProvidedData = inventoryDAO.getProvidedData(inventoryID)

        def currentProvidedDataIds = currentProvidedData.collect { it.sourceID }

        providedData.each { dataSource ->
            if (currentProvidedDataIds.contains(dataSource.sourceID)) {
                inventoryDAO.updateProvidedData((DataSource) dataSource, inventoryID)
                //Update provided data fields
                createUpdateDeleteFields(
                        dataSource.fields, inventoryID, dataSource.sourceID, PROVIDED_DATA_DB_TYPE)
            } else {
                inventoryDAO.createProvidedData((DataSource) dataSource, inventoryID)

                dataSource.fields.each { field ->
                    inventoryDAO.createField(
                            (Field) field, dataSource.sourceID, PROVIDED_DATA_DB_TYPE, inventoryID)
                }
            }
        }

        def providedDataIDsToDelete = currentProvidedDataIds -
                providedData.collect { it.sourceID }

        providedDataIDsToDelete.each {
            inventoryDAO.deleteProvidedData(inventoryID, it)
            inventoryDAO.deleteFields(inventoryID, null, it, PROVIDED_DATA_DB_TYPE)
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
        inventoryDAO.deleteFields(inventoryID, null, null, null)
        inventoryDAO.deleteConsumingEntities(inventoryID, null)
        inventoryDAO.deleteProvidedData(inventoryID, null)
    }
}
