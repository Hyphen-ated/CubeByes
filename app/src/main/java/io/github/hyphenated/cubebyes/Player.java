package io.github.hyphenated.cubebyes;

public class Player implements Comparable<Player> {
    public final String name;
    public final int affinity;

    public Player(String name, int affinity) {
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
