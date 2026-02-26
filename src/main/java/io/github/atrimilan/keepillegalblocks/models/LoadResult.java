package io.github.atrimilan.keepillegalblocks.models;

public record LoadResult(String type, long total, int blacklisted) {

    public String chatFormat() {
        return "<green>" + type + " materials loaded: <white>" + total +
               (blacklisted > 0 ? " <gray>(" + blacklisted + " blacklisted)" : "");
    }

    public String consoleFormat() {
        return type + " materials loaded: " + total + (blacklisted > 0 ? " (" + blacklisted + " blacklisted)" : "");
    }
}
