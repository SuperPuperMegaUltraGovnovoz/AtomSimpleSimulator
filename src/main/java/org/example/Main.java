package org.example;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;

public class Main {
    public static void main(String[] args) {
        float screenWidth1 = 1240;
        float screenHeight1 = 720;
        SetConfigFlags(FLAG_WINDOW_RESIZABLE);
        InitWindow((int)screenWidth1, (int)screenHeight1, "Demo");

        SetTargetFPS(-1);
        int countObjects = 100;
        Rectangle player = new Rectangle().x(400).y(280).width(40).height(40);
        int cols = 10;

        Atom[] atoms = new Atom[countObjects];
        for (int i = 0; i < countObjects; i++) {
            int row = i / cols;
            int col = i % cols;
            atoms[i] = new Atom((int) player.x() + col * 45,(int) player.y() + row * 45, 20, VIOLET);
            atoms[i].velocity.x((float) (Math.random() * 4 - 2));
            atoms[i].velocity.y((float) (Math.random() * 4 - 2));
        }
        float dt = 0.005f;

        Camera2D camera = new Camera2D();
        camera.target(new Vector2().x(player.x() + 20).y(player.y() + 20));
        camera.offset(new Vector2().x(screenWidth1/2).y(screenHeight1/2));
        camera.rotation(0.0f);
        camera.zoom(1.0f);

        float screenWidth = screenWidth1;
        float screenHeight = screenHeight1;


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

            Atom.SimulateAtoms(atoms, dt, screenWidth, screenHeight, countObjects);

            BeginDrawing();
            ClearBackground(RAYWHITE);
            BeginMode2D(camera);
                for(Atom atom : atoms){
                    DrawCircle((int)atom.position.x(), (int)atom.position.y(), atom.radius, atom.color);
                    // Вектор скорости (зелёный)
                    DrawLine((int)atom.position.x(), (int)atom.position.y(),
                            (int)(atom.position.x() + atom.velocity.x() * 10),
                            (int)(atom.position.y() + atom.velocity.y() * 10), GREEN);
                    // Вектор силы (красный)
                    DrawLine((int)atom.position.x(), (int)atom.position.y(),
                            (int)(atom.position.x() + atom.force.x() * 0.1),
                            (int)(atom.position.y() + atom.force.y() * 0.1), RED);
                }

            EndMode2D();

            DrawFPS(20, 20);
            DrawText("zoom: " + camera.zoom(), 20, 40, 20, RED);
            DrawText("objects: " + countObjects, 20, 60, 20, RED);

            EndDrawing();
            float totalForce = 0.0f;
            float totalVelocity = 0.0f;
            for (Atom atom : atoms) {
                totalForce += Vector2Length(atom.force);
                totalVelocity += Vector2Length(atom.velocity);
            }
            System.out.println("Total force: " + totalForce + " | Total velocity: " + totalVelocity);

        }
        CloseWindow();
    }
}
