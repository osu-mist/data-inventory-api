package edu.oregonstate.mist.inventory

class ErrorMessages {
    public final static String invalidUUID = "ID is not a valid UUID. " +
            "Event ID must follow UUID structure detailed here: " +
            "https://tools.ietf.org/html/rfc4122.html"

    public final static String idExists = "Event ID already exists."

    public final static String castError = "Inventory object contains invalid fields."
}
