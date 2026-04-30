# Setup

Follow these steps in order. If something isn't working, check the troubleshooting section at the bottom before asking in the group chat.

---

## 1. Make sure Java is installed

Open a terminal and run:

```
java -version
javac -version
```

Both should say version 17 or higher. If not installed:

- **Windows / Mac:** Download from [Adoptium](https://adoptium.net/) (free, recommended)
- **Linux / WSL:** `sudo apt update && sudo apt install openjdk-17-jdk`

---

## 2. Install Eclipse

Download from [eclipse.org/downloads](https://www.eclipse.org/downloads/). Pick **"Eclipse IDE for Java Developers"** — Maven support is already included in that version, so you don't need extra plugins.

---

## 3. Install Git

Check if you have it:

```
git --version
```

If not, download from [git-scm.com](https://git-scm.com/).

---

## 4. Clone the repo

```
git clone https://github.com/DEV-KYO/CS4440-Final-Project.git
```

---

## 5. Import into Eclipse

1. Open Eclipse
2. Go to **File → Import → Maven → Existing Maven Projects**
   - (Not "Existing Projects into Workspace" — that's the old way and won't work with Maven)
3. Browse to the `CS4440-Final-Project` folder you just cloned
4. Eclipse will detect `pom.xml` automatically — make sure the checkbox next to it is checked
5. Click **Finish**

Eclipse will spend about a minute downloading the WebSocket and JSON libraries the first time. You'll see a progress bar in the bottom-right corner. Wait for it to finish before doing anything else.

---

## 6. Verify it looks right

In the Package Explorer on the left you should see:

- `src/main/java` → the `quizblitz` package with the `.java` files
- `src/main/resources/web` → `index.html` and `display.html`
- `Maven Dependencies` → the downloaded libraries

If you see red error markers, right-click the project → **Maven → Update Project → OK**. That usually fixes it.

---

## 7. Run the server

Right-click `GameServer.java` → **Run As → Java Application**

Or from the command line inside the project folder:

```
mvn clean exec:java
```

You should see this printed in the console:

```
─────────────────────────────────────────
  QuizBlitz server running!
  Player page:  http://192.168.x.x:8081/
  Display page: http://192.168.x.x:8081/display.html
─────────────────────────────────────────
```

- Open the **Display page** on the laptop that's running the server — this is the host view with the QR code and Start Game button
- Players scan the QR code (or type the Player page URL) to join from their phones

---

## 8. WiFi setup

University WiFi blocks devices from talking to each other directly, so phones usually can't reach your laptop over it. The fix:

1. Turn on a **phone hotspot** on one team member's phone
2. Connect the **laptop** to that hotspot
3. Connect all **player phones** to that same hotspot
4. Restart the server — it'll print the new IP address for that network

---

## 9. Firewall (if phones can't connect)

**Windows** — run this once in PowerShell as Administrator:

```
New-NetFirewallRule -DisplayName "QuizBlitz" -Direction Inbound -Protocol TCP -LocalPort 8080,8081 -Action Allow
```

**Mac** — the firewall is off by default, so this usually isn't needed. If phones still can't connect after the hotspot setup, check if it's on:

System Settings → Network → Firewall

If it's on, click **Options** and add Java to the allowed apps, or temporarily turn the firewall off for the demo. Alternatively, run this in Terminal:

```
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --add /usr/bin/java
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --unblockapp /usr/bin/java
```

---

## 10. If port 8080 is already in use

This happens if you ran the server before and it didn't shut down cleanly.

**Windows:**
```
netstat -ano | findstr :8080
taskkill /pid <PID> /F
```

**Mac / Linux / WSL:**
```
lsof -i :8080
kill -9 <PID>
```

Replace `<PID>` with the number you see in the output.

---

## Troubleshooting

**"Cannot resolve symbol org.java_websocket"**
Maven hasn't finished downloading yet. Right-click the project → Maven → Update Project. Wait for the progress bar to finish.

**Red error markers on the project**
Right-click project → Maven → Update Project → OK. If that doesn't fix it, try closing and reopening Eclipse.

**Eclipse doesn't show a Maven option under Import**
You installed the wrong Eclipse package. Reinstall using "Eclipse IDE for Java Developers".

**Port 8080 already in use after running**
Eclipse sometimes leaves the process running in the background. Use the commands in step 10, or just restart Eclipse.

**Phones connect to the hotspot but still can't reach the server**
Check that the firewall rule from step 9 was added. Also make sure you're using the IP address shown in the server console — it updates when you switch networks.
