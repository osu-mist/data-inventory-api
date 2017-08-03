package edu.oregonstate.mist.inventory

import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.inventory.core.ConsumingEntity
import edu.oregonstate.mist.inventory.core.DataSource
import edu.oregonstate.mist.inventory.core.Field
import edu.oregonstate.mist.inventory.core.Inventory
import edu.oregonstate.mist.inventory.db.InventoryDAO
import edu.oregonstate.mist.inventory.db.InventoryDAOWrapper
import groovy.mock.interceptor.MockFor
import groovy.mock.interceptor.StubFor
import org.junit.Test

import javax.ws.rs.core.UriBuilder

class InventoryDAOWrapperTest {
    URI selfLinkBase = new URI("https://www.foo.com")

    /**
     * Check that if the DAO returns null, so does the wrapperDAO method.
     */
    @Test
    public void testNullGetInventoryByID() {
        def mockDAO = new MockFor(InventoryDAO)

        mockDAO.demand.getInventoryByID() { null }

        def inventoryDAOWrapper = new InventoryDAOWrapper(
                inventoryDAO: mockDAO.proxyInstance(),
                selfLinkBase: selfLinkBase)

        assert !inventoryDAOWrapper.getInventoryById("test")
    }

    /**
     * Test that separate components of an inventory object
     * are merged correctly into a resourceObject.
     */
    @Test
    public void testGetInventoryByID() {
        Inventory testInventory = new Inventory(
                id: "123456foo",
                name: "Test Inventory",
                description: "Just some test data",
                type: "API"
        )
        List<Field> fields = []
        fields.add(new Field(
                fieldID: "1",
                field: "Foo",
                description: "Eggplant"
        ))
        List<ConsumingEntity> consumingEntities = []
        consumingEntities.add(new ConsumingEntity(
                entityID: "1",
                entityName: "Bar",
                applicationName: "App Thing",
                entityContactName: "Sales Person",
                entityEmail: "salesperson@bar.com",
                entityPhone: "555-555-5555",
                entityUrl: "www.bar.com",
                internal: false,
                dataManagementRequest: "www.linktofile.com"
        ))
        List<DataSource> dataSources = []
        dataSources.add(new DataSource(
                sourceID: "1",
                source: "Some huge data warehouse",
                sourceDescription: "Just a huge data warehouse",
                sourceType: "DB",
                internal: true
        ))

        def stubDAO = getStubInventoryDAO(testInventory, fields, consumingEntities, dataSources)

        InventoryDAO inventoryDAO = stubDAO.proxyInstance()
        def inventoryDAOWrapper = new InventoryDAOWrapper(
                inventoryDAO: inventoryDAO,
                selfLinkBase: selfLinkBase
        )

        ResourceObject daoResourceObject = inventoryDAOWrapper.getInventoryById("test")

        testInventory.apiQueryParams = fields
        testInventory.consumingEntities = consumingEntities
        dataSources[0].fields = fields
        testInventory.providedData = dataSources

        ResourceObject expectedResult = new ResourceObject(
                id: testInventory.id,
                type: "inventory",
                attributes: testInventory,
                links: [
                        'self': UriBuilder.fromUri(selfLinkBase)
                                .path(testInventory.id).build()
                ]
        )

        assert expectedResult.id == daoResourceObject.id
        assert expectedResult.type == daoResourceObject.type
        assert expectedResult.attributes == daoResourceObject.attributes
        assert expectedResult.links == daoResourceObject.links
    }

    /**
     * Build stub Inventory DAO with test data.
     * @param testInventory
     * @param fields
     * @param consumingEntities
     * @param dataSources
     * @return
     */
    private def getStubInventoryDAO(Inventory testInventory,
                                    List<Field> fields,
                                    List<ConsumingEntity> consumingEntities,
                                    List<DataSource> dataSources) {
        def stubDAO = new StubFor(InventoryDAO)

        stubDAO.demand.getInventoryByID() { testInventory }
        stubDAO.demand.getFields(2..2) {type, parentID, inventoryID -> fields}
        stubDAO.demand.getConsumingEntities() { consumingEntities }
        stubDAO.demand.getProvidedData() { dataSources }

        stubDAO
    }
}
