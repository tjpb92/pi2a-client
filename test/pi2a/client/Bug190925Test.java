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

/**
 * Programme permettant de tester le comportement du programme d'xtraction d'événements dans le cas du bug du 25 septembre 2019.
 * @author Thierry Baribaud
 * @version 0.21
 */
public class Bug190925Test {
    
    /**
     * Common Jackson object mapper
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Bug190925Test() {
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
     * Programme permettant de tester le comportement du programme d'xtraction d'événements dans le cas du bug du 25 septembre 2019.
     */
    @Test
    public void testBug190925() {
        String[] args = {"-webserver", "prod", "-dbserver", "pre-prod", "-d"};
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
//        JsonString jsonString;
//        String json;
        int i;
       
        System.out.println("Bug190925");

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
            
            from = "2019-09-24T13:43:14Z";
            to = "2019-09-24T13:44:19Z";
            baseCommand = HttpsClient.EVENT_API_PATH + HttpsClient.TICKETS_CMDE;
            if (debugMode) {
                System.out.println("  Commande pour récupérer les événements : " + baseCommand);
            }
            range = new Range();
//            range.setOffset(0);
            range.setLimit(17);
            range.setCount(18);
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
            i=0;
            for(Event event:events) {
                i++;
                System.out.println("  event(" + i + "), processUid:" + event.getProcessUid() + ", date:" + event.getDate() + ", eventType:" + event.getEventType() );
            }
            
            // Ce qui suit est bugué !!!
//            jsonString = new JsonString(response);
//            i=0;
//            while ((json = jsonString.next()) != null) {
//                i++;
//                System.out.println("  json(" + i + "):" + json);
//            }
            
        } catch (utils.GetArgsException | IOException | WebServerException ex) {
            Logger.getLogger(Bug190925Test.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(Bug190925Test.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
}
