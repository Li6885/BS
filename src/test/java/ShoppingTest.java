import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import queries.*;
import utils.ConnectConfig;
import utils.DatabaseConnector;

public class ShoppingTest {

    private DatabaseConnector connector;
    private ShoppingPriceSystem shopping;

    private static ConnectConfig connectConfig = null;

    static {
        try {
            // parse connection config from "resources/application.yaml"
            connectConfig = new ConnectConfig();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public ShoppingTest() {
        try {
            // connect to database
            connector = new DatabaseConnector(connectConfig);
            shopping = new ShoppingPriceSystemImpl(connector);
            System.out.println("Successfully init class shoppingWebTest.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Before
    public void prepareTest() {
        boolean connStatus = connector.connect();
        Assert.assertTrue(connStatus);
        System.out.println("Successfully connect to database.");
        ApiResult result = shopping.resetDatabase();
        if (!result.ok) {
            System.out.printf("Failed to reset database, reason: %s\n", result.message);
            Assert.fail();
        }
        System.out.println("Successfully reset database.");
    }

    @After
    public void afterTest() {
        boolean releaseStatus = connector.release();
        if (releaseStatus) {
            System.out.println("Successfully release database connection.");
        } else {
            System.out.println("Failed to release database connection.");
        }
    }
}