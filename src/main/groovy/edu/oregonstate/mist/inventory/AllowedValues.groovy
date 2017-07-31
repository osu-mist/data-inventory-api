package edu.oregonstate.mist.inventory

class AllowedValues {

    List<String> list

    /**
     * Create english sentence from list of strings.
     * @return
     */
    public String pretty() {
        list[0..-2].join(", ") + ", or " + list[-1]
    }
}
