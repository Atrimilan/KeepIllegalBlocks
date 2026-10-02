package io.github.atrimilan.keepillegalblocks.models;

public enum MaterialGroup {
    INTERACTABLE("interactable"),
    REACTIVE("reactive");

    private final String name;

    MaterialGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getBlacklistSectionKey() {
        return name + "-materials.blacklist";
    }

    public String getCategoriesSectionKey() {
        return name + "-materials.categories";
    }
}
