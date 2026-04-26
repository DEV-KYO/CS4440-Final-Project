# Scoreboard.java

**Owner:** Person A

**What it is:** The shared data object that holds every player's score. This is the most important file for demonstrating OS concepts — it's where synchronization happens.

**OS concepts:** Shared memory, mutual exclusion (synchronized methods), race condition prevention.

---

## Why synchronization matters here

Multiple threads access this object at the same time. When Player 1 and Player 2 both answer correctly in the same instant, two different threads both try to call `addPoints()`. Without `synchronized`, this sequence can happen:

1. Thread 1 reads score = 100
2. Thread 2 reads score = 100 (hasn't been updated yet)
3. Thread 1 writes score = 200
4. Thread 2 writes score = 200 (should be 300!)

The `synchronized` keyword prevents this by letting only one thread into a method at a time. This is the same pattern as `Counter.java` from class.

---

## Fields

- `HashMap<String, Integer> scores` — maps player names to their point totals

---

## Methods

Every method below must be `synchronized`.

### `addPlayer(String name)`
- Adds a new entry to the HashMap with a score of 0
- Called by `GameServer.onMessage()` when a `join` message arrives

### `addPoints(String name, int points)`
- Reads the current score, adds points, writes it back
- Called by `GameServer.onMessage()` when a correct answer is detected
- This is the method where the race condition would occur without `synchronized`

### `getScore(String name)`
- Returns the current score for one player

### `playerCount()`
- Returns `scores.size()`
- Called by `GameLoop` to check if enough players have joined

### `snapshot()`
- Returns a copy of the entire scores HashMap (or its string/JSON representation)
- Called by `GameLoop` when broadcasting `round_end` results
- Must be synchronized so we don't read scores mid-update

### `winner()`
- Loops through the HashMap and returns the name + score of the highest scorer
- Called by `GameLoop` when broadcasting `game_over`

---

## How to test it alone

Write a temporary `main()` method inside this class:

1. Create a `Scoreboard` instance
2. Call `addPlayer()` for 4 fake players
3. Spin up 4 threads, each calling `addPoints(name, 10)` in a loop 100 times
4. Join all threads
5. Print `snapshot()`
6. The total across all players should be exactly 4000 (4 players × 10 points × 100 loops)

If the total is wrong, synchronization is broken. If it's exactly 4000 every time you run it, you're good.

Delete the `main()` before integration in Phase 3.
