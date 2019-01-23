package com.example.cubebyes;

public class Player implements Comparable<Player> {
    public final CharSequence name;
    public final int affinity;

    public Player(CharSequence name, int affinity) {
        this.name = name;
        this.affinity = affinity;
    }

    @Override
    public int compareTo(Player o) {
        if (affinity < o.affinity) return 1;
        if (affinity > o.affinity) return -1;
        return 0;
    }
}
