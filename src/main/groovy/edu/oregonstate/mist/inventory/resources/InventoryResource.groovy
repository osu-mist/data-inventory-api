package edu.oregonstate.mist.inventory.resources

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.Error
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.inventory.AllowedValues
import edu.oregonstate.mist.inventory.ErrorMessages
import edu.oregonstate.mist.inventory.core.ConsumingEntity
import edu.oregonstate.mist.inventory.core.DataSource
import edu.oregonstate.mist.inventory.core.Field
import edu.oregonstate.mist.inventory.core.Inventory
import edu.oregonstate.mist.inventory.db.InventoryDAOWrapper
import groovy.transform.TypeChecked
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.annotation.security.PermitAll
import javax.validation.Valid
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import static java.util.UUID.randomUUID

@Path("inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
@TypeChecked
class InventoryResource extends Resource {

    Logger logger = LoggerFactory.getLogger(InventoryResource.class)

    private InventoryDAOWrapper inventoryDAOWrapper

    AllowedValues allowedTypes = new AllowedValues(list: ['API', 'Talend', 'Other'])
    AllowedValues allowedSourceTypes = new AllowedValues(list: ['API', 'Database', 'Other'])

    InventoryResource(InventoryDAOWrapper inventoryDAOWrapper) {
        this.inventoryDAOWrapper = inventoryDAOWrapper
    }

    /**
     * Get all inventory objects that aren't deleted.
     * @return
     */
    @Timed
    @GET
    Response getAllInventories() {
        List<ResourceObject> inventories = inventoryDAOWrapper.getAllInventories()

        ok(new ResultObject(data: inventories)).build()
    }

    /**
     * Get one inventory object by ID.
     * @param inventoryID
     * @return
     */
    @Timed
    @GET
    @Path('{id: [0-9a-zA-Z-]+}')
    Response getInventoryByID(@PathParam("id") String inventoryID) {
        ResourceObject inventory = inventoryDAOWrapper.getInventoryById(inventoryID)

        if (!inventory) {
            return notFound().build()
        }

        ok(new ResultObject(data: inventory)).build()
    }

    /**
     * Create a new inventory object.
     * @param newResultObject
     * @return
     */
    @Timed
    @POST
    Response createInventory(@Valid ResultObject newResultObject) {
        List<Error> errors = getErrors(newResultObject)

        if (errors) {
            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.BAD_REQUEST)
            return responseBuilder.entity(errors).build()
        }
        Inventory newInventory = resultObjectToInventory(newResultObject)
        newInventory.id = newResultObject.data['id'] ?: randomUUID() as String

        inventoryDAOWrapper.createInventory(newInventory)

        ResultObject newCreatedInventory = new ResultObject(
                data: (inventoryDAOWrapper.getInventoryById(newInventory.id))
        )

        created(newCreatedInventory).build()
    }

    /**
     * Delete one inventory object by ID.
     * @param inventoryID
     * @return
     */
    @Timed
    @DELETE
    @Path('{id: [0-9a-zA-Z-]+}')
    Response deleteInventoryByID(@PathParam("id") String inventoryID) {
        ResourceObject inventory = inventoryDAOWrapper.getInventoryById(inventoryID)
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

    /**
     * Cast attributes object in resultObject as Inventory object.
     * @param resultObject
     * @return
     */
    private Inventory resultObjectToInventory(ResultObject resultObject) {
        (Inventory) resultObject.data['attributes']
    }

    /**
     * Check user-submitted resultObject for errors.
     * @param resultObject
     * @return
     */
    public List<Error> getErrors(ResultObject resultObject) {
        List<Error> errors = []

        // Invalid UUID
        if (resultObject.data["id"]) {
            String id = resultObject.data["id"]

            if (!id.matches(uuidRegEx)) {
                errors.add(ErrorMessages.invalidUUID())
            }

            if (inventoryDAOWrapper.getInventoryById(id)) {
                errors.add(ErrorMessages.idExists())
            }
        }

        // Try casting resultObject as Inventory object.
        try {
            testConversion(resultObject)
        } catch (GroovyCastException e) {
            errors.add(ErrorMessages.castError())

            // If it can't cast the ResultObject as an Inventory object, return.
            return errors
        }

        Inventory inventory = resultObjectToInventory(resultObject)

        // Type is not null and is an allowed type.
        if (!inventory.type) {
            errors.add(ErrorMessages.noType(allowedTypes.pretty()))
        } else if (!(inventory.type in allowedTypes.list)) {
            errors.add(ErrorMessages.badType(
                    inventory.type.toString(), allowedTypes.pretty()))
        }

        // If Type is Other, otherType may not be null.
        if (inventory.type == "Other" && !inventory.otherType) {
            errors.add(ErrorMessages.otherType())
        }

        inventory.providedData.each {
            if (!it.sourceType) {
                errors.add(ErrorMessages.noSourceType(allowedSourceTypes.pretty()))
            } else if (!(it.sourceType in allowedSourceTypes.list)) {
                errors.add(ErrorMessages.badSourceType(
                        it.sourceType.toString(), allowedSourceTypes.pretty()))
            }

            if (it.sourceType == "Other" && !it.otherSourceType) {
                errors.add(ErrorMessages.otherSourceType())
            }
        }

        errors
    }

    /**
     * Test casting inventory object and various subclasses.
     * @param resultObject
     */
    public void testConversion(ResultObject resultObject) {
        Inventory inventory

        inventory = resultObjectToInventory(resultObject)

        inventory.apiQueryParams.each { field ->
            (Field) field
        }

        inventory.consumingEntities.each { consumingEntity ->
            (ConsumingEntity) consumingEntity
        }

        inventory.providedData.each { dataSource ->
            (DataSource) dataSource

            dataSource.fields.each { field ->
                (Field) field
            }
        }
    }
}