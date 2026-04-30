# GameServer.java

**Owner:** Person A

**What it does:** This is the main file that runs the server. It listens for players connecting over WebSocket (port 8080), reads their messages, and figures out what to do with them. It also runs a small web server (port 8081) so players can load the game page in their browser by scanning a QR code.

**OS concepts covered:** Sockets/Processes — the server opens a socket on port 8080 and accepts one connection per player, the same model covered in class. Threads — the WebSocket library creates a new thread for each connected client, so multiple players can send messages at the same time without blocking each other.

---

## The basic idea

Think of this file as a traffic cop. Players send messages like "I want to join" or "my answer is B", and this file reads those messages and passes them to the right place — either the scoreboard or the game loop.

The WebSocket library takes care of the low-level networking. You just fill in four callback methods that get called automatically when something happens.

---

## Fields (the data it keeps track of)

- `scoreboard` — the shared scoreboard object, created once and used by both this file and GameLoop
- `gameLoop` — the game engine that controls rounds and timing
- `playerNames` — a map of each connected player to their name. Uses `ConcurrentHashMap` because multiple threads can add/remove players at the same time
- `localIp` — the laptop's IP address on the local network, used to build the QR code URL

---

## The four callbacks

These methods are called automatically by the WebSocket library — you don't call them yourself.

### `onOpen` — a new player connected
- Sends them the game URL (so the display page can show the QR code)
- Sends the current player list (so a display page that joins late can catch up)
- Doesn't add them as a player yet — waits for them to send a join message

### `onMessage` — a player sent a message
- Parses the JSON to figure out what type of message it is
- Routes it to `handleJoin`, `handleAnswer`, or `handleStartGame`
- Sends back an error if the message doesn't make sense

### `onClose` — a player disconnected
- Removes them from the player map
- Removes them from the scoreboard so the game loop doesn't keep waiting for their answer
- Broadcasts the updated player list to everyone else

### `onError` — something went wrong
- Prints the error so you can see it in the console

---

## Message handlers

### `handleJoin` — player wants to join
- Makes sure they sent a name
- Adds them to the scoreboard and the player map
- Sends them a welcome message
- Broadcasts the updated player list to everyone

### `handleAnswer` — player submitted an answer
- Checks if their answer is correct by asking the game loop
- Awards 10 points if correct
- Tells the game loop that one more player has answered (so the round can end early)
- Sends the player feedback (right/wrong + their current score)

### `handleStartGame` — host pressed Start Game
- Checks that at least 2 players have joined
- Tells the game loop to start
- Broadcasts a "game is starting" message to everyone

---

## Helper methods

- `buildMessage(type, data)` — builds a standard JSON message so every sender uses the same format
- `buildPlayerListJson()` — builds the player list message from whoever is currently connected
- `sendError(conn, reason)` — sends an error message back to one player
- `broadcastToAll(message)` — sends a message to every connected client. GameLoop calls this to push questions and results
- `startHttpServer()` — starts the web server that serves index.html and display.html
- `send404(exchange)` — sends a "not found" response when someone requests a page that doesn't exist
- `getLocalIp()` — finds the laptop's IP address on the WiFi network

---

## Messages (the JSON protocol)

### Player → Server

| Type | Data | When |
|---|---|---|
| `join` | `{"name":"Alice"}` | Player taps Join |
| `answer` | `{"questionId":1,"choice":"B"}` | Player taps an answer |
| `start_game` | *(none)* | Host presses Start Game |

### Server → Player (sent from this file)

| Type | Data | When |
|---|---|---|
| `server_info` | `{"gameUrl":"http://..."}` | When anyone connects |
| `player_list` | `{"players":[...],"count":2}` | When someone joins or leaves |
| `welcome` | `{"name":"Alice"}` | After a successful join |
| `answer_result` | `{"correct":true,"score":10}` | After each answer |
| `game_starting` | `{"playerCount":3}` | When the game starts |
| `error` | *(message)* | When something goes wrong |

*(The question, round results, and game over messages are sent by GameLoop, not this file)*
