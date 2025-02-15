Install Playwright's Browser: see https://playwright.dev/java/docs/browsers?utm_source=pocket_shared#install-browsers.  
(This will ba done automatically the first time the program is run.)
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

Uninstall them: see https://playwright.dev/java/docs/browsers?utm_source=pocket_shared#uninstall-browsers.
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="uninstall --all"
```

Build and run
```bash
mvn clean package
export GOOGLE_APPLICATION_CREDENTIALS=myproject_creds.json
java -jar target/web-vision-analyzer-1.0-SNAPSHOT.jar "https://mazure.fr"
```


mvn clean spring-boot:run

curl -X POST http://localhost:8080/api/load_image?url=https%3A%2F%2Fexample.com  
curl -v -X GET http://localhost:8080/api/get_image/0 --output toto.png  
curl -v -X GET http://localhost:8080/api/get_annotation/0

http://localhost:8080/
