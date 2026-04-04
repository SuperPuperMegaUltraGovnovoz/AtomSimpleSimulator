package org.example;

public class Collision {
    static void collision(Player player, Object object){
        if(object.y == player.y + player.height){
            if(object.x <= player.x + player.width && object.x + object.width >= player.x - player.width){
                player.onFloor = true;
            }else{player.onFloor = false;}
        }else{player.onFloor = false;}

        if(object.y + object.height == player.y - player.height){
            if(object.x <= player.x + player.width && object.x + object.width >= player.x - player.width){
                player.CollisionWithUp = true;
            }else{player.CollisionWithUp = false;}
        }else{player.CollisionWithUp = false;}

        if(object.y <= player.y + player.height && object.y + object.height >= player.y - player.height){
            if(object.x == player.x + player.width){
                player.CollisionWithRight = true;
            }else{player.CollisionWithRight = false;}
        }else{player.CollisionWithRight = false;}

        if(object.y <= player.y + player.height && object.y + object.height >= player.y - player.height){
            if(object.x + object.width == player.x - player.width){
                player.CollisionWithLeft = true;
            }else{player.CollisionWithLeft = false;}
        }else{player.CollisionWithLeft = false;}
    }
}
