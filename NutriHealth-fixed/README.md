# NutriHealth — Web-Based Diet Planning & Health Check-up System

A monolithic Spring Boot MVC web application for diet planning, health-record management, progress monitoring, appointments, reporting, and role-based healthcare workflows.

## Technology

- Java 17
- Spring Boot 3.2.5
- Spring MVC
- Spring Security 6
- Spring Data JPA / Hibernate
- Thymeleaf
- HTML5 + CSS3 + Vanilla JavaScript
- MySQL
- Maven

## Run in IntelliJ IDEA

1. Open this folder in IntelliJ IDEA.
2. Make sure JDK 17+ is selected.
3. Create a MySQL database named `nutrihealth`.
4. Configure database credentials in `src/main/resources/application.properties` or environment variables.
5. Reload the Maven project.
6. Run `NutriHealthApplication.java`.
7. Open `http://localhost:8080`.

## Demo accounts

- Patient: `patient@nutrihealth.com` / `Patient@123`
- Doctor: `doctor@nutrihealth.com` / `Doctor@123`
- Nutritionist: `nutritionist@nutrihealth.com` / `Nutritionist@123`
- Coordinator: `coordinator@nutrihealth.com` / `Coordinator@123`
- Clinic Admin: `admin@nutrihealth.com` / `Admin@123`
- IT Operations: `it@nutrihealth.com` / `IT@123`

## UI update

The project includes a redesigned premium dark healthcare interface with:

- Glass-style navigation and dashboard surfaces
- Responsive role-specific dashboards
- Rich KPI cards and health telemetry panels
- Improved diet-plan, appointment and recommendation cards
- Premium landing page hero and role demo cards
- Responsive authentication screens
- Consistent typography, spacing, borders, shadows and hover states
- Print-friendly report styling

The application is intentionally a single Spring Boot MVC + Thymeleaf project. No React/Vite frontend is required.

## UI Theme Update
The application uses a clean white healthcare visual theme with soft green/teal accents. The landing-page hero includes a locally bundled, subtle animated healthcare background video under `src/main/resources/static/video/healthcare-bg.mp4`, so no separate frontend build or external video dependency is required.


## Demo login troubleshooting

The application reconciles all six demo accounts on every startup. The demo
passwords are reset to the documented values and each account is forced active.

- Patient: `patient@nutrihealth.com` / `Patient@123`
- Doctor: `doctor@nutrihealth.com` / `Doctor@123`
- Nutritionist: `nutritionist@nutrihealth.com` / `Nutritionist@123`
- Coordinator: `coordinator@nutrihealth.com` / `Coordinator@123`
- Clinic Admin: `admin@nutrihealth.com` / `Admin@123`
- IT Operations: `it@nutrihealth.com` / `IT@123`

If IntelliJ still shows an old session after replacing the project, stop the
old application process and start the new `NutriHealthApplication` process.
Then open `/login` in a fresh browser tab.

## CRUD Operations

The application now includes role-protected CRUD operations for the main functional areas:

- Doctor: create, read, update and delete health records and clinical assessments.
- Nutritionist: create, read, update and delete diet plans, meal recommendations and food-database items.
- Patient: create, read, update and delete food-intake entries; create, read, update and delete owned appointments (with cancel support).
- Clinic Admin: update/delete clinic user accounts, update/delete appointments, and delete diet plans.
- IT Officer: create, read, update and delete/provision user accounts, plus activate/deactivate controls.
- Patient: create, read, update and delete dedicated health-progress entries (weight, BMI, blood pressure, blood sugar and heart rate).
- Clinic Admin: create, read, update and delete saved clinic-report definitions, plus create/update/delete appointments.

Update/delete operations verify ownership where appropriate before changing clinical or patient-owned data.
