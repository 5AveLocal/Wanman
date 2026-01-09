## Wanman

One-man Fare System with reference to Japanese railways<br/>
This plugin allows passengers to hop on a train without passing through a fare gate,<br/>
and pay automatically when they leave the train.<br/>
*Unlike in real life, this plugin does not require passengers to walk to the front of the whole train.*

## 🔗 Requirement

Must be installed in Minecraft 1.12 or above.</br>
[BKCommonLib](https://www.spigotmc.org/resources/bkcommonlib.39590/history)
and [TrainCarts](https://www.spigotmc.org/resources/traincarts.39592/history) is required, latest version is
recommended.

## ⚙️ Commands

`/wanmandist` for speedometer, distance measurer, and time measurer.</br>
`/wanmanfaretable` or `/wanmanft` for a distance-to-fare table.

## 🪧 Signs

### adddist

```
[+train]
adddist
<dist> [trdisc]
```

where

- `<dist>` is the distance (in m) to the next station
- `[trdisc]` is an optional argument to apply a transfer discount tag

### wmtrans

```
[+train]
wmtrans
<trdisc>
```

where

- `<trdisc>` is a transfer discount tag

## ⚠️ Warnings

Restart is required if Wanman signs cannot be detected. (The error should contain `zip file closed` in the console)</br>
Any misuse of the plugin may cause unexpected behaviour.
