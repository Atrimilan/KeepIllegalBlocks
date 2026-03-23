package io.github.atrimilan.keepillegalblocks.core.types;

public enum KibGroup {
    FRAGILE("fragile-blocks."),
    CONNECTABLE("connectable-blocks."),
    INTERACTABLE("interactable-blocks.");

    private final String sectionKey;

    KibGroup(String sectionKey) {
        this.sectionKey = sectionKey;
    }

    public String getSectionKey() {
        return sectionKey;
    }
}
