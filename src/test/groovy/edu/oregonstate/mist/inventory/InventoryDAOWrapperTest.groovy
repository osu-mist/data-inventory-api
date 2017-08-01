package edu.oregonstate.mist.inventory

import edu.oregonstate.mist.inventory.db.InventoryDAO
import edu.oregonstate.mist.inventory.db.InventoryDAOWrapper
import groovy.mock.interceptor.MockFor
import org.junit.Test

class InventoryDAOWrapperTest {
    URI selfLinkBase = new URI("https://www.foo.com")

    @Test
    public void testNullGetInventoryByID() {
        def mockDAO = new MockFor(InventoryDAO)

        mockDAO.demand.getInventoryByID() { null }

        def inventoryDAOWrapper = new InventoryDAOWrapper(
                inventoryDAO: mockDAO.proxyInstance(),
                selfLinkBase: selfLinkBase)

        assert !inventoryDAOWrapper.getInventoryById("test")
    }
}
