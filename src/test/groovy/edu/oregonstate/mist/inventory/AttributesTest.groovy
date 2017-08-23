package edu.oregonstate.mist.inventory

import edu.oregonstate.mist.inventory.core.ConsumingEntity
import edu.oregonstate.mist.inventory.core.DataSource
import edu.oregonstate.mist.inventory.core.Inventory
import org.junit.Test

class AttributesTest {

    @Test
    public void testNullIsFalse() {
        DataSource dataSource = new DataSource(internal: null)

        assert dataSource.internal == false

        ConsumingEntity consumingEntity = new ConsumingEntity(internal: null)

        assert consumingEntity.internal == false
    }

    @Test
    public void testTrim() {
        String originalName = " Name    "
        String originalDescription = "  Description "

        Inventory inventory = new Inventory(
                name: originalName,
                description: originalDescription
        )

        def testNameAndDescription = { String name, String description ->
            assert inventory.name == name
            assert inventory.description == description
        }

        testNameAndDescription(originalName, originalDescription)

        inventory.trimNameAndDescription()

        testNameAndDescription(originalName.trim(), originalDescription.trim())
    }
}
