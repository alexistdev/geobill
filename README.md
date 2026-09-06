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
- GeoLicense client starter, bundled in `PLUGIN/`

### Installation Instructions
1. Open your terminal at the root of this repository.
2. Install the license plugin first. The backend depends on `com.alexistdev:geolicense-client-starter`, whose source ships in `PLUGIN/`. It is not published to Maven Central, so this command is what puts the artifact into your local `~/.m2` repository:
   ```bash
   cd PLUGIN && mvn clean install
   ```
   Skipping this step makes the backend build fail with `Could not resolve dependencies ... geolicense-client-starter:jar:1.0.2`.
3. Move into the backend module and install its dependencies:
   ```bash
   cd ../BE/geobill && mvn install
   ```
4. Create an empty MySQL database named `geobill`.
5. Edit the `application.properties` file to configure your database credentials:
   ```properties
   spring.datasource.username=[your_database_username]
   spring.datasource.password=[your_database_password]
   ```
6. Configure the license properties in the same file. Obtain a key for the `BILL` product from https://geolicense.my.id. Without a valid key the application refuses to start:
   ```properties
   geolicense.server-url=https://geolicense.my.id
   geolicense.license-key=[your_license_key]
   geolicense.product-sku=[your_product_sku]
   ```
7. Start the backend server:
   ```bash
   mvn spring-boot:run
   ```
8. Set up Basic Authentication in Postman or Insomnia using the email and password provided during registration.

With these steps, you'll have the GeoBill application up and running, ready for customization and use.

## Screenshots

#### Administrator Page:
![Administrator Page](https://github.com/alexistdev/geobill/blob/main/gambar/gambar1.png?raw=true)
![Administrator Page](https://github.com/alexistdev/geobill/blob/main/gambar/gambar2.png?raw=true)
![Administrator Page](https://github.com/alexistdev/geobill/blob/main/gambar/gambar3.png?raw=true)
![Administrator Page](https://github.com/alexistdev/geobill/blob/main/gambar/gambar4.png?raw=true)
