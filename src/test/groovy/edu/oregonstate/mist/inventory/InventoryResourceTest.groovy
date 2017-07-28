package edu.oregonstate.mist.inventory

import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.inventory.resources.InventoryResource
import org.junit.Test

class InventoryResourceTest {

    private InventoryResource inventoryResource = new InventoryResource(null)

    /**
     * Test that a known-good inventory doesn't return errors
     */
    @Test
    public void testGoodInventory() {
        def goodInventory = [
                'name':'good name',
                'description': 'good description',
                'type': 'API',
                'otherType': null,
                'apiQueryParams': [
                        [
                                'fieldID': '1',
                                'field': 'good field',
                                'description': 'good description'
                        ]
                ],
                'consumingEntities': [
                        [
                                'entityID': '1',
                                'entityName': 'some department somewhere',
                                'applicationName': 'cool app',
                                'entityContactName': 'test person',
                                'entityEmail': 'email@domain.com',
                                'entityPhone': '555-555-5555',
                                'entityUrl': 'www.somedepartmentsomewhere.com',
                                'internal': false,
                                'mou': null,
                                'dataManagementRequest': 'www.example.com',
                        ]
                ],
                'providedData': [
                        [
                                'sourceID': '1',
                                'source': 'Some DB',
                                'sourceDescription': 'Some DB somewhere',
                                'sourceType': 'Database',
                                'otherSourceType': null,
                                'apiUrl': null,
                                'internal': true,
                                'fields': [
                                        [
                                                'fieldID': 1,
                                                'field': 'someField',
                                                'description': 'some field being provided'
                                        ]
                                ]
                        ],
                        [
                                'sourceID': '2',
                                'source': 'Some DB',
                                'sourceDescription': 'Some DB somewhere',
                                'sourceType': 'Other',
                                'otherSourceType': 'Not DB or API',
                                'apiUrl': null,
                                'internal': true,
                                'fields': [
                                        [
                                                'fieldID': 1,
                                                'field': 'someField',
                                                'description': 'some field being provided'
                                        ]
                                ]
                        ]
                ]
        ]

        assert getErrorCount(goodInventory) == 0
    }

    /**
     * Test that an object with an unrecognized field returns errors.
     */
    @Test
    public void testMalformedInventory() {
        def badInventory = ['name': 'Test Name',
                            'description': 'Test description',
                            'badField': 'bad data']
        assert getErrorCount(badInventory) == 1
    }

    /**
     * Test various inventory types return errors as necessary.
     */
    @Test
    public void testInventoryTypes() {
        def badType = ['type': 'BadType']
        assert getErrorCount(badType) == 1

        def noType = ['type': null]
        assert getErrorCount(noType) == 1

        def nullOther = [
                'type': 'Other',
                'otherType': null
        ]
        assert getErrorCount(nullOther) == 1

        def goodType = ['type': 'Talend']
        assert getErrorCount(goodType) == 0
    }

    /**
     * Test various source types return errors as necessary.
     */
    @Test
    public void testSourceTypes() {
        def inventoryWithProvidedData = [
                'type': 'API',
                'providedData': [
                        [
                                'sourceID': '1',
                                'source': 'Some DB',
                                'sourceDescription': 'Some DB somewhere',
                                'sourceType': 'Excel Spreadsheet',
                                'otherSourceType': null,
                        ],
                        [
                                'sourceID': '2',
                                'source': 'Some DB',
                                'sourceDescription': 'Some DB somewhere',
                                'sourceType': 'Other',
                                'otherSourceType': 'Not DB or API',
                        ]
                ]
        ]
        assert getErrorCount(inventoryWithProvidedData) == 1

        inventoryWithProvidedData.providedData[0].sourceType = 'Other'
        assert getErrorCount(inventoryWithProvidedData) == 1

        inventoryWithProvidedData.providedData[0].sourceType = null
        assert getErrorCount(inventoryWithProvidedData) == 1

        inventoryWithProvidedData.providedData[0].sourceType = 'API'
        assert getErrorCount(inventoryWithProvidedData) == 0
    }

    /**
     * Get number of errors from inventory object
     * @param inventory
     * @return
     */
    private Integer getErrorCount(def inventory) {
        inventoryResource.getErrors(inventoryToResultObject(inventory)).size()
    }

    /**
     * Create ResultObject from inventory object.
     * @param inventory
     * @return
     */
    private inventoryToResultObject(def inventory) {
        new ResultObject(
                data: new ResourceObject(attributes: inventory)
        )
    }
}
