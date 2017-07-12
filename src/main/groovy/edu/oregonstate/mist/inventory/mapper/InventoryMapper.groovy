package edu.oregonstate.mist.inventory.mapper

import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.inventory.core.Inventory
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class InventoryMapper implements ResultSetMapper<Inventory> {
    public Inventory map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new Inventory(
                id: rs.getString("INVENTORY_ID"),
                name: rs.getString("NAME"),
                description: rs.getString("DESCRIPTION"),
                type: rs.getString("TYPE"),
                otherType: rs.getString("OTHER_TYPE"),
                created: (rs.getString("CREATED_AT")) ?
                        ZonedDateTime.parse(rs.getString("CREATED_AT"), dbFormatter) : null,
                updated: (rs.getString("UPDATED_AT")) ?
                        ZonedDateTime.parse(rs.getString("UPDATED_AT"), dbFormatter) : null
        )
    }

    private static DateTimeFormatter dbFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("UTC"))
}
