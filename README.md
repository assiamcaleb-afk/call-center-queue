# Call Center Queue System 📞

A full-stack web application demonstrating the real-world application of a **Queue (FIFO)** data structure. This project simulates a call center where customers join a waiting queue and agents answer them in the exact order they called.

## Why a Queue?
A call center is the perfect real-world example of a **First-In, First-Out (FIFO)** system. If we used a Stack (LIFO), the person who just called would be answered first, while the person who has been waiting on hold for 20 minutes would be ignored. That is unfair and violates customer service standards. The `LinkedBlockingQueue` in Java is used to ensure thread safety and O(1) enqueue/dequeue times.

## Tech Stack
* **Backend:** Java 17+, Spring Boot 3
* **Data Structure:** `java.util.concurrent.BlockingQueue` (Thread-safe Queue)
* **Frontend:** HTML, CSS, Vanilla JavaScript (served directly by Spring Boot)

## Features
* Real-time dashboard showing the waiting queue and agent statuses.
* Customers can join the queue with their name, phone, and issue type.
* Agents can "Take Call" (Dequeue the next customer in line) and "End Call".
* Live statistics tracking total callers served and average wait times.

## How to Run Locally
1. Make sure you have Java and Maven installed.
2. Navigate to the `backend` folder in your terminal.
3. Run the application:
   ```bash
   mvn spring-boot:run