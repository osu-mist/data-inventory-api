package edu.oregonstate.mist.inventory

class AllowedValues {

    List<String> list

    AllowedValues(List<String> allowedTypes) {
        this.list = allowedTypes
    }

    /**
     * Create english sentence from list of strings.
     * @return
     */
    public String pretty() {
        String prettyString = ""

        list.each {
            if (it == list.last()) {
                prettyString += " or "
            } else if (it != list.first()) {
                prettyString += ", "
            }

            prettyString += it
        }

        prettyString
    }
}
