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
import org.skife.jdbi.v2.sqlobject.BindBean
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
            TO_CHAR(CREATED_AT AT TIME ZONE 'UTC', 'yyyy-mm-dd hh24:mi:ss') AS CREATED_AT,
            TO_CHAR(UPDATED_AT AT TIME ZONE 'UTC', 'yyyy-mm-dd hh24:mi:ss') AS UPDATED_AT
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
            TO_CHAR(CREATED_AT AT TIME ZONE 'UTC', 'yyyy-mm-dd hh24:mi:ss') AS CREATED_AT,
            TO_CHAR(UPDATED_AT AT TIME ZONE 'UTC', 'yyyy-mm-dd hh24:mi:ss') AS UPDATED_AT
        FROM INVENTORY_INVENTORY
        WHERE DELETED_AT IS NULL
        AND INVENTORY_ID = :inventoryID
        """)
    @Mapper(InventoryMapper)
    Inventory getInventoryByID(@Bind("inventoryID") String inventoryID)

    /**
     * Get fields for query params or provided data fields
     * @param type
     * @param parentID - the parent ID for the field.
     * The parent ID for an apiQueryParam is also the inventoryID
     * @param inventoryID - the parent inventory object for the field
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
        AND INVENTORY_ID = :inventoryID
        AND DELETED_AT IS NULL
        """)
    @Mapper(FieldMapper)
    List<Field> getFields(@Bind("type") String type,
                          @Bind("parentID") String parentID,
                          @Bind("inventoryID") String inventoryID)

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
            MOU,
            DMR
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
     * Create an inventory object
     * @param inventory
     */
    @SqlUpdate("""
        INSERT INTO INVENTORY_INVENTORY (
            INVENTORY_ID, NAME, DESCRIPTION,
            TYPE, OTHER_TYPE, CREATED_AT
            )
        VALUES (
            :id,
            :name,
            :description,
            :type,
            :otherType,
            SYSDATE
            )
        """)
    abstract void createInventory(@BindBean Inventory inventory)

    /**
     * Create a field object
     * @param field
     * @param parentID
     * @param type
     * @param inventoryID
     */
    @SqlUpdate("""
        INSERT INTO INVENTORY_FIELDS (
            FIELD_ID, CLIENT_FIELD_ID, PARENT_ID,
            FIELD, DESCRIPTION, TYPE, CREATED_AT, INVENTORY_ID
            )
        VALUES (
            INVENTORY_FIELDS_SEQ.NEXTVAL,
            :fieldID,
            :parentID,
            :field,
            :description,
            :type,
            SYSDATE,
            :inventoryID
            )
        """)
    abstract void createField(@BindBean Field field,
                              @Bind("parentID") String parentID,
                              @Bind("type") String type,
                              @Bind("inventoryID") String inventoryID)

    /**
     * Create a consuming entity object
     * @param consumingEntity
     * @param inventoryID
     */
    @SqlUpdate("""
        INSERT INTO INVENTORY_CONSUMING_ENTITIES (
            ENTITY_ID, CLIENT_ENTITY_ID, INVENTORY_ID,
            ENTITY_NAME, APPLICATION_NAME, ENTITY_CONTACT_NAME,
            ENTITY_EMAIL, ENTITY_PHONE, ENTITY_URL, INTERNAL,
            MOU, DMR, CREATED_AT
            )
        VALUES (
            INVENTORY_CONSUMERS_SEQ.NEXTVAL,
            :entityID,
            :inventoryID,
            :entityName,
            :applicationName,
            :entityContactName,
            :entityEmail,
            :entityPhone,
            :entityUrl,
            :internal,
            :mou,
            :dataManagementRequest,
            SYSDATE
            )
        """)
    abstract void createConsumingEntity(@BindBean ConsumingEntity consumingEntity,
                                        @Bind("inventoryID") String inventoryID)

    /**
     * Create a data source object
     * @param dataSource
     * @param inventoryID
     */
    @SqlUpdate("""
        INSERT INTO INVENTORY_PROVIDED_DATA (
            DATA_ID, CLIENT_DATA_ID, INVENTORY_ID,
            SOURCE, SOURCE_DESCRIPTION, SOURCE_TYPE,
            OTHER_SOURCE_TYPE, API_URL, INTERNAL, CREATED_AT
            )
        VALUES (
            INVENTORY_PROVIDED_DATA_SEQ.NEXTVAL,
            :sourceID,
            :inventoryID,
            :source,
            :sourceDescription,
            :sourceType,
            :otherSourceType,
            :apiUrl,
            :internal,
            SYSDATE
            )
        """)
    abstract void createProvidedData(@BindBean DataSource dataSource,
                                     @Bind("inventoryID") String inventoryID)

    /**
     * Update an inventory object by ID
     * @param inventory
     * @param inventoryID
     */
    @SqlUpdate("""
        UPDATE INVENTORY_INVENTORY
        SET NAME = :name,
            DESCRIPTION = :description,
            TYPE = :type,
            OTHER_TYPE = :otherType,
            UPDATED_AT = SYSDATE
        WHERE INVENTORY_ID = :inventoryID
        """)
    abstract void updateInventory(@BindBean Inventory inventory,
                                  @Bind("inventoryID") String inventoryID)

    /**
     * Update a field object
     * @param field
     * @param parentID
     * @param type
     * @param inventoryID
     */
    @SqlUpdate("""
        UPDATE INVENTORY_FIELDS
        SET FIELD = :field,
            DESCRIPTION = :description,
            UPDATED_AT = SYSDATE
        WHERE CLIENT_FIELD_ID = :fieldID
            AND PARENT_ID = :parentID
            AND TYPE = :type
            AND INVENTORY_ID = :inventoryID
        """)
    abstract void updateField(@BindBean Field field,
                              @Bind("parentID") String parentID,
                              @Bind("type") String type,
                              @Bind("inventoryID") String inventoryID)

    /**
     * Update a consuming entity object
     * @param consumingEntity
     * @param inventoryID
     */
    @SqlUpdate("""
        UPDATE INVENTORY_CONSUMING_ENTITIES
        SET ENTITY_NAME = :entityName,
            APPLICATION_NAME = :applicationName,
            ENTITY_CONTACT_NAME = :entityContactName,
            ENTITY_EMAIL = :entityEmail,
            ENTITY_PHONE = :entityPhone,
            ENTITY_URL = :entityUrl,
            INTERNAL = :internal,
            MOU = :mou,
            DMR = :dataManagementRequest,
            UPDATED_AT = SYSDATE
        WHERE CLIENT_ENTITY_ID = :entityID
            AND INVENTORY_ID = :inventoryID
        """)
    abstract void updateConsumingEntity(@BindBean ConsumingEntity consumingEntity,
                                        @Bind("inventoryID") String inventoryID)

    /**
     * Update a data source object
     * @param dataSource
     * @param inventoryID
     */
    @SqlUpdate("""
        UPDATE INVENTORY_PROVIDED_DATA
        SET SOURCE = :source,
            SOURCE_DESCRIPTION = :sourceDescription,
            SOURCE_TYPE = :sourceType,
            OTHER_SOURCE_TYPE = :otherSourceType,
            API_URL = :apiUrl,
            INTERNAL = :internal,
            UPDATED_AT = SYSDATE
        WHERE CLIENT_DATA_ID = :sourceID
            AND INVENTORY_ID = :inventoryID
        """)
    abstract void updateProvidedData(@BindBean DataSource dataSource,
                                     @Bind("inventoryID") String inventoryID)

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
            AND (CLIENT_FIELD_ID = :clientFieldID OR :clientFieldID IS NULL)
            AND (PARENT_ID = :parentID OR :parentID IS NULL)
            AND (TYPE = :type OR :type IS NULL)
            AND DELETED_AT IS NULL
    """)
    abstract void deleteFields(@Bind("inventoryID") String inventoryID,
                               @Bind("clientFieldID") String clientFieldID,
                               @Bind("parentID") String parentID,
                               @Bind("type") String type)

    /**
     * Soft delete consuming entity objects
     * @param inventoryID
     */
    @SqlUpdate("""
        UPDATE INVENTORY_CONSUMING_ENTITIES
        SET DELETED_AT = SYSDATE
        WHERE INVENTORY_ID = :inventoryID
            AND (CLIENT_ENTITY_ID = :entityID OR :entityID IS NULL)
            AND DELETED_AT IS NULL
    """)
    abstract void deleteConsumingEntities(@Bind("inventoryID") String inventoryID,
                                          @Bind("entityID") String entityID)

    /**
     * Soft delete provided data objects
     * @param inventoryID
     */
    @SqlUpdate("""
        UPDATE INVENTORY_PROVIDED_DATA
        SET DELETED_AT = SYSDATE
        WHERE INVENTORY_ID = :inventoryID
            AND (CLIENT_DATA_ID = :sourceID OR :sourceID IS NULL)
            AND DELETED_AT IS NULL
    """)
    abstract void deleteProvidedData(@Bind("inventoryID") String inventoryID,
                                     @Bind("sourceID") String sourceID)

    @Override
    void close()
}
