# PunishmentPlugin v1.2.0

A complete, precise punishment plugin for Spigot 1.8.8 built with Java 17.

## v1.2.0 Changelog

### Whomuted is now optional (like reason)
All punishment commands now have BOTH `whomuted` and `reason` as optional parameters. Use `by:Name` to specify who punished. If omitted, the executor's name is used.

Examples:
- `/ban Steve` - neither whomuted nor reason (executor = whomuted)
- `/ban Steve cheating` - reason only, whomuted = executor
- `/ban Steve by:kxbir` - whomuted only, no reason
- `/ban Steve by:kxbir cheating` - both
- `/ban Steve cheating by:kxbir` - both (order flexible)

### 4 config variants per message
Every customizable message now has 4 variants based on whether whomuted and/or reason were provided:
- `both` - both whomuted and reason provided
- `whomuted-only` - whomuted provided, no reason
- `reason-only` - reason provided, whomuted = executor (not shown)
- `neither` - neither provided

### Log format changed (security fix)
Old format: `[timestamp] [PUNISHMENT] /ban test console`
New format: `[timestamp] [PUNISHMENT] <executor> used : /ban test console`

The actual player who ran the command is now always logged, regardless of what they put in the `by:` field. This prevents admins from hiding their identity.

### No comments in source files
All `#` comments removed from config.yml and plugin.yml. All `//` and `/* */` comments removed from Java source files.

## Build

```bash
mvn clean package
# output: target/PunishmentPlugin-1.2.0.jar
```

## Install

1. Drop the JAR into your Spigot 1.8.8 server's `plugins/` folder.
2. Delete any old `config.yml` (the structure has changed significantly).
3. Restart the server. New config will be generated.
4. Customize `config.yml` as needed.

## Command Syntax

All commands follow this pattern:
```
/<command> <player> [by:whomuted] [reason]
/<command> <player> <time> [by:whomuted] [reason]    (for temp commands)
```

The `by:Name` flag can appear anywhere after the player name (or after time for temp commands). Everything that doesn't start with `by:` is treated as the reason.

## Commands

| Command | Usage | Permission |
|---------|-------|------------|
| /ban | `/ban <player> [by:whomuted] [reason]` | punishment.ban |
| /tban | `/tban <player> <time> [by:whomuted] [reason]` | punishment.tban |
| /ipban | `/ipban <player> [by:whomuted] [reason]` | punishment.ipban |
| /iptban | `/iptban <player> <time> [by:whomuted] [reason]` | punishment.iptban |
| /sban | `/sban <player> [by:whomuted] [reason]` | punishment.sban |
| /stban | `/stban <player> <time> [by:whomuted] [reason]` | punishment.stban |
| /ipsban | `/ipsban <player> [by:whomuted] [reason]` | punishment.ipsban |
| /ipstban | `/ipstban <player> <time> [by:whomuted] [reason]` | punishment.ipstban |
| /unban | `/unban <player> [by:unbanned_by] [reason]` | punishment.unban |
| /sunban | `/sunban <player> [by:unbanned_by]` | punishment.sunban |
| /kick | `/kick <player> [by:kicked_by] [reason]` | punishment.kick |
| /mute | `/mute <player> [by:whomuted] [reason]` | punishment.mute |
| /tmute | `/tmute <player> <time> [by:whomuted] [reason]` | punishment.tmute |
| /ipmute | `/ipmute <player> [by:whomuted] [reason]` | punishment.ipmute |
| /iptmute | `/iptmute <player> <time> [by:whomuted] [reason]` | punishment.iptmute |
| /smute | `/smute <player> [by:whomuted] [reason]` | punishment.smute |
| /stmute | `/stmute <player> <time> [by:whomuted] [reason]` | punishment.stmute |
| /unmute | `/unmute <player> [by:unmuted_by] [reason]` | punishment.unmute |
| /sunmute | `/sunmute <player> [by:unmuted_by]` | punishment.sunmute |
| /punishment | `/punishment` | punishment.help |
| /checkban | `/checkban <player>` | punishment.checkban |
| /checkmute | `/checkmute <player>` | punishment.checkmute |
| /punishhistory | `/punishhistory [player]` | punishment.punishhistory(.others) |

## Placeholders

- `{player}` - punished player's name
- `{banned_by}` / `{whomuted}` / `{kicked_by}` / `{unbanned_by}` / `{unmuted_by}` - who punished
- `{reason}` - punishment reason
- `{time}` - original duration string
- `{time_left}` - remaining time (no seconds)
- `{issued_at}` - timestamp (checkban/checkmute)
- `{type}` - punishment type key (checkban/checkmute)
- `{silent}` - yes/no (checkban/checkmute)
- `{spammer}` - anti-spam only
- `{index}` / `{line}` / `{count}` - punishhistory

## Time Format

Combinations of: `y` (year), `d` (day), `h` (hour), `m` (minute).
Examples: `90d`, `2h`, `1y6h`, `30m`.

## Author

kxbir - built for Spigot 1.8.8 with Java 17.
