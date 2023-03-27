package pi2a.client;

import bkgpi2a.AgencyAbstractList;
import bkgpi2a.ContactMediumView;
import bkgpi2a.ContactMediumViewList;
import bkgpi2a.SimplifiedRequestDetailedView;
import bkgpi2a.SimplifiedRequestSearchView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import utils.ApplicationProperties;
import utils.GetArgsException;

/**
 * Programme de test de la classe PI2AClient.sendAlert() vis à vis du bug du 21
 * lars 2023 avec l'objet AgencyAbstract, issue#41.
 *
 * @author Thierry Baribaud
 * @version 0.31
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
//            testMode = getArgs.getTestMode();

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
//        String testFilename = "testSimplifiedRequestDetailedView_bug230321.json";
        SimplifiedRequestDetailedView simplifiedRequestDetailedView = null;
//        SimplifiedRequestDetailedView expSimplifiedRequestDetailedView = null;
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

            sendAlert2(simplifiedRequestSearchView, simplifiedRequestDetailedView, emails);

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

    /**
     * Envoie une alerte par mail ATTENTION : Mauvaise pratique, il faut rendre
     * la méthode sendAlert() de PI2AClient public et static afin de pouvoir la
     * tester indépendamment de PI2AClient.
     */
    private void sendAlert2(SimplifiedRequestSearchView simplifiedRequestSearchView, SimplifiedRequestDetailedView simplifiedRequestDetailedView, String emails) {
        String alertSubject;
        StringBuffer alertMessage;
        String agency = "non définie";
        AgencyAbstractList agencies;
        ContactMediumViewList medium;
        String phone = "non défini";
        String email = "non défini";
        DateTime dateTime;
        DateTimeFormatter ddmmyy_hhmm = DateTimeFormat.forPattern("dd/MM/YY à HH:mm");
        String requestDate;
        String requester;
        String category;
        String reference;
        String address;
//        boolean isValid;
        String clientCompanyUUID = simplifiedRequestDetailedView.getLinkedEntities().getCompany().getUid();

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

//      Formatage de la date et de l'heure        
        dateTime = isoDateTimeFormat1.parseDateTime(simplifiedRequestSearchView.getRequestDate());
        requestDate = dateTime.toString(ddmmyy_hhmm);
        requester = simplifiedRequestDetailedView.getRequester().getName();
        category = simplifiedRequestSearchView.getCategory().getLabel();
        reference = simplifiedRequestSearchView.getPatrimony().getRef();
        address = simplifiedRequestSearchView.getPatrimony().getName();

//      On récupére dans un premier temps le dernier numéro de téléphone et le dernier mail, faire mieux plus tard
        if ((medium = simplifiedRequestDetailedView.getRequester().getMedium()) != null) {
            for (ContactMediumView contactMediumView : medium) {
                if (contactMediumView.getMediumType().equalsIgnoreCase("PHONE")) {
                    phone = contactMediumView.getIdentifier();
                } else if (contactMediumView.getMediumType().equalsIgnoreCase("MAIL")) {
                    email = contactMediumView.getIdentifier();
                }
            }
        }

        if (!"non défini".equals(phone)) {
            try {
                Phonenumber.PhoneNumber frNumberProto = phoneUtil.parse(phone, "FR");
                System.out.println("  phone:" + phone);

                if (phoneUtil.isValidNumber(frNumberProto)) {
//                    System.out.println(phoneUtil.format(frNumberProto, PhoneNumberFormat.INTERNATIONAL));
//                    System.out.println(phoneUtil.format(frNumberProto, PhoneNumberFormat.NATIONAL));
//                    System.out.println(phoneUtil.format(frNumberProto, PhoneNumberFormat.E164));
                    phone = phoneUtil.format(frNumberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
                }
            } catch (NumberParseException exception) {
                System.err.println("NumberParseException was thrown: " + exception.toString());
            }
        }

//          On récupère dans un premier temps que la première agence, faire mieux plus tard            
        if ((agencies = simplifiedRequestDetailedView.getLinkedEntities().getAgencies()) != null) {
            if (agencies.size() > 0) {
                agency = agencies.get(0).getName();
            }
        }

//      If CBRE PM company, then do not send alert to call center
//      ATTENTION : Mauvaise pratique, l'adresse mail est à placer dans le 
//      fichier .prop à l'avenir
        if (clientCompanyUUID.equals(Pi2aClient.CBRE_PM_UUID_PROD)
                || clientCompanyUUID.equals(Pi2aClient.CBRE_PM_UUID_PRE_PROD)) {
            emails = "thierry.baribaud@gmail.com";
        }
        System.out.println("emails:" + emails);

        try {
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", mailServer.getIpAddress());
            Session session = Session.getDefaultInstance(properties, null);
            javax.mail.Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailServer.getFromAddress()));
            InternetAddress[] internetAddresses = new InternetAddress[1];
//            internetAddresses[0] = new InternetAddress(mailServer.getToAddress());
            internetAddresses[0] = new InternetAddress(emails);
            message.setRecipients(javax.mail.Message.RecipientType.TO, internetAddresses);

            alertSubject = "DI DeclarImmo du " + requestDate + ", " + requester + ", " + category + ", ref:" + reference + ", " + address;
            message.setSubject(alertSubject);

            alertMessage = new StringBuffer("Demande d'intervention via DeclarImmo").append(System.lineSeparator()).append(System.lineSeparator()).append(System.lineSeparator());
            alertMessage.append("Client concerné : ").append(simplifiedRequestDetailedView.getLinkedEntities().getCompany().getName()).append(System.lineSeparator());
            alertMessage.append("Agence : ").append(agency).append(System.lineSeparator()).append(System.lineSeparator());
            alertMessage.append("Emise le : ").append(requestDate).append(System.lineSeparator());
            alertMessage.append("Référence de la demande : ").append(simplifiedRequestSearchView.getUid()).append(System.lineSeparator());
            alertMessage.append("    (à reporter sur Eole2)").append(System.lineSeparator());
            alertMessage.append("Etat : ").append(simplifiedRequestSearchView.getState()).append(System.lineSeparator());
            alertMessage.append("Motif : ").append(category).append(System.lineSeparator());
            alertMessage.append("Demandeur : ").append(requester).append(System.lineSeparator());
            alertMessage.append("Téléphone : ").append(phone).append(System.lineSeparator());
            alertMessage.append("Mail : ").append(email).append(System.lineSeparator());
            alertMessage.append("Référence adresse : ").append(reference).append(System.lineSeparator());
            alertMessage.append("Adresse : ").append(address).append(System.lineSeparator());
            alertMessage.append("Commmentaires : ").append(simplifiedRequestDetailedView.getDescription()).append(System.lineSeparator()).append(System.lineSeparator());
            alertMessage.append("Cordialement").append(System.lineSeparator()).append("L'équipe DeclarImmo").append(System.lineSeparator());
            alertMessage.append(".").append(System.lineSeparator());

            message.setText(alertMessage.toString());
            System.out.println("message:" + alertMessage.toString());

            message.setHeader("X-Mailer", "Java");
            message.setSentDate(new Date());
            session.setDebug(debugMode);
            Transport.send(message);
        } catch (AddressException exception) {
            System.out.println("Problème avec une adresse mail " + exception);
        } catch (MessagingException exception) {
            System.out.println("Problème avec les mails " + exception);
        }

    }
}
