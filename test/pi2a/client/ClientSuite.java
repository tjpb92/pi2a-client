package pi2a.client;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Jeux de tests pour tester toutes les classes du projet
 *
 * @author Thierry Baribaud
 * @version 0.32.7
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    Bug190925Test.class,
    Bug230728Test.class,
    DissociateProviderContactFromPatrimonyTest.class,
    NotifyRepairRequestByMail_bug230321Test.class,
    ProviderIsGoingCheckedTest.class,
    ProviderContactedCheckedTest.class
})
public class ClientSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

}
