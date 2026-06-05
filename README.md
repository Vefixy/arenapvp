<p align="center">
  <img src="docs/banner.png" alt="ArenaPVP">
</p>

# Welcome to **ArenaPVP**

> **ArenaPVP** is a Paper plugin built for PvP servers — arenas, kits, stats, ranks and economy in one place.

Works with Paper **26.1.2** (Minecraft 26.1.2). Requires **Java 25** on the server.

---

## Features

### Arenas
- Multiple named arenas via `/arenapvp setarena <name>`
- `/arenapvp warp` opens a GUI with every arena
- `/arenapvp warp <name>` teleports directly
- World names stored as-is — works with **Multiverse-Core**

### Kits
- Local kits saved to SQLite (`/arenapvp kit create <name>`)
- Optional **Essentials** kit sync (`essentials`, `local` or `both` in config)
- GUI picker or direct claim: `/arenapvp kit <name>`

### Stats & Ranks
- Kills, deaths, KDR, kill streaks (best streak tracked)
- Rank tiers: Iron → Gold → Diamond → Netherite (I/II/III)
- Deaths count on PvP kills, `/kill`, suicide and any death without a player killer
- `/arenapvp stats` and `/arenapvp stats <player>`

### Economy
- **Vault** hook — reward on kill, penalty on death
- Min balance floor so players never go below a set amount

### PlaceholderAPI
- `%arenapvp_kills%`, `%arenapvp_deaths%`, `%arenapvp_kdr%`
- `%arenapvp_streak%`, `%arenapvp_best_streak%`, `%arenapvp_rank%`
- `%arenapvp_top_streak_<position>_name%` / `_value%`

### Updates
- Checks [GitHub Releases](https://github.com/Vefixy/arenapvp/releases) on startup
- Ops get a notice on join when a newer version is out

---

<p align="center">
  <img src="docs/fully-customizable.png" alt="Fully Customizable">
</p>

## Customization

Everything below can be toggled or tuned without touching Java.

| File | What you control |
|------|------------------|
| `config.yml` | Feature toggles, economy amounts, rank thresholds, kit mode, GitHub repo for updates |
| `messages.yml` | Prefix, every player-facing string — swap to ES/PT or your own wording |

`config.yml` ships with the project ASCII header as comments at the top. Delete those lines if you want — they are not read by the plugin.

**LuckPerms nodes:** `arenapvp.warp`, `arenapvp.setarena`, `arenapvp.kit`, `arenapvp.kit.create`, `arenapvp.kit.delete`, `arenapvp.stats`, `arenapvp.stats.others`, `arenapvp.reload`, `arenapvp.update`, `arenapvp.help` — or `arenapvp.*` for the full set.

---

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/arenapvp warp [arena]` | `arenapvp.warp` | GUI or direct warp |
| `/arenapvp setarena <name>` | `arenapvp.setarena` | Save arena at your location |
| `/arenapvp kit [name]` | `arenapvp.kit` | GUI or give kit |
| `/arenapvp kit create <name>` | `arenapvp.kit.create` | Save inventory as local kit |
| `/arenapvp kit delete <name>` | `arenapvp.kit.delete` | Remove local kit |
| `/arenapvp stats [player]` | `arenapvp.stats` / `.others` | View stats |
| `/arenapvp reload` | `arenapvp.reload` | Reload config & messages |
| `/arenapvp update` | `arenapvp.update` | Check for new release |
| `/arenapvp help` | `arenapvp.help` | Command list |

Aliases: `/apvp`, `/arena`

---

## Soft dependencies

| Plugin | Used for |
|--------|----------|
| Vault | Kill rewards / death penalties |
| PlaceholderAPI | Stat placeholders |
| Essentials | External kits |
| Multiverse-Core | Multi-world arenas |
| LuckPerms | Permission nodes (optional) |

---

## Build

```bash
./gradlew shadowJar
```

Output: `build/libs/ArenaPVP-<version>.jar`

Run Gradle with Java 21–24; the project toolchain targets Java 25 for Paper 26.1.2.

---

## Install

1. Drop the jar in `plugins/`
2. Start the server once
3. Edit `plugins/ArenaPVP/config.yml` and `messages.yml`
4. `/arenapvp reload`

---

## Links

- Repository: https://github.com/Vefixy/arenapvp
- Issues & releases: same repo
