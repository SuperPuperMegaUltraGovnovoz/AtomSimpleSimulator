package org.example;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;

public class Main {

    public static void main(String[] args) {

        float screenWidth1 = 1280;
        float screenHeight1 = 720;
        SetConfigFlags(FLAG_WINDOW_RESIZABLE);
        InitWindow((int)screenWidth1, (int)screenHeight1, "Demo");
        SetTargetFPS(-1);

        Rectangle world = new Rectangle().x(300).y(300).width(40).height(40);
        Object object = new Object((int)world.x(), (int)world.y(), 90, 20);

        Camera2D camera = new Camera2D();
        camera.target(new Vector2().x(world.x() + 20).y(world.y() - 60));
        camera.offset(new Vector2().x(screenWidth1/2).y(screenHeight1/2));
        camera.rotation(0.0f);
        camera.zoom(1.0f);
        float velosityX = 0.2f;
        float velosityX1 = 0.2f;
        float velosityY = 0.2f;
        float velosityY1 = 0.2f;

        Player player = new Player((int)camera.target().x(), (int)camera.target().y(), 30, 40, false);

        while (!WindowShouldClose()) {
            if(player.onFoor){
                velosityY1 = 0;
            }else{velosityY1 = 0.2f;}
            if(player.CollisionWithUp){
                velosityY = 0;
            }else{velosityY = 0.2f;}
            if(player.CollisionWithLeft){
                velosityX = 0;
            }else{velosityX = 0.2f;}
            if(player.CollisionWithRight){
                velosityX1 = 0;
            }else{velosityX1 = 0.2f;}

            if(player.onFoor) {
                if (IsKeyDown(KEY_W)) {
                    camera.target().y(camera.target().y() + (-velosityY) * GetFrameTime() * 1000);
                }
            }

            if (IsKeyDown(KEY_S)) {
                camera.target().y(camera.target().y() + velosityY1 * GetFrameTime() * 1000);
            }

            if(!player.onFoor){
                camera.target().y(camera.target().y() + 0.1f * GetFrameTime() * 1000);
            }

            if (IsKeyDown(KEY_A)) {
                camera.target().x(camera.target().x() + (-velosityX) * GetFrameTime() * 1000);
            }


            if (IsKeyDown(KEY_D)) {
                camera.target().x(camera.target().x() + velosityX1 * GetFrameTime() * 1000);
            }

            player.x = (int)camera.target().x();
            player.y = (int)camera.target().y();
            camera.zoom((camera.zoom()) + (GetMouseWheelMove() * 0.03f));
            camera.zoom(Math.max(camera.zoom(), 0.02f));

            BeginDrawing();
            ClearBackground(RAYWHITE);
            BeginMode2D(camera);

            DrawRectangle(object.x, object.y, object.width, object.height, VIOLET);

            DrawEllipse(player.x, player.y, player.width, player.height, RED);

            EndMode2D();

            DrawFPS(20, 20);
            DrawText("zoom: " + camera.zoom(), 20, 40, 20, RED);
            EndDrawing();

            //столкновение
            Collision.collision(player, object);
        }
        CloseWindow();
    }
}