package com.arenapvp.model;

import java.util.UUID;

public final class PlayerStats {

    private final UUID uuid;
    private String name;
    private int kills;
    private int deaths;
    private int currentStreak;
    private int bestStreak;

    public PlayerStats(UUID uuid, String name, int kills, int deaths, int currentStreak, int bestStreak) {
        this.uuid = uuid;
        this.name = name;
        this.kills = kills;
        this.deaths = deaths;
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
    }

    public UUID uuid() { return uuid; }
    public String name() { return name; }
    public int kills() { return kills; }
    public int deaths() { return deaths; }
    public int currentStreak() { return currentStreak; }
    public int bestStreak() { return bestStreak; }

    public void setName(String name) { this.name = name; }
    public void setKills(int kills) { this.kills = kills; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }
}
