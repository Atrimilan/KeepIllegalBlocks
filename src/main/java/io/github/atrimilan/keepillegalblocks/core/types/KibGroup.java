package io.github.atrimilan.keepillegalblocks.core.types;

public enum KibGroup {
    FRAGILE("fragile"),
    CONNECTABLE("connectable"),
    INTERACTABLE("interactable");

    private final String sectionKey;

    KibGroup(String sectionKey) {
        this.sectionKey = sectionKey;
    }

    public String getSectionKey() {
        return sectionKey + "-materials.";
    }
}
