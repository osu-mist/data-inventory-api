package edu.oregonstate.mist.inventory

import edu.oregonstate.mist.api.Error

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

    static Error badType(String value, String allowedTypes) {
        Error.badRequest(
                "${value} is not a valid inventory type. Allowed types are: ${allowedTypes}")
    }

    static Error noType(String allowedTypes) {
        Error.badRequest(
                "attributes.type is a required field. Allowed values are: ${allowedTypes}")
    }

    static Error otherType() {
        Error.badRequest("If type is Other, the field otherType cannot be null.")
    }

    static Error badSourceType(String value, String allowedSourceTypes) {
        Error.badRequest(
                "${value} is not a valid sourceType. " +
                "Allowed source types are: ${allowedSourceTypes}"
        )
    }

    static Error noSourceType(String allowedSourceTypes) {
        Error.badRequest(
                "Provided data must have value for sourceType. " +
                "Allowed values are: ${allowedSourceTypes}"
        )
    }

    static Error otherSourceType() {
        Error.badRequest("If sourceType is Other, the field otherSourceType cannot be null.")
    }
}
