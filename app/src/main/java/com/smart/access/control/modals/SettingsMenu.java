package com.smart.access.control.modals;

public class SettingsMenu {

    private int icon;
    private String title;
    private String description;

    public SettingsMenu(int icon, String title, String description) {
        this.icon = icon;
        this.title = title;
        this.description = description;
    }


    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
