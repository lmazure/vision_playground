# Application
Build and run
```bash
export GOOGLE_APPLICATION_CREDENTIALS=myproject_creds.json
mvn clean spring-boot:run
```
Display http://localhost:8080/.

# Playwright
Install Playwright's Browser: see https://playwright.dev/java/docs/browsers?utm_source=pocket_shared#install-browsers.  
(This will ba done automatically the first time the program is run.)
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

Uninstall them: see https://playwright.dev/java/docs/browsers?utm_source=pocket_shared#uninstall-browsers.
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="uninstall --all"
```

-----

`tempo` contains a Python script `web-vision-automation.py` that clicks on an element given its text description.
```bash
python web-vision-automation.py
```
