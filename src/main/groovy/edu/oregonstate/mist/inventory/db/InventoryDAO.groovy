package edu.oregonstate.mist.inventory.db

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

    /**
     * Get all inventory objects that aren't deleted
     * @return List of inventory objects
     */
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

    /**
     * Get a single inventory object by ID
     * @param inventoryID
     * @return inventory object
     */
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

    /**
     * Get fields for query params or provided data fields
     * @param type
     * @param parentID
     * @return List of fields object
     */
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

    /**
     * Get consuming entities for an inventory object
     * @param inventoryID
     * @return List of consuming entities
     */
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

    /**
     * Get provided data for an inventory object
     * @param inventoryID
     * @return List of provided data objects
     */
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

    /**
     * Soft delete inventory object
     * @param inventoryID
     */
    @SqlUpdate("""
        UPDATE INVENTORY_INVENTORY
        SET DELETED_AT = SYSDATE
        WHERE INVENTORY_ID = :inventoryID
    """)
    abstract void deleteInventory(@Bind("inventoryID") String inventoryID)

    /**
     * Soft delete field objects
     * @param parentID
     * @param type
     */
    @SqlUpdate("""
        UPDATE INVENTORY_FIELDS
        SET DELETED_AT = SYSDATE
        WHERE INVENTORY_ID = :inventoryID
    """)
    abstract void deleteFields(@Bind("inventoryID") String inventoryID)

    /**
     * Soft delete consuming entity objects
     * @param inventoryID
     */
    @SqlUpdate("""
        UPDATE INVENTORY_CONSUMING_ENTITIES
        SET DELETED_AT = SYSDATE
        WHERE INVENTORY_ID = :inventoryID
    """)
    abstract void deleteConsumingEntities(@Bind("inventoryID") String inventoryID)

    /**
     * Soft delete provided data objects
     * @param inventoryID
     */
    @SqlUpdate("""
        UPDATE INVENTORY_PROVIDED_DATA
        SET DELETED_AT = SYSDATE
        WHERE INVENTORY_ID = :inventoryID
    """)
    abstract void deleteProvidedData(@Bind("inventoryID") String inventoryID)

    @Override
    void close()
}
