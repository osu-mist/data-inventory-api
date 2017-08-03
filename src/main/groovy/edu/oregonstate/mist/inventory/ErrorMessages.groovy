package edu.oregonstate.mist.inventory

import edu.oregonstate.mist.api.Error

class ErrorMessages {
    static Error invalidUUID() {
        Error.badRequest(Error.getFromProperties('inventory.invalidUUID'))
    }

    static Error idExists() {
        Error.badRequest(Error.getFromProperties('inventory.idExists'))
    }

    static Error castError() {
        Error.badRequest(Error.getFromProperties('inventory.castError'))
    }

    static Error badType(String value, String allowedTypes) {
        Error.badRequest(
                "${value} ${Error.getFromProperties('inventory.badType')} ${allowedTypes}")
    }

    static Error noType(String allowedTypes) {
        Error.badRequest(
                "${Error.getFromProperties('inventory.noType')} ${allowedTypes}")
    }

    static Error otherType() {
        Error.badRequest(Error.getFromProperties('inventory.otherType'))
    }

    static Error badSourceType(String value, String allowedSourceTypes) {
        Error.badRequest(
                "${value} ${Error.getFromProperties('inventory.badSourceType')}" +
                        " ${allowedSourceTypes}"
        )
    }

    static Error noSourceType(String allowedSourceTypes) {
        Error.badRequest(
                "${Error.getFromProperties('inventory.noSourceType')} ${allowedSourceTypes}"
        )
    }

    static Error otherSourceType() {
        Error.badRequest(Error.getFromProperties('inventory.otherSourceType'))
    }

    static String getInventoryError() {
        Error.getFromProperties('inventory.getInventoryError')
    }
}
