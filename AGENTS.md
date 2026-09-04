# Project Engineering Rules & Guidelines

These instructions govern all development across the Order Management & Digital Wallet project:

1. **Dedicated Feature Guides with Flow Diagrams**: For every new feature, domain, or integration, create a dedicated markdown file in `order-management/docs/` explaining the logic with clear visual flowcharts, architectural decisions (SOLID, ACID, etc.), and interview Q&As.
2. **Thin Controllers & Modular Files (No Fat Classes)**: Controllers must only handle routing and validation. Keep classes and methods small, focused, and single-purpose to ensure easy debugging and high scalability.
3. **Plain-English Code Comments**: Add intuitive comments in code that explain the business logic clearly so even a non-technical reader can easily understand the flow.
4. **SOLID, ACID, & DRY**: Enforce constructor injection, SRP separation between layers, DTO pattern (never return entities), atomicity via `@Transactional`, and pessimistic/optimistic locking for concurrency.
5. **Prevent N+1 Queries**: Always use `JOIN FETCH` or `@EntityGraph` when loading related entities.
6. **Testing**: Write unit/integration tests (`*Test.java`) covering both successful flows and failure/edge cases for every feature.

