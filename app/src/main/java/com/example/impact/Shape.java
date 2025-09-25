package com.example.impact;

public abstract class Shape {
    private Integer x;
    private Integer y;
    private String color = "blue";
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
