# Insurance Login App (Phase 1)

A small Spring Boot app: login, register, secured dashboard. This is the seed
app we'll wrap in the full company-style pipeline (Jenkins -> Maven -> S3 ->
Ansible -> Docker -> DockerHub -> AWS) in later phases.
Ansible -> Docker -> DockerHub -> AWS) in later phases.
## Requirements to run locally
- Java 17+ (`java -version`)
- Maven 3.8+ (`mvn -version`)
- Internet access on YOUR machine (to download dependencies from Maven Central
  the first time - this sandbox environment can't reach Maven Central, so the
  build has not been run/verified here; run it on your own laptop)

## Run it
```bash
cd insurance-login-app
mvn spring-boot:run
```
Then open: http://localhost:8080

- A demo user is auto-created on startup: **username: demo / password: demo1234**
- Or click "Create an account" to register your own
- H2 console (to inspect the in-memory DB directly): http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:insurancedb`
  - User: `sa`, Password: (blank)

## What's real here (not mocked)
- Spring Security form login with BCrypt password hashing
- A real `app_user` table via Spring Data JPA
- Route protection - `/dashboard` requires authentication, `/register` and `/login` don't

## What's next (later phases)
1. Swap H2 for MySQL, pointed at a local MySQL, then later an AWS RDS instance
   (the commented block in `application.properties` shows the swap)
2. Push this to GitHub, set up branching (develop / feature / release)
3. Jenkins master-slave setup + multibranch pipeline
4. Maven build -> copy artifact to S3 -> Ansible pulls from S3 -> configures Tomcat/app server
5. Dockerize the app, push to DockerHub, pull + deploy per environment
6. Provision Dev/QA/UAT on AWS (separate accounts/IAM as planned), wire up CloudWatch/CloudTrail

## Project structure
```
src/main/java/com/insurance/loginapp/
  config/SecurityConfig.java     - security rules, password encoder
  config/DataSeeder.java         - creates demo user on startup
  controller/AuthController.java - login/register/dashboard routes
  model/User.java                - JPA entity
  repository/UserRepository.java - DB access
  service/UserDetailsServiceImpl.java - tells Spring Security how to load users
src/main/resources/
  application.properties         - DB config (H2 now, RDS later)
  templates/                     - login.html, register.html, dashboard.html
  static/css/style.css
```
