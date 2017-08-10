package edu.oregonstate.mist.api

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * Error representation class.
 */
class Error {
    Integer status
    String developerMessage
    String userMessage
    Integer code
    String details

    @JsonIgnore
    private static Properties prop = new Properties()

    /**
     * Static initializer to load error text
     * from the resource.properties file.
     */
    static {
        def stream = this.getResourceAsStream('resource.properties')
        if (stream == null) {
            throw new IOException("couldn't open resource.properties")
        }
        prop.load(stream)
        stream.close()
    }

    /**
     * Returns a new Error for a HTTP 400 ("bad request") response.
     *
     * @param message the error message
     * @return error
     */
    static Error badRequest(String message) {
        new Error(
            status: 400,
            developerMessage: message,
            userMessage: getFromProperties('badRequest.userMessage'),
            code: parseInt(getFromProperties('badRequest.code')),
            details: getFromProperties('badRequest.details')
        )
    }

    static Error invalidUUID() {
        badRequest(getFromProperties('badRequest.invalidUUID'))
    }

    static Error idExists() {
        badRequest(getFromProperties('badRequest.idExists'))
    }

    /**
     * Returns a new Error for a HTTP 404 ("not found") response.
     *
     * @return error
     */
    static Error notFound() {
        new Error(
            status: 404,
            developerMessage: getFromProperties('notFound.developerMessage'),
            userMessage: getFromProperties('notFound.userMessage'),
            code: parseInt(getFromProperties('notFound.code')),
            details: getFromProperties('notFound.details')
        )
    }

    /**
     * Returns a new Error for a HTTP 409 ("conflict") response.
     *
     * @return error
     */
    static Error conflict() {
        new Error(
            status: 409,
            developerMessage: getFromProperties('conflict.developerMessage'),
            userMessage: getFromProperties('conflict.userMessage'),
            code: parseInt(getFromProperties('conflict.code')),
            details: getFromProperties('conflict.details')
        )
    }

    /**
     * Returns a new Error for a HTTP 500 ("internal server error") response.
     *
     * @param message the error message
     * @return error
     */
    static Error internalServerError(String message) {
        new Error(
            status: 500,
            developerMessage: message,
            userMessage: getFromProperties('internalServerError.userMessage'),
            code: parseInt(getFromProperties('internalServerError.code')),
            details: getFromProperties('internalServerError.details')
        )
    }

    static String getFromProperties(String property) {
        prop.getProperty(property)
    }
    
    private static Integer parseInt(String s) {
        if (s != null) {
            Integer.parseInt(s)
        }
    }
}
