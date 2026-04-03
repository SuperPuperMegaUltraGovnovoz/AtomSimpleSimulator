package org.example;

public class Player {
    int x;
    int y;
    int width;
    int height;

    boolean onFoor;
    boolean CollisionWithRight;
    boolean CollisionWithLeft;
    boolean CollisionWithUp;

    public Player(int x, int y, int width, int height, boolean onFoor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onFoor = onFoor;
        this.CollisionWithRight = false;
        this.CollisionWithLeft = false;
        this.CollisionWithUp = false;
    }
}
