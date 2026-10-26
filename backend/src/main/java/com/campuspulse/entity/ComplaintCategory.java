package com.campuspulse.entity;

public enum ComplaintCategory {
    ELECTRICAL("Electrical"),
    PLUMBING("Plumbing"),
    CLEANLINESS("Cleanliness"),
    INFRASTRUCTURE("Infrastructure"),
    INTERNET("Internet"),
    SAFETY("Safety"),
    LABORATORY_EQUIPMENT("Laboratory Equipment"),
    CLASSROOM_EQUIPMENT("Classroom Equipment"),
    OTHER("Other");

    private final String displayName;

    ComplaintCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ComplaintCategory fromString(String text) {
        if (text == null) return OTHER;
        for (ComplaintCategory b : ComplaintCategory.values()) {
            if (b.name().equalsIgnoreCase(text) || b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return OTHER;
    }
}
