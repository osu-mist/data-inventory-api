package edu.oregonstate.mist.inventory.resources

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.inventory.db.InventoryDAOWrapper
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
import javax.ws.rs.core.UriBuilder

@Path("inventory")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@TypeChecked
class InventoryResource extends Resource {

    Logger logger = LoggerFactory.getLogger(InventoryResource.class)

    private InventoryDAOWrapper inventoryDAOWrapper
    private URI endpointUri

    InventoryResource(InventoryDAOWrapper inventoryDAOWrapper, URI endpointUri) {
        this.inventoryDAOWrapper = inventoryDAOWrapper
        this.endpointUri = UriBuilder.fromUri(endpointUri).path(this.class).build()
    }

    /**
     * Get all inventory objects that aren't deleted
     * @return
     */
    @Timed
    @GET
    Response getAllInventories() {
        List<ResourceObject> inventories = inventoryDAOWrapper.getAllInventories(
                endpointUri)

        ok(new ResultObject(data: inventories)).build()
    }

    /**
     * Get one inventory object by ID
     * @param inventoryID
     * @return
     */
    @Timed
    @GET
    @Path('{id: [0-9a-zA-Z-]+}')
    Response getInventoryByID(@PathParam("id") String inventoryID) {
        ResourceObject inventory = inventoryDAOWrapper.getInventoryById(
                inventoryID, endpointUri
        )

        if (!inventory) {
            return notFound().build()
        }

        ok(new ResultObject(data: inventory)).build()
    }

    /**
     * Delete one inventory object by ID
     * @param inventoryID
     * @return
     */
    @Timed
    @DELETE
    @Path('{id: [0-9a-zA-Z-]+}')
    Response deleteInventoryByID(@PathParam("id") String inventoryID) {
        ResourceObject inventory = inventoryDAOWrapper.getInventoryById(
                inventoryID, endpointUri
        )
        Response response

        if (!inventory) {
            response = notFound().build()
        } else {
            try {
                inventoryDAOWrapper.deleteInventory(inventoryID)
                response = Response.noContent().build()
            } catch (Exception e) {
                logger.error("Error deleting inventory record or associated records.", e)
                response = internalServerError(
                        "There was a problem when deleting the inventory record.")
                        .build()
            }
        }

        response
    }
}