package org.example;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;

public class Main {

    public static void main(String[] args) {

        float screenWidth1 = 1280;
        float screenHeight1 = 720;
        SetConfigFlags(FLAG_WINDOW_RESIZABLE);
        InitWindow((int)screenWidth1, (int)screenHeight1, "Demo");
        int tgFPS = 60;

        Rectangle world = new Rectangle().x(300).y(300).width(40).height(40);
        Object[] object = new Object[2];
        object[0] = new Object((int)world.x(), (int)world.y() - 115, 40 * 3, 20 * 1);
        object[1] = new Object((int)world.x(), (int)world.y(), 40 * 3, 40 * 1);
        //object[2] = new Object((int)world.x(), (int)world.y() - 115, 40 * 3, 20 * 1);

        Camera2D camera = new Camera2D();
        camera.target(new Vector2().x(world.x() + 60).y(world.y() - 60));
        camera.offset(new Vector2().x(screenWidth1/2).y(screenHeight1/2));
        camera.rotation(0.0f);
        camera.zoom(1.0f);
        float velosityX = 0f;
        float velosityX1 = 0f;
        float velosityY = 0f;;
        float velosityY1 = 0f;
        boolean CollisionWithUp = false;

        Player player = new Player((int)camera.target().x(), (int)camera.target().y() - 300, 30 * 1, 40 * 1, false);

        while (!WindowShouldClose()) {
            SetTargetFPS(tgFPS);
            if(IsKeyDown(KEY_ONE)){tgFPS = -1;}
            if(IsKeyDown(KEY_TWO)){tgFPS = 60;}

            //столкновение
            for(int i = 0; i < object.length; i++){
            Collision.collision(player, object[i]);}

            if(!player.onFloor){
                velosityY = velosityY - 0.01f * GetFrameTime() * 50;// velosityY = Math.max(velosityY, -0.4f);
                camera.target().y(camera.target().y() + (-velosityY) * GetFrameTime() * 500);
            }

            if(player.onFloor) {
                if (IsKeyDown(KEY_W)) {
                    if(player.CollisionWithUp) {
                        velosityY = 0f;
                    }else{velosityY = 0.2f;}
                    //velosityY = 0.2f;
                    camera.target().y(camera.target().y() + (-velosityY) * GetFrameTime() * 500);
                }
            }

            if (IsKeyDown(KEY_S)) {
                if(player.onFloor){
                    velosityY1 = 0;
                }else{velosityY1 = 0.2f;}
                camera.target().y(camera.target().y() + velosityY1 * GetFrameTime() * 500);
            }

            if (IsKeyDown(KEY_A)) {
                if(player.CollisionWithLeft){
                    velosityX = 0;
                }else{velosityX = 0.2f;}
                camera.target().x(camera.target().x() + (-velosityX) * GetFrameTime() * 500);
            }


            if (IsKeyDown(KEY_D)) {
                if(player.CollisionWithRight){
                    velosityX1 = 0;
                }else{velosityX1 = 0.2f;}
                camera.target().x(camera.target().x() + velosityX1 * GetFrameTime() * 500);
            }

            player.x = (int)camera.target().x();
            player.y = (int)camera.target().y();
            camera.zoom((camera.zoom()) + (GetMouseWheelMove() * 0.03f));
            camera.zoom(Math.max(camera.zoom(), 0.02f));

            BeginDrawing();
            ClearBackground(RAYWHITE);
            BeginMode2D(camera);

            for (int i = 0; i < object.length; i++){
                DrawRectangle(object[i].x, object[i].y, object[i].width, object[i].height, VIOLET);
            }

            DrawEllipse(player.x, player.y, player.width, player.height, RED);

            EndMode2D();

            DrawFPS(20, 20);
            DrawText("zoom: " + camera.zoom(), 20, 40, 20, RED);
            DrawText("onFloor " + player.onFloor,20, 60, 20, RED);
            DrawText("CollisionWithUp " + player.CollisionWithUp,20, 80, 20, RED);
            DrawText("CollisionWithR " + player.CollisionWithRight,20, 100, 20, RED);
            DrawText("CollisionWithL " + player.CollisionWithLeft,20, 120, 20, RED);
            DrawText("Speed " + ((velosityY + velosityY1 + velosityX1 + velosityX)/4),20, 140, 20, GREEN);
            EndDrawing();

            Collision.disCollision(player);
        }
        CloseWindow();
    }
}