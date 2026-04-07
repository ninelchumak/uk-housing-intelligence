# UK Housing Intelligence Platform
Data-intensive application based on DDIA principles (Event Sourcing & CQRS).

## 📌 Project Overview

This project is a high-performance backend system for managing property listings, designed with **Event Sourcing** and **CQRS** (Command Query Responsibility Segregation) patterns.

Inspired by **"Designing Data-Intensive Applications" (DDIA)** by Martin Kleppmann, the system treats the "state" not as a fixed row in a database, but as a sequence of immutable events. This ensures 100% auditability and the ability to reconstruct the state of any property at any point in time.

## 🏗 Key Architectural Patterns

- **Event Sourcing**: Every change (creation, price update) is stored as an immutable event in an append-only log (`EventStore`).
- **Aggregate Root (`PropertyAggregate`)**: Encapsulates business logic and ensures consistency. It is "rehydrated" by replaying historical events.
- **Command Side**: Decoupled handlers that process user intent and generate events.
- **Multi-Module Maven Structure**: Clear separation between API contracts, Command logic, and Query models.

## 📂 Project Structure

- `housing-service-api`: Shared DTOs, Commands, and Event definitions (Shared Kernel).
- `housing-command-side`: Core business logic, Aggregates, and Repositories.
- `housing-query-side`: (In progress) Materialized views and read-optimized projections.
- `housing-data-ingest`: Utilities for bulk data processing.

## 🛠 Tech Stack

- **Java 21**: Utilizing Records for immutable events and Sealed Classes for type safety.
- **Maven**: Multi-module dependency management.
- **JUnit 5 & Mockito**: Comprehensive unit and E2E integration testing.

## 🚀 Getting Started

### Prerequisites
- JDK 21 or higher
- Maven 3.8+

### Installation & Build
```bash
git clone [https://github.com/ninelchumak/uk-housing-intelligence.git](https://github.com/ninelchumak/uk-housing-intelligence.git)
cd uk-housing-intelligence
mvn clean install
