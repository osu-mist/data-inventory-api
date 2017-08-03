package edu.oregonstate.mist.inventory

import org.junit.Test

class AllowedValuesTest {

    @Test
    public void testOverTwoItemPrettyString() {
        List<String> testValues = ['bob', 'john', 'frank']
        List<String> parsedPrettyString = getPrettyStringFromList(testValues).tokenize(',')

        testValues.each {
            if (it == testValues[-1]) {
                assert "or " + it == parsedPrettyString[-1].trim()
            } else {
                assert it == parsedPrettyString[testValues.indexOf(it)].trim()
            }
        }
    }

    @Test
    public void testTwoItemPrettyString() {
        List<String> testValues = ['bob', 'frank']

        assert  getPrettyStringFromList(testValues) == testValues[0] + " or " + testValues[1]
    }

    @Test
    public void testOneItemPrettyString() {
        List<String> testValues = ['bob']

        assert getPrettyStringFromList(testValues) == testValues[0]
    }

    @Test
    public void testZeroItemPrettyString() {
        List<String> testValues = []

        assert !getPrettyStringFromList(testValues)
    }

    private String getPrettyStringFromList(List<String> list) {
        AllowedValues testValues = new AllowedValues(list: list)

        testValues.pretty()
    }
}
