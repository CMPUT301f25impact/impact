package com.example.impact;

public abstract class Shape {
    private Integer x;
    private Integer y;
    String color = "yellow";

    public Shape(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

}
