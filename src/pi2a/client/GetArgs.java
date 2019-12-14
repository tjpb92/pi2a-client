package pi2a.client;

import utils.ValidServers;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import utils.Md5;
import utils.GetArgsException;

/**
 * Cette classe sert à vérifier et à récupérer les arguments passés en ligne de
 * commande au programme pi2a-client.
 *
 * @author Thierry Baribaud
 * @version 0.23
 */
public class GetArgs {

    private static final DateFormat MyDateFormat = new SimpleDateFormat("dd/MM/yyyy");

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
     * begDate : date de début de l'export à 0h. Valeur par défaut : la veille.
     */
    private Timestamp begDate = new Timestamp((new java.util.Date().getTime()) - 1000 * 60 * 60 * 24);

    /**
     * endDate : date de fin de l'export à 0h. Valeur par défaut : aujourd'hui.
     */
    private Timestamp endDate = new Timestamp(new java.util.Date().getTime());

    /**
     * unum : référence au service d'urgence (identifiant interne)
     */
    private int unum;

    /**
     * clientCompanyUuid : identifiant universel unique du service d'urgence
     */
    private String clientCompanyUuid = null;

    /**
     * readClientCompanies : demande la lecture des sociétés (true/false).
     * Valeur par défaut : false. Remplace readCompanies.
     */
    private boolean readClientCompanies = false;

    /**
     * readPatrimonies : demande la lecture des patrimoines (true/false). Valeur
     * par défaut : false.
     */
    private boolean readPatrimonies = false;

    /**
     * readProviderContacts : demande la lecture des fournisseurs (true/false).
     * Valeur par défaut : false.
     */
    private boolean readProviderContacts = false;

    /**
     * readProviderCompanies : demande la lecture des sociétés des fournisseurs
     * (true/false). Valeur par défaut : false.
     */
    private boolean readProviderCompanies = false;

    /**
     * readEvents : demande la lecture des événements (true/false). Valeur par
     * défaut : false.
     */
    private boolean readEvents = false;

    /**
     * readRequests : demande la lecture des demandes d'intervention émises par
     * l'application mobile (true/false). Valeur par défaut : false.
     */
    private boolean readSimplifiedRequests = false;

    /**
     * debugMode : fonctionnement du programme en mode debug (true/false).
     * Valeur par défaut : false.
     */
    private boolean debugMode = false;

    /**
     * testMode : fonctionnement du programme en mode test (true/false). Valeur
     * par défaut : false.
     */
    private boolean testMode = false;

    /**
     * @return webServerType : retourne la valeur pour le serveur source.
     */
    public String getWebServerType() {
        return (webServerType);
    }

    /**
     * @return begDate : date de début de l'export à 0h.
     */
    public Timestamp getBegDate() {
        return (begDate);
    }

    /**
     * @return endDate : date de fin de l'export à 0h.
     */
    public Timestamp getEndDate() {
        return (endDate);
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

    /**
     * @param webServerType : définit le serveur web à la source.
     */
    public void setWebServerType(String webServerType) {
        this.webServerType = webServerType;
    }

    /**
     * @param begDate : date de début de l'export à 0h.
     */
    public void setBegDate(Timestamp begDate) {
        this.begDate = begDate;
    }

    /**
     * @param endDate : date de fin de l'export à 0h.
     */
    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
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
     * @param args arguments de la ligne de commande.
     * @throws GetArgsException en cas d'erreur sur les paramètres
     */
    public GetArgs(String args[]) throws GetArgsException {

        int i;
        int n;
        int ip1;
        Date date;
        String currentParam;
        String nextParam;

        // Demande une analyse d'une date valide
        MyDateFormat.setLenient(false);
        n = args.length;

        System.out.println("nargs=" + n);
//    for(i=0; i<n; i++) System.out.println("args["+i+"]="+Args[i]);
        i = 0;
        while (i < n) {
//            System.out.println("args[" + i + "]=" + Args[i]);
            currentParam = args[i];
            ip1 = i + 1;
            nextParam = (ip1 < n) ? args[ip1] : null;
            switch (currentParam) {
                case "-webserver":
                    if (ip1 < n) {
                        if (ValidServers.isAValidServer(nextParam)) {
                            webServerType = nextParam;
                        } else {
                            throw new GetArgsException("Mauvais serveur web : " + nextParam);
                        }
                        i = ip1;
                    } else {
                        throw new GetArgsException("Serveur Web non défini");
                    }
                    break;
                case "-dbserver":
                    if (ip1 < n) {
                        if (ValidServers.isAValidServer(nextParam)) {
                            dbServerType = nextParam;
                        } else {
                            throw new GetArgsException("Mauvaise base de données : " + nextParam);
                        }
                        i = ip1;
                    } else {
                        throw new GetArgsException("Base de données non définie");
                    }
                    break;
                case "-b":
                    if (ip1 < n) {
                        try {
                            date = (Date) MyDateFormat.parse(nextParam);
                            begDate = new Timestamp(date.getTime());
                            i = ip1;
                        } catch (Exception exception) {
                            throw new GetArgsException("La date de début doit être valide jj/mm/aaaa : " + nextParam);
                        }
                    } else {
                        throw new GetArgsException("Date de début non définie");
                    }
                    break;
                case "-e":
                    if (ip1 < n) {
                        try {
                            date = (Date) MyDateFormat.parse(nextParam);
                            endDate = new Timestamp(date.getTime());
                            i = ip1;
                        } catch (Exception exception) {
                            throw new GetArgsException("La date de fin doit être valide jj/mm/aaaa : " + nextParam);
                        }
                    } else {
                        throw new GetArgsException("Date de fin non définie");
                    }
                    break;
                case "-u":
                    if (nextParam != null) {
                        try {
                            this.unum = Integer.parseInt(nextParam);
                            i = ip1;
                        } catch (Exception exception) {
                            throw new GetArgsException("L'identifiant du service d'urgence doit être numérique : " + nextParam);
                        }

                    } else {
                        throw new GetArgsException("ERREUR : Identifiant du service d'urgence non défini");
                    }
                    break;
                case "-clientCompanyUuid":
                    if (nextParam != null) {
                        this.clientCompanyUuid = nextParam;
                        i = ip1;
                    } else {
                        throw new GetArgsException("ERREUR : Identifiant UUID du service d'urgence non défini");
                    }
                    break;
                case "-clientCompanies":
                    readClientCompanies = true;
                    break;
                case "-patrimonies":
                    readPatrimonies = true;
                    break;
                case "-providerContacts":
                    readProviderContacts = true;
                    break;
                case "-providerCompanies":
                    readProviderCompanies = true;
                    break;
                case "-events":
                    readEvents = true;
                    break;
                case "-requests":
                    readSimplifiedRequests = true;
                    break;
                case "-d":
                    debugMode = true;
                    break;
                case "-t":
                    testMode = true;
                    break;
                default:
                    usage();
                    throw new GetArgsException("Mauvais argument : " + currentParam);
            }
            i++;
        }
        if (begDate.after(endDate)) {
            throw new GetArgsException("La date de début " + MyDateFormat.format(begDate)
                    + " doit être antérieure à la date de fin " + MyDateFormat.format(endDate));
        }
        if (unum > 0) {
            if (clientCompanyUuid != null) {
                System.out.println("unum:" + unum + ", clientCompanyUuid:" + clientCompanyUuid);
                throw new GetArgsException("ERREUR : Veuillez choisir unum ou uuid");
            } else {
                clientCompanyUuid = Md5.encode("u:" + unum);
            }
        }
    }

    /**
     * Affiche le mode d'utilisation du programme.
     */
    public static void usage() {
        System.out.println("Usage : java pi2a-client [-webserver webserver]"
                + " [-dbserver dbserver]"
                + " [-b début] [-f fin]"
                + " [-clientCompanies] [-patrimonies]"
                + " [-providerCompanies] [-providerContacts]"
                + " [-u unum|-clientCompany uuid]"
                + " [-events]"
                + " [-requests]"
                + " [-d] [-t]");
    }

    /**
     * @return le serveur de base de données de destination
     */
    public String getDbServerType() {
        return dbServerType;
    }

    /**
     * @param dbServerType définit le serveur de base de données de destination
     */
    public void setDbServerType(String dbServerType) {
        this.dbServerType = dbServerType;
    }

    /**
     * @return s'il faut lire ou non les sociétés. Remplace getReadCompanies()
     */
    public boolean getReadClientCompanies() {
        return readClientCompanies;
    }

    /**
     * @param readClientCompanies demande ou non la lecture des sociétés.
     * Remplace setReadCompanies()
     */
    public void setReadClientCompanies(boolean readClientCompanies) {
        this.readClientCompanies = readClientCompanies;
    }

    /**
     * @return s'il faut lire ou non les patrimoines
     */
    public boolean getReadPatrimonies() {
        return readPatrimonies;
    }

    /**
     * @param readPatrimonies demande ou non la lecture des patrimoines
     */
    public void setReadPatrimonies(boolean readPatrimonies) {
        this.readPatrimonies = readPatrimonies;
    }

    /**
     * @return retourne la référence au service d'urgence (identifiant interne)
     */
    public int getUnum() {
        return unum;
    }

    /**
     * @param unum définit la référence au service d'urgence (identifiant
     * interne)
     */
    public void setUnum(int unum) {
        this.unum = unum;
    }

    /**
     * @return l'identifiant du client
     */
    public String getClientCompanyUuid() {
        return clientCompanyUuid;
    }

    /**
     * @param clientCompanyUuid définit l'identifiant du client
     */
    public void setClientCompanyUuid(String clientCompanyUuid) {
        this.clientCompanyUuid = clientCompanyUuid;
    }

    /**
     * @return s'il faut lire ou non les fournisseurs
     */
    public boolean getReadProviderContacts() {
        return readProviderContacts;
    }

    /**
     * @param readProviderContacts demande ou non la lecture des fournisseurs
     */
    public void setReadProviderContacts(boolean readProviderContacts) {
        this.readProviderContacts = readProviderContacts;
    }

    /**
     * @return s'il faut lire ou non les sociétés des fournisseurs
     */
    public boolean getReadProviderCompanies() {
        return readProviderCompanies;
    }

    /**
     * @param readProviderCompanies demande ou non la lecture des sociétés des
     * fournisseurs
     */
    public void setReadProviderCompanies(boolean readProviderCompanies) {
        this.readProviderCompanies = readProviderCompanies;
    }

    /**
     * @return s'il faut lire ou non les événements
     */
    public boolean getReadEvents() {
        return readEvents;
    }

    /**
     * @param readEvents demande ou non la lecture des événements
     */
    public void setReadEvents(boolean readEvents) {
        this.readEvents = readEvents;
    }

    /**
     * @return s'il faut lire ou non les demandes d'intervention émises depuis
     * l'application mobile
     */
    public boolean getReadSimplifiedRequests() {
        return readSimplifiedRequests;
    }

    /**
     * @param readSimplifiedRequests demande ou non la lecture des demandes
     * d'intervention émises depuis l'application mobile
     */
    public void setReadSimplifiedRequests(boolean readSimplifiedRequests) {
        this.readSimplifiedRequests = readSimplifiedRequests;
    }

    /**
     * Affiche le contenu de GetArgs.
     *
     * @return retourne le contenu de GetArgs.
     */
    @Override
    public String toString() {
        return "GetArg: {"
                + "webServerType:" + getWebServerType()
                + ", dbServerType:" + getDbServerType()
                + ", début:" + MyDateFormat.format(getBegDate())
                + ", fin:" + MyDateFormat.format(getEndDate())
                + ", clientCompanies:" + getReadClientCompanies()
                + ", patrimonies:" + getReadPatrimonies()
                + ", providerCompanies:" + getReadProviderCompanies()
                + ", unum:" + unum
                + ", clientCompanyUuid:" + getClientCompanyUuid()
                + ", providerContacts:" + getReadProviderContacts()
                + ", events:" + getReadEvents()
                + ", requests:" + getReadSimplifiedRequests()
                + ", debugMode:" + getDebugMode()
                + ", testMode:" + getTestMode()
                + "}";
    }

}
