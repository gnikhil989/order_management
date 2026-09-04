---
name: order-management-workflow
description: >-
  Standard engineering workflow and guidelines for the Order Management & Digital Wallet project.
  Enforces dedicated documentation per feature, visual flow diagrams, plain-English code comments,
  thin controllers, modular services, SOLID/ACID/DRY compliance, N+1 query prevention, and testing.
---

# Order Management Engineering Workflow & Guidelines

This skill defines the mandatory coding, architecture, documentation, and testing standards for developing the **Order Management & Digital Wallet System**.

---

## 1. Feature Documentation Rule (Dedicated Markdown File per Feature)

For **every single feature, domain slice, or infrastructure integration** (e.g., Auth, JWT, Wallet, Product Search, Order Placement, Redis Caching, Kafka Events, Rate Limiting, Exception Handling):
* A **dedicated markdown document** must be created in `order-management/docs/` (e.g., `04-user-auth-jwt-guide.md`, `05-wallet-concurrency-guide.md`).
* **Content Requirements**:
  1. **Visual Flow Diagrams**: Include clear ASCII/text or Mermaid flowcharts demonstrating the request lifecycle, validation steps, and data flow.
  2. **What is happening & Why**: The exact classes and components introduced.
  3. **Architectural Decisions & Rationale**: Explicitly link design choices to **SOLID**, **ACID**, or design patterns (e.g., *"Why SRP drove separating `JwtService` from `UserService`"*, *"Why Pessimistic Locking was chosen over Optimistic Locking for Wallet"*).
  4. **Interview Talking Points**: Specific questions product company interviewers ask about this feature and how to answer them.

---

## 2. Code Design & Architecture Standards

### A. Thin Controllers & Small, Modular Files (No Fat Controllers)
* **Controllers must be paper-thin**: Their ONLY responsibility is receiving HTTP requests, triggering `@Valid` validation, delegating to the service, and returning a `ResponseEntity<DTO>`. No business logic or database queries in controllers.
* **Keep classes small and focused**: Break large services into cohesive, single-purpose classes (e.g., `AuthService`, `JwtService`, `WalletService`, `ProductSpecification`) to make debugging effortless and increase scalability.

### B. Plain-English, Intuitive Code Comments
* Comments must be written in **clear, friendly, plain English** so that anyone (even a non-technical reader) can follow what the code is doing step by step.
* Explain the **business intent** behind logic rather than restating code syntax.

### C. SOLID Principles
* **Single Responsibility (SRP)**: One reason to change per class.
* **Open/Closed (OCP)**: Interfaces and polymorphic handlers for extensible operations.
* **Liskov Substitution (LSP)**: Derived classes must honor base contracts.
* **Interface Segregation (ISP)**: Lean, dedicated interfaces.
* **Dependency Inversion (DIP)**: Always use **Constructor Injection** with `private final` fields.

### D. ACID & Concurrency Control
* **Atomicity**: Complex multi-step operations wrapped in `@Transactional(rollbackFor = Exception.class)`.
* **Consistency & Locking**: Pessimistic locks (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) on financial/balance updates to prevent race conditions; Optimistic locks (`@Version`) on high-read inventory stock.
* **Ledger Auditability**: Immutable transaction history records for every balance modification.

### E. DRY & DTO Security
* **Never Expose Entities**: Always use Java Records / DTOs for request payloads and response bodies.
* **Centralized Error Handling**: Use `@RestControllerAdvice` to translate domain exceptions into standardized JSON responses.

### F. Zero N+1 Queries
* Never trigger lazy loading within loops.
* Always use `JOIN FETCH`, `@EntityGraph`, or batch fetching (`@BatchSize`) for related entities.

---

## 3. Testing Requirements

* Every feature must have corresponding **unit and/or integration test classes** in `src/test/java/`.
* Test both **Happy Paths** and **Edge Cases / Failure Paths** (e.g., insufficient balance, out of stock, duplicate idempotency key, invalid credentials).
* Use JUnit 5 and AssertJ (`assertThat`).

