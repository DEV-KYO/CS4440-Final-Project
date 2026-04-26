# Setup checklist

This project uses **Maven** to manage dependencies. You don't need to download any JARs manually — Maven handles that automatically when you import the project.

Follow these steps in order. If you get stuck, ask in the group chat before moving on.

---

## 1. Install Java JDK 17+

Check if you already have it:

```
java -version
javac -version
```

Both should report 17 or higher. If not installed:

- **Windows / Mac:** Download from [Adoptium](https://adoptium.net/) (recommended) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
- **Linux / WSL:** `sudo apt update && sudo apt install openjdk-17-jdk`

---

## 2. Install Eclipse

Download from [eclipse.org](https://www.eclipse.org/downloads/). Use **"Eclipse IDE for Java Developers"** — Maven support (m2e) is built in, so you don't need any extra plugins.

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
gh repo clone DEV-KYO/CS4440-Final-Project
```

Or with plain git:

```
git clone https://github.com/DEV-KYO/CS4440-Final-Project.git
```

---

## 5. Import into Eclipse as a Maven project

1. Open Eclipse
2. **File → Import → Maven → Existing Maven Projects** (NOT "Existing Projects into Workspace" — that's the old way)
3. Browse to the cloned `CS4440-Final-Project` folder
4. Eclipse should detect `pom.xml` automatically — make sure the checkbox next to it is checked
5. Click **Finish**

Eclipse will spend a minute downloading the WebSocket library and the JSON library on the first import. You'll see progress in the bottom-right corner. Wait for it to finish before moving on.

---

## 6. Verify it builds

In Eclipse's Package Explorer, you should see:

- `src/main/java` — where your `.java` files live (in the `quizblitz` package)
- `src/main/resources` — for `index.html` and any other static files
- **Maven Dependencies** — a virtual folder showing the JARs Maven downloaded for you

If you see red error markers on the project, right-click the project → **Maven → Update Project... → OK**. This re-syncs Eclipse with `pom.xml`.

---

## 7. Run the server

Right-click `GameServer.java` → **Run As → Java Application**. You should see:

```
Server started on port 8080
```

Or from the command line:

```
mvn exec:java
```

Open `src/main/resources/web/index.html` in a browser. You should be able to connect.

---

## 8. If port 8080 is busy

**Mac/Linux/WSL:**
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

## 9. Find your local IP (for multiplayer testing)

Other players' phones will connect to `ws://<your-ip>:8080`, so you need to know your IP on the local network.

**Mac:**
```
ipconfig getifaddr en0
```

**Linux / WSL:**
```
hostname -I | awk '{print $1}'
```

**Windows:**
```
ipconfig
```

Look for the IPv4 address under your Wi-Fi adapter (usually starts with `192.168`). All player devices must be on the same Wi-Fi network as the server.

---

## Troubleshooting

**"Cannot resolve symbol org.java_websocket"**
Maven hasn't finished downloading dependencies yet. Right-click project → Maven → Update Project. Wait for the progress bar in the bottom-right.

**"Project is missing required source folder: src/main/java"**
The folder structure isn't right. The repo should already have it set up correctly — try re-cloning.

**Eclipse doesn't show a "Maven" option under Import**
You probably installed "Eclipse IDE for Enterprise Java" or a different package. Reinstall using "Eclipse IDE for Java Developers".

**Port 8080 already in use after running once**
Eclipse sometimes leaves processes running. Use the commands in step 8, or restart Eclipse.
