package edu.oregonstate.mist.inventory

import edu.oregonstate.mist.api.Error

class ErrorMessages extends Error {
    static Error invalidUUID() {
        badRequest(getFromProperties('inventory.invalidUUID'))
    }

    static Error idExists() {
        badRequest(getFromProperties('inventory.idExists'))
    }

    static Error castError() {
        badRequest(getFromProperties('inventory.castError'))
    }

    static Error badType(String value, String allowedTypes) {
        badRequest(
                "${value} ${getFromProperties('inventory.badType')} ${allowedTypes}")
    }

    static Error noType(String allowedTypes) {
        badRequest(
                "${getFromProperties('inventory.noType')} ${allowedTypes}")
    }

    static Error otherType() {
        badRequest(getFromProperties('inventory.otherType'))
    }

    static Error badSourceType(String value, String allowedSourceTypes) {
        badRequest(
                "${value} ${getFromProperties('inventory.badSourceType')}" +
                        " ${allowedSourceTypes}"
        )
    }

    static Error noSourceType(String allowedSourceTypes) {
        badRequest(
                "${getFromProperties('inventory.noSourceType')} ${allowedSourceTypes}"
        )
    }

    static Error otherSourceType() {
        badRequest(getFromProperties('inventory.otherSourceType'))
    }

    static String getInventoryError() {
        getFromProperties('inventory.getInventoryError')
    }
}
