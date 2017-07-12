package edu.oregonstate.mist.inventory.mapper

import edu.oregonstate.mist.inventory.core.ConsumingEntity
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

class ConsumingEntityMapper implements ResultSetMapper<ConsumingEntity> {
    public ConsumingEntity map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new ConsumingEntity(
                entityID: rs.getString("CLIENT_ENTITY_ID"),
                entityName: rs.getString("ENTITY_NAME"),
                applicationName: rs.getString("APPLICATION_NAME"),
                entityContactName: rs.getString("ENTITY_CONTACT_NAME"),
                entityEmail: rs.getString("ENTITY_EMAIL"),
                entityPhone: rs.getString("ENTITY_PHONE"),
                entityUrl: rs.getString("ENTITY_URL"),
                internal: rs.getBoolean("INTERNAL"),
                mou: rs.getString("MOU")
        )
    }
}
