package edu.oregonstate.mist.inventory

import org.junit.Test

class AllowedValuesTest {

    private AllowedValues testValues = new AllowedValues(list: ['foo', 'bar', 'eggplant'])

    @Test
    public void testPrettyString() {
        String prettyString = testValues.pretty()

        List<String> parsedPrettyString = prettyString.tokenize(',')
        println prettyString
        parsedPrettyString.each {
            println(it)
        }
        testValues.list.each {
            if (it == testValues.list[-1]) {
                assert "or " + it == parsedPrettyString[-1].trim()
            } else {
                assert it == parsedPrettyString[testValues.list.indexOf(it)].trim()
            }
        }
    }
}
