package edu.oregonstate.mist.inventory.mapper

import edu.oregonstate.mist.inventory.core.Field
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

class FieldMapper implements ResultSetMapper<Field> {
    public Field map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new Field(
                fieldID: rs.getString("CLIENT_FIELD_ID"),
                field: rs.getString("FIELD"),
                description: rs.getString("DESCRIPTION")
        )
    }
}
