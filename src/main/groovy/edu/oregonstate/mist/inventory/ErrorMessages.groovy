package edu.oregonstate.mist.inventory

import edu.oregonstate.mist.api.Error
import edu.oregonstate.mist.inventory.resources.InventoryResource

class ErrorMessages {
    static Error invalidUUID() {
        Error.badRequest("ID is not a valid UUID. " +
                "Event ID must follow UUID structure detailed here: " +
                "https://tools.ietf.org/html/rfc4122.html")
    }

    static Error idExists() {
        Error.badRequest("Event ID already exists.")
    }

    static Error castError() {
        Error.badRequest("Inventory object contains invalid fields.")
    }

    static Error badType(String allowedTypes) {
        Error.badRequest("Type is not allowed. Allowed types are: ${allowedTypes}")
    }

    static Error otherType() {
        Error.badRequest("If type is Other, the field \"otherType\" cannot be null.")
    }
}
