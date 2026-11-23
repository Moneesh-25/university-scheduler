# University Course Timetabling and Examination Scheduler
### Automated Multi-Resource Academic Scheduling & Resource Optimization Platform

---

## 1. Executive Summary & Overview

The **University Course Timetabling and Examination Scheduler** is an enterprise-grade, full-stack academic scheduling system developed using **Java 21 LTS**, **Spring Boot 3.3.5**, **MySQL 8.0**, and **React 18**. 

Manual timetable construction in higher-education institutions is an **NP-complete combinatorial optimization problem**. Schedulers must satisfy strict hard constraints (avoiding faculty double-bookings, classroom clashes, student cohort overlaps, room capacity deficits, and laboratory requirements) while simultaneously maximizing soft constraints (minimizing student idle gaps, balancing faculty teaching workloads, and avoiding excessive consecutive lectures).

This application automates both **Course Timetabling** and **Examination Scheduling** using two distinct algorithm paradigms:
1. **Constraint Satisfaction Problem (CSP) Backtracking Engine** augmented with **Minimum Remaining Values (MRV / Most Constrained Variable)**, **Degree Heuristic**, and **Least Constraining Value (LCV)** ordering.
2. **Greedy Graph Coloring Engine** based on the **Welsh-Powell (Largest Degree First)** heuristic over an activity conflict graph.
3. **Examination Scheduling Engine** enforcing student exam spacing, classroom capacity bounds, and invigilator load-balancing.
4. **Thread-Safe Concurrent Data Structures**:
   - `BoundedTaskQueue`: A bounded producer-consumer queue backed by `ReentrantLock` and dual `Condition` variables (`notEmpty`, `notFull`) for asynchronous scheduling tasks.
   - `ConcurrentScheduleStack`: A thread-safe LIFO stack protected by `ReentrantReadWriteLock` for multi-level schedule rollback and undo operations.

---

## 2. Technology Stack

| Layer | Technology | Specification / Role |
|---|---|---|
| **Programming Language** | Java 21 LTS | Pattern matching, records, sealed interfaces, virtual threads ready |
| **Backend Framework** | Spring Boot 3.3.5 | RESTful APIs, Spring Data JPA, Hibernate ORM 6.5, HikariCP |
| **Database** | MySQL 8.0+ | Relational schema, foreign keys, unique constraint indexes |
| **Build Tool** | Maven Wrapper 3.10.0 | Reproducible backend compilation and dependency management |
| **Frontend Framework** | React 18.3.1 | Single-Page Application (SPA), hooks, responsive layouts |
| **Frontend Bundler** | Vite 5.4.10 / WebAssembly | Pure WASM Rollup bundler bypassing native OS DLL restrictions |
| **Styling** | Modern CSS3 / Tailwind Utility Classes | University administrative portal dashboard design |
| **Concurrency Utilities** | `java.util.concurrent.locks` | `ReentrantLock`, `Condition`, `ReentrantReadWriteLock` |
| **Testing** | JUnit 5 & Mockito | 15 unit and integration tests covering constraints and algorithms |

---

## 3. System Architecture & High-Level Design

```mermaid
flowchart TD
    subgraph Client ["Frontend Client (Port 5173 / Port 8081)"]
        UI["React 18 SPA UI"]
        Dash["Dashboard & Analytics"]
        Grid["Timetable Grid & Conflict Scanner"]
        Bench["Algorithm Benchmark Dashboard"]
        Conc["Concurrency Live Simulator"]
        Exam["Examination Scheduler"]
    end

    subgraph Backend ["Spring Boot 3.3.5 Application (Port 8081)"]
        API["12 REST API Controllers"]
        subgraph Engine ["Algorithmic Engines"]
            CSP["Backtracking CSP (MRV + Degree + LCV)"]
            WG["Welsh-Powell Graph Coloring"]
            EX["Exam Scheduling Engine"]
            CC["Constraint Checker (Hard + Soft)"]
        end
        subgraph Concurrency ["Concurrent Data Structures"]
            BQ["BoundedTaskQueue (ReentrantLock + Conditions)"]
            CS["ConcurrentScheduleStack (ReadWriteLock)"]
        end
        Service["Service Layer (TimetableService, FacultyService, etc.)"]
        Repo["Spring Data JPA Repositories (10 Interfaces)"]
    end

    subgraph Database ["MySQL 8.0 Database (Port 3306)"]
        DB[("university_scheduler")]
    end

    UI -->|REST / JSON| API
    API --> Service
    Service --> CSP
    Service --> WG
    Service --> EX
    Service --> CC
    Service --> BQ
    Service --> CS
    Service --> Repo
    Repo -->|HikariCP / JDBC| DB
```

---

## 4. Database Schema Design

The system database `university_scheduler` is normalized to **Third Normal Form (3NF)** with explicit foreign key integrity, cascade options, and index optimizations:

```mermaid
erDiagram
    DEPARTMENTS ||--o{ FACULTY : employs
    DEPARTMENTS ||--o{ STUDENT_GROUPS : belongs_to
    DEPARTMENTS ||--o{ COURSES : offers
    FACULTY ||--o{ FACULTY_AVAILABILITIES : defines
    FACULTY ||--o{ TIMETABLE_ENTRIES : conducts
    FACULTY ||--o{ EXAMINATIONS : invigilates
    CLASSROOMS ||--o{ TIMETABLE_ENTRIES : hosts
    CLASSROOMS ||--o{ EXAMINATIONS : accommodates
    STUDENT_GROUPS ||--o{ COURSE_STUDENT_GROUPS : enrolls
    COURSES ||--o{ COURSE_STUDENT_GROUPS : assigned_to
    COURSES ||--o{ TIMETABLE_ENTRIES : scheduled_in
    COURSES ||--o{ EXAMINATIONS : evaluated_in
    TIME_SLOTS ||--o{ TIMETABLE_ENTRIES : allocated_at
    TIME_SLOTS ||--o{ EXAMINATIONS : slotted_at
    TIME_SLOTS ||--o{ FACULTY_AVAILABILITIES : restricts

    DEPARTMENTS {
        bigint id PK
        varchar code UK
        varchar name
    }
    FACULTY {
        bigint id PK
        varchar faculty_code UK
        varchar name
        varchar email UK
        bigint department_id FK
        varchar specialization
        int max_weekly_hours
    }
    CLASSROOMS {
        bigint id PK
        varchar room_number UK
        varchar building
        int capacity
        varchar room_type
        boolean is_available
    }
    STUDENT_GROUPS {
        bigint id PK
        varchar group_code UK
        varchar group_name
        bigint department_id FK
        int semester
        int student_count
    }
    COURSES {
        bigint id PK
        varchar course_code UK
        varchar course_name
        bigint department_id FK
        int semester
        int credits
        bigint assigned_faculty_id FK
        varchar required_room_type
        int weekly_sessions
        int duration_slots
    }
    TIME_SLOTS {
        bigint id PK
        varchar day_of_week
        time start_time
        time end_time
        varchar slot_label
        boolean is_break
        boolean is_lunch
    }
    TIMETABLE_ENTRIES {
        bigint id PK
        bigint course_id FK
        bigint faculty_id FK
        bigint student_group_id FK
        bigint classroom_id FK
        bigint time_slot_id FK
        varchar algorithm_used
        int session_index
        timestamp created_at
    }
    EXAMINATIONS {
        bigint id PK
        bigint course_id FK
        bigint student_group_id FK
        bigint classroom_id FK
        bigint time_slot_id FK
        bigint invigilator_id FK
        varchar exam_period
        date exam_date
        int duration_minutes
    }
```

---

## 5. Algorithmic Scheduling Engines

### A. Welsh-Powell Graph Coloring Algorithm
- **Conflict Graph Formulation**:
  - Each course session is represented as a vertex $v \in V$.
  - An undirected edge $e = (u, v) \in E$ exists between two sessions if they share the same faculty member, the same student group, or if they require the same specialized laboratory space.
  - Colors $C = \{c_1, c_2, \dots, c_k\}$ represent discrete available time slots.
- **Welsh-Powell Step-by-Step Procedure**:
  1. Calculate the degree $\text{deg}(v)$ of each vertex in the conflict graph.
  2. Order vertices in descending order of degree: $\text{deg}(v_1) \ge \text{deg}(v_2) \ge \dots \ge \text{deg}(v_n)$.
  3. Traverse the ordered list and assign the lowest index color $c_i$ that is not assigned to any adjacent vertex.
  4. Repeat until all vertices are colored or no available slots remain.
- **Time Complexity**: $\mathcal{O}(|V|^2 + |E|)$, making it exceptionally fast (~5–10 ms).

### B. Backtracking Constraint Satisfaction Algorithm (CSP)
- **State Representation**:
  - **Variables ($X$)**: Unscheduled course activities requiring $(c, f, g, r, t)$ assignment.
  - **Domain ($D_i$)**: Cartesian product of valid classrooms and available non-break time slots. Static pre-pruning eliminates rooms with insufficient capacity ($\text{capacity}(r) < \text{size}(g)$) and mismatched room types ($\text{type}(r) \ne \text{type}(c)$).
  - **Constraints ($C$)**: Hard and soft constraints.
- **Search Heuristics**:
  1. **Minimum Remaining Values (MRV / Most Constrained Variable)**: Selects the activity $X_i$ with the fewest remaining candidate assignments. Fails fast if a tight bottleneck exists.
  2. **Degree Heuristic**: Used as a tie-breaker when multiple variables share identical MRV domains, selecting the activity involved in the largest number of conflict edges.
  3. **Least Constraining Value (LCV)**: Orders feasible slots by the minimum number of conflicts they introduce for remaining unassigned variables.
- **Time Complexity**: Worst-case $\mathcal{O}(d^n)$, but with static domain pre-filtering and MRV pruning, typical execution terminates in under 60 ms for full semester cohorts.

### C. Hard vs. Soft Constraints

| Category | Constraint Rule | Mathematical Condition / Penalty |
|---|---|---|
| **Hard** | No Faculty Clashes | $\forall e_1, e_2 \in T: (e_1.f = e_2.f \land e_1.t = e_2.t) \implies e_1 = e_2$ |
| **Hard** | No Room Clashes | $\forall e_1, e_2 \in T: (e_1.r = e_2.r \land e_1.t = e_2.t) \implies e_1 = e_2$ |
| **Hard** | No Student Group Clashes | $\forall e_1, e_2 \in T: (e_1.g = e_2.g \land e_1.t = e_2.t) \implies e_1 = e_2$ |
| **Hard** | Classroom Capacity | $\text{capacity}(e.r) \ge \text{size}(e.g)$ |
| **Hard** | Room Type Match | $e.c.\text{requiredType} = \text{LABORATORY} \implies e.r.\text{type} = \text{LABORATORY}$ |
| **Hard** | Working Hours / Breaks | $e.t.\text{isBreak} = \text{FALSE} \land e.t.\text{isLunch} = \text{FALSE}$ |
| **Hard** | Faculty Availability | Faculty must be marked `is_available = true` for slot $e.t$ |
| **Soft** | Minimize Student Idle Gaps | Deducts 2.0 points per disjoint idle gap in student group schedules |
| **Soft** | Faculty Workload Balance | Deducts 1.0 point per day exceeding 4 lecture hours per faculty |
| **Soft** | Consecutive Class Cap | Deducts 3.0 points if a student group has $>3$ consecutive classes without a break |
| **Soft** | Fair Daily Spread | Rewards schedules with uniform course distribution across Monday–Friday |

---

## 6. Concurrent Data Structures

Academic scheduling systems encounter heavy concurrent request loads during enrollment windows and administrative updates. This project implements and demonstrates two core thread-safe data structures:

### A. Bounded Concurrent Queue (`BoundedTaskQueue<T>`)
- **Implementation**: Custom thread-safe bounded FIFO queue using `java.util.concurrent.locks.ReentrantLock` and two `Condition` variables:
  - `notEmpty`: Blocks worker consumer threads when the queue has 0 tasks.
  - `notFull`: Blocks scheduler producer threads when the queue reaches capacity.
- **Application**: Queues asynchronous background scheduling requests, exam generation jobs, and export tasks without memory exhaustion.
- **Time Complexity**: $\mathcal{O}(1)$ push, $\mathcal{O}(1)$ pop, zero deadlocks.

### B. Concurrent Schedule Stack (`ConcurrentScheduleStack<T>`)
- **Implementation**: Custom thread-safe LIFO stack governed by a `java.util.concurrent.locks.ReentrantReadWriteLock`.
  - Multiple threads can concurrently read or inspect recent schedule snapshots (`readLock`).
  - Push and pop (rollback/undo) operations acquire the exclusive `writeLock`.
- **Application**: Maintains transactional rollback history of timetable batches, allowing administrators to click **"Undo Last Schedule"** safely even under concurrent operations.
- **Time Complexity**: $\mathcal{O}(1)$ push, $\mathcal{O}(1)$ pop, $\mathcal{O}(1)$ peek.

---

## 7. Empirical Algorithm Benchmark Results

*Evaluated on an identical baseline dataset consisting of 3 Departments, 8 Faculty members, 4 Student Cohorts (163 students), 12 Courses (32 weekly sessions), 7 Classrooms/Labs, and 45 Time Slots:*

| Metric | Welsh-Powell Graph Coloring | Backtracking CSP (MRV + LCV) | Evaluation Notes |
|---|---|---|---|
| **Execution Time** | **~6 ms** | **~59 ms** | Welsh-Powell is ~10x faster due to greedy heuristic |
| **Sessions Scheduled** | 28 / 32 | **32 / 32 (100%)** | Backtracking resolves constrained lab capacity bottlenecks |
| **Unscheduled Sessions** | 4 | **0** | Graph coloring reached local minima on laboratory slots |
| **Hard Conflicts** | **0** | **0** | Both algorithms guarantee zero double-bookings |
| **Backtracking Operations** | 0 | 12 | Minimal search tree pruning thanks to MRV heuristic |
| **Soft Quality Score (0–100)** | 71.50 | **93.04** | Backtracking produces vastly superior workload spread |

---

## 8. Multi-Threaded Concurrency Test Harness Results

*Live simulation executed via `/api/concurrency/run?producers=4&consumers=2&itemsPerProducer=50&queueCapacity=20`:*

- **Total Tasks Ingested**: 200 tasks (4 producer threads $\times$ 50 tasks)
- **Total Tasks Consumed**: 200 tasks (2 consumer worker threads)
- **Queue Capacity Bound**: 20 items (strictly enforced via `notFull.await()`)
- **Total Execution Duration**: ~238 ms
- **Lossless Verification**: `lossless = true` (0 lost tasks, 0 duplicate tasks)
- **Thread Safety**: 100% verified under high lock contention

---

## 9. Step-by-Step Installation & Execution Guide

### Prerequisites
1. **Java Development Kit (JDK) 21 LTS** installed and configured (`java -version`).
2. **MySQL Server 8.0+** running locally on port **3306** (or port **3307**).
3. **Node.js 18+** & **npm** (for running frontend development server).

---

### Step 1: Database Setup
1. Log in to MySQL CLI or MySQL Workbench:
   ```sql
   CREATE DATABASE university_scheduler;
   ```
2. The database schema and realistic university seed data are provided in:
   - `database/schema.sql`
   - `database/data.sql`
3. Execute the SQL scripts in order:
   ```bash
   mysql -u root -p university_scheduler < database/schema.sql
   mysql -u root -p university_scheduler < database/data.sql
   ```
   *(Note: The Spring Boot backend includes an automatic `DataInitializer` that seeds the database on first run if empty!)*

---

### Step 2: Configure Environment (Optional)
If your MySQL password or port differs from `root:root` on port `3306`, configure them via environment variables:
```powershell
$env:DB_PORT = "3306"
$env:DB_NAME = "university_scheduler"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your_mysql_password"
```

---

### Step 3: Run the Full-Stack Application

#### Option A: Unified Executable JAR (Recommended — Single Command!)
The React frontend is pre-built and embedded into the Spring Boot JAR file. A single command boots both the REST API backend and the React SPA UI:

```powershell
# Navigate to backend directory
cd university-scheduler\backend

# Run the unified Spring Boot JAR
java -jar target\university-scheduler-1.0.0.jar
```

Once started, open your web browser and navigate to:
👉 **`http://localhost:8081`**

---

#### Option B: Separate Frontend & Backend Development Servers
If you wish to edit React UI components with hot-reloading:

**Terminal 1 (Backend):**
```powershell
cd university-scheduler\backend
.\mvnw.cmd spring-boot:run
```

**Terminal 2 (Frontend):**
```powershell
cd university-scheduler\frontend
npm.cmd run dev
```

Open your web browser and navigate to:
👉 **`http://localhost:5173`** *(automatically proxies `/api` calls to port 8081)*

---

## 10. Role-Based Login Credentials

The application provides a role-switcher for administrative demonstrations:

| Role | Username | Password | Permitted Actions |
|---|---|---|---|
| **Administrator** | `admin@university.edu` | `admin123` | Full access: CRUD, Timetable generation, Exam generation, Rollback |
| **Faculty Member** | `ramanathan@university.edu` | `faculty123` | View assigned courses, personal timetable, invigilation duties |
| **Student** | `student@university.edu` | `student123` | View group timetable, examination schedule, room locations |

---

## 11. REST API Endpoint Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard/stats` | Aggregated dashboard KPI counters and recent activity |
| `GET` | `/api/faculty` | List all faculty members |
| `POST` | `/api/faculty` | Create new faculty member (validates email & code uniqueness) |
| `PUT` | `/api/faculty/{id}` | Update faculty details |
| `DELETE` | `/api/faculty/{id}` | Delete faculty member |
| `GET` | `/api/courses` | List all courses with department and assigned faculty |
| `POST` | `/api/courses` | Add new course with weekly session and room type requirements |
| `GET` | `/api/classrooms` | List classrooms and laboratories with capacities |
| `POST` | `/api/classrooms` | Register new classroom/laboratory |
| `GET` | `/api/student-groups` | List student cohorts and enrolled courses |
| `POST` | `/api/student-groups` | Register student group |
| `GET` | `/api/time-slots` | List configured time slots (Monday–Friday) |
| `POST` | `/api/timetable/generate?algorithm=BACKTRACKING` | Trigger Backtracking CSP course timetable generation |
| `POST` | `/api/timetable/generate?algorithm=GRAPH_COLORING` | Trigger Welsh-Powell course timetable generation |
| `POST` | `/api/timetable/undo` | Undo/rollback last generated timetable batch using `ConcurrentScheduleStack` |
| `GET` | `/api/timetable/entries` | Query timetable entries filtered by group, faculty, room, or semester |
| `GET` | `/api/timetable/export/csv` | Download complete generated timetable as CSV file |
| `GET` | `/api/conflicts` | Run real-time hard constraint conflict scanner |
| `POST` | `/api/examinations/generate?examPeriod=MID_TERM` | Automatically generate conflict-free examination schedule |
| `GET` | `/api/examinations` | Query examination entries with invigilators and room allocations |
| `GET` | `/api/benchmark/compare` | Execute fair side-by-side benchmark of Welsh-Powell vs Backtracking |
| `POST` | `/api/concurrency/run` | Execute multi-threaded producer-consumer simulation harness |

---

## 12. Automated JUnit 5 Test Suite

The project includes 15 automated test cases validating constraints, algorithms, and concurrency:

```powershell
cd university-scheduler\backend
.\mvnw.cmd test
```

### Verified Test Classes:
1. **`ConstraintCheckerTest`**:
   - `testFacultyDoubleBookingViolation()`: Asserts hard conflict when a faculty member is double-booked.
   - `testRoomDoubleBookingViolation()`: Asserts conflict on duplicate room allocation.
   - `testStudentGroupDoubleBookingViolation()`: Asserts student cohort overlap detection.
   - `testRoomCapacityViolation()`: Detects classroom capacity insufficient for cohort size.
   - `testLabRoomTypeViolation()`: Enforces laboratory requirement for practical courses.
   - `testLunchBreakViolation()`: Detects and rejects scheduling during designated lunch periods.
   - `testSoftConstraintScoring()`: Validates soft constraint penalty calculations.
2. **`BacktrackingSchedulerTest`**:
   - `testGenerateConflictFreeSchedule()`: Asserts 100% conflict-free schedule generation.
   - `testStaticDomainPruning()`: Asserts invalid rooms/slots are filtered before search.
3. **`GraphColoringSchedulerTest`**:
   - `testWelshPowellLargestDegreeFirstOrder()`: Validates conflict graph vertex ordering.
   - `testGreedyColorAssignment()`: Asserts color-to-timeslot assignment without adjacent clashes.
4. **`ExamSchedulerTest`**:
   - `testGenerateExamSchedule()`: Asserts conflict-free examination date, room, and invigilator assignment.
5. **`BoundedTaskQueueTest`**:
   - `testProducerConsumerBehavior()`: Validates FIFO ordering and bounded capacity blocking.
   - `testMultiThreadedThreadSafety()`: Stresses queue with concurrent producer and consumer threads.
6. **`ConcurrentStackTest`**:
   - `testConcurrentPushAndPop()`: Validates thread-safe rollback stack under `ReentrantReadWriteLock`.

---

## 13. M.Tech IT Mini-Project Viva Voce & Presentation Guide

### Key Technical Questions & Answers for Viva:

**Q1: Why is course timetabling considered NP-Complete?**
> *Answer:* Course timetabling reduces directly to the **Graph Coloring Problem** and **Exact Cover by 3-Sets (XC3)**. Finding a timetable of length $k$ slots without clashes is equivalent to finding a valid $k$-coloring of an arbitrary graph, which Richard Karp proved to be NP-complete in 1972. As the number of courses, rooms, and constraints grows, the search space expands factorially ($\mathcal{O}(d^n)$).

**Q2: What are the distinct roles of MRV and LCV heuristics in your Backtracking algorithm?**
> *Answer:* 
> - **MRV (Minimum Remaining Values)** decides *which variable to assign next* by choosing the course session with the smallest candidate domain of feasible slots. This minimizes the branching factor at the top of the search tree and triggers early backtrack failures.
> - **LCV (Least Constraining Value)** decides *which value (time slot/room) to assign first* to the chosen variable. It picks the value that leaves the maximum number of feasible options open for neighboring variables, maximizing the probability of finding a solution along the first branch.

**Q3: How does your Welsh-Powell implementation differ from Backtracking?**
> *Answer:* Welsh-Powell is a greedy graph coloring algorithm. It sorts vertices by descending degree (number of conflicting courses) and colors them sequentially with the lowest available color. It is $\mathcal{O}(V^2)$ and executes in ~6 ms, but cannot backtrack when an assignment leads to an impossible state later. Backtracking explores the search space systematically with recursion, pruning, and backtracking, guaranteeing a conflict-free solution (100% of 32 sessions) where greedy coloring scheduled 28 sessions.

**Q4: How do you prevent deadlocks and race conditions in `BoundedTaskQueue`?**
> *Answer:* `BoundedTaskQueue` uses a single `ReentrantLock` ensuring mutual exclusion. Synchronization between producers and consumers is decoupled using two distinct `Condition` objects: `notFull` and `notEmpty`. When the queue is full, producer threads invoke `notFull.await()`, releasing the lock until a consumer removes an item and calls `notFull.signal()`. This eliminates busy-waiting, CPU spinning, and race conditions.

**Q5: What is the architectural purpose of `ConcurrentScheduleStack`?**
> *Answer:* In a multi-user university administrative system, multiple department heads or scheduling officers may view timetables while an administrator modifies or rolls back a batch. Using `ReentrantReadWriteLock` allows unlimited concurrent read operations (`readLock`) with zero contention, while exclusive write access (`writeLock`) is acquired only during push and pop (undo) operations.
