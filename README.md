# 🚨 Emergency STOMP Messaging Platform

A client-server emergency messaging platform built using the **STOMP (Simple Text Oriented Messaging Protocol)**. This system enables users to subscribe to emergency channels (such as fire, police, or medical), report incidents, and receive real-time updates.

Developed as part of the SPL course at Ben-Gurion University.

---

## 📋 Features

- 🔌 **Java Server**:
  - Supports **Thread-Per-Client (TPC)** and **Reactor** server modes.
  - Efficient real-time message distribution using the STOMP protocol.
  - Handles subscriptions, message broadcasting, and client disconnections.

- 💻 **C++ Client**:
  - Multithreaded client to handle concurrent input/output.
  - Subscribe/unsubscribe from emergency channels.
  - Report emergencies and receive live updates from other users.
  - Generate reports and summaries for emergency events.

- 📡 **STOMP Protocol Support**:
  - Full implementation of standard STOMP frames (CONNECT, SEND, SUBSCRIBE, etc.).
  - Real-time updates and acknowledgments using RECEIPT and MESSAGE frames.

---

## 🚀 Getting Started

### 🔨 **Server Setup (Java)**
1. Compile the server: mvn compile
2. Run the server: 
   - For Thread-Per-Client (TPC) mode:  mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="<port> tpc"
   - For Reactor mode: mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="<port> reactor"

### 💻 **Client Setup (C++)**
1. Build the client: make
2. Run the client: ./bin/StompEMIClient

📄 Commands (Client):
- login {host:port} {username} {password} – Connect to the server.
- join {channel_name} – Subscribe to an emergency channel.
- exit {channel_name} – Unsubscribe from a channel.
- report {file} – Send emergency reports from a JSON file.
- summary {channel_name} {user} {file} – Generate a summary report.
- logout – Disconnect from the server.
