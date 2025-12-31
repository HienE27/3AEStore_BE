Security & Environment variables (local development)

1. Never commit real secrets (client secrets, API keys) to git.

2. Local workflow:
   - Create a `.env` file in the project root (do NOT commit it).
   - Add the variables:
     ```
     GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
     GOOGLE_CLIENT_SECRET=your-google-client-secret
     APP_BASE_URL=http://localhost:8080
     ```
   - Start your application with those env vars available (see Windows instructions below).

3. If a secret was already committed:
   - Revoke the secret in Google Console and create a new one.
   - Consider removing the secret from git history (advanced): use `git filter-repo` or `git filter-branch`.

Windows: set environment variables for the current terminal (PowerShell):

```powershell
$env:GOOGLE_CLIENT_ID = "your-google-client-id.apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET = "your-google-client-secret"
# then run your Spring Boot app in the same terminal, e.g.:
mvn spring-boot:run
```

Permanently (System/User environment variables):
1. Search "Environment Variables" in Start menu → "Edit the system environment variables" → "Environment Variables..."
2. Under "User variables" click "New..." and add `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET`.
3. Restart IDE/terminal to pick up changes.

Spring Boot: you can reference env vars as:
```
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
```


