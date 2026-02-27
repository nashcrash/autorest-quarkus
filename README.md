# autorest-quarkus
Automatically generates a full CRUD REST API for the annotated entity.

Per deployare:
```commandline
mvn deploy -P release -s .settings.xml
```
e arriva sul maven-repository:
https://repo.maven.apache.org/maven2/io/github/nashcrash/autorest/

## Keystore
Assicurarsi che la chiave sia nel secret store:
```commandline
gpg --list-secret-keys --keyid-format LONG
```
altrimenti:
```commandline
gpg --import /c/Temp/000_Cancellare/Salvatore\ De\ Bonis_0xD3E13185DA4E2CC8_SECRET.asc
```
