package pi2a.client;

import bkgpi2a.Event;
import bkgpi2a.EventList;
import bkgpi2a.HttpsClient;
import bkgpi2a.Identifiants;
import bkgpi2a.Range;
import bkgpi2a.TicketEventResultView;
import bkgpi2a.WebServer;
import bkgpi2a.WebServerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.ApplicationProperties;
import com.anstel.ticketEvents.TicketReopened;

/**
 * Programme permettant de tester le comportement du programme d'extraction
 * d'événements dans le cas l'absence de détection de l'attribut date de
 * l'événnement TicketReopened.
 *
 * @author Thierry Baribaud
 * @version 0.32.14
 */
public class TicketReopenedTest {

    /**
     * Common Jackson object mapper
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public TicketReopenedTest() {
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
     * Programme permettant de tester le comportement du programme d'extraction
     * d'événements dans le cas l'absence de détection de l'attribut date de
     * l'événnement TicketReopened.
     */
    @Test
    public void testTicketReopened() {
        String[] args = {"-webserver", "prod2", "-dbserver", "pre-prod", "-d"};
        GetArgs getArgs;
        WebServer webServer;
        ApplicationProperties applicationProperties;
        boolean debugMode;
        String value;
        Identifiants identifiants = new Identifiants();
        HttpsClient httpsClient;
        String from;
        String to;
        String baseCommand;
        Range range;
        StringBuffer command;
        int responseCode;
        String response;
        TicketEventResultView ticketEventResultView;
        EventList events;
        int i;
        TicketReopened ticketReopened;

        System.out.println("TicketReopenedTest");

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

            // from=2024-01-28T06:06:23Z&to=2024-01-28T06:07:32Z
            from = "2024-01-28T06:06:23Z";
            to = "2024-01-28T06:07:32Z";
            baseCommand = HttpsClient.EVENT_API_PATH + HttpsClient.TICKETS_CMDE;
            if (debugMode) {
                System.out.println("  Commande pour récupérer les événements : " + baseCommand);
            }
            range = new Range();
//            range.setOffset(0);
            range.setLimit(20);
            range.setCount(21);
            command = new StringBuffer(baseCommand);
            command.append("?range=").append(range.getRange()).append("&");
            command.append("from=").append(from);
            command.append("&to=").append(to);
            if (debugMode) {
                System.out.println("  Commande et arguments pour récupérer les événements : " + command);
            }
            httpsClient.sendGet(command.toString());
            responseCode = httpsClient.getResponseCode();
            System.out.println("  responseCode:" + responseCode);
            response = httpsClient.getResponse();
            System.out.println("  response:" + response);
            ticketEventResultView = objectMapper.readValue(response, TicketEventResultView.class);
            events = ticketEventResultView.getEvents();
            System.out.println("  nb event(s):" + events.size());
            i = 0;
            for (Event event : events) {
                i++;
                System.out.println("  event(" + i + "), processUid:" + event.getProcessUid() + ", date:" + event.getDate() + ", eventType:" + event.getEventType());
                if (event instanceof TicketReopened) {
                    ticketReopened = (TicketReopened) event;
                    System.out.println("    " + ticketReopened);
                    assertNotNull(ticketReopened.getDate());
                }
            }
        } catch (utils.GetArgsException | IOException | WebServerException ex) {
            Logger.getLogger(TicketReopenedTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(TicketReopenedTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
