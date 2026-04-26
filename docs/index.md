# index.html

**Owner:** Person D

**What it is:** A single HTML file containing all CSS and JavaScript inline. This is what players see on their phone. No frameworks, no build tools — just one file.

**OS concepts:** Asynchronous event-driven architecture (the WebSocket listener is a callback that fires whenever the server pushes a message).

---

## How it works

The page opens a WebSocket connection to the Java server. All communication happens through `ws.send()` (client → server) and `ws.onmessage` (server → client). The page never refreshes — JavaScript shows and hides different screens based on what message type arrives.

---

## Connection setup

```javascript
const ws = new WebSocket("ws://<server-ip>:8080");

ws.onopen = function() {
    // Connected — show the join screen
};

ws.onmessage = function(event) {
    const msg = JSON.parse(event.data);
    // Route based on msg.type
};

ws.onclose = function() {
    // Show a "disconnected" message
};
```

The server IP will need to be configurable — either hardcoded for testing (`localhost`) or entered by the player on a setup screen.

---

## Screens

The page has five states. Only one is visible at a time. Use `display: none` / `display: block` to switch between them.

### 1. Join screen
- A text input for the player's name
- A "Join" button
- On tap: send `{"type":"join","data":{"name":"..."}}`
- Shows while waiting for a `welcome` response

### 2. Waiting screen
- Shows: "Waiting for players... 3 / 6"
- Updates every time a `waiting` message arrives
- Transitions to the question screen when a `question` message arrives

### 3. Question screen
- Displays the question text at the top
- Four large, colored answer buttons (A, B, C, D)
- A countdown timer (starts at 20, decreases each second)
- On button tap: send `{"type":"answer","data":{"questionId":...,"choice":"A"}}`
- After tapping, disable all buttons (prevent double-submit)
- Timer updates come from `timer` messages from the server

### 4. Results screen
- Shows the correct answer (highlight it in green)
- Shows the current scoreboard (list of names + scores, sorted highest first)
- Visible for 3 seconds, then transitions back to the question screen for the next round
- Triggered by a `round_end` message

### 5. Game over screen
- Shows the winner's name prominently
- Shows the final scoreboard
- Triggered by a `game_over` message

---

## Messages this file sends

| When | What to send |
|---|---|
| Player taps "Join" | `{"type":"join","data":{"name":"Jonas"}}` |
| Player taps an answer | `{"type":"answer","data":{"questionId":1,"choice":"B"}}` |

---

## Messages this file receives and how to handle them

| Type | Action |
|---|---|
| `welcome` | Switch from join screen to waiting screen |
| `waiting` | Update the player count display |
| `question` | Switch to question screen, populate question text and choices, start timer at 20 |
| `timer` | Update the countdown display |
| `round_end` | Switch to results screen, highlight correct answer, show scores |
| `game_over` | Switch to game over screen, show winner and final scores |

---

## Design guidelines

- **Mobile-first.** Most players will be on phones. Design for a small vertical screen.
- **Big buttons.** Answer buttons should be at least 48px tall with plenty of padding. Players are tapping quickly.
- **Distinct colors per choice.** Classic Kahoot uses red/blue/yellow/green for A/B/C/D.
- **Readable timer.** Large font, centered, maybe a progress bar that shrinks.
- **No scrolling.** Everything should fit on one screen without scrolling.
- **Keep it simple.** This is a demo, not a production app. Clean and functional beats fancy.

---

## How to test it alone

You don't need the real server to build and test the UI.

1. Open the HTML file directly in a browser
2. In the browser's developer console, simulate incoming messages:

```javascript
// Simulate a welcome message
handleMessage({ type: "welcome", data: { name: "TestPlayer" } });

// Simulate a question
handleMessage({ type: "question", data: {
    id: 1,
    text: "What prevents race conditions?",
    choiceA: "Sockets",
    choiceB: "Synchronization",
    choiceC: "Deadlock",
    choiceD: "Paging"
}});

// Simulate round end
handleMessage({ type: "round_end", data: {
    correct: "B",
    scores: { "TestPlayer": 100, "OtherPlayer": 0 }
}});
```

Structure your code so `handleMessage(msg)` is a standalone function that you call both from `ws.onmessage` and from the console during testing.

Delete the test helpers before integration in Phase 3.
