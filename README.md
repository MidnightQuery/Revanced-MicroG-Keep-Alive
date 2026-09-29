# microG HyperOS KeepAlive

A companion helper app for **microG** on **Xiaomi HyperOS** that prevents the system from aggressively killing microG in the background.

---

## The Problem

Xiaomi HyperOS has extremely aggressive background app management. It will automatically **disable autostart** for apps that don't hold an **Accessibility Service** permission — effectively killing microG's ability to stay alive in memory. This breaks:

- Push notifications (FCM/GCM)
- Background audio playback (YouTube Music, etc.)
- Account sync and service binding
- Any app relying on microG staying resident

Without microG running, ReVanced-patched apps (YouTube, YT Music, etc.) lose their core functionality.

---

## The Solution

This companion app registers a lightweight **Accessibility Service** whose only purpose is to keep microG alive. Because HyperOS treats apps with accessibility permissions as "important" and exempt from aggressive autostart restrictions, the helper app can:

1. **Survive HyperOS's autostart killer**
2. **Bind to microG's process** (`app.revanced.android.gms`) to prevent it from being reaped
3. **Maintain the accessibility service connection** so HyperOS keeps both processes resident

In short: *it's a keep-alive wrapper that tricks HyperOS into leaving microG alone.*

---

## How It Works

1. The helper app (`app.revanced.android.gms.keepalive`) declares an Accessibility Service in its manifest.
2. Once enabled in **Settings → Accessibility**, HyperOS grants it elevated process priority.
3. The service binds to microG and holds the connection open.
4. HyperOS's `AccessibilityManagerService` no longer rejects the service, and microG stays in memory.

---

## Requirements

- **HyperOS** (Xiaomi / Redmi / POCO devices)
- **microG** installed as `app.revanced.android.gms` (typically via [Morphe](https://github.com/MorpheApp) or ReVanced Manager)
- USB debugging (for verification, optional)
- The microG **HyperOS KeepAlive** toggle enabled

---

## Installation

1. Install the companion app
2. Open **Settings → Accessibility → Downloaded apps**.
3. Enable **microG HyperOS KeepAlive**.
4. Open the helper app and toggle **KeepAlive** on.
5. (Optional) Confirm microG is set as the default location/account provider in microG settings.

---

## Verification

Use any of the three methods below to confirm the keep-alive is working.

### Method 1: `adb logcat` (Immediate Confirmation)

```bash
adb logcat -c
adb logcat | grep -E "KeepAliveService|AccessibilityManagerService|app.revanced.android.gms"
```

**What to look for:**

- ✅ **No IPC errors** — `AccessibilityManagerService` no longer prints the red
  `Skipping service ... larger than safe parcelable limits` error when opening Accessibility settings.
- ✅ **Service connection confirmed** — When you toggle **microG HyperOS KeepAlive** on, logcat shows the accessibility service bound successfully.

---

### Method 2: Inspect Active Processes via ADB

```bash
adb shell "ps -A | grep app.revanced.android.gms"
```

**Expected output — two distinct processes:**

| Process | Role |
|---|---|
| `app.revanced.android.gms` | microG itself |
| `app.revanced.android.gms.keepalive` | The helper service |

Leave the phone **locked for 15–30 minutes**, then re-run the command. If `app.revanced.android.gms` is still listed, HyperOS is no longer killing microG.

---

### Method 3: microG Cloud Messaging Status

1. Open **microG Settings** (app drawer, or via Morphe/ReVanced Settings).
2. Go to **Google Cloud Messaging (GCM) / Firebase Cloud Messaging (FCM)**.
3. Confirm the status reads **Connected** with a recent ping timestamp.
4. Under *Current apps*, confirm YouTube / YT Music are registered.
5. **Real-world test:** Start background audio, lock the screen, and wait. If playback continues smoothly and notifications arrive without delay, the keep-alive service is doing its job.

---

## FAQ

**Q: Why does this need an Accessibility Service?**
Because HyperOS disable autostart requests after a while from apps without one. The accessibility permission is what gives the helper app enough priority to keep microG resident. It does **not** read screen content or perform any accessibility actions.

**Q: Is this safe?**
Yes. The accessibility service does nothing except maintain a process binding. No screen content is read, logged, or transmitted.

**Q: Will this drain battery?**
No measurable impact. The service is idle — it just holds a binding.

**Q: Does this work on MIUI 14 or older?**
It's designed for **HyperOS**. MIUI 14 users typically don't need it, but it won't hurt.

**Q: Do I still need to disable battery optimization for microG?**
Yes, as a belt-and-braces measure. Combine both for best results.

---

## Credits

- **microG Project** — [github.com/microg](https://github.com/microg)
- **ReVanced** — [revanced.app](https://revanced.app)
- **Morphe** — [github.com/MorpheApp](https://github.com/MorpheApp)
