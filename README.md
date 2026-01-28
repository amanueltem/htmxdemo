# 🚀 FinanceManager-HTMX

A **high-performance, hypermedia-driven finance management system** built with **Java 25, Spring Boot 4**, and **HTMX**.

FinanceManager-HTMX provides:

* A **shared roommate fund system** with an enterprise-grade **Maker–Checker workflow**
* A **private personal expense tracker** for individual budgeting

The goal is simple: **maximum correctness, minimum complexity**.

---

## 📌 Project Philosophy: The HTMX Advantage

This project intentionally rejects modern *JavaScript fatigue*.

Instead of React, Angular, or Vue, the UI is powered by **HTMX + server-rendered HTML** to deliver a true SPA-like experience—without a frontend framework.

### Why HTMX over React / Vue / Angular?

* **Unified State**
  The *single source of truth* lives on the server (Java + DB). No split-brain state between Redux stores and REST APIs.

* **Reduced Complexity**
  No NPM. No Webpack. No transpilers. No hydration bugs.

* **Performance by Default**
  HTML fragments are streamed directly to the browser and swapped instantly—no JSON → JS → DOM overhead.

* **Locality of Behavior**
  What a button does is visible right in the HTML:

  ```html
  <button hx-post="/expenses" hx-target="#content">Save</button>
  ```

* **Long-Term Maintainability**
  HTML + HTTP + Java. Boring. Predictable. Scalable.

---

## ✨ Key Features

### 👥 Roommate Group Fund (Shared)

* **Maker–Checker Workflow**
  High-integrity transaction processing:

  * One user *inputs* a transaction
  * Another user *authorizes* or *rejects* it

* **Rolling Balance Logic**
  Automatically computes carry-over balances using *all historical authorized transactions*.

* **Edit Request System**

  * Authorized records are immutable
  * Changes require a formal edit request
  * Edit requests follow the same approval workflow

This mirrors real-world **banking and core-finance systems**.

---

### 💳 Personal Expense Tracker (Private)

* Private ledger per user
* Category-based tracking (Food, Taxi, Shower, etc.)
* **Dynamic data visualization** using **Chart.js**
* Charts are **lazy-loaded via HTMX** for fast initial page loads

---

## ⚡ Technical Excellence

* **Java 25 & Spring Boot 4.0.1**
  Leveraging the latest JVM optimizations and language features

* **GraalVM Native Image**
  Ahead-of-Time (AOT) compilation into a native Windows `.exe`:

  * Millisecond startup time
  * Extremely low memory footprint

* **Reflection-Free Architecture**

  * Java Records for DTOs
  * `@RegisterReflectionForBinding` for GraalVM compatibility
  * Minimal runtime reflection

---

## 🛠 Tech Stack

| Layer    | Technology                            |
| -------- | ------------------------------------- |
| Language | Java 25 (OpenJDK / GraalVM)           |
| Backend  | Spring Boot 4.0.1                     |
| Frontend | HTMX, Thymeleaf, Tailwind-inspired CSS |
| Database | PostgreSQL   |
| Charts   | Chart.js                              |
| Security | Spring Security (Role-based access)   |

---

## 🗄️ User Roles & Responsibilities

| Role           | Responsibility                                                          |
| -------------- | ----------------------------------------------------------------------- |
| **INPUTTER**   | Create expenses/deposits and request edits on authorized data           |
| **AUTHORIZER** | Review group activity and authorize or reject entries created by others |

---

## 🚀 Getting Started

### Prerequisites

* **GraalVM JDK 25**
  Download from GraalVM Community Edition

* **Maven 3.9+**

* **Windows (Native Build only)**
  Visual Studio 2022 with **Desktop Development with C++** installed

---

### Installation

```bash
git clone git@github.com:amanueltem/htmxdemo.git
cd htmxdemo
```

---

### Running in Development

```powershell
./mvnw spring-boot:run
```

Application will start using the JVM with hot reload enabled.

---

### Compiling to Native Image (Windows)

To generate a standalone executable that starts in milliseconds:

```powershell
# Ensure you are in the x64 Native Tools Command Prompt for VS 2022
./mvnw native:compile -Pnative
```

The resulting binary will be located at:

```
target/FinanceManager-HTMX.exe
```

---

## 📊 Business Logic: The "Carry Over" Formula

The net group fund balance is calculated using **only AUTHORIZED records**.

### 1️⃣ Opening Balance

Sum of all historical transactions **before the selected month**:

```
AUTHORIZED Deposits − AUTHORIZED Expenses
```

---

### 2️⃣ Period Activity

Transactions **within the selected month**:

```
Monthly Deposits − Monthly Expenses
```

---

### 3️⃣ Net Fund Balance

```
Net Fund = Opening Balance + Period Activity
```

This guarantees:

* Full auditability
* Zero double-counting
* Deterministic results at any point in time

---

## 🧠 Design Inspiration

This system borrows ideas from:

* Core banking platforms
* Double-entry accounting principles
* Hypermedia-driven REST (HATEOAS)

All implemented with **boring, proven Java tech**.

---

## 📜 License

---

> *"Complexity is easy. Correctness is hard."*
> FinanceManager-HTMX chooses correctness.
