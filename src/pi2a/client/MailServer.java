package pi2a.client;

import utils.ApplicationProperties;

/**
 * Classe décrivant les paramètres d'accès à un serveur de mail.
 * 
 * ATTENTION : A placer dans LibUtils et à supprimer de A2ITClient ultérieurement.
 *
 * @author Thierry Baribaud
 * @version 0.24
 */
public class MailServer {

    /**
     * Nom du serveur de mail
     */
    private String name;

    /**
     * Adresse IP du serveur de mail
     */
    private String ipAddress;

    /**
     * Adresse de l'émetteur
     */
    private String fromAddress;

    /**
     * Adresse du destinataire
     */
    private String toAddress;

    /**
     * Nom d'utilisateur
     */
    private String user;

    /**
     * Mot de passe de l'utilisateur
     */
    private String password;

    /**
     * Exception pouvant être lancée en cas de mauvais paramètres dans le
     * fichier des propriétés
     */
    public class MailServerException extends Exception {

        private final static String ERRMSG
                = "Problem during instantiation of MailServer object";

        public MailServerException() {
            super(ERRMSG);
        }

        public MailServerException(String errMsg) {
            super(ERRMSG + " : " + errMsg);
        }
    }

    /**
     * Constructeur principal de la classe MailServer
     *
     * @param applicationProperties définit les propiétés de l'API REST depuis
     * un fichier de propriétés
     * @throws a2itclient.MailServer.MailServerException exception lancé en cas
     * de problème
     */
    public MailServer(ApplicationProperties applicationProperties) throws MailServerException {
        String value;

        value = applicationProperties.getProperty("mailServer.name");
        if (value != null) {
            this.name = value;
        } else {
            throw new MailServerException("Le nom du serveur de mail n'est pas défini");
        }

        value = applicationProperties.getProperty("mailServer.ipAddress");
        if (value != null) {
            this.ipAddress = value;
        } else {
            throw new MailServerException("L'adresse IP du serveur de mail n'est pas définie");
        }

        value = applicationProperties.getProperty("mailServer.fromAddress");
        if (value != null) {
            this.fromAddress = value;
        } else {
            throw new MailServerException("L'adresse de l'émetteur n'est pas définie");
        }

        value = applicationProperties.getProperty("mailServer.toAddress");
        if (value != null) {
            this.toAddress = value;
        } else {
            throw new MailServerException("L'adresse du destinataire n'est pas définie");
        }

        value = applicationProperties.getProperty("mailServer.user");
        if (value != null) {
            this.user = value;
        } else {
            throw new MailServerException("Le nom de l'utilisateur n'est pas défini");
        }

        value = applicationProperties.getProperty("mailServer.password");
        if (value != null) {
            this.password = value;
        } else {
            throw new MailServerException("Le mot de passe de l'utilisateur n'est pas défini");
        }
    }

    /**
     * Retourne le nom du serveur de mail
     *
     * @return le nom du serveur de mail
     */
    public String getName() {
        return (name);
    }

    /**
     * Définit le nom du serveur de mail
     *
     * @param name définit le nom du serveur de mail
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return retourne l'adresse IP du serveur de mail
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * @param ipAddress définit l'adresse IP du serveur de mail
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * @return retourne l'adresse de l'émetteur
     */
    public String getFromAddress() {
        return fromAddress;
    }

    /**
     * @param fromAddress définit l'adresse de l'émetteur
     */
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    /**
     * @return l'adresse du destinataire
     */
    public String getToAddress() {
        return toAddress;
    }

    /**
     * @param toAddress définit l'adresse du destinataire
     */
    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    /**
     * @return le nom de l'utilisateur
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user définit le nom de l'utilisateur
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return le mot de passe de l'utilisateur
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password définit le mot de passe de l'utilisateur
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Retourne les valeurs sous forme textuelle
     *
     * @return Retourne les valeurs sous forme textuelle
     */
    @Override
    public String toString() {
        return "MailServer:{"
                + "name:" + name
                + ", ipAddress:" + ipAddress
                + ", fromAddress:" + fromAddress
                + ", toAddress:" + toAddress
                + ", user:" + user
                + ", password:" + password
                + "}";
    }

}
