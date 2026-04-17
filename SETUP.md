# Setup checklist

Follow these steps in order. If you get stuck, ask in the group chat before moving on.

---

## 1. Install Java JDK 17+

Check if you already have it:

```
java -version
```

If not installed, download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [Adoptium](https://adoptium.net/).

---

## 2. Install Eclipse

Download from [eclipse.org](https://www.eclipse.org/downloads/). Use "Eclipse IDE for Java Developers".

---

## 3. Install Git

Check if you already have it:

```
git --version
```

If not, download from [git-scm.com](https://git-scm.com/).

---

## 4. Clone the repo

```
git clone https://github.com/<our-org>/QuizBlitz.git
```

Or use GitHub CLI:

```
gh repo clone <our-org>/QuizBlitz
```

---

## 5. Import into Eclipse

1. Open Eclipse
2. File → Import → General → Existing Projects into Workspace
3. Browse to the `QuizBlitz` folder
4. Click Finish

---

## 6. Add the WebSocket library

1. Download `Java-WebSocket-1.5.7.jar` from the `lib/` folder in the repo (or from [GitHub releases](https://github.com/TooTallNate/Java-WebSocket/releases))
2. In Eclipse: right-click the project → Build Path → Add External JARs
3. Select `Java-WebSocket-1.5.7.jar`
4. Click Apply and Close

---

## 7. Verify it works

Run `GameServer.java`. You should see:

```
Server started on port 8080
```

Open `web/index.html` in a browser. You should be able to connect.

If the port is busy, check for leftover processes:

**Mac/Linux:**
```
lsof -i :8080
kill -9 <PID>
```

**Windows:**
```
netstat -ano | findstr :8080
taskkill /pid <PID> /F
```

---

## 8. Find your local IP (for multiplayer testing)

**Mac:**
```
ipconfig getifaddr en0
```

**Windows:**
```
ipconfig
```

Look for the IPv4 address under your Wi-Fi adapter (usually starts with `192.168`). This is what other players connect to.
