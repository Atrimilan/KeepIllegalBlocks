package io.github.atrimilan.keepillegalblocks.core.types;

public enum KibGroup {
    INTERACTABLE("interactable"),
    REACTIVE("reactive");

    private final String groupName;

    KibGroup(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getBlacklistSectionKey() {
        return groupName + "-materials.blacklist";
    }

    public String getCategoriesSectionKey() {
        return groupName + "-materials.categories";
    }
}
