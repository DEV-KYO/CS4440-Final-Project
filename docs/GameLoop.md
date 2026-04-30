# GameLoop.java

**Owner:** Person D

**What it does:** This is the game engine. It runs on its own thread and controls the flow of the game — waiting for the host to start, showing questions one at a time, tracking when everyone has answered, and ending the game when all questions are done.

**OS concepts covered:** Threads — the game loop runs on its own dedicated thread using `implements Runnable`, separate from the network threads. Process Synchronization — `volatile` is used for simple flags that multiple threads read, and `synchronized` is used for the answer counter where a read-modify-write operation requires mutual exclusion to prevent a race condition.

---

## The basic idea

Think of this file as the game show host. It decides when to show the next question, how long players have to answer, and when to reveal the results. It runs on its own thread so the timing logic doesn't interfere with the network logic in GameServer.

---

## How the game flows

```
Wait for host to press Start Game
  ↓
For each question:
  1. Send the question to all players
  2. Wait up to 20 seconds (check every 200ms if everyone answered early)
  3. Close the round so late answers don't count
  4. Send the correct answer and current scores to everyone
  5. Wait 3 seconds before the next question
  ↓
Send the final results and winner
```

---

## Fields (the data it keeps track of)

- `scoreboard` — the shared scoreboard (same object that GameServer uses)
- `questionBank` — the list of all questions
- `currentQuestion` — the question being asked right now (marked `volatile` so all threads see the latest value)
- `currentQuestionId` — the ID of the current question. Set to -1 between rounds to reject late answers. Marked `volatile` for the same reason
- `answersThisRound` — how many players have submitted an answer this round. Protected by `synchronized` because multiple players can answer at the same time
- `gameStarted` — starts as false, flipped to true when the host presses Start. Marked `volatile` so the waiting loop sees the change immediately
- `MIN_PLAYERS = 2` — minimum players needed to start (also used by GameServer)

---

## Methods

### `startGame()`
- Flips `gameStarted` to true
- Called by GameServer when the host presses the Start Game button

### `checkAnswer(questionId, choice)`
- Returns true if the answer is correct AND the round is still active
- Called by GameServer when a player submits an answer
- The `questionId` check prevents late answers from a finished round from counting

### `recordAnswer(questionId)` — synchronized
- Adds 1 to `answersThisRound` if the round is still active
- **Why synchronized?** The `++` operation is actually three steps: read the value, add 1, write it back. If two players answer at the exact same instant on two different threads, both threads could read the same value, both add 1, and both write the same result — meaning one answer gets lost. `synchronized` prevents two threads from doing this at the same time.

### `getAnswersThisRound()` — private, synchronized
- Safely reads `answersThisRound` from the game loop thread
- Has to be `synchronized` for the same reason as `recordAnswer` — reading and writing need to use the same lock

### `run()`
- The main game loop (called automatically when the thread starts)
- See the flow diagram above

---

## `volatile` vs `synchronized` — what's the difference?

Both are tools for process synchronization, the topic from class — they just solve different parts of the problem.

- **`volatile`** is for simple flags where one thread writes and others only read. Good for `gameStarted` and `currentQuestionId` — no math involved, just "has this value changed?"
- **`synchronized`** is for critical sections where a thread needs to read, modify, and write a value as one uninterrupted unit. Required for `answersThisRound` because `++` is three operations under the hood. Without the lock, two threads can interleave and you get the race condition example from class — both threads read the same value, both add 1, one update disappears.
