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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.ApplicationProperties;

/**
 * Programme permettant de si l’on peut avoir deux connexions HTTPS ouvertes en
 * même temps et sans interférence.
 *
 * @author Thierry Baribaud
 * @version 0.32.11
 */
public class DoubleHttpsClientTest {

    /**
     * Common Jackson object mapper
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public DoubleHttpsClientTest() {
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
     * Programme permettant de si l’on peut avoir deux connexions HTTPS ouvertes
     * en même temps et sans interférence.
     */
    @Test
    public void testDoubleHttpsClient() {
        String[] args = {"-webserver", "prod2", "-d"};
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
        int nbEvents;
        int nbPages;

        System.out.println("DoubleHttpsClientTest");

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
            System.out.println("coucou");

            from = "2024-01-16T08:00:00Z";
            to = "2024-01-16T09:00:00Z";
            baseCommand = HttpsClient.EVENT_API_PATH + HttpsClient.TICKETS_CMDE;
            if (debugMode) {
                System.out.println("  Commande pour récupérer les événements : " + baseCommand);
            }

            nbEvents = 0;
            nbPages = 0;
            range = new Range();
            do {
                command = new StringBuffer(baseCommand);
                if (nbPages > 0) {
                    command.append("?range=").append(range.getRange()).append("&");
                } else {
                    command.append("?");
                }
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

                if (responseCode == 200 || responseCode == 206) {
                    range.contentRange(httpsClient.getContentRange());
                    range.setPage(httpsClient.getAcceptRange());
                    System.out.println(range);

                    nbEvents += processEvents(nbEvents, response);
                    nbPages++;
                }
            } while (range.hasNext());

        } catch (Exception ex) {
            Logger.getLogger(DoubleHttpsClientTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int processEvents(int offset, String response) {

        TicketEventResultView ticketEventResultView;
        EventList events;
        int nbEvents;

        nbEvents = 0;
        try {
            
            ticketEventResultView = objectMapper.readValue(response, TicketEventResultView.class);
            events = ticketEventResultView.getEvents();
            System.out.println("  nb event(s):" + events.size());
            for (Event event : events) {
                nbEvents++;
                System.out.println("  event(" + (offset + nbEvents) + "), processUid:" + event.getProcessUid() + ", aggregateuuid:" + event.getAggregateUid() + ", date:" + event.getDate() + ", sentDate:" + event.getSentDate() + ", eventType:" + event.getEventType());
            }
        } catch (IOException ex) {
            Logger.getLogger(DoubleHttpsClientTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return nbEvents;
    }
}
