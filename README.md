## Overview
A Java-based application implementing a Publisher-Subscriber architecture. The project focuses on handling concurrent processes between publishers and subscribers, managing synchronization, and ensuring system consistency within an operating systems context.

## Technical Challenges
* **Concurrency Management:** The main challenge was managing race conditions in a multi-threaded environment.
* **Synchronization:** I implemented specific synchronization mechanisms to ensure that messages between publishers and subscribers were processed in the correct order without data corruption.

## Key Concepts
* **Publisher-Subscriber Pattern:** Used to decouple components.
* **Thread Safety:** Handled through proper synchronization primitives.

## Technologies Used
* **Language:** Java
* **Concepts:** Multi-threading, Synchronization, Concurrency utilities.
