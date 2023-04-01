package pi2a.client;

import bkgpi2a.SimplifiedRequestDetailedView;
import bkgpi2a.SimplifiedRequestSearchView;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static pi2a.client.Pi2aClient.sendAlert;
import utils.ApplicationProperties;
import utils.GetArgsException;

/**
 * Programme de test de la classe PI2AClient.sendAlert() vis à vis du bug du 21
 * mars 2023 avec l'objet AgencyAbstract, issue#41.
 *
 * @author Thierry Baribaud
 * @version 0.32.2
 */
public class NotifyRepairRequestByMail_bug230321Test {

    /**
     * Pour convertir les datetimes du format texte au format DateTime et vice
     * versa
     */
    public static final DateTimeFormatter isoDateTimeFormat1 = ISODateTimeFormat.dateTimeParser();

    /**
     * Common Jackson object mapper
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Serveur de mail pour les notifications
     */
    private static MailServer mailServer;

    /**
     * debugMode : fonctionnement du programme en mode debug (true/false).
     * Valeur par défaut : false.
     */
    private static boolean debugMode = false;

    public NotifyRepairRequestByMail_bug230321Test() {
    }

    @BeforeClass
    public static void setUpClass() {
        String[] args = {"-d"};
        GetArgs getArgs;
        ApplicationProperties applicationProperties;

        try {
            System.out.println("Analyse des arguments de la ligne de commande ...");
            getArgs = new GetArgs(args);
            debugMode = getArgs.getDebugMode();

            System.out.println("Lecture des paramètres d'exécution ...");
            applicationProperties = new ApplicationProperties("pi2a-client.prop");

            System.out.println("Lecture des paramètres du serveur de mail ...");
            mailServer = new MailServer(applicationProperties);
            System.out.println("Paramètres du serveur Mongo lus.");
            if (debugMode) {
                System.out.println(mailServer);
            }
        } catch (GetArgsException ex) {
            Logger.getLogger(NotifyRepairRequestByMail_bug230321Test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NotifyRepairRequestByMail_bug230321Test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MailServer.MailServerException ex) {
            Logger.getLogger(NotifyRepairRequestByMail_bug230321Test.class.getName()).log(Level.SEVERE, null, ex);
        }

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
     * Test de la notification par mail d'une demande d'intervention venant de
     * la mobilité.
     */
    @Test
    public void testNotifRepairReqestByMail_bug230321() {
        System.out.println("NotifyRepairRequestByMail_bug230321");

        String filename = "SimplifiedRequestDetailedView_bug230321.json";
        SimplifiedRequestDetailedView simplifiedRequestDetailedView;
        SimplifiedRequestSearchView simplifiedRequestSearchView = new SimplifiedRequestSearchView();
        String emails = mailServer.getToAddress();

        try {
            simplifiedRequestDetailedView = objectMapper.readValue(new File(filename), SimplifiedRequestDetailedView.class);
            System.out.println("simplifiedRequestDetailedView:" + simplifiedRequestDetailedView);
            
            // Artifice pour peupler simplifiedRequestSearchView à partir de simplifiedRequestDetailedView
            simplifiedRequestSearchView.setRequestDate(simplifiedRequestDetailedView.getRequestDate());
            simplifiedRequestSearchView.setCategory(simplifiedRequestDetailedView.getCategory());
            simplifiedRequestSearchView.setPatrimony(simplifiedRequestDetailedView.getLinkedEntities().getPatrimony());
            simplifiedRequestSearchView.setUid(simplifiedRequestDetailedView.getUid());
            simplifiedRequestSearchView.setState(simplifiedRequestDetailedView.getState());
            System.out.println("simplifiedRequestSearchView" + simplifiedRequestSearchView);

            sendAlert(mailServer, simplifiedRequestSearchView, simplifiedRequestDetailedView, emails, debugMode);

//            objectMapper.writeValue(new File(testFilename), simplifiedRequestDetailedView);
//            expSimplifiedRequestDetailedView = objectMapper.readValue(new File(filename), SimplifiedRequestDetailedView.class);
//            System.out.println("expSimplifiedRequestDetailedView:" + expSimplifiedRequestDetailedView);
            assertNotNull(simplifiedRequestDetailedView);
//            assertNotNull(expSimplifiedRequestDetailedView);
//            assertEquals(simplifiedRequestDetailedView.toString(), expSimplifiedRequestDetailedView.toString());
        } catch (IOException ex) {
            Logger.getLogger(NotifyRepairRequestByMail_bug230321Test.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }
}
