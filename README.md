# Network_Bulletin_Board
Java application modeling a server and client interacting by placing notes and pins on a bulletin board.

## How to Run

**Prerequisites:**
- To make sure you have can run the application, make sure you have Java installed 
- Open your terminal/command prompt.
- Navigate to the root directory `Network_Bulletin_Board`.

### 1. Server
The server manages the board state. You must start the server first.

**Navigate into the server folder:**
```bash
cd ServerConfig
```

**Compile:**
```bash
javac *.java
```

**Run:**
Usage: `java BBoard <port> <board_width> <board_height> <note_width> <note_height> yellow green blue pink white`

Example:
```bash
java BBoard 4554 6 6 2 2 yellow green blue pink white
```

### 2. Client
The client connects to the server to post and view notes.

**Navigate into the client folder:**
```bash
cd ClientConfig
```

**Compile:**
```bash
javac *.java
```

**Run:**
```bash
java BulletinBoardClient
```
*Note: The client GUI will launch. Enter the IP (localhost) and Port (4554) to connect.*
