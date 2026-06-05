package io.github.atrimilan.keepillegalblocks.core.types;

public enum KibGroup {
    INTERACTABLE("interactable"),
    REACTIVE("reactive");

    private final String sectionKey;

    KibGroup(String sectionKey) {
        this.sectionKey = sectionKey;
    }

    public String getSectionKey() {
        return sectionKey + "-materials.";
    }
}
