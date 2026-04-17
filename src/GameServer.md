# GameServer.java

**Owner:** Person B

**What it is:** The entry point of the application. Extends `WebSocketServer` from the Java-WebSocket library. Starts the server, handles client connections, parses incoming messages, and routes them to `Scoreboard` and `GameLoop`.

**OS concepts:** Sockets, I/O, network communication.

---

## How it works

The Java-WebSocket library gives you a class called `WebSocketServer`. You extend it and fill in four callback methods. The library handles threading for you — each client connection gets its own thread internally.

This replaces what `ServerSocket.accept()` + `ClientHandler implements Runnable` did in the Mailbox assignment. Same idea, the library just manages the threads behind the scenes.

---

## Fields

- `Scoreboard scoreboard` — the single shared instance, created in `main()`
- `GameLoop gameLoop` — the game engine, also created in `main()`
- `Map<WebSocket, String> playerConnections` — maps each WebSocket connection to the player's name

---

## Callbacks (provided by the library, you write the logic)

### `onOpen(WebSocket conn, ClientHandshake handshake)`
- A new client just connected
- Log it: `System.out.println("New connection from " + conn.getRemoteSocketAddress())`
- Don't add them as a player yet — wait for their `join` message

### `onMessage(WebSocket conn, String message)`
- A client sent a message (JSON string)
- Parse the JSON to get `type` and `data`
- If `type` is `"join"`:
    - Extract `name` from `data`
    - Call `scoreboard.addPlayer(name)`
    - Store the mapping: `playerConnections.put(conn, name)`
    - Send back: `{"type":"welcome","data":{"name":"..."}}`
    - Broadcast updated waiting count to all clients
- If `type` is `"answer"`:
    - Extract `questionId` and `choice` from `data`
    - Look up the player name from `playerConnections`
    - Call `gameLoop.checkAnswer(questionId, choice)`
    - If correct: call `scoreboard.addPoints(name, 100)`

### `onClose(WebSocket conn, int code, String reason, boolean remote)`
- A client disconnected
- Remove them from `playerConnections`
- Log it

### `onError(WebSocket conn, Exception ex)`
- Something went wrong
- Print the error: `ex.printStackTrace()`

---

## Methods

### `main(String[] args)`
1. Create a `Scoreboard` instance
2. Create a `QuestionBank` instance
3. Create a `GameLoop` instance, passing it the scoreboard and question bank
4. Start the game loop on its own thread: `new Thread(gameLoop).start()`
5. Create `GameServer` instance on port 8080, passing scoreboard and game loop
6. Call `server.start()`
7. Print the server URL and local IP so players know where to connect

### `broadcastToAll(String message)`
- Sends a JSON string to every connected client
- Called by `GameLoop` to push questions, timer updates, round results, and game over
- Use the library's `broadcast()` method or loop through all connections

### (Optional) `startHttpServer()`
- Start a simple HTTP server on port 8081 using `com.sun.net.httpserver.HttpServer`
- Serves `index.html` so players can access it by navigating to `http://<your-ip>:8081`
- About 15 lines of code — makes the QR code flow work

---

## JSON parsing

You can parse JSON manually (split strings) or use a small library. The simplest approach without extra dependencies:

- `org.json` is a lightweight option (one more jar)
- Or use basic string manipulation since our messages are simple

Pick whichever the team is comfortable with. Just make sure the key names match the protocol in the README exactly.

---

## How to test it alone

1. Use the Phase 1 skeleton `index.html`
2. Run the server
3. Open the HTML in a browser
4. Send raw JSON messages from the browser console: `ws.send('{"type":"join","data":{"name":"Test"}}')`
5. Check your server console — you should see the parsed name printed
6. Use a stub Scoreboard that just prints "addPlayer called" instead of actually storing data

Delete the stubs before integration in Phase 3.
