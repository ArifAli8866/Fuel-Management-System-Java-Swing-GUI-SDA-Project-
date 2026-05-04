 # ⛽ University Fuel Management System (UFMS)

**FAST National University of Computing & Emerging Sciences**

| | |
|---|---|
| **Course** | Software Design & Analysis (SDA) |
| **Instructor** | Engr. Muhammad Umer Haroon |
| **Group Members** | Saad Ahmed Ijaz (24P-0669), Arif Ali (24P-0736), Arslan Tariq (24P-0610) |

---

## 📌 Project Overview

A Java Swing GUI application for managing university vehicle fuel operations — built directly from the use cases, class diagram, and requirements specified in Assignment 1.

### Core Use Cases Implemented
| UC | Title | Role |
|---|---|---|
| UC-001 | Submit Fuel Request | Driver |
| UC-002 | Approve / Reject Fuel Request | Transport Admin |
| UC-003 | Record Fuel Dispensing | Fuel Attendant |
| UC-004 | Generate Reports | Admin / Finance |

---

## 🚀 How to Compile & Run

### Prerequisites
- **Java 17+** (you said OpenJDK 17 is already installed on Pop!_OS ✅)
- **Antigraviton IDE** or any terminal

### Option 1 – Terminal (Recommended)

```bash
# 1. Navigate to project root
cd FuelManagementSystem

# 2. Compile all Java files
mkdir -p out
find src -name "*.java" | xargs javac -d out

# 3. Run the application
java -cp out com.ufms.Main
```

### Option 2 – Antigraviton IDE

1. Open Antigraviton → **File → Open Project**
2. Select the `FuelManagementSystem` folder
3. Set source root to `src/`
4. Set output directory to `out/`
5. Right-click `src/com/ufms/Main.java` → **Run**

### Option 3 – Create a runnable JAR

```bash
cd FuelManagementSystem
mkdir -p out
find src -name "*.java" | xargs javac -d out

# Create manifest
echo "Main-Class: com.ufms.Main" > manifest.txt

# Package JAR
jar cfm UFMS.jar manifest.txt -C out .

# Run JAR
java -jar UFMS.jar
```

---

## 🔐 Demo Login Credentials

| Role | Username | Password |
|---|---|---|
| Transport Admin | `admin` | `admin123` |
| Driver | `driver1` | `pass123` |
| Driver | `driver2` | `pass123` |
| Fuel Attendant | `attendant1` | `pass123` |
| Finance Dept | `finance1` | `pass123` |
| System Admin | `sysadmin` | `admin123` |

---

## 🗂️ Project Structure

```
FuelManagementSystem/
├── src/
│   └── com/ufms/
│       ├── Main.java                    ← Entry point
│       ├── model/
│       │   ├── User.java                ← User entity (roles)
│       │   ├── Vehicle.java             ← Vehicle entity
│       │   ├── FuelRequest.java         ← Fuel request entity
│       │   ├── FuelTransaction.java     ← Transaction record
│       │   └── FuelStock.java           ← Fuel stock tracker
│       ├── service/
│       │   └── DataStore.java           ← Singleton in-memory DB
│       ├── gui/
│       │   ├── LoginFrame.java          ← Login screen
│       │   ├── MainFrame.java           ← Main window + sidebar nav
│       │   ├── DashboardPanel.java      ← Stats overview
│       │   ├── FuelRequestPanel.java    ← UC-001: Submit request
│       │   ├── MyRequestsPanel.java     ← Driver's request history
│       │   ├── ApprovalPanel.java       ← UC-002: Approve/reject
│       │   ├── DispensingPanel.java     ← UC-003: Record dispensing
│       │   ├── VehiclesPanel.java       ← Vehicle CRUD
│       │   ├── UsersPanel.java          ← User management
│       │   ├── TransactionsPanel.java   ← All transactions view
│       │   ├── ReportsPanel.java        ← UC-004: Generate reports
│       │   ├── FuelStockPanel.java      ← Stock management
│       │   └── AuditLogPanel.java       ← System audit log
│       └── util/
│           └── UITheme.java             ← Centralized UI theme
├── README.md
└── compile_run.sh                       ← One-click script
```

---

## 🔗 Design → Code Mapping

| Design Element | Code |
|---|---|
| `User` class | `model/User.java` with Role enum |
| `Vehicle` class | `model/Vehicle.java` |
| `FuelRequest` class | `model/FuelRequest.java` |
| `FuelTransaction` class | `model/FuelTransaction.java` |
| Role-based access (UC diagram) | `MainFrame.java` sidebar |
| FR-01 Authentication | `LoginFrame.java` + `DataStore.authenticate()` |
| FR-04 Fuel Request | `FuelRequestPanel.java` |
| FR-05 Fuel Approval | `ApprovalPanel.java` |
| FR-06 Fuel Dispensing | `DispensingPanel.java` |
| FR-08 Stock Management | `FuelStockPanel.java` |
| FR-09/10/11 Reports | `ReportsPanel.java` |
| FR-21 Audit Trail | `AuditLogPanel.java` + `DataStore.addAuditLog()` |

---

## 🧩 Design Patterns Used

### 1. Singleton — `DataStore.java`
**Problem solved:** Multiple GUI panels all need access to the same shared data (users, vehicles, requests). Without Singleton, each panel would have its own copy and changes wouldn't be visible across panels.
**Would code work without it?** No — panels would be out of sync.
**Limitation:** No thread-safety (fine for single-user desktop app).

### 2. Strategy (Role-based Navigation) — `MainFrame.java`
**Problem solved:** Different roles (Driver, Admin, Attendant, Finance) need different menus and panels. Strategy pattern lets us swap the navigation strategy at runtime based on the logged-in user's role.
**Would code work without it?** You'd need massive if-else blocks everywhere.
**Limitation:** Adding a new role requires modifying `MainFrame.buildSidebarNav()`.

### 3. Observer (Refresh Pattern) — All panels
**Problem solved:** After approving a request or dispensing fuel, the data changes and other panels need to show updated information. Each panel implements `loadData()` that re-queries the DataStore.
**Would code work without it?** Tables would show stale data after operations.
**Limitation:** Manual refresh buttons needed — not fully reactive/automatic.

---

## 📹 Part 2 – LLM Reflection Notes (for video)

### 2.1 Why Claude?
- Strong Java/Swing code generation
- Fast context understanding of class diagrams and use cases
- Handles complex GUI layout (BoxLayout, GridBagLayout, BorderLayout)
- Generated full multi-class architecture in one session

### 2.2 Good vs Bad

**Good:**
1. Generated complete, compilable class hierarchies matching the design
2. Consistent color theming across all 14+ GUI classes
3. Correctly mapped UC-001 → UC-004 to specific panel classes

**Bad:**
1. No real persistence (in-memory only) — needs database for production
2. Cannot compile/test in restricted environment — manual verification needed
3. Sometimes over-engineers UI components

### 2.3 Recommended Design Patterns
Claude recommended: **Singleton, Strategy, Observer**
- Agree with Singleton for DataStore — essential for shared state
- Agree with Strategy for role-based navigation
- Observer is partially implemented (manual refresh) — ideally needs PropertyChangeListener

### 2.4 Pattern Analysis
See "Design Patterns Used" section above for per-pattern breakdown.

---

## 📋 Functional Requirements Coverage

| FR | Requirement | Status |
|---|---|---|
| FR-01 | User Authentication | ✅ LoginFrame |
| FR-02 | Vehicle Registration | ✅ VehiclesPanel |
| FR-03 | Driver Registration | ✅ UsersPanel |
| FR-04 | Fuel Request | ✅ FuelRequestPanel |
| FR-05 | Fuel Approval | ✅ ApprovalPanel |
| FR-06 | Fuel Dispensing | ✅ DispensingPanel |
| FR-07 | Odometer Entry | ✅ DispensingPanel |
| FR-08 | Fuel Stock Management | ✅ FuelStockPanel |
| FR-09/10/11 | Reports | ✅ ReportsPanel |
| FR-12 | Driver-wise Reports | ✅ ReportsPanel |
| FR-15 | Fuel Efficiency Calculation | ✅ ReportsPanel + ApprovalPanel |
| FR-16 | Cost Tracking | ✅ TransactionsPanel |
| FR-18 | Search/Filter | ✅ ReportsPanel filters |
| FR-21 | Audit Trail | ✅ AuditLogPanel |

---

*Built with Java 17 + Swing | Pop!_OS compatible | No external dependencies*
