Install Playwright's Browser: see https://playwright.dev/java/docs/browsers?utm_source=pocket_shared#install-browsers.
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
