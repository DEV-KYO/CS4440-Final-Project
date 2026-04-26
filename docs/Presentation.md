# Presentation + Integration

**Owner:** Person F

**What this role is:** You're the glue. While the other five teammates each own one file, you own "does the whole thing actually work end-to-end, and can we present it well." This role doesn't write much code — it writes a small amount of important code, runs the system, finds bugs, and prepares the demo.

---

## Your three deliverables

### 1. QR code + HTTP server (~50 lines of Java)

Right now, players have to manually open `index.html` from disk and connect to `ws://<ip>:8080`. For the demo, we want one QR code that a phone can scan to open the game.

This needs two small additions to `GameServer.java`:

**a) An embedded HTTP server on port 8081** that serves `index.html` so phones can load it from `http://<server-ip>:8081`. Use Java's built-in `com.sun.net.httpserver.HttpServer` — no extra dependencies needed.

**b) A QR code printed to the console** when the server starts, encoding `http://<server-ip>:8081`. Use the [ZXing library](https://github.com/zxing/zxing) — add it to `pom.xml`:

```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.3</version>
</dependency>
```

Print the QR as ASCII art using block characters (`█` and spaces) so it shows up directly in the terminal. The professor scans, phone opens, game loads. No copy-pasting URLs.

**Coordinate with Person A (server lead)** — your additions go inside `GameServer.java`, so don't merge yours until theirs is in. Easiest pattern: write your code in a separate class (`HttpFileServer.java`, `QrPrinter.java`) and have Person A call them from `main()`. That way you can develop in parallel without merge conflicts.

### 2. Integration testing (Phase 3)

You're the QA. When everyone's code lands, you're the one running the full game and finding what breaks.

A checklist to work through:

- Server starts cleanly (no port conflicts, no missing dependencies)
- QR code displays and opens the right page on phone scan
- Two devices can join and see each other's count update
- A question appears on both screens within 1 second of each other
- The 20-second timer counts down on both screens
- Tapping an answer is rejected after the timer hits zero
- Score updates correctly when a correct answer is submitted
- Score does NOT update on a wrong answer
- Round results show the right correct answer
- Game over screen shows the actual winner
- Disconnecting a player mid-game doesn't crash the server
- Two players answering at the exact same time both get credit (race condition test)

When something breaks, file an issue in the GitHub repo with: which step failed, what you saw, what you expected. Don't try to fix it yourself unless it's a one-line typo — the file's owner fixes their own code.

### 3. Slide deck + demo script

A 5-slide deck (rough outline — adjust as you build it):

1. **Title + team** — what we built, who built it
2. **Architecture diagram** — reuse `architecture.svg` from the repo
3. **The four OS concepts in our code** — Sockets (Person A), Multithreading (Person D), Shared Memory (Person C), Synchronization (Person C). Show one code snippet per concept.
4. **Live demo** — everyone gets out their phones, scans the QR, plays a round
5. **What we'd do differently** — honest reflection. Examples: "we'd use a real database for persistent leaderboards," "we'd add reconnection logic"

The demo script is a one-pager:

- What to say while the QR is loading
- What to do if the WiFi is bad (have a backup hotspot ready)
- Who presents which slide
- Order of code walk-throughs
- A backup question if all 10 questions go too fast

**Tip:** Run a full dry run the day before. The first time you do this in front of an audience is not the time to find out the projector doesn't display QR codes well.

---

## How to test it alone

You can build the QR code + HTTP server independently:

1. Write `HttpFileServer.java` as a standalone main — verify it serves a static HTML file on port 8081
2. Write `QrPrinter.java` with a static method `printQr(String url)` — call it from a temp main with `printQr("http://test.com")` and verify the QR scans correctly with your phone
3. Once both work, hand them off to Person A to wire into `GameServer.main()`

For integration testing and slide work, you obviously need everyone else's code first. Until then, your time is best spent on the QR/HTTP code and a slide outline.

---

## Why this role matters

The other five teammates each own one piece. Their goal is "my piece works." Your goal is "the whole thing works." That's a different mindset — you're thinking about handoffs, edge cases, the demo experience, what the grader actually sees.

In real software teams this is called the integration role, and it's usually held by someone senior. For this project, it's the role that turns "five working files" into "a working product."
