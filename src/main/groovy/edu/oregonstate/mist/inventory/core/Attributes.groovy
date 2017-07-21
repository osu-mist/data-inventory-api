package edu.oregonstate.mist.inventory.core

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore

import java.time.ZonedDateTime

class Inventory {
    @JsonIgnore
    String id
    String name
    String description
    String type
    String otherType
    List<Field> apiQueryParams = []
    List<ConsumingEntity> consumingEntities = []
    List<DataSource> providedData = []

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC")
    ZonedDateTime created

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC")
    ZonedDateTime updated

    @Override
    public String toString() {
        "Inventory{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", otherType='" + otherType + '\'' +
                ", apiQueryParams=" + apiQueryParams +
                ", consumingEntities=" + consumingEntities +
                ", providedData=" + providedData +
                ", created=" + created +
                ", updated=" + updated +
                '}'
    }
}

class Field {
    String fieldID
    String field
    String description
}

class ConsumingEntity {
    String entityID
    String entityName
    String applicationName
    String entityContactName
    String entityEmail
    String entityPhone
    String entityUrl
    Boolean internal
    String mou
}

class DataSource {
    String sourceID
    String source
    String sourceDescription
    String sourceType
    String otherSourceType
    String apiUrl
    Boolean internal
    List<Field> fields = []
}
