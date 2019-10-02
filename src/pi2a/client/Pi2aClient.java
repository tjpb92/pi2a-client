package pi2a.client;

import bkgpi2a.Agency;
import bkgpi2a.AgencyContainer;
import bkgpi2a.AgencyDAO;
import bkgpi2a.ClientCompany;
import bkgpi2a.ClientCompanyContainer;
import bkgpi2a.ClientCompanyDAO;
import bkgpi2a.Event;
import bkgpi2a.EventContainer;
import bkgpi2a.EventDAO;
import bkgpi2a.EventList;
import bkgpi2a.HttpsClient;
import bkgpi2a.Identifiants;
import bkgpi2a.LastRun;
import bkgpi2a.LastRunDAO;
import bkgpi2a.Patrimony;
import bkgpi2a.PatrimonyContainer;
import bkgpi2a.PatrimonyDAO;
//import bkgpi2a.ProviderCompany;
//import bkgpi2a.ProviderCompanyContainer;
//import bkgpi2a.ProviderCompanyDAO;
import bkgpi2a.ProviderContact;
import bkgpi2a.ProviderContactDAO;
import bkgpi2a.ProviderContactDetailedQueryView;
import bkgpi2a.ProviderContactQueryView;
import bkgpi2a.ProviderContactResultView;
import bkgpi2a.Range;
import bkgpi2a.TicketEventResultView;
import bkgpi2a.User;
import bkgpi2a.UserContainer;
import bkgpi2a.UserDAO;
import bkgpi2a.WebServer;
import bkgpi2a.WebServerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.ApplicationProperties;
import utils.DBServer;
import utils.DBServerException;
import utils.GetArgsException;

/**
 * pi2a-client, programme qui lit les données au travers de l'API Rest d'un
 * serveur Web et les importe dans une base de données MongoDb locale.
 *
 * @author Thierry Baribaud.
 * @version 0.21
 */
public class Pi2aClient {

    /**
     * webServerType : prod pour le serveur de production, pre-prod pour le
     * serveur de pré-production. Valeur par défaut : pre-prod.
     */
    private String webServerType = "pre-prod";

    /**
     * dbServerType : prod pour le serveur de production, pre-prod pour le
     * serveur de pré-production. Valeur par défaut : pre-prod.
     */
    private String dbServerType = "pre-prod";

    /**
     * webId : identifiants pour se connecter au serveur Web courant. Pas de
     * valeur par défaut, ils doivent être fournis dans le fichier
     * MyDatabases.prop.
     */
    private Identifiants webId;

    /**
     * dbId : identifiants pour se connecter à la base de données courante. Pas
     * de valeur par défaut, ils doivent être fournis dans le fichier
     * MyDatabases.prop.
     */
    private Identifiants dbId;

    /**
     * debugMode : fonctionnement du programme en mode debug (true/false).
     * Valeur par défaut : false.
     */
    private static boolean debugMode = false;

    /**
     * testMode : fonctionnement du programme en mode test (true/false). Valeur
     * par défaut : false.
     */
    private static boolean testMode = false;

    /**
     * Constructeur de la classe Pi2aClient
     *
     * @param args arguments de la ligne de commande.
     * @throws java.io.IOException en cas d'erreur d'entrée/sortie.
     * @throws WebServerException en cas d'erreur avec le serveur Web.
     * @throws utils.DBServerException en cas d'erreur avec le serveur de base
     * de données.
     * @throws GetArgsException en cas d'erreur avec les paramètres en ligne de
     * commande
     */
    public Pi2aClient(String[] args) throws IOException,
            WebServerException, DBServerException, GetArgsException, Exception {

        ApplicationProperties applicationProperties;
        DBServer dbServer;
        WebServer webServer;
        GetArgs getArgs;
        HttpsClient httpsClient;
        MongoClient mongoClient;
        MongoDatabase mongoDatabase;

        System.out.println("Création d'une instance de Pi2aClient ...");

        System.out.println("Analyse des arguments de la ligne de commande ...");
        getArgs = new GetArgs(args);
        setWebServerType(getArgs.getWebServerType());
        setDbServerType(getArgs.getDbServerType());
        debugMode = getArgs.getDebugMode();
        testMode = getArgs.getTestMode();

        System.out.println("Lecture des paramètres d'exécution ...");
        applicationProperties = new ApplicationProperties("pi2a-client.prop");

        System.out.println("Lecture des paramètres du serveur Web ...");
        webServer = new WebServer(getWebServerType(), applicationProperties);
        if (debugMode) {
            System.out.println(webServer);
        }
        setWebId(applicationProperties);
        if (debugMode) {
            System.out.println(webId);
        }

        System.out.println("Lecture des paramètres du serveur de base de données ...");
        dbServer = new DBServer(getDbServerType(), applicationProperties);
        if (debugMode) {
            System.out.println(dbServer);
        }
        setDbId(applicationProperties);
        if (debugMode) {
            System.out.println(dbId);
        }

        System.out.println("Ouverture de la connexion au serveur de base de données : " + dbServer.getName());
        mongoClient = new MongoClient(dbServer.getIpAddress(), (int) dbServer.getPortNumber());

        System.out.println("Connexion à la base de données : " + dbServer.getDbName());
        mongoDatabase = mongoClient.getDatabase(dbServer.getDbName());

        System.out.println("Ouverture de la connexion au site Web : " + webServer.getName());
        httpsClient = new HttpsClient(webServer.getIpAddress(), webId, debugMode, testMode);

        System.out.println("Authentification en cours ...");
        httpsClient.sendPost(HttpsClient.REST_API_PATH + HttpsClient.LOGIN_CMDE);

        if (getArgs.getReadClientCompanies()) {
            System.out.println("Récupération des compagnies ...");
            processClientCompanies(httpsClient, null, mongoDatabase);
        }

        if (getArgs.getReadPatrimonies()) {
            System.out.println("Récupération des patrimoines ...");
            processPatrimonies(httpsClient, mongoDatabase, getArgs.getClientCompanyUuid());
        }

        if (getArgs.getReadProviderContacts()) {
            System.out.println("Récupération des intervenants ...");
            processProviderContacts(httpsClient, mongoDatabase);
        }

//        if (getArgs.getReadProviderCompanies()) {
//            System.out.println("Récupération des sociétés des intervenants ...");
//            processProviderCompanies(httpsClient, mongoDatabase);
//        }
        if (getArgs.getReadEvents()) {
            System.out.println("Récupération des événements ...");
            processEvents(httpsClient, mongoDatabase);
        }

    }

    /**
     * @param webServerType définit le serveur Web
     */
    private void setWebServerType(String webServerType) {
        this.webServerType = webServerType;
    }

    /**
     * @param dbServerType définit le serveur de base de données
     */
    private void setDbServerType(String dbServerType) {
        this.dbServerType = dbServerType;
    }

    /**
     * @return webServerType le serveur web
     */
    private String getWebServerType() {
        return (webServerType);
    }

    /**
     * @return dbServerType le serveur de base de données
     */
    private String getDbServerType() {
        return (dbServerType);
    }

    /**
     * Retourne le contenu de Pi2aClient
     *
     * @return retourne le contenu de Pi2aClient
     */
    @Override
    public String toString() {
        return "Pi2aClient:{webServer=" + getWebServerType()
                + ", dbServer=" + getDbServerType() + "}";
    }

    /**
     * Programme principal pour lancer Pi2aClient.
     *
     * @param args paramètre de ligne de commande (cf. constructeur).
     */
    public static void main(String[] args) {

        Pi2aClient pi2aClient;

        System.out.println("Lancement de Pi2aClient ...");

        try {
            pi2aClient = new Pi2aClient(args);
            if (debugMode) {
                System.out.println(pi2aClient);
            }
        } catch (Exception exception) {
            System.out.println("Problème lors de l'instanciation de Pi2aClient");
            exception.printStackTrace();
        }

        System.out.println("Fin de Pi2aClient");
    }

    /**
     * @return les identifiants pour accéder au serveur Web
     */
    public Identifiants getWebId() {
        return webId;
    }

    /**
     * @param webId définit les identifiants pour accéder au serveur Web
     */
    public void setWebId(Identifiants webId) {
        this.webId = webId;
    }

    /**
     * @param applicationProperties définit les identifiants pour accéder au
     * serveur Web
     * @throws WebServerException en cas d'erreur sur la lecteur des
     * identifiants
     */
    public void setWebId(ApplicationProperties applicationProperties) throws WebServerException {
        String value;
        Identifiants identifiants = new Identifiants();

        value = applicationProperties.getProperty(getWebServerType() + ".webserver.login");
        if (value != null) {
            identifiants.setLogin(value);
        } else {
            throw new WebServerException("Nom utilisateur pour l'accès Web non défini");
        }

        value = applicationProperties.getProperty(getWebServerType() + ".webserver.passwd");
        if (value != null) {
            identifiants.setPassword(value);
        } else {
            throw new WebServerException("Mot de passe pour l'accès Web non défini");
        }
        setWebId(identifiants);
    }

    /**
     * @return les identifiants pour accéder à la base de données
     */
    public Identifiants getDbId() {
        return dbId;
    }

    /**
     * @param dbId définit les identifiants pour accéder à la base de données
     */
    public void setDbId(Identifiants dbId) {
        this.dbId = dbId;
    }

    /**
     * @param applicationProperties définit les identifiants pour accéder au
     * serveur Web
     * @throws WebServerException en cas d'erreur sur la lecteur des
     * identifiants
     */
    public void setDbId(ApplicationProperties applicationProperties) throws WebServerException {
        String value;
        Identifiants identifiants = new Identifiants();

        value = applicationProperties.getProperty(getDbServerType() + ".dbserver.login");
        if (value != null) {
            identifiants.setLogin(value);
        } else {
            throw new WebServerException("Nom utilisateur pour l'accès base de données non défini");
        }

        value = applicationProperties.getProperty(getDbServerType() + ".dbserver.passwd");
        if (value != null) {
            identifiants.setPassword(value);
        } else {
            throw new WebServerException("Mot de passe pour l'accès base de données non défini");
        }
        setDbId(identifiants);
    }

    /**
     * Récupère les sociétés enregistrées sur le site Web
     *
     * @param httpsClient connexion au site Web
     * @param uid identifiant unique d'une compagnie, filiale ou agence
     * @param mongoDatabase connexion à la base de données locale
     */
    private void processClientCompanies(HttpsClient httpsClient, String uid, MongoDatabase mongoDatabase) {
        ClientCompanyContainer clientCompanyContainer;
        ObjectMapper objectMapper;
        int nbCompanies;
        int i;
        String command;
        Range range;
        ClientCompanyDAO clientCompanyDAO;
        UserDAO userDAO;
        AgencyDAO agencyDAO;

        clientCompanyDAO = new ClientCompanyDAO(mongoDatabase);
        userDAO = new UserDAO(mongoDatabase);
        agencyDAO = new AgencyDAO(mongoDatabase);

        if (uid != null) {
            command = HttpsClient.REST_API_PATH + HttpsClient.CLIENT_COMPANIES_CMDE + "/" + uid + "/" + HttpsClient.AGENCIES_CMDE;
        } else {
            command = HttpsClient.REST_API_PATH + HttpsClient.CLIENT_COMPANIES_CMDE;
            System.out.println("Suppression des compagnies ...");
            clientCompanyDAO.drop();
            System.out.println("Suppression des agences ...");
            agencyDAO.drop();
            System.out.println("Suppression des utilisateurs ...");
            userDAO.drop();
        }
        if (debugMode) {
            System.out.println("Commande pour récupérer les clients : " + command);
        }
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        try {
            do {
                httpsClient.sendGet(command + "?range=" + range.getRange());
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                if (debugMode) {
                    System.out.println(range);
                }
                clientCompanyContainer = objectMapper.readValue(httpsClient.getResponse(), ClientCompanyContainer.class);

                nbCompanies = clientCompanyContainer.getClientCompanyList().size();
                System.out.println(nbCompanies + " compagnie(s) récupérée(s)");
                for (ClientCompany clientCompany : clientCompanyContainer.getClientCompanyList()) {
                    i++;
                    System.out.println("  " + i + " Company:" + clientCompany.getLabel()
                            + " " + clientCompany.getCompanyType()
                            + " " + clientCompany.getBillingType());
                    clientCompanyDAO.insert(clientCompany);
                    processUsers(httpsClient, clientCompany.getUid(), mongoDatabase);
                    if ("ClientAccountHolding".equals(clientCompany.getCompanyType())) {
                        processSubsidiaries(httpsClient, clientCompany.getUid(), mongoDatabase);
                    } else if ("ClientAccount".equals(clientCompany.getCompanyType())) {
                        processAgencies(httpsClient, clientCompany.getUid(), mongoDatabase);
                    }
                }
            } while (range.hasNext());
        } catch (IOException ex) {
            Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Récupére les utilisateurs de la compagnie/filiale/agence passée en
     * paramètre
     *
     * @param httpsClient connexion au site Web
     * @param uid identifiant unique d'une compagnie, filiale ou agence
     * @param mongoDatabase connexion à la base de données locale
     */
    private void processUsers(HttpsClient httpsClient, String uid, MongoDatabase mongoDatabase) {
        ObjectMapper objectMapper;
        int nbUsers;
        int i;
        String command;
        Range range;
        UserContainer userContainer;
        UserDAO usersDAO;

        usersDAO = new UserDAO(mongoDatabase);

//        command = HttpsClient.REST_API_PATH + HttpsClient.CLIENT_COMPANIES_CMDE + "/" + uid + "/" + HttpsClient.USERS_CMDE;
//  Pas très propre, faire mieux plus tard, TB, le 1er novembre 2017.
        command = HttpsClient.REST_API_PATH + HttpsClient.CLIENT_COMPANIES_CMDE + "/" + uid + "/" + HttpsClient.USERS_CMDE;
        if (debugMode) {
            System.out.println("  Commande pour récupérer les utilisateurs : " + command);
        }
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        try {
            do {
                httpsClient.sendGet(command + "?range=" + range.getRange());
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                if (debugMode) {
                    System.out.println(range);
                }
                userContainer = objectMapper.readValue(httpsClient.getResponse(), UserContainer.class);
                nbUsers = userContainer.getUserList().size();
                System.out.println("  " + nbUsers + " utilisateur(s) récupéré(s)");
                for (User user : userContainer.getUserList()) {
                    i++;
                    System.out.println("  " + i + " User:" + user.getLastName() + " " + user.getFirstName() + " " + user.getClass().getSimpleName());
                    usersDAO.insert(user);
                }
            } while (range.hasNext());
        } catch (Exception ex) {
            Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Récupére les utilisateurs de la compagnie/filiale/agence passée en
     * paramètre
     *
     * @param httpsClient connexion au site Web
     * @param uid identifiant unique d'une compagnie, filiale ou agence
     * @param mongoDatabase connexion à la base de données locale
     */
    private void processAgencies(HttpsClient httpsClient, String uid, MongoDatabase mongoDatabase) {
        ObjectMapper objectMapper;
        AgencyContainer agencyContainer;
        int nbAgencies;
        int i;
        String command;
        Range range;
        AgencyDAO agencyDAO;

        agencyDAO = new AgencyDAO(mongoDatabase);

        command = HttpsClient.REST_API_PATH + HttpsClient.CLIENT_COMPANIES_CMDE + "/" + uid + "/" + HttpsClient.AGENCIES_CMDE;
        if (debugMode) {
            System.out.println("  Commande pour récupérer les agences : " + command);
        }
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        try {
            do {
                httpsClient.sendGet(command + "?range=" + range.getRange());
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                if (debugMode) {
                    System.out.println(range);
                }
                agencyContainer = objectMapper.readValue(httpsClient.getResponse(), AgencyContainer.class);
                nbAgencies = agencyContainer.getAgencyList().size();
                System.out.println(nbAgencies + " agence(s) récupérée(s)");
                for (Agency agency : agencyContainer.getAgencyList()) {
                    i++;
                    System.out.println("  " + i + " Agency:" + agency.getLabel());
                    agencyDAO.insert(agency);
                    processUsers(httpsClient, agency.getUid(), mongoDatabase);
                }
            } while (range.hasNext());
        } catch (Exception ex) {
            Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Récupére les filiale de la compagnie passée en paramètre
     *
     * @param httpsClient connexion au site Web
     * @param uid identifiant unique d'une compagnie
     * @param mongoDatabase connexion à la base de données locale
     */
    private void processSubsidiaries(HttpsClient httpsClient, String uid, MongoDatabase mongoDatabase) {
        ClientCompanyContainer subsidiaryContainer;
        ObjectMapper objectMapper;
        int nbSubsidiaries;
        int i;
        String command;
        Range range;
        ClientCompanyDAO subsidiaryDAO;

        subsidiaryDAO = new ClientCompanyDAO(mongoDatabase);

        command = HttpsClient.REST_API_PATH + HttpsClient.CLIENT_COMPANIES_CMDE + "/" + uid + "/" + HttpsClient.SUBSIDIARIES_CMDE;
        if (debugMode) {
            System.out.println("  Commande pour récupérer les filiales : " + command);
        }
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        try {
            do {
                httpsClient.sendGet(command + "?range=" + range.getRange());
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                if (debugMode) {
                    System.out.println(range);
                }
                subsidiaryContainer = objectMapper.readValue(httpsClient.getResponse(), ClientCompanyContainer.class);
                nbSubsidiaries = subsidiaryContainer.getClientCompanyList().size();
                System.out.println(nbSubsidiaries + " filiale(s) récupérée(s)");
                for (ClientCompany subsidiary : subsidiaryContainer.getClientCompanyList()) {
                    i++;
                    System.out.println("    " + i + " Subsidiary:" + subsidiary.getLabel() + ":" + subsidiary.getCompanyType());
                    subsidiaryDAO.insert(subsidiary);
                    processUsers(httpsClient, subsidiary.getUid(), mongoDatabase);
                    processAgencies(httpsClient, subsidiary.getUid(), mongoDatabase);
                }
            } while (range.hasNext());
        } catch (Exception ex) {
            Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Récupére les patrimoines enregistrés sur le site Web
     *
     * @param httpsClient connexion au site Web
     * @param mongoDatabase connexion à la base de données locale
     */
    private void processPatrimonies(HttpsClient httpsClient, MongoDatabase mongoDatabase, String clientCompanyUuid) {
        ObjectMapper objectMapper;
        int nbPatrimonies;
        int i;
        PatrimonyContainer patrimonyContainer;
        StringBuffer command;
        Range range;
        PatrimonyDAO patrimonyDAO;
        String separator = "?";

        patrimonyDAO = new PatrimonyDAO(mongoDatabase);

        System.out.println("Suppression des patrimoines ...");
        patrimonyDAO.drop();
        command = new StringBuffer(HttpsClient.REST_API_PATH + HttpsClient.PATRIMONIES_CMDE);
        if (clientCompanyUuid != null) {
            command.append("?companyuid=").append(clientCompanyUuid);
            separator = "&";
        }
        if (debugMode) {
            System.out.println("  Commande pour récupérer les patrimoines : " + command);
        }
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        try {
            do {
                httpsClient.sendGet(command + separator + "range=" + range.getRange());
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                if (debugMode) {
                    System.out.println(range);
                }
                patrimonyContainer = objectMapper.readValue(httpsClient.getResponse(), PatrimonyContainer.class);

                nbPatrimonies = patrimonyContainer.getPatrimonyList().size();
                System.out.println(nbPatrimonies + " patrimoire(s) récupéré(s)");
                for (Patrimony patrimony : patrimonyContainer.getPatrimonyList()) {
                    i++;
                    System.out.println(i + ", ref:" + patrimony.getRef() + ", label:" + patrimony.getLabel() + ", uid:" + patrimony.getUid());
                    patrimonyDAO.insert(patrimony);
                }
            } while (range.hasNext());
        } catch (IOException ex) {
            Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Récupére les intervenants enregistrés sur le site Web
     *
     * @param httpsClient connexion au site Web
     * @param mongoDatabase connexion à la base de données locale
     */
    private void processProviderContacts(HttpsClient httpsClient, MongoDatabase mongoDatabase) {
        ObjectMapper objectMapper;
        int nbProviderContacts;
        int i;
        String command;
        Range range;
        ProviderContactDAO providerContactDAO;
        String response;
        ProviderContactResultView providerContactResultView;
        ProviderContact providerContact;

        providerContactDAO = new ProviderContactDAO(mongoDatabase);

        System.out.println("Suppression des providerContacts ...");
        providerContactDAO.drop();
        command = HttpsClient.REST_API_PATH + HttpsClient.PROVIDER_CONTACTS_CMDE;
        if (debugMode) {
            System.out.println("  Commande pour récupérer les providerContacts : " + command);
        }
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        try {
            do {
                httpsClient.sendGet(command + "?range=" + range.getRange());
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                if (debugMode) {
                    System.out.println(range);
                }
                response = httpsClient.getResponse();
                System.out.println("Réponse=" + response);
                providerContactResultView = objectMapper.readValue(response, ProviderContactResultView.class);
                nbProviderContacts = providerContactResultView.getProviderContactQueryViewtList().size();
                System.out.println(nbProviderContacts + " providerContact(s) récupéré(s)");
                for (ProviderContactQueryView providerContactQueryView : providerContactResultView.getProviderContactQueryViewtList()) {
                    i++;
                    providerContact = providerContactQueryView.getProviderContact();
                    System.out.println(i + "  label:" + providerContact.getLabel() + ", name:" + providerContact.getName());
                    processProviderContact(httpsClient, providerContact.getUid(), providerContactDAO);
                }
            } while (range.hasNext());
        } catch (Exception ex) {
            Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Récupére un intervenant enregistré sur le site Web
     *
     * @param httpsClient connexion au site Web
     * @param uid identifiant de providerContact
     * @param mongoDatabase connexion à la base de données locale
     */
    private void processProviderContact(HttpsClient httpsClient, String uid, ProviderContactDAO providerContactDAO) {
        ObjectMapper objectMapper;
        int nbProviderContacts;
        String command;
        Range range;
        String response;
        ProviderContactResultView providerContactResultView;
        ProviderContact providerContact;
        ProviderContactDetailedQueryView providerContactDetailedQueryView;

        command = HttpsClient.REST_API_PATH + HttpsClient.PROVIDER_CONTACTS_CMDE + "/" + uid;
        if (debugMode) {
            System.out.println("  Commande pour récupérer le providerContact : " + command);
        }
        objectMapper = new ObjectMapper();
//        range = new Range();
        try {
            httpsClient.sendGet(command);
//            httpsClient.sendGet(command + "?range=" + range.getRange());
//            range.contentRange(httpsClient.getContentRange());
//            range.setPage(httpsClient.getAcceptRange());
//            if (debugMode) {
//                System.out.println(range);
//            }
            response = httpsClient.getResponse();
            System.out.println("Réponse=" + response);
            providerContactDetailedQueryView = objectMapper.readValue(response, ProviderContactDetailedQueryView.class);
            providerContact = providerContactDetailedQueryView.getProviderContact();

            System.out.println("    name:" + providerContact.getName());
            providerContactDAO.insert(providerContact);
        } catch (Exception ex) {
            Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    /**
//     * Récupére les sociétés des intervenants enregistrées sur le site Web
//     *
//     * @param httpsClient connexion au site Web
//     * @param mongoDatabase connexion à la base de données locale
//     */
//    private void processProviderCompanies(HttpsClient httpsClient, MongoDatabase mongoDatabase) {
//        ObjectMapper objectMapper;
//        int nbProviderCompanies;
//        ProviderCompanyContainer providerCompanyContainer;
//        int i;
//        String command;
//        Range range;
//        ProviderCompanyDAO providerCompanyDAO;
//        String response;
//
//        providerCompanyDAO = new ProviderCompanyDAO(mongoDatabase);
//
//        System.out.println("Suppression des providerCompanies ...");
//        providerCompanyDAO.drop();
//        command = HttpsClient.REST_API_PATH + HttpsClient.PROVIDER_COMPANIES_CMDE;
//        if (debugMode) System.out.println("  Commande pour récupérer les providerCompanies : " + command);
//        objectMapper = new ObjectMapper();
//        range = new Range();
//        i = 0;
//        try {
//            do {
//                httpsClient.sendGet(command + "?range=" + range.getRange());
//                range.contentRange(httpsClient.getContentRange());
//                range.setPage(httpsClient.getAcceptRange());
//                if (debugMode) {
//                    System.out.println(range);
//                }
//                response = httpsClient.getResponse();
//                System.out.println("Réponse=" + response);
//                providerCompanyContainer = objectMapper.readValue(response, ProviderCompanyContainer.class);
//                nbProviderCompanies = providerCompanyContainer.getProviderCompanyList().size();
//                System.out.println(nbProviderCompanies + " société(s) récupéréxe(s)");
//                for (ProviderCompany providerCompany : providerCompanyContainer.getProviderCompanyList()) {
//                    i++;
//                    System.out.println(i + "  ProviderCompany:" + providerCompany.getName() +
//                            ", SIRET:" + providerCompany.getSiretNumber());
//                    providerCompanyDAO.insert(providerCompany);
//                }
//            } while (range.hasNext());
//        } catch (Exception ex) {
//            Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    /**
     * Récupére les événéments enregistrés sur le site Web
     *
     * @param httpsClient connexion au site Web
     * @param mongoDatabase connexion à la base de données locale
     */
    private void processEvents(HttpsClient httpsClient, MongoDatabase mongoDatabase) {
        ObjectMapper objectMapper;
        int i;
        String baseCommand;
        StringBuffer command;
        Range range;
        EventDAO eventDAO;
        String from;
        String to;
        LastRun thisRun;
        LastRun lastRun;
        LastRunDAO lastRunDAO;
        int responseCode;
        Event sameEvent;
        TicketEventResultView ticketEventResultView;
        EventList events;

        eventDAO = new EventDAO(mongoDatabase);
        lastRunDAO = new LastRunDAO(mongoDatabase);

        thisRun = new LastRun("pi2a-client");
        lastRun = lastRunDAO.find("pi2a-client");
        System.out.println("  " + lastRun + ", " + thisRun);
        lastRunDAO.update(thisRun);

        from = lastRun.getLastRun();
        to = thisRun.getLastRun();

        baseCommand = HttpsClient.EVENT_API_PATH + HttpsClient.TICKETS_CMDE;
        if (debugMode) {
            System.out.println("  Commande pour récupérer les événements : " + baseCommand);
        }
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        do {
            command = new StringBuffer(baseCommand);
            if (i > 0) {
                command.append("?range=").append(range.getRange()).append("&");
            } else {
                command.append("?");
            }
            command.append("from=").append(from);
            command.append("&to=").append(to);
            try {
                httpsClient.sendGet(command.toString());
                responseCode = httpsClient.getResponseCode();
            } catch (Exception exception) {
//                Logger.getLogger(Pi2aClient.class.getName()).log(Level.SEVERE, null, exception);
                System.out.println("ERREUR : httpsClient.sendGet " + exception);
                responseCode = 0;
            }

            if (responseCode == 200 || responseCode == 206) {
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                System.out.println(range);

                try {
                    ticketEventResultView = objectMapper.readValue(httpsClient.getResponse(), TicketEventResultView.class);
                    events = ticketEventResultView.getEvents();
                    System.out.println("nb event(s):" + events.size());

                    for (Event event : events) {
                        i++;
                        System.out.println(i + ", event:" + event);
                        if ((sameEvent = eventDAO.findOne(event.getProcessUid())) == null) {
                            eventDAO.insert(event);
                        } else {
                            System.out.println("ERROR : événement rejeté car déjà lu");
                        }
                    }
                } catch (InvalidTypeIdException exception) {
                    System.out.println("ERROR : événement inconnu " + exception);
//                    Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException exception) {
//                    Logger.getLogger(Pi2aClient.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("ERROR : IO exception on event " + exception);
                }
            }
        } while (range.hasNext());
    }

    /**
     * @param debugMode : fonctionnement du programme en mode debug
     * (true/false).
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * @param testMode : fonctionnement du programme en mode test (true/false).
     */
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    /**
     * @return debugMode : retourne le mode de fonctionnement debug.
     */
    public boolean getDebugMode() {
        return (debugMode);
    }

    /**
     * @return testMode : retourne le mode de fonctionnement test.
     */
    public boolean getTestMode() {
        return (testMode);
    }

}
