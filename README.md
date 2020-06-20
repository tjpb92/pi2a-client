# pi2a-client

Programme permettant d'importer des données depuis un extranet dans une base de données MongoDb locale.

## Utilisation:
```
java pi2a-client [-webserver webserver] [-dbserver dbserver] [-u unum|-clientCompany uuid] [-clientCompanies] [-patrimonies] [-providerContacts] 
                 [-events [-b début -e fin]] [-requests] [-d] [-t] 
```
où :
* ```-webserver webserver``` est la référence au serveur web distant, par défaut désigne le serveur de pré-production. Voir fichier *pi2a-client.prop* (optionnel).
* ```-dbserver dbserver``` est la référence à la base de données, par défaut désigne la base de données de pré-production. Voir fichier *pi2a-client.prop* (optionnel).
* ```-u unum|-clientCompany uuid``` est l'identifiant unique du client (paramètre optionnel).
* ```-clientCompanies``` demande la synchronisation des clients (paramètre optionnel).
* ```-patrimonies``` demande la synchronisation des patrimoines (paramètre optionnel).
* ```-providerContacts``` demande la synchronisation des fournisseurs (paramètre optionnel).
* ```-events``` demande la synchronisation des événements (paramètre optionnel).
* ```-b``` date de début (incluse) pour l'extraction des événéments au format ISO8601, non défini par défaut (paramètre optionnel).
* ```-e``` date de fin (exclue) pour l'extraction des événéments au format ISO8601, non défini par défaut (paramètre optionnel).
* ```-requests``` demande la synchronisation des demandes d'intervention émises depuis l'application mobile (paramètre optionnel).
* ```-d``` le programme s'exécute en mode débug, il est beaucoup plus verbeux. Désactivé par défaut (paramètre optionnel).
* ```-t``` le programme s'exécute en mode test, les transcations en base de données ne sont pas faites. Désactivé par défaut (paramètre optionnel).

## Pré-requis :
- Java 6 ou supérieur.
- Driver Mongodb pour Java
- Jackson
- Formatage des numéros de téléphone : [libphonenumber-8.11.1.jar](https://github.com/google/libphonenumber)

## Références:

http://websystique.com/java/json/jackson-convert-java-object-to-from-json/

http://www.baeldung.com/jackson-annotations
http://tutorials.jenkov.com/java-json/jackson-annotations.html
http://websystique.com/java/json/jackson-json-annotations-example/
https://avaldes.com/json-tutorial-jackson-annotations-part-2/
https://www.mkyong.com/java/jackson-tree-model-example/
https://www.tutorialspoint.com/jackson/jackson_tree_model.htm

https://www.javacodegeeks.com/2012/09/simple-rest-client-in-java.html
https://jersey.java.net/documentation/latest/client.html

https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
http://alvinalexander.com/blog/post/java/simple-https-example
https://www.mkyong.com/java/java-https-client-httpsurlconnection-example/

http://java2s.com/Tutorials/Java/URL_Connection_Address/Get_and_set_cookie_through_URLConnection_in_Java.htm

https://buzut.fr/commandes-de-base-de-mongodb/

