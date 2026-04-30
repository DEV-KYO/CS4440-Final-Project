# QuizBlitz

A real-time multiplayer quiz game built in Java. Players join from their phones by scanning a QR code, answer timed questions, and compete for the top score.

**OS concepts in this project:** Sockets/Processes, Threads, Process Synchronization

---

## How it works

1. One person runs the server on their laptop (`GameServer.java`)
2. The server prints a URL — open it in a browser to get the **host display page**
3. The host display shows a QR code — players scan it on their phones to join
4. Players type their name and tap Join
5. Once everyone is in, the host presses **Start Game**
6. Each round: a question appears on everyone's screen, 20 seconds to answer
7. The round ends early if all players answer before time runs out
8. After all questions, the winner is shown

**WiFi note:** All devices need to be on the same network. University WiFi usually blocks devices from talking to each other, so use a phone hotspot and connect the laptop and all player phones to it.

---

## File structure

```
CS4440-Final-Project/
├── README.md                        ← you are here
├── SETUP.md                         ← how to get it running
├── architecture.svg
├── pom.xml                          ← Maven config (handles dependencies automatically)
├── .gitignore
├── start-windows.bat                ← double-click to run on Windows (demo day)
├── start-mac.command                ← double-click to run on Mac (demo day)
│
├── docs/                            ← one doc per file, read the one for your role
│   ├── GameServer.md
│   ├── GameLoop.md
│   ├── Scoreboard.md
│   ├── QuestionBank.md
│   ├── index.md
│   └── display.md
│
└── src/
    └── main/
        ├── java/
        │   └── quizblitz/           ← all .java files go here
        │       ├── GameServer.java
        │       ├── GameLoop.java
        │       ├── Scoreboard.java
        │       ├── Question.java
        │       └── QuestionBank.java
        └── resources/
            └── web/
                ├── index.html       ← player page (phones)
                └── display.html     ← host display page (shows QR code + game state)
```

> `quizblitz.jar` is not committed to the repo (see `.gitignore`). Run `mvn package` to generate it — see SETUP.md step 7.

---

## Who owns what

| Person | File(s) | What it does |
|---|---|---|
| A | `GameServer.java` | Runs the server, handles player connections, routes messages |
| B | `index.html` | What players see on their phones — styling is the main job now |
| C | `Scoreboard.java` | Tracks scores, handles thread safety |
| D | `GameLoop.java` | Controls game flow, round timing, question broadcasting |
| E | `Question.java`, `QuestionBank.java` | The quiz questions and how they're stored |

Read the doc in `docs/` for your file — it explains what's already built and what you need to do.

---

## Message protocol

All messages between the server and clients are JSON. Every message has a `type` field and usually a `data` field.

### Player → Server

| Type | Data | When |
|---|---|---|
| `join` | `{"name":"Jonas"}` | Player taps Join |
| `answer` | `{"questionId":1,"choice":"B"}` | Player taps an answer |
| `start_game` | *(none)* | Host presses Start Game on the display page |

### Server → Player

| Type | Data | When |
|---|---|---|
| `server_info` | `{"gameUrl":"http://..."}` | When anyone first connects |
| `player_list` | `{"players":[...],"count":2}` | When someone joins or leaves |
| `welcome` | `{"name":"Jonas"}` | After a successful join |
| `waiting` | `{"count":2,"needed":2}` | While waiting for the host to start |
| `game_starting` | `{"playerCount":3}` | When the host presses Start |
| `question` | `{"id":1,"text":"...","choiceA":"...","choiceB":"...","choiceC":"...","choiceD":"..."}` | Start of each round |
| `answer_result` | `{"correct":true,"score":10}` | Right after a player answers |
| `round_end` | `{"correct":"B","scores":{"Jonas":10,"Alex":0}}` | End of each round |
| `game_over` | `{"winner":"Jonas","finalScores":{...}}` | After all rounds |

**Important:** The JSON field names have to match exactly. If a key is spelled differently, the other person's code won't read it correctly.

---

## How the pieces connect

```
GameServer.onMessage()
  ├── "join"       → scoreboard.addPlayer(name)
  ├── "answer"     → gameLoop.checkAnswer()
  │                    └── if correct → scoreboard.addPoints()
  │                    └── gameLoop.recordAnswer()  (so round can end early)
  └── "start_game" → gameLoop.startGame()

GameLoop.run()
  ├── waits for gameStarted == true
  ├── for each question:
  │     ├── broadcasts question via server.broadcastToAll()
  │     ├── polls every 200ms until all answered OR 20s up
  │     ├── broadcasts round_end via scoreboard.snapshot()
  │     └── sleeps 3 seconds
  └── broadcasts game_over via scoreboard.winner()
```

---

## Key concepts cheat sheet

**Threads** — A thread is a unit of execution running inside a program. This project uses two threads: one for the game loop (timing, rounds) and one per player connection (reading messages). They run at the same time, which is what makes synchronization necessary.

**`Runnable`** — The Java way to define what a thread should do. You put your logic in `run()`, then hand it to `new Thread(yourRunnable).start()`. That's how `GameLoop` gets its own thread.

**Sockets** — A socket is one end of a network connection. The server opens a socket on port 8080 and waits for clients to connect — the same concept from the class lectures, just using a library instead of raw `ServerSocket`. Each player connection is its own socket.

**Process Synchronization** — When multiple threads share data, you need rules about who can touch it and when. Without those rules you get race conditions. In this project, `synchronized` methods on `Scoreboard` and `GameLoop` enforce mutual exclusion — only one thread can update the score or answer count at a time.

**`synchronized`** — The Java keyword that implements mutual exclusion. If Thread A is inside a `synchronized` method, Thread B has to wait outside until A is done. This is the critical section concept from class.

**`volatile`** — Guarantees that every thread reads the most recent value of a variable. Used here for simple flags like `gameStarted` where only one thread writes and others just read. Not enough for `count++` (read-modify-write needs `synchronized`).

**Race condition** — What happens without synchronization. Two threads both read a value, both modify it, both write it back — and one update gets lost. The classic example from class, visible in `Scoreboard.addPoints()` without `synchronized`.

**Maven** — Handles downloading the libraries this project needs (WebSocket, JSON). You don't install them manually — Maven reads `pom.xml` and does it automatically.
