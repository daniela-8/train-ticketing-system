DROP DATABASE IF EXISTS tickets_db;
CREATE DATABASE tickets_db;
USE tickets_db;

CREATE TABLE Users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       role VARCHAR(50) NOT NULL
);

CREATE TABLE Stations (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE Routes (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL
);

CREATE TABLE Trains (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        total_capacity INT NOT NULL
);

CREATE TABLE Rides (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       train_id BIGINT NOT NULL,
                       route_id BIGINT NOT NULL,
                       delay_minutes INT NOT NULL DEFAULT 0,
                       FOREIGN KEY (train_id) REFERENCES Trains(id),
                       FOREIGN KEY (route_id) REFERENCES Routes(id)
);

CREATE TABLE RideSegments (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              ride_id BIGINT NOT NULL,
                              from_station_id BIGINT NOT NULL,
                              to_station_id BIGINT NOT NULL,
                              available_seats INT NOT NULL,
                              FOREIGN KEY (ride_id) REFERENCES Rides(id) ON DELETE CASCADE,
                              FOREIGN KEY (from_station_id) REFERENCES Stations(id),
                              FOREIGN KEY (to_station_id) REFERENCES Stations(id)
);

CREATE TABLE Tickets (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         customer_id BIGINT NOT NULL,
                         ride_id BIGINT NOT NULL,
                         departure_station_id BIGINT NOT NULL,
                         arrival_station_id BIGINT NOT NULL,
                         number_of_seats INT NOT NULL,
                         status VARCHAR(50) NOT NULL,
                         FOREIGN KEY (customer_id) REFERENCES Users(id),
                         FOREIGN KEY (ride_id) REFERENCES Rides(id),
                         FOREIGN KEY (departure_station_id) REFERENCES Stations(id),
                         FOREIGN KEY (arrival_station_id) REFERENCES Stations(id)
);

ALTER TABLE RideSegments
    ADD COLUMN departure_time DATETIME NOT NULL,
    ADD COLUMN arrival_time DATETIME NOT NULL;

INSERT INTO Stations (name) VALUES
                                ('Cluj-Napoca'),
                                ('Bucharest'),
                                ('Brasov'),
                                ('Timisoara');


INSERT INTO Trains (name, total_capacity) VALUES ('InterCity-Express', 100);

INSERT INTO Routes (name) VALUES ('Cluj to Bucharest');

INSERT INTO Rides (train_id, route_id, delay_minutes) VALUES (1, 1, 0);

INSERT INTO RideSegments (ride_id, from_station_id, to_station_id, available_seats, departure_time, arrival_time)
VALUES (1, 1, 3, 100, '2023-12-01 08:00:00', '2023-12-01 11:00:00');

INSERT INTO RideSegments (ride_id, from_station_id, to_station_id, available_seats, departure_time, arrival_time)
VALUES (1, 3, 2, 100, '2023-12-01 11:15:00', '2023-12-01 14:00:00');

INSERT INTO Users (name, email, role) VALUES ('Test User', 'test@example.com', 'CUSTOMER');