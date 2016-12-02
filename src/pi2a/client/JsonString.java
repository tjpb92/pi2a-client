package pi2a.client;

/**
 * Classe représentant la réponses d'une requête HTTP au format JSON dans une
 * chine de caractères.
 *
 * @author Thierry Baribaud
 * @version 0.08
 */
public class JsonString {

    /**
     * Chaine de caractères venant de la requête HTTP
     */
    private final String jsons;

    /**
     * Indice courant du dernier caractère lu dans jsons
     */
    private int idx = 0;

    /**
     * Niveau d'analyse courant
     */
    private int level = 0;

    /**
     * Constructeur principal de la classe JsonString
     *
     * @param jsons chaine de caractères venant de la requête HTTP
     */
    public JsonString(String jsons) {
        this.jsons = jsons;
    }

    /**
     * @return retourne le json courant et se postionne sur le suivant
     */
    public String next() {
        char c;
        StringBuffer json = new StringBuffer();
        boolean flag;

        String retString = null;

        flag = idx < jsons.length();
        while (flag) {
//            System.out.print("idx=" + idx);
            c = jsons.charAt(idx);
//            System.out.print("." + idx + "=" + c);
            if (c == '{') {
                level++;
            } else if (c == '}') {
                level--;
            }
            idx++;
            flag = idx < jsons.length();

            if (level >= 2) {
                json.append(c);
            } else if (level == 1 && c == '}') {
                json.append(c);
                retString = json.toString();
                flag = false;
            }
        }
        return retString;
    }
}
