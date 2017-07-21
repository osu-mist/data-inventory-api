package edu.oregonstate.mist.inventory.mapper

import edu.oregonstate.mist.inventory.core.DataSource
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper

import java.sql.ResultSet
import java.sql.SQLException

class DataSourceMapper implements ResultSetMapper<DataSource> {
    public DataSource map (int i, ResultSet rs, StatementContext sc) throws SQLException {
        new DataSource(
                sourceID: rs.getString("CLIENT_DATA_ID"),
                source: rs.getString("SOURCE"),
                sourceDescription: rs.getString("SOURCE_DESCRIPTION"),
                sourceType: rs.getString("SOURCE_TYPE"),
                otherSourceType: rs.getString("OTHER_SOURCE_TYPE"),
                apiUrl: rs.getString("API_URL"),
                internal: rs.getBoolean("INTERNAL")
        )
    }
}
