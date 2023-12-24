# Redis Server Implementation

This is a simple Redis server implementation in Java, providing an in-memory database system to support key-value stores, lists, sets, and Pub/Sub functionality.

## Features

- **Key-Value Stores:** Support for basic key-value store operations (SET, GET).
- **Lists:** Implementation of list operations (LPUSH, RPUSH, LPOP, RPOP, LLEN, LINDEX, LRANGE, LTRIM).
- **Sets:** Support for set operations (SADD, SREM, SCARD, SMEMBERS, SISMEMBER).
- **Pub/Sub:** Basic Pub/Sub functionality for message broadcasting.

## Components

- **Persistence:** Append-only file (AOF) for persistence of data on disk.
- **Handlers:** Separation of concerns with individual handlers for hash stores, lists, sets, and Pub/Sub.

## Getting Started

1. **Clone the repository:**

   ```bash
   git clone https://github.com/yourusername/redis-server.git
   cd redis-server

2. **Compile the java files**

   ```bash
   javac *.java

3. **Run Server**

   ```bash
   java Server

4. **Connect to server using redis-cli**

   ```bash
   redis-cli -h localhost -p 6379
