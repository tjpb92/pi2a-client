package pi2a.client;

import bkgpi2a.Agency;
import bkgpi2a.AgencyContainer;
import bkgpi2a.AgencyDAO;
import bkgpi2a.Company;
import bkgpi2a.CompanyContainer;
import bkgpi2a.CompanyDAO;
import bkgpi2a.Event;
import bkgpi2a.EventContainer;
import bkgpi2a.EventDAO;
import bkgpi2a.GetArgs;
import bkgpi2a.GetArgsException;
import bkgpi2a.HttpsClient;
import bkgpi2a.Identifiants;
import bkgpi2a.LastRun;
import bkgpi2a.LastRunDAO;
import bkgpi2a.Patrimony;
import bkgpi2a.PatrimonyContainer;
import bkgpi2a.PatrimonyDAO;
import bkgpi2a.ProviderContact;
import bkgpi2a.ProviderContactContainer;
import bkgpi2a.ProviderContactDAO;
import bkgpi2a.Range;
import bkgpi2a.User;
import bkgpi2a.UserContainer;
import bkgpi2a.UserDAO;
import bkgpi2a.WebServer;
import bkgpi2a.WebServerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.ApplicationProperties;
import utils.DBServer;
import utils.DBServerException;

/**
 * pi2a-client, programme qui lit les données au travers de l'API Rest d'un
 * serveur Web et les importe dans une base de données MongoDb locale.
 *
 * @author Thierry Baribaud.
 * @version 0.03
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
     * Constructeur de la classe Bkgpi2a
     * <p>
     * Les arguments en ligne de commande permettent de changer le mode de
     * fonctionnement.</p><ul>
     * <li>-webserver : référence au serveur web source, par défaut fait
     * référence au serveur de pré-production (cf. fichier de paramètres
     * <i>myDatabases.prop</i>) (optionnel)</li>
     * <li>-dbserver : référence à la base de donnée, par défaut fait référence
     * à la base de pré-production (cf. fichier de paramètres
     * <i>myDatabases.prop</i> (optionnel)</li>
     * <li>-b début : date de début de l'extraction à 0h, hier par défaut,
     * format DD/MM/AAAA (optionnel).</li>
     * <li>-b fin : date de fin de l'extraction à 0h, aujourd'hui par défaut,
     * format DD/MM/AAAA (optionnel).</li>
     * <li>-d : le programme fonctionne en mode débug le rendant plus verbeux,
     * désactivé par défaut (optionnel).</li>
     * <li>-t : le programme fonctionne en mode de test, les transactions en
     * base de données ne sont pas exécutées, désactivé par défaut
     * (optionnel).</li>
     * </ul>
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

        System.out.println("Création d'une instance de Bkgpi2a ...");

        System.out.println("Analyse des arguments de la ligne de commande ...");
        getArgs = new GetArgs(args);
        setWebServerType(getArgs.getWebServerType());
        setDbServerType(getArgs.getDbServerType());

        System.out.println("Lecture des paramètres d'exécution ...");
        applicationProperties = new ApplicationProperties("MyDatabases.prop");

        System.out.println("Lecture des paramètres du serveur Web ...");
        webServer = new WebServer(getWebServerType(), applicationProperties);
        System.out.println(webServer);
        setWebId(applicationProperties);
        System.out.println(getWebId());

        System.out.println("Lecture des paramètres du serveur de base de données ...");
        dbServer = new DBServer(getDbServerType(), applicationProperties);
        System.out.println(dbServer);
        setDbId(applicationProperties);
        System.out.println(getDbId());

        System.out.println("Ouverture de la connexion au serveur de base de données : " + dbServer.getName());
        mongoClient = new MongoClient(dbServer.getIpAddress(), (int) dbServer.getPortNumber());

        System.out.println("Connexion à la base de données : " + dbServer.getDbName());
        mongoDatabase = mongoClient.getDatabase(dbServer.getDbName());

        System.out.println("Ouverture de la connexion au site Web : " + webServer.getName());
        httpsClient = new HttpsClient(webServer.getIpAddress(), getWebId());

        System.out.println("Authentification en cours ...");
        httpsClient.sendPost(HttpsClient.REST_API_PATH + HttpsClient.LOGIN_CMDE);

        if (getArgs.getReadCompanies()) {
            System.out.println("Récupération des compagnies ...");
            processCompanies(httpsClient, null, mongoDatabase);
        }

        if (getArgs.getReadPatrimonies()) {
            System.out.println("Récupération des patrimoines ...");
            processPatrimonies(httpsClient, mongoDatabase);
        }

        if (getArgs.getReadProviders()) {
            System.out.println("Récupération des intervenants ...");
            processProviderContacts(httpsClient, mongoDatabase);
        }

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
     * Retourne le contenu de Bkgpi2a
     *
     * @return retourne le contenu de Bkgpi2a
     */
    @Override
    public String toString() {
        return "Bkgpi2a:{webServer=" + getWebServerType()
                + ", dbServer=" + getDbServerType() + "}";
    }

    /**
     * Programme principal pour lancer Bkgpi2a.
     *
     * @param args paramètre de ligne de commande (cf. constructeur).
     */
    public static void main(String[] args) {

        Pi2aClient pi2aClient;

        System.out.println("Lancement de Pi2aClient ...");

        try {
            pi2aClient = new Pi2aClient(args);
            System.out.println(pi2aClient);
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
    private void processCompanies(HttpsClient httpsClient, String uid, MongoDatabase mongoDatabase) {
        CompanyContainer companyContainer;
        ObjectMapper objectMapper;
        int nbCompanies;
        int i;
        String command;
        Range range;
        CompanyDAO companyDAO;
        UserDAO userDAO;
        AgencyDAO agencyDAO;

        companyDAO = new CompanyDAO(mongoDatabase);
        userDAO = new UserDAO(mongoDatabase);
        agencyDAO = new AgencyDAO(mongoDatabase);

        if (uid != null) {
            command = HttpsClient.REST_API_PATH + HttpsClient.COMPANIES_CMDE + "/" + uid + "/" + HttpsClient.AGENCIES_CMDE;
        } else {
            command = HttpsClient.REST_API_PATH + HttpsClient.COMPANIES_CMDE;
//            System.out.println("Suppression des compagnies ...");
//            companyDAO.drop();
//            System.out.println("Suppression des agences ...");
//            agencyDAO.drop();
//            System.out.println("Suppression des utilisateurs ...");
//            userDAO.drop();
        }
        System.out.println("Commande pour récupérer les clients : " + command);
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        try {
            do {
                httpsClient.sendGet(command + "?range=" + range.getRange());
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                System.out.println(range);
                companyContainer = objectMapper.readValue(httpsClient.getResponse(), CompanyContainer.class);

                nbCompanies = companyContainer.getCompanyList().size();
                System.out.println(nbCompanies + " compagnie(s) récupérée(s)");
                for (Company company : companyContainer.getCompanyList()) {
                    i++;
                    System.out.println("  " + i + " Company:" + company.getLabel() + " " + company.getCompanyType());
                    companyDAO.insert(company);
                    processUsers(httpsClient, company.getUid(), mongoDatabase);
                    if ("ClientAccountHolding".equals(company.getCompanyType())) {
                        processSubsidiaries(httpsClient, company.getUid(), mongoDatabase);
                    } else if ("ClientAccount".equals(company.getCompanyType())) {
                        processAgencies(httpsClient, company.getUid(), mongoDatabase);
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

        command = HttpsClient.REST_API_PATH + HttpsClient.COMPANIES_CMDE + "/" + uid + "/" + HttpsClient.USERS_CMDE;
        System.out.println("  Commande pour récupérer les utilisateurs : " + command);
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        try {
            do {
                httpsClient.sendGet(command + "?range=" + range.getRange());
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                System.out.println(range);
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

        command = HttpsClient.REST_API_PATH + HttpsClient.COMPANIES_CMDE + "/" + uid + "/" + HttpsClient.AGENCIES_CMDE;
        System.out.println("  Commande pour récupérer les agences : " + command);
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        try {
            do {
                httpsClient.sendGet(command + "?range=" + range.getRange());
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                System.out.println(range);
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
        CompanyContainer subsidiaryContainer;
        ObjectMapper objectMapper;
        int nbSubsidiaries;
        int i;
        String command;
        Range range;
        CompanyDAO subsidiaryDAO;

        subsidiaryDAO = new CompanyDAO(mongoDatabase);

        command = HttpsClient.REST_API_PATH + HttpsClient.COMPANIES_CMDE + "/" + uid + "/" + HttpsClient.SUBSIDIARIES_CMDE;
        System.out.println("  Commande pour récupérer les filiales : " + command);
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        try {
            do {
                httpsClient.sendGet(command + "?range=" + range.getRange());
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                System.out.println(range);
                subsidiaryContainer = objectMapper.readValue(httpsClient.getResponse(), CompanyContainer.class);
                nbSubsidiaries = subsidiaryContainer.getCompanyList().size();
                System.out.println(nbSubsidiaries + " filiale(s) récupérée(s)");
                for (Company subsidiary : subsidiaryContainer.getCompanyList()) {
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
    private void processPatrimonies(HttpsClient httpsClient, MongoDatabase mongoDatabase) {
        ObjectMapper objectMapper;
        int nbPatrimonies;
        int i;
        PatrimonyContainer patrimonyContainer;
        String command;
        Range range;
        PatrimonyDAO patrimonyDAO;

        patrimonyDAO = new PatrimonyDAO(mongoDatabase);

        System.out.println("Suppression des patrimoines ...");
        patrimonyDAO.drop();
        command = HttpsClient.REST_API_PATH + HttpsClient.PATRIMONIES_CMDE;
        System.out.println("  Commande pour récupérer les patrimoines : " + command);
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        try {
            do {
                httpsClient.sendGet(command + "?range=" + range.getRange());
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                System.out.println(range);
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
        ProviderContactContainer providerContactContainer;
        int i;
        String command;
        Range range;
        ProviderContactDAO providerContactDAO;

        providerContactDAO = new ProviderContactDAO(mongoDatabase);

        System.out.println("Suppression des providerContacts ...");
        providerContactDAO.drop();
        command = HttpsClient.REST_API_PATH + HttpsClient.PROVIDER_CONTACTS_CMDE;
        System.out.println("  Commande pour récupérer les providerContacts : " + command);
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        try {
            do {
                httpsClient.sendGet(command + "?range=" + range.getRange());
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                System.out.println(range);
                providerContactContainer = objectMapper.readValue(httpsClient.getResponse(), ProviderContactContainer.class);
                nbProviderContacts = providerContactContainer.getProviderContactList().size();
                System.out.println(nbProviderContacts + " contact(s) récupéréxe(s)");
                for (ProviderContact providerContact : providerContactContainer.getProviderContactList()) {
                    i++;
                    System.out.println(i + "  ProviderContact:" + providerContact.getLabel());
                    providerContactDAO.insert(providerContact);
                }
            } while (range.hasNext());
        } catch (Exception ex) {
            Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Récupére les événéments enregistrés sur le site Web
     *
     * @param httpsClient connexion au site Web
     * @param mongoDatabase connexion à la base de données locale
     */
    private void processEvents(HttpsClient httpsClient, MongoDatabase mongoDatabase) {
        ObjectMapper objectMapper;
        int nbEvents;
        int i;
        EventContainer eventContainer;
        String baseCommand;
        StringBuffer command;
        Range range;
        EventDAO eventDAO;
        String from;
        String to;
        LastRun thisRun;
        LastRun lastRun;
        LastRunDAO lastRunDAO;

        eventDAO = new EventDAO(mongoDatabase);
        lastRunDAO = new LastRunDAO(mongoDatabase);

        thisRun = new LastRun("pi2a-client");
        lastRun = lastRunDAO.find("pi2a-client");
        lastRunDAO.update(thisRun);
        
        from = lastRun.getLastRun();
        to = thisRun.getLastRun();
        
        baseCommand = HttpsClient.EVENT_API_PATH + HttpsClient.TICKETS_CMDE;
        System.out.println("  Commande pour récupérer les événements : " + baseCommand);
        objectMapper = new ObjectMapper();
        range = new Range();
        i = 0;
        try {
            do {
                command = new StringBuffer(baseCommand);
                if (i > 0) command.append("?range=").append(range.getRange()).append("&");
                else command.append("?");
                
                command.append("from=").append(from);
                command.append("&to=").append(to);
                
                httpsClient.sendGet(command.toString());
                range.contentRange(httpsClient.getContentRange());
                range.setPage(httpsClient.getAcceptRange());
                System.out.println(range);
                eventContainer = objectMapper.readValue(httpsClient.getResponse(), EventContainer.class);

                nbEvents = eventContainer.getEventList().size();
                System.out.println(nbEvents + " événement(s) récupéré(s)");
                for (Event event : eventContainer.getEventList()) {
                    i++;
                    System.out.println(i + ", event:" + event);
                    eventDAO.insert(event);
                }
            } while (range.hasNext());
        } catch (IOException ex) {
            Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(HttpsClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
