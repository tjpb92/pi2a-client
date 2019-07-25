package pi2a.client;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Cette classe sert à vérifier et à récupérer les arguments passés en ligne de
 * commande au programme pi2a-client.
 *
 * @author Thierry Baribaud
 * @version 0.17
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
     * readClientCompanies : demande la lecture des sociétés (true/false).
     * Valeur par défaut : false. Remplace readCompanies.
     */
    private boolean readClientCompanies = false;

    /**
     * readPatrimonies : demande la lecture des patrimoines (true/false). Valeur
     * par défaut : false.
     */
    private boolean readPatrimonies = false;

    /** clientCompanyUuid : identifiant du client pour lequel lire le patrimoine.
     * Non définit par défaut.
     */
    private String clientCompanyUuid = null;
    
    /**
     * readProviders : demande la lecture des fournisseurs (true/false). Valeur
     * par défaut : false.
     */
    private boolean readProviders = false;

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

        // Demande une analyse d'une date valide
        MyDateFormat.setLenient(false);
        n = args.length;

        System.out.println("nargs=" + n);
//    for(i=0; i<n; i++) System.out.println("args["+i+"]="+Args[i]);
        i = 0;
        while (i < n) {
//            System.out.println("args[" + i + "]=" + Args[i]);
            ip1 = i + 1;
            if (args[i].equals("-webserver")) {
                if (ip1 < n) {
                    if (args[ip1].equals("pre-prod") || args[ip1].equals("prod")) {
                        webServerType = args[ip1];
                    } else {
                        throw new GetArgsException("Mauvais serveur web : " + args[ip1]);
                    }
                    i = ip1;
                } else {
                    throw new GetArgsException("Serveur Web non défini");
                }
            } else if (args[i].equals("-dbserver")) {
                if (ip1 < n) {
                    if (args[ip1].equals("pre-prod") || args[ip1].equals("prod") || args[ip1].equals("dev")) {
                        dbServerType = args[ip1];
                    } else {
                        throw new GetArgsException("Mauvaise base de données : " + args[ip1]);
                    }
                    i = ip1;
                } else {
                    throw new GetArgsException("Base de données non définie");
                }
            } else if (args[i].equals("-b")) {
                if (ip1 < n) {
                    try {
                        date = (Date) MyDateFormat.parse(args[ip1]);
                        begDate = new Timestamp(date.getTime());
                        i = ip1;
                    } catch (Exception exception) {
                        throw new GetArgsException("La date de début doit être valide jj/mm/aaaa : " + args[ip1]);
                    }
                } else {
                    throw new GetArgsException("Date de début non définie");
                }
            } else if (args[i].equals("-e")) {
                if (ip1 < n) {
                    try {
                        date = (Date) MyDateFormat.parse(args[ip1]);
                        endDate = new Timestamp(date.getTime());
                        i = ip1;
                    } catch (Exception exception) {
                        throw new GetArgsException("La date de fin doit être valide jj/mm/aaaa : " + args[ip1]);
                    }
                } else {
                    throw new GetArgsException("Date de fin non définie");
                }
            } else if (args[i].equals("-clientCompanies")) {
                readClientCompanies = true;
            } else if (args[i].equals("-patrimonies")) {
                readPatrimonies = true;
                if (ip1 < n) {
                    clientCompanyUuid = args[ip1];
                    i = ip1;
                } else {
                    throw new GetArgsException("Identifiant du client non défini");
                }
            } else if (args[i].equals("-providers")) {
                readProviders = true;
            } else if (args[i].equals("-providerCompanies")) {
                readProviderCompanies = true;
            } else if (args[i].equals("-events")) {
                readEvents = true;
            } else if (args[i].equals("-d")) {
                debugMode = true;
            } else if (args[i].equals("-t")) {
                testMode = true;
            } else {
                throw new GetArgsException("Mauvais argument : " + args[i]);
            }
            i++;
        }
        if (begDate.after(endDate)) {
            throw new GetArgsException("La date de début " + MyDateFormat.format(begDate)
                    + " doit être antérieure à la date de fin " + MyDateFormat.format(endDate));
        }
    }

    /**
     * Affiche le mode d'utilisation du programme.
     */
    public static void usage() {
        System.out.println("Usage : java pi2a-client [-webserver prod|pre-prod]"
                + " [-dbserver prod|pre-prod]"
                + " [-b début] [-f fin]"
                + " [-clientCompanies] [-companies] [-patrimonies]"
                + " [-providerCompanies clientCompanyUuid] [-providers]"
                + " [-events]"
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
    public boolean getReadProviders() {
        return readProviders;
    }

    /**
     * @param readProviders demande ou non la lecture des fournisseurs
     */
    public void setReadProviders(boolean readProviders) {
        this.readProviders = readProviders;
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
                + ", clientCompanyUuid:" + getClientCompanyUuid()
                + ", providers:" + getReadProviders()
                + ", events:" + getReadEvents()
                + ", debugMode:" + getDebugMode()
                + ", testMode:" + getTestMode()
                + "}";
    }

}
