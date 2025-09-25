package com.example.impact;

public class Rectangle extends Shape {
    private int width;
    private int height;

    public Rectangle(Integer x, Integer y, int width, int height) {
        super(x, y);
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // optional: calculate area
    public int getArea() {
        return width * height;
    }
}
