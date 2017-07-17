package edu.oregonstate.mist.inventory.mapper

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
                created: dbParser(rs.getString("CREATED_AT")),
                updated: dbParser(rs.getString("UPDATED_AT"))
        )
    }

    /**
     * Parse date string for DB into ZonedDateTime
     * @param dateString
     * @return ZonedDateTime
     */
    private static ZonedDateTime dbParser(String dateString) {
        dateString ? ZonedDateTime.parse(dateString, dbFormatter) : null
    }

    /**
     * Constant for format and timezone of date returned from DB.
     */
    private static DateTimeFormatter dbFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("UTC"))
}
