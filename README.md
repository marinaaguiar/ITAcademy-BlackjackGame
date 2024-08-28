# Blackjack Game API with Spring Boot

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Technologies](#technologies)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Database Configuration](#database-configuration)
- [Testing](#testing)
- [Documentation](#documentation)

## Introduction

This project is a reactive API developed in Java using Spring Boot for a Blackjack game. The primary objective is to learn how to implement a reactive API with Spring WebFlux, connecting to both relational (MySQL) and non-relational (MongoDB) databases. The application includes functionalities to manage players, hands of cards, and game rules. Additionally, the project aims to implement unit tests, apply documentation techniques using Swagger, and explore Dockerization and deployment of the Spring Boot application.

## Features

- **Reactive Programming**: Implemented using Spring WebFlux for non-blocking, asynchronous processing.
- **Database Integration**: Utilizes both MongoDB and MySQL for data persistence.
- **Comprehensive Game Management**: Includes endpoints for creating games, making moves, deleting games, and retrieving player rankings.
- **Global Exception Handling**: Centralized error handling using a GlobalExceptionHandler.
- **Unit Testing**: Tested with JUnit and Mockito to ensure the reliability of the API.
- **API Documentation**: Automatically generated using Swagger for easy exploration and testing of the API.
- **Dockerization**: The application is Dockerized for ease of deployment and scalability.

## Technologies

- **Java 17**
- **Spring Boot 3.x**
- **Spring WebFlux**
- **MySQL**
- **MongoDB**
- **JUnit 5**
- **Mockito**
- **Swagger**
- **Docker**

## Getting Started

### Prerequisites

Ensure that you have the following installed:

- JDK 17 or higher
- Maven 3.x
- Docker (optional, for containerization)
- MySQL and MongoDB instances (local or remote)

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/blackjack-api.git
   cd blackjack-api

2. Configure the database connections in application.properties or application.yml:
    ```
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/blackjack
        username: root
        password: yourpassword
      data:
        mongodb:
          uri: mongodb://localhost:27017/blackjack
    ```

3. Build the project:
      ```
      mvn clean install
      ```

4. Run the application:
      ``` 
      mvn spring-boot:run
      ```
### API Endpoints
  #### Game Management

- Create Game
  
  - Method: POST
  - Endpoint: /game/new
  - Description: Creates a new Blackjack game for a single player.
  - Request Body: { "playerName": "Marina Aguiar" }
  - Response: 201 Created with game details.

- Get Game Details
  
  - Method: GET
  - Endpoint: /game/{id}
  - Description: Retrieves details of a specific Blackjack game.
  - Response: 200 OK with game details.

- Make Move
  
  - Method: POST
  - Endpoint: /game/{id}/play
  - Description: Makes a move in an existing Blackjack game.
  - Request Body: { "playerAction": "HIT", "amountBet": 100 }
  - Response: 200 OK with the result of the move.
  
- Delete Game
  
  - Method: DELETE
  - Endpoint: /game/{id}/delete
  - Description: Deletes an existing Blackjack game.
  - Response: 204 No Content.

  #### Player Management

- Get Player Rankings
  - Method: GET
  - Endpoint: /player/ranking
  - Description: Retrieves the ranking of players based on their performance in Blackjack games.
  - Response: 200 OK with the ranking list.

- Change Player Name

  - Method: PUT
  - Endpoint: /player/{playerId}
  - Description: Changes the name of a player.
  - Request Body: { "newName": "Marina Aguiar" }
  - Response: 200 OK with updated player information.
  
### Database Configuration
This application is configured to use both MySQL and MongoDB. Ensure that both databases are running and accessible from your Spring Boot application.

### MySQL
Configure your MySQL database connection in the application.properties or application.yml file:

    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/blackjack
        username: root
        password: yourpassword
    
### MongoDB
Configure your MongoDB database connection similarly:

    spring:
      data:
        mongodb:
          uri: mongodb://localhost:27017/blackjack

### Testing
Unit tests have been implemented using JUnit 5 and Mockito to ensure the application's functionality. You can run the tests using Maven:
    
    mvn test

### Documentation
Swagger has been integrated to provide automatic API documentation. Once the application is running, you can access the Swagger UI at:

    http://localhost:8080/swagger-ui.html




