
# Event Scheduling System

This project is an Event Scheduling System built with Java, Spring Boot, and JUnit 5 for unit testing. It enables users to create, invite, propose, vote, and finalize events. The application also handles user authentication and provides a simple API for managing events.

## Features

- User authentication and JWT token management for secure access.
- Event creation, viewing, and invitation management.
- Timeslot proposal and voting for events.
- Ability to finalize events.
- RESTful API with endpoints for event management.

## Technologies Used

- **Java 17+** for backend development.
- **Spring Boot** for the web application framework.
- **JUnit 5** for unit testing.
- **JWT** for user authentication.
- **H2 In-memory Database** for event and user data storage.
- **Maven** for dependency management.

## Prerequisites

- Java 17+ installed on your machine.
- Maven installed for building the project.
- An IDE such as IntelliJ IDEA, Eclipse, or VSCode for development.
- Postman or cURL for API testing.

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/SpencerWarezak/event-scheduling.git
cd event-scheduling
```

### 2. Build the Project

Ensure that Maven is installed, and build the project using the following command:

```bash
mvn clean install
```

### 3. Run the Application

Start the application by running:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

### 4. Testing the Application

You can test the API endpoints using tools like Postman or cURL.

#### Endpoints:

- **Signup**: `POST /auth/signup`
- **Login**: `POST /auth/login`
- **Create Event**: `POST /events/create`
- **Get Events**: `GET /events/getEvents`
- **Invite Event**: `POST /events/invite`
- **Decline Event**: `POST /events/decline`
- **Propose Event**: `POST /events/propose`
- **Vote Event**: `POST /events/vote`
- **Get Vote Event**: `GET /events/getVotes`
- **Finalize Event**: `POST /events/finalizeEvent`
- 

Include the JWT token in the `Authorization` header for protected endpoints.

```bash
Authorization: Bearer <your-jwt-token>
```

### 5. Running Unit Tests

To run the unit tests with JUnit 5, use the following Maven command:

```bash
mvn test
```

## API Endpoints

### 1. **Authentication Endpoints**

#### Signup

`POST /auth/signup`

Request Body:

```json
{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "password123"
}
```

#### Login

`POST /auth/login`

Request Body:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### 2. **Event Management Endpoints**

#### Create Event

`POST /events/create`

Request Body:

```json
{
  "creatorId": 1,
  "title": "Team Meeting",
  "description": "Monthly team sync-up meeting",
  "startDate": "2025-05-15T10:00:00Z",
  "endDate": "2025-05-15T12:00:00Z",
  "requiredVotes": 3
}
```

#### Get Events

`GET /events/getEvents`

Request Params:

```text
userId=1
```

#### Invite User to Event

`POST /events/invite`

Request Params:

```text
senderId=1&eventId=2&userId=3
```

#### Propose Timeslot

`POST /events/propose`

Request Params:

```text
eventId=2&userId=3&startTime=2025-05-15T10:00:00Z&endTime=2025-05-15T12:00:00Z
```

#### Vote on Timeslot

`POST /events/vote`

Request Params:

```text
userId=3&eventId=2&timeslotId=1&remove=false
```

#### Remove Vote on Timeslot

`POST /events/vote`

Request Params:

```text
userId=3&eventId=2&timeslotId=1&remove=true
```

#### Finalize Event

`POST /events/finalizeEvent`

Request Params:

```text
userId=1&eventId=2
```

## Folder Structure

```
/event-scheduling-app
    ├── src/
    │   ├── main/
    │   │   ├── java/
    │   │   └── resources/
    │   ├── test/
    │   │   └── java/
    ├── pom.xml
    ├── .gitignore
    ├── README.md
    └── target/
```

## Contributing

We welcome contributions! Please feel free to fork this repository, make changes, and submit a pull request.

1. Fork the repository.
2. Create a new branch (`git checkout -b feature-name`).
3. Commit your changes (`git commit -am 'Add feature'`).
4. Push to the branch (`git push origin feature-name`).
5. Submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
