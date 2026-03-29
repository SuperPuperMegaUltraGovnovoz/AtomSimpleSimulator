package org.example;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;

public class Main {
    public static void main(String[] args) {
        float screenWidth = 800;
        float screenHeight = 600;
        SetConfigFlags(FLAG_WINDOW_RESIZABLE);
        InitWindow(800, 600, "Demo");

        SetTargetFPS(60);
        int countObjects = 50;
        Rectangle player = new Rectangle().x(400).y(280).width(40).height(40);
        int cols = 10;

        Atom[] atoms = new Atom[countObjects];
        for (int i = 0; i < countObjects; i++) {
            int row = i / cols;
            int col = i % cols;
            atoms[i] = new Atom((int) player.x() + col * 45,(int) player.y() + row * 45, 20, VIOLET);
        }
        float dt = 0.005f;

        Camera2D camera = new Camera2D();
        camera.target(new Vector2().x(player.x() + 20).y(player.y() + 20));
        camera.offset(new Vector2().x(screenWidth/2).y(screenHeight/2));
        camera.rotation(0.0f);
        camera.zoom(1.0f);


        while (!WindowShouldClose()) {
            if (IsWindowResized()) {
                screenWidth = GetScreenWidth();
                screenHeight = GetScreenHeight();
            }
            if (IsMouseButtonDown(MOUSE_BUTTON_MIDDLE)){
                camera.target(Vector2Add(camera.target(), Vector2Scale(GetMouseDelta(), -1.0f/camera.zoom())));
            }
            camera.zoom((camera.zoom()) + (GetMouseWheelMove() * 0.03f));
            if (camera.zoom() <= 0) {
                camera.zoom(0.009999f);
            }

            Atom.SimulateAtoms(atoms, dt, screenWidth, screenHeight);

            BeginDrawing();
            ClearBackground(RAYWHITE);
            BeginMode2D(camera);
                for(Atom atom : atoms){
                    DrawCircle((int)atom.position.x(), (int)atom.position.y(), atom.radius, atom.color);
                }

            EndMode2D();

            DrawFPS(20, 20);
            DrawText("zoom: " + camera.zoom(), 20, 40, 20, RED);
            DrawText("objects: " + countObjects, 20, 60, 20, RED);

            EndDrawing();
        }
        CloseWindow();
    }
}
