# index.html

**Owner:** Person B

**What it does:** This is what players see on their phone when they scan the QR code. It connects to the server over WebSocket and shows the right screen depending on what's happening in the game.

**Your job:** The game logic is already fully built and working. All you need to do is make it look good — add CSS, style the buttons, pick colors, whatever makes it feel like a real quiz game.

---

## How it connects to the server

The page automatically connects to the server using the same IP address the player used to load the page. So if the player loaded the page from `http://192.168.1.5:8081`, it connects to `ws://192.168.1.5:8080`. No hardcoding needed — it just works.

---

## The five screens

Only one screen is visible at a time. The JS switches between them automatically based on messages from the server.

### 1. Join screen (`screen-join`)
- A text box for the player's name and a Join button
- Already wired up — pressing the button (or hitting Enter) sends the join message

### 2. Waiting screen (`screen-waiting`)
- Shows while the game hasn't started yet
- Updates with the current player count as people join
- Changes to "Game starting with N players..." when the host presses Start

### 3. Question screen (`screen-question`)
- Shows the question and four answer buttons (A, B, C, D)
- Has a countdown timer that ticks down from 20 on its own (client-side)
- Buttons disable themselves after the player taps one, or when time runs out
- Shows "Correct! Your score: X" or "Wrong. Your score: X" after the server responds

### 4. Round end screen (`screen-round-end`)
- Shows what the correct answer was
- Shows the current scores sorted from highest to lowest
- The player's own row is marked with "(you)"
- Automatically moves to the next question when the server sends one

### 5. Game over screen (`screen-game-over`)
- Shows the winner
- Shows the final scores
- Has a Play Again button that reloads the page

---

## What to style

Everything is plain unstyled HTML right now. You're free to add whatever CSS you want. Some ideas:

- Big colorful answer buttons (classic quiz game style)
- A bold, easy-to-read timer
- A clean scoreboard layout
- Mobile-friendly sizing so everything fits on a phone screen without scrolling

**One thing to be careful about:** the JS finds elements by their `id` (like `document.getElementById("choiceA")`). Don't rename any of the existing IDs or the buttons will stop working. You can add classes, change the HTML structure around them, wrap them in new divs — just keep the IDs the same.
