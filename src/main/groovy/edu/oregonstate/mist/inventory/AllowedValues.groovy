package edu.oregonstate.mist.inventory

class AllowedValues {

    List<String> list

    /**
     * Create english sentence from list of strings.
     * @return
     */
    public String pretty() {
        if (list.size() == 0) {
            null
        } else if (list.size() > 2) {
            list[0..-2].join(", ") + ", or " + list[-1]
        } else {
            list[0..-1].join(" or ")
        }
    }
}
