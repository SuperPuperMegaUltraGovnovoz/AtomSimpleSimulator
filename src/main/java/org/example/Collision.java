package org.example;

public class Collision {
    static void disCollision(Player player){
        player.CollisionWithUp = false;
        player.CollisionWithRight = false;
        player.CollisionWithLeft = false;
        player.onFloor = false;
    }

    static void collision(Player player, Object object){
        if(!player.onFloor) {
            if (object.y >= player.y + player.height && object.y - 1 <= player.y + player.height) {
                if (object.x < player.x + player.width && object.x + object.width > player.x - player.width) {
                    player.onFloor = true;
                }
            }
        }

        if(!player.CollisionWithUp) {
            //if (object.y >= player.y && object.y + 1 <= player.y) {
            if (object.y + object.height <= player.y - player.height && object.y + object.height + 1 >= player.y - player.height) {
                if (object.x < player.x + player.width && object.x + object.width > player.x - player.width) {
                    player.CollisionWithUp = true;
                }
            }
        }

        if(!player.CollisionWithRight) {
            if (object.y <= player.y + player.height && object.y + object.height >= player.y - player.height) {
                if (object.x <= player.x + player.width && object.x + 1 >= player.x + player.width) {
                    player.CollisionWithRight = true;
                }
            }
        }

        if(!player.CollisionWithLeft) {
            if (object.y <= player.y + player.height && object.y + object.height >= player.y - player.height) {
                if (object.x + object.width == player.x - player.width) {
                    player.CollisionWithLeft = true;
                }
            }
        }
    }
}
