package com.smart.access.control.modals;

public class GridItem {
    private String title;
    private int img;

    // Constructor
    public GridItem(String title, int img) {
        this.title = title;
        this.img = img;
    }

    // Getter for title
    public String getTitle() {
        return title;
    }

    // Setter for title
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter for img
    public int getImg() {
        return img;
    }

    // Setter for img
    public void setImg(int img) {
        this.img = img;
    }

    @Override
    public String toString() {
        return "GridItem{" +
                "title='" + title + '\'' +
                ", img=" + img +
                '}';
    }
}

