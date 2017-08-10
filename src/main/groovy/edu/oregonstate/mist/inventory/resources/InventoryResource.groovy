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

        ResourceObject newInventoryFromDB = inventoryDAOWrapper.getInventoryById(newInventory.id)

        if (!newInventoryFromDB) {
            internalServerError(ErrorMessages.getInventoryError()).build()
        } else {
            created(new ResultObject(
                    data: newInventoryFromDB
            )).build()
        }
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

        try {
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
            testConversion(resultObject)
        } catch (NullPointerException e) {
            errors.add(ErrorMessages.emptyError())

            // If we can't get a data object from the resultObject, return the errors.
            return errors
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

        def addNullError = { String fieldName ->
            errors.add(ErrorMessages.notNull(fieldName))
        }

        // If type is Talend, there shouldn't be any apiQueryParams.
        if (inventory.type != "API" && (inventory.apiQueryParams.size() != 0)) {
            errors.add(ErrorMessages.nonAPIQueryParams())
        } else if (nullID(inventory.apiQueryParams, "fieldID")) {
            addNullError("apiQueryParams.fieldID")
        }

        // If Type is Other, otherType may not be null.
        if (inventory.type == "Other" && !inventory.otherType) {
            errors.add(ErrorMessages.otherType())
        }

        if (nullID(inventory.consumingEntities, "entityID")) {
            addNullError("consumingEntities.entityID")
        }

        if (nullID(inventory.providedData, "sourceID")) {
            addNullError("providedData.sourceID")
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

            if (nullID(it.fields, "fieldID")) {
                addNullError("providedData.fields.fieldID")
            }

            // If the sourceType is not API, apiUrl should be null.
            if (it.sourceType != "API" && it.apiUrl) {
                errors.add(ErrorMessages.nonAPIWithAPIUrl())
            }
        }

        errors
    }

    /**
     * Returns true if a Field in a List<Field> contains a null fieldID
     * @param fields
     * @return
     */
    private Boolean nullID(def obj, String key) {
        obj.find {
            if (!it[key]) {
                return true
            }
            false
        }
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