package edu.oregonstate.mist.inventory.resources

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.inventory.core.Inventory
import edu.oregonstate.mist.inventory.db.InventoryDAO
import groovy.transform.TypeChecked
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.annotation.security.PermitAll
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("inventory")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@TypeChecked
class InventoryResource extends Resource {

    Logger logger = LoggerFactory.getLogger(InventoryResource.class)

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

    @Timed
    @GET
    @Path('{id: [0-9a-zA-Z-]+}')
    Response getInventoryByID(@PathParam("id") String inventoryID) {
        Inventory inventory = inventoryDAO.getInventoryByID(inventoryID)

        if (!inventory) {
            return notFound().build()
        }

        ResourceObject resourceObject = getResourceObject(inventory)

        ok(new ResultObject(data: resourceObject)).build()
    }

    @Timed
    @DELETE
    @Path('{id: [0-9a-zA-Z-]+}')
    Response deleteInventoryByID(@PathParam("id") String inventoryID) {
        Inventory inventory = inventoryDAO.getInventoryByID(inventoryID)
        Response response

        if (!inventory) {
            response = notFound().build()
        } else {
            try {
                deleteInventory(inventory)
                response = Response.noContent().build()
            } catch (Exception e) {
                logger.error("Error deleting inventory record or associated records.", e)
                response = internalServerError(
                        "There was a problem when deleting the inventory record. " +
                                "It may not have been deleted.").build()
            }
        }

        response
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

    private void deleteInventory(Inventory inventory) {
        ResourceObject resourceObject = getResourceObject(inventory)

        inventoryDAO.deleteInventory(resourceObject.id)
        inventoryDAO.deleteConsumingEntities(resourceObject.id)
        inventoryDAO.deleteProvidedData(resourceObject.id)
        inventoryDAO.deleteFields(resourceObject.id, QUERY_DB_TYPE)

        resourceObject.attributes["providedData"].each {
            inventoryDAO.deleteFields(it["internalID"].toString(), PROVIDED_DATA_DB_TYPE)
        }
    }
}
