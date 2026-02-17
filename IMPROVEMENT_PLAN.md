# SpaceStacker Improvement Plan

This document outlines a roadmap for improving the robustness and feature set of the SpaceStacker project.

## 1. Technical Debt & Robustness
### 1.1 Dependency & Build Modernization
- **Update POM:** Migrate from legacy dependencies (e.g., `commons-lang` 2.6 to `commons-lang3`).
- **Standardize Java Version:** Ensure consistent use of Java 17/21 across environments.
- **CI/CD Pipeline:** Implement GitHub Actions for automated testing and building on every push.
- **Dependency Management:** Utilize a cleaner dependency management structure (parent POM or BOM where applicable).

### 1.2 Code Quality & Error Handling
- **Improve JaroWinkler Implementation:** Ensure the `JaroAlg` is numerically stable and correctly handles edge cases (like empty strings) beyond the basic fix already applied.
- **Refactor Event Listeners:** Decouple logic from `listeners` into service classes to improve testability.
- **Centralized Logging:** Implement a proper logging wrapper for consistent debug/error reporting across the Spigot/Paper versions.
- **Unit Testing:** Expand test coverage for core logic (merging logic, location calculations, storage adapters).

### 1.3 Persistence Layer
- **Robust Storage Adapters:** Move beyond simple TypeAdapters to a more resilient storage service (supporting JSON/Flatfile and SQL/MariaDB).
- **Data Validation:** Implement validation checks for `JoLocation` and `StackedEntity` data when loading from disk to prevent corruption-related crashes.

## 2. New Features & Enhancements
### 2.1 Core Mechanics
- **Smart Stacking:** Intelligent stacking logic based on NBT data to prevent losing custom entity attributes.
- **Async Processing:** Move entity and item merge calculations to an asynchronous task to minimize main-thread impact (TPS optimization).
- **Expanded Upgrade System:** Introduce a modular upgrade system for `StackedSpawner` (e.g., speed, range, drop multipliers).

### 2.2 User Experience (UX)
- **Holographic Integration:** Improve the integration with HolographicDisplays or use modern Display Entities (1.20+) for stack labels.
- **Advanced GUI:** A more intuitive and configurable `UpgradeContainer` GUI with support for custom items and sounds.
- **Detailed Command Feedback:** Use the `MessageFactory` to provide richer, color-coded feedback and interactive hover messages for commands.

### 2.3 Performance Optimization
- **Spatial Indexing:** Optimize entity lookups for merging using a simple spatial hash or quadtree to handle high-entity environments.
- **Resource Caching:** Cache frequent lookups (e.g., configuration values, localized strings).

## 3. Deployment & Release
- **Proper Maven Deployment:** Configure `distributionManagement` to support automated releases to a private or public Maven repository.
- **Update Documentation:** Comprehensive Wiki or README covering the new upgrade system and configuration options.
