package edu.oregonstate.mist.inventory.db

import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.inventory.core.ConsumingEntity
import edu.oregonstate.mist.inventory.core.DataSource
import edu.oregonstate.mist.inventory.core.Field
import edu.oregonstate.mist.inventory.core.Inventory
import edu.oregonstate.mist.inventory.mapper.ConsumingEntityMapper
import edu.oregonstate.mist.inventory.mapper.DataSourceMapper
import edu.oregonstate.mist.inventory.mapper.FieldMapper
import edu.oregonstate.mist.inventory.mapper.InventoryMapper
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.SqlUpdate
import org.skife.jdbi.v2.sqlobject.customizers.Mapper

public interface InventoryDAO extends Closeable {

    @SqlQuery("SELECT 1 FROM dual")
    Integer checkHealth()

    @SqlQuery("""
        SELECT
            INVENTORY_ID,
            NAME,
            DESCRIPTION,
            TYPE,
            OTHER_TYPE,
            TO_CHAR(CREATED_AT, 'yyyy-mm-dd hh24:mi:ss') AS CREATED_AT,
            TO_CHAR(UPDATED_AT, 'yyyy-mm-dd hh24:mi:ss') AS UPDATED_AT
        FROM INVENTORY_INVENTORY
        WHERE DELETED_AT IS NULL
        """)
    @Mapper(InventoryMapper)
    List<Inventory> getInventories()

    @SqlQuery("""
        SELECT
            INVENTORY_ID,
            NAME,
            DESCRIPTION,
            TYPE,
            OTHER_TYPE,
            TO_CHAR(CREATED_AT, 'yyyy-mm-dd hh24:mi:ss') AS CREATED_AT,
            TO_CHAR(UPDATED_AT, 'yyyy-mm-dd hh24:mi:ss') AS UPDATED_AT
        FROM INVENTORY_INVENTORY
        WHERE DELETED_AT IS NULL
        AND INVENTORY_ID = :inventoryID
        """)
    @Mapper(InventoryMapper)
    Inventory getInventoryByID(@Bind("inventoryID") String inventoryID)

    @SqlQuery("""
        SELECT
            CLIENT_FIELD_ID,
            FIELD,
            DESCRIPTION
        FROM INVENTORY_FIELDS
        WHERE PARENT_ID = :parentID
        AND TYPE = :type
        AND DELETED_AT IS NULL
        """)
    @Mapper(FieldMapper)
    List<Field> getFields(@Bind("type") String type,
                          @Bind("parentID") String parentID)

    @SqlQuery("""
        SELECT
            CLIENT_ENTITY_ID,
            ENTITY_NAME,
            APPLICATION_NAME,
            ENTITY_CONTACT_NAME,
            ENTITY_EMAIL,
            ENTITY_PHONE,
            ENTITY_URL,
            INTERNAL,
            MOU
        FROM INVENTORY_CONSUMING_ENTITIES
        WHERE INVENTORY_ID = :inventoryID
        AND DELETED_AT IS NULL
        """)
    @Mapper(ConsumingEntityMapper)
    List<ConsumingEntity> getConsumingEntities(@Bind("inventoryID") String inventoryID)

    @SqlQuery("""
        SELECT
            DATA_ID,
            CLIENT_DATA_ID,
            SOURCE,
            SOURCE_DESCRIPTION,
            SOURCE_TYPE,
            OTHER_SOURCE_TYPE,
            API_URL,
            INTERNAL
        FROM INVENTORY_PROVIDED_DATA
        WHERE INVENTORY_ID = :inventoryID
        AND DELETED_AT is null
        """)
    @Mapper(DataSourceMapper)
    List<DataSource> getProvidedData(@Bind("inventoryID") String inventoryID)

    @SqlUpdate("""
        UPDATE INVENTORY_INVENTORY
        SET DELETED_AT = SYSDATE
        WHERE INVENTORY_ID = :inventoryID
    """)
    void deleteInventory(@Bind("inventoryID") String inventoryID)

    @SqlUpdate("""
        UPDATE INVENTORY_FIELDS
        SET DELETED_AT = SYSDATE
        WHERE PARENT_ID = :parentID
        AND TYPE = :type
    """)
    void deleteFields(@Bind("parentID") String parentID,
                      @Bind("type") String type)

    @SqlUpdate("""
        UPDATE INVENTORY_CONSUMING_ENTITIES
        SET DELETED_AT = SYSDATE
        WHERE INVENTORY_ID = :inventoryID
    """)
    void deleteConsumingEntities(@Bind("inventoryID") String inventoryID)

    @SqlUpdate("""
        UPDATE INVENTORY_PROVIDED_DATA
        SET DELETED_AT = SYSDATE
        WHERE INVENTORY_ID = :inventoryID
    """)
    void deleteProvidedData(@Bind("inventoryID") String inventoryID)

    @Override
    void close()
}
