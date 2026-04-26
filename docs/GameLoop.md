# GameLoop.java

**Owner:** Person C (also owns `Question.java` and `QuestionBank.java` — see `QuestionBank.md`)

**What it is:** The game engine. Implements `Runnable` and runs on its own dedicated thread. Controls when rounds start and end, manages the 20-second timer, and tells the server when to broadcast messages to all players.

**OS concepts:** Multithreading (`implements Runnable`, `Thread.sleep`), inter-thread communication (`volatile`).

---

## How it works

The game loop follows this pattern:

```
Wait for enough players
  ↓
For each question:
  → Set the current question
  → Tell server to broadcast it
  → Sleep 20 seconds
  → Lock out late answers
  → Tell server to broadcast results
  → Sleep 3 seconds (break between rounds)
  ↓
Tell server to broadcast game over
```

This runs entirely on its own thread. The main thread (GameServer) handles network I/O. This thread handles timing. They communicate through the shared `Scoreboard` and through a broadcaster reference.

---

## Fields

- `Scoreboard scoreboard` — the shared instance (passed in via constructor)
- `QuestionBank questionBank` — the list of questions (passed in via constructor)
- `Question currentQuestion` — the question currently being played
- `volatile int currentQuestionId` — set to the question's ID during a round, set to -1 between rounds. Marked `volatile` so handler threads always read the latest value without needing `synchronized`.
- A reference to the server's broadcast method (so the loop can push messages to all clients)
- `int MIN_PLAYERS` — how many players needed to start (use 2 for testing, higher for the real demo)
- `int ROUND_SECONDS` — 20
- `int BREAK_SECONDS` — 3

---

## Methods

### Constructor
- Takes a `Scoreboard` and a `QuestionBank`
- Stores both as instance fields

### `run()`
This is the main game loop. It runs when the thread starts.

1. **Wait for players:** Loop until `scoreboard.playerCount() >= MIN_PLAYERS`. On each loop iteration, broadcast a `waiting` message and sleep for 1 second.
2. **Play rounds:** For each `Question` in the question bank:
    - Set `currentQuestion` and `currentQuestionId`
    - Broadcast a `question` message with the question data
    - Sleep for `ROUND_SECONDS * 1000` milliseconds
    - Set `currentQuestionId = -1` (this rejects any answers that arrive after time runs out)
    - Broadcast a `round_end` message with the correct answer and `scoreboard.snapshot()`
    - Sleep for `BREAK_SECONDS * 1000` milliseconds
3. **End game:** Broadcast a `game_over` message with `scoreboard.winner()`

### `checkAnswer(int questionId, String choice)`
- Called by `GameServer.onMessage()` when a player submits an answer
- Returns `true` only if:
    - `questionId` matches `currentQuestionId` (the round is still active)
    - `choice` matches `currentQuestion.getCorrect()`
- Returns `false` otherwise (wrong answer or late submission)

### `getCurrentQuestionId()`
- Returns `currentQuestionId`
- This is a `volatile` read — always returns the most up-to-date value

### `setBroadcaster(...)`
- Accepts a way to call `GameServer.broadcastToAll()` from within the game loop
- This could be a direct reference to the server, a callback interface, or a lambda
- Called once during setup in `GameServer.main()`

---

## About `volatile`

`volatile` is simpler than `synchronized`. It only works for single variable reads and writes (not read-modify-write like score updates). It's perfect for `currentQuestionId` because:
- One thread writes it (the game loop)
- Many threads read it (the connection handlers checking if an answer is still valid)
- No math is involved — just "is it equal to X?"

If `volatile` feels unfamiliar, you can replace it with a `synchronized` getter and setter. Same result, just slightly more code.

---

## How to test it alone

Write a temporary `main()` method:

1. Create a real `Scoreboard` instance
2. Call `scoreboard.addPlayer()` for 3 fake players
3. Create a `QuestionBank` with 2-3 hardcoded questions
4. Create a `GameLoop`, passing the scoreboard and question bank
5. Replace the broadcaster with a simple `System.out.println` call
6. Run the game loop on a new thread: `new Thread(gameLoop).start()`
7. Watch the console — you should see `question` messages appearing every 23 seconds (20 round + 3 break)

Delete the `main()` before integration in Phase 3.
