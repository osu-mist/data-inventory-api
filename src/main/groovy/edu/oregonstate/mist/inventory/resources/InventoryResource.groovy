package edu.oregonstate.mist.inventory.resources

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.inventory.core.Inventory
import edu.oregonstate.mist.inventory.db.InventoryDAO
import groovy.transform.TypeChecked

import javax.annotation.security.PermitAll
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("inventory")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@TypeChecked
class InventoryResource extends Resource {

    private InventoryDAO inventoryDAO
    private URI endpointUri

    private final String QUERY_DB_TYPE = "Query"
    private final String PROVIDED_DATA_DB_TYPE = "Provided Data"

    InventoryResource(InventoryDAO inventoryDAO, URI endpointUri) {
        this.inventoryDAO = inventoryDAO
        this.endpointUri = endpointUri
    }

    @Timed
    @GET
    Response getAllInventories() {
        List<Inventory> inventories = inventoryDAO.getInventories()
        List<ResourceObject> resourceObjects = getResourceObjects(inventories)

        ok(new ResultObject(data: resourceObjects)).build()
    }

    private List<ResourceObject> getResourceObjects(List<Inventory> inventories) {
        List<ResourceObject> resourceObjects = []

        inventories.each {
            resourceObjects += getResourceObject(it)
        }

        resourceObjects
    }

    private ResourceObject getResourceObject (Inventory inventory) {
        inventory.apiQueryParams = inventoryDAO.getFields(QUERY_DB_TYPE, inventory.id)
        inventory.consumingEntities = inventoryDAO.getConsumingEntities(inventory.id)
        inventory.providedData = inventoryDAO.getProvidedData(inventory.id)

        inventory.providedData.each {
            it.fields = inventoryDAO.getFields(
                    PROVIDED_DATA_DB_TYPE, it.internalID
            )
        }

        new ResourceObject(
                id: inventory.id,
                type: "inventory",
                attributes: inventory
        )
    }
}
