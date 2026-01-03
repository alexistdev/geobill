# GeoBill v1.0

GeoBill is a billing software website tailored for those launching hosting businesses. It aims to provide a cost-effective alternative to expensive commercial hosting software. GeoBill is built using Angular 20 for the frontend and Java Spring Boot 3 for the backend.

## Golang Version:
GeoBill Backend with Go Language: [https://github.com/alexistdev/geobill_golang_versions](https://github.com/alexistdev/geobill_golang_versions)

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
4. For testing purposes, the following credentials can be used. Please ensure the backend is running before attempting to login:
   ```bash
   Role User:
   Username: user@gmail.com
   Password: password

   Role Admin:
   Username: admin@gmail.com
   Password: password
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
6. Set up Basic Authentication in Postman or Insomnia using the email and password provided during registration.

With these steps, you'll have the GeoBill application up and running, ready for customization and use.
