## GeoBill v.1.0 Apps
#### A website for hosting billing software, designed for those starting a hosting business but find commercial hosting software too expensive. Built using Angular 20 and Java Spring Boot 3

### Frontend
#### Specification
- Build with Angular 20.0.4
- Template Rocker
#### Installation FE:
- run in terminal : npm install
- run in terminal : ng serve

### Backend
#### Specification
- OpenJDK 21
- Springboot 3
- MySQL database
#### Installation BE:
- run in terminal : mvn install
- create empty mysql database with name is "geobill"
- edit file application.properties:
  <pre>
    spring.datasource.username=[username DB]
    spring.datasource.password=[password DB]
  </pre>
- run in terminal : spring-boot:run
- Register at URL in postman or Insomnia : [POST] http://localhost:8082/v1/api/auth/register
  <pre>
     {
    	"fullName": "Administrator",
    	"email" : "admin@gmail.com",
    	"password": "1234"
     }
   </pre>
- Check your database and set ROLE to "ADMIN" because this registration url will create user with ROLE="USER"
- Setup at Postman or Insomnia with Basic Authentication, dan put those email and password 
  
