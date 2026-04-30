# Scoreboard.java

**Owner:** Person C

**What it does:** Keeps track of every player's score. It's a shared object — both GameServer and GameLoop use the same instance at the same time, from different threads.

**OS concepts covered:** Process Synchronization — this is the clearest example of it in the whole project. Multiple threads share the same scoreboard object, so without synchronization you get a race condition on score updates. The `synchronized` keyword enforces mutual exclusion: only one thread can be inside a method at a time, which is the critical section solution from class.

---

## Why this file is important for the class

This is the clearest example of a race condition in the whole project. When two players answer correctly at the same instant, two threads both try to add points at the same time.

Without `synchronized`, this can happen:

1. Thread A reads Player 1's score: 100
2. Thread B reads Player 1's score: 100 (before Thread A wrote anything)
3. Thread A adds 10, writes 110
4. Thread B adds 10, writes 110 — **wrong, should be 120**

The `synchronized` keyword makes only one thread run the method at a time, so this can never happen. It's the same concept as the `Counter.java` example from class.

---

## Fields

- `HashMap<String, Integer> scores` — stores player name → their score

---

## Methods

All methods must be `synchronized`.

### `addPlayer(name)`
- Adds the player to the map with a starting score of 0
- Called by GameServer when a player joins

### `removePlayer(name)`
- Removes a player from the scoreboard entirely
- Called by GameServer when a player disconnects so the round doesn't get stuck waiting for someone who left

### `addPoints(name, points)`
- Adds points to a player's score
- Called by GameServer when a player answers correctly (10 points per correct answer)
- This is where the race condition would happen without `synchronized`

### `getScore(name)`
- Returns one player's current score
- Called by GameServer to include in the answer feedback message

### `playerCount()`
- Returns how many players are in the game
- Called by GameLoop to check if everyone has answered (to end the round early)

### `snapshot()`
- Returns a copy of the full scores map
- Called by GameLoop when sending round results and the final game over message
- Needs `synchronized` so the copy doesn't happen while someone else is updating scores

### `winner()`
- Returns the name of the player with the highest score
- Called by GameLoop for the game over message

---

## How to test it by itself

1. Create a `Scoreboard`
2. Add 4 players
3. Spin up 4 threads, each calling `addPoints` 100 times for their player (10 points each call)
4. Wait for all threads to finish
5. Print the scores — every player should have exactly 1000 points, no exceptions

If the numbers are ever wrong, synchronization isn't working.
