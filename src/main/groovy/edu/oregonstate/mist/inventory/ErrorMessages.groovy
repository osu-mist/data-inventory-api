package edu.oregonstate.mist.inventory

import edu.oregonstate.mist.api.Error

class ErrorMessages {
    static Error invalidUUID() {
        Error.badRequest(Error.prop.getProperty('inventory.invalidUUID'))
    }

    static Error idExists() {
        Error.badRequest(Error.prop.getProperty('inventory.idExists'))
    }

    static Error castError() {
        Error.badRequest(Error.prop.getProperty('inventory.castError'))
    }

    static Error badType(String value, String allowedTypes) {
        Error.badRequest(
                "${value} ${Error.prop.getProperty('inventory.badType')} ${allowedTypes}")
    }

    static Error noType(String allowedTypes) {
        Error.badRequest(
                "${Error.prop.getProperty('inventory.noType')} ${allowedTypes}")
    }

    static Error otherType() {
        Error.badRequest(Error.prop.getProperty('inventory.otherType'))
    }

    static Error badSourceType(String value, String allowedSourceTypes) {
        Error.badRequest(
                "${value} ${Error.prop.getProperty('inventory.badSourceType')}" +
                        " ${allowedSourceTypes}"
        )
    }

    static Error noSourceType(String allowedSourceTypes) {
        Error.badRequest(
                "${Error.prop.getProperty('inventory.noSourceType')} ${allowedSourceTypes}"
        )
    }

    static Error otherSourceType() {
        Error.badRequest(Error.prop.getProperty('inventory.otherSourceType'))
    }

    static String getInventoryError() {
        Error.prop.getProperty('inventory.getInventoryError')
    }
}
