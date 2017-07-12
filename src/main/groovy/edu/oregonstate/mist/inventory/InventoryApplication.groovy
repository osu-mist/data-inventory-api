package edu.oregonstate.mist.inventory

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.api.Configuration
import edu.oregonstate.mist.inventory.db.InventoryDAO
import edu.oregonstate.mist.inventory.health.InventoryHealthCheck
import edu.oregonstate.mist.inventory.resources.InventoryResource
import io.dropwizard.jdbi.DBIFactory
import io.dropwizard.setup.Environment
import org.skife.jdbi.v2.DBI

/**
 * Main application class.
 */
class InventoryApplication extends Application<InventoryConfiguration> {
    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(InventoryConfiguration configuration, Environment environment) {
        this.setup(configuration, environment)

        DBIFactory factory = new DBIFactory()
        DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "jdbi")
        InventoryDAO inventoryDAO = jdbi.onDemand(InventoryDAO.class)
        environment.jersey().register(new InventoryResource(inventoryDAO,
                configuration.api.endpointUri))

        InventoryHealthCheck healthCheck = new InventoryHealthCheck(inventoryDAO)
        environment.healthChecks().register("inventoryHealthCheck", healthCheck)
    }

    /**
     * Instantiates the application class with command-line arguments.
     *
     * @param arguments
     * @throws Exception
     */
    public static void main(String[] arguments) throws Exception {
        new InventoryApplication().run(arguments)
    }
}
