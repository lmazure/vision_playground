Install Playwright's Browser: see https://playwright.dev/java/docs/browsers?utm_source=pocket_shared#install-browsers/.  
Uninstall them: see https://playwright.dev/java/docs/browsers?utm_source=pocket_shared#uninstall-browsers.

```bash
mvn clean package
export GOOGLE_APPLICATION_CREDENTIALS=vision-450621-29bd9b45387b.json
java -jar target/web-vision-analyzer-1.0-SNAPSHOT.jar "https://mazure.fr"
```
