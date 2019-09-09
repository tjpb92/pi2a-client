package pi2a.client;

import bkgpi2a.DissociateProviderContactFromPatrimony;
import bkgpi2a.HttpsClient;
import bkgpi2a.Identifiants;
import bkgpi2a.WebServer;
import bkgpi2a.WebServerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import utils.ApplicationProperties;

/**
 * Programme de test de la classe DissociateProviderContactFromPatrimony
 *
 * @author Thierry Baribaud
 * @version 0.20
 */
public class DissociateProviderContactFromPatrimonyTest {

    /**
     * Common Jackson object mapper
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public DissociateProviderContactFromPatrimonyTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of serialization from and to a file in Json format, of class
     * DissociateProviderContactFromPatrimony.
     */
    @Test
    public void testDissociateProviderContactFromPatrimonyJsonSerialization() {
        System.out.println("DissociateProviderContactFromPatrimonyJsonSerialization");

        String filename = "DissociateProviderContactFromPatrimony.json";
        String testFilename = "testDissociateProviderContactFromPatrimony.json";
        DissociateProviderContactFromPatrimony dissociateProviderContactFromPatrimony = null;
        DissociateProviderContactFromPatrimony expDissociateProviderContactFromPatrimony = null;
        String json;

        try {
            dissociateProviderContactFromPatrimony = objectMapper.readValue(new File(filename), DissociateProviderContactFromPatrimony.class);
            System.out.println("dissociateProviderContactFromPatrimony:" + dissociateProviderContactFromPatrimony);
            objectMapper.writeValue(new File(testFilename), dissociateProviderContactFromPatrimony);

            json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dissociateProviderContactFromPatrimony);
            System.out.println("    json(dissociateProviderContactFromPatrimony):" + json);

            expDissociateProviderContactFromPatrimony = objectMapper.readValue(new File(filename), DissociateProviderContactFromPatrimony.class);
            System.out.println("expDissociateProviderContactFromPatrimony:" + expDissociateProviderContactFromPatrimony);
        } catch (IOException ex) {
            Logger.getLogger(DissociateProviderContactFromPatrimonyTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
        assertNotNull(dissociateProviderContactFromPatrimony);
        assertNotNull(expDissociateProviderContactFromPatrimony);
        assertEquals(dissociateProviderContactFromPatrimony.toString(), expDissociateProviderContactFromPatrimony.toString());
    }

    /**
     * Test of serialization from and to a file in Json format, of class
     * DissociateProviderContactFromPatrimony.
     */
    @Test
    public void testDissociateProviderContact() {
        String[] args = {"-d"};
        GetArgs getArgs;
        WebServer webServer;
        ApplicationProperties applicationProperties;
        boolean debugMode;
        String value;
        Identifiants identifiants = new Identifiants();
        HttpsClient httpsClient;

        System.out.println("DissociateProviderContact");

        try {
            System.out.println("Analyse des arguments de la ligne de commande ...");
            getArgs = new GetArgs(args);
            System.out.println(getArgs);
            assertNotNull(getArgs);
            debugMode = getArgs.getDebugMode();

            System.out.println("Lecture des paramètres d'exécution ...");
            applicationProperties = new ApplicationProperties("pi2a-client.prop");

            System.out.println("Lecture des paramètres du serveur Web ...");
            webServer = new WebServer(getArgs.getWebServerType(), applicationProperties);
            if (debugMode) {
                System.out.println(webServer);
            }
            
            value = applicationProperties.getProperty(getArgs.getWebServerType() + ".webserver.login");
            if (value != null) {
                identifiants.setLogin(value);
            } else {
                throw new WebServerException("Nom utilisateur pour l'accès Web non défini");
            }

            value = applicationProperties.getProperty(getArgs.getWebServerType() + ".webserver.passwd");
            if (value != null) {
                identifiants.setPassword(value);
            } else {
                throw new WebServerException("Mot de passe pour l'accès Web non défini");
            }
            if (debugMode) {
                System.out.println(identifiants);
            }
            
            System.out.println("Ouverture de la connexion au site Web : " + webServer.getName());
            httpsClient = new HttpsClient(webServer.getIpAddress(), identifiants, debugMode, false);

            System.out.println("Authentification en cours ...");
            httpsClient.sendPost(HttpsClient.REST_API_PATH + HttpsClient.LOGIN_CMDE);

        } catch (utils.GetArgsException | IOException | WebServerException ex) {
            Logger.getLogger(DissociateProviderContactFromPatrimonyTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(DissociateProviderContactFromPatrimonyTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
}
