Install Playwright's system dependencies
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

```bash
mvn clean package
export GOOGLE_APPLICATION_CREDENTIALS=vision-450621-29bd9b45387b.json
java -jar target/web-vision-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar
```

