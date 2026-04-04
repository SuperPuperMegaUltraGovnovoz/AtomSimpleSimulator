package org.example;

public class Player {
    int x;
    int y;
    int width;
    int height;

    boolean onFloor;
    boolean CollisionWithRight;
    boolean CollisionWithLeft;
    boolean CollisionWithUp;

    public Player(int x, int y, int width, int height, boolean onFloor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onFloor = onFloor;
        this.CollisionWithRight = false;
        this.CollisionWithLeft = false;
        this.CollisionWithUp = false;
    }
}
