# GeoBill v1.0

GeoBill is a billing software website tailored for those launching hosting businesses. It aims to provide a cost-effective alternative to expensive commercial hosting software. GeoBill is built using Angular 20 for the frontend and Java Spring Boot 3 for the backend.

## Frontend

### Specifications
- Built with Angular 20.0.4
- Utilizes the Rocker template

### Installation Instructions
1. Open your terminal.
2. Run the following command to install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   ng serve
   ```

## Backend

### Specifications
- OpenJDK 21
- Spring Boot 3
- MySQL Database

### Installation Instructions
1. Open your terminal.
2. Run the following command to install dependencies:
   ```bash
   mvn install
   ```
3. Create an empty MySQL database named `geobill`.
4. Edit the `application.properties` file to configure your database credentials:
   ```properties
   spring.datasource.username=[your_database_username]
   spring.datasource.password=[your_database_password]
   ```
5. Start the backend server:
   ```bash
   mvn spring-boot:run
   ```
6. Register a new user at the following endpoint using Postman or Insomnia:
   - **POST** `http://localhost:8082/v1/api/auth/register`
   ```json
   {
     "fullName": "Administrator",
     "email": "admin@gmail.com",
     "password": "1234"
   }
   ```
7. After registration, update the user's role to "ADMIN" in your database since the registration API defaults to the "USER" role.
8. Set up Basic Authentication in Postman or Insomnia using the email and password provided during registration.

With these steps, you'll have the GeoBill application up and running, ready for customization and use.
