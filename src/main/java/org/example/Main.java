package org.example;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;

public class Main {
    public static void main(String[] args) {
        float screenWidth1 = 1280;
        float screenHeight1 = 720;
        SetConfigFlags(FLAG_WINDOW_RESIZABLE);
        InitWindow((int) screenWidth1, (int) screenHeight1, "LJ + Gravity + Timer");

        SetTargetFPS(-1);
        int countObjects = 300;   // настройте под свою систему
        Rectangle spawnArea = new Rectangle().x(20).y(20).width(40).height(40);
        int cols = 30;

        Atom[] atoms = new Atom[countObjects];
        for (int i = 0; i < countObjects; i++) {
            int row = i / cols;
            int col = i % cols;
            atoms[i] = new Atom(
                    (int) spawnArea.x() + col * 45,
                    (int) spawnArea.y() + row * 45,
                    20, VIOLET
            );
            atoms[i].velocity.x((float) (Math.random() * 1.0 - 0.5));
            atoms[i].velocity.y((float) (Math.random() * 1.0 - 0.5));
        }

        Camera2D camera = new Camera2D();
        camera.target(new Vector2().x(spawnArea.x() + 200).y(spawnArea.y() + 200));
        camera.offset(new Vector2().x(screenWidth1 / 2).y(screenHeight1 / 2));
        camera.rotation(0.0f);
        camera.zoom(1.0f);

        float screenWidth = screenWidth1 + 150;
        float screenHeight = screenHeight1 + 150;
        float simulationSpeed = 1.0f;

        // Таймер
        double startTime = GetTime();      // сохраняем момент запуска

        while (!WindowShouldClose()) {
            if (IsWindowResized()) {
                screenWidth1 = GetScreenWidth();
                screenHeight1 = GetScreenHeight();
            }
            if (IsMouseButtonDown(MOUSE_BUTTON_MIDDLE)) {
                camera.target(Vector2Add(camera.target(),
                        Vector2Scale(GetMouseDelta(), -1.0f / camera.zoom())));
            }
            camera.zoom(camera.zoom() + GetMouseWheelMove() * 0.03f);
            if (camera.zoom() <= 0.1f) camera.zoom(0.1f);

            if (IsKeyPressed(KEY_EQUAL) || IsKeyPressed(KEY_KP_ADD)) simulationSpeed *= 1.2f;
            if (IsKeyPressed(KEY_MINUS) || IsKeyPressed(KEY_KP_SUBTRACT)) simulationSpeed /= 1.2f;
            simulationSpeed = Clamp(simulationSpeed, 0.1f, 10.0f);

            // Сброс таймера по клавише R
            if (IsKeyPressed(KEY_R)) {
                startTime = GetTime();
            }

            float dt = 0.016f * simulationSpeed;
            Atom.SimulateAtoms(atoms, dt, screenWidth, screenHeight);

            float elapsed = (float)(GetTime() - startTime); // прошедшее время

            float kinetic = Atom.CalculateKineticEnergy(atoms);
            float potential = Atom.CalculatePotentialEnergy(atoms, screenWidth, screenHeight, 80.0f);
            float total = kinetic + potential;

            BeginDrawing();
            ClearBackground(RAYWHITE);
            BeginMode2D(camera);

            // Границы поля
            DrawRectangleLines(0, 0, (int) screenWidth, (int) screenHeight, DARKGRAY);

            for (Atom atom : atoms) {
                DrawCircle((int) atom.position.x(), (int) atom.position.y(), atom.radius, atom.color);
                DrawLine(
                        (int) atom.position.x(), (int) atom.position.y(),
                        (int) (atom.position.x() + atom.velocity.x() * 0.5f),
                        (int) (atom.position.y() + atom.velocity.y() * 0.5f),
                        GREEN
                );
                DrawLine(
                        (int) atom.position.x(), (int) atom.position.y(),
                        (int) (atom.position.x() + atom.force.x() * 0.01f),
                        (int) (atom.position.y() + atom.force.y() * 0.01f),
                        RED
                );
            }
            EndMode2D();

            // Интерфейс
            DrawFPS(20, 20);
            DrawText("Sim speed: " + String.format("%.1f", simulationSpeed), 20, 40, 20, RED);
            DrawText("Objects: " + countObjects, 20, 60, 20, RED);
            DrawText(String.format("Kinetic: %.1f", kinetic), 20, 80, 20, RED);
            DrawText(String.format("Potential: %.1f", potential), 20, 100, 20, RED);
            DrawText(String.format("Total energy*: %.1f", total), 20, 120, 20, RED);
            DrawText("* without gravity potential", 20, 140, 10, GRAY);
            DrawText(String.format("Time: %.2f s", elapsed), 20, 155, 20, DARKBLUE);
            DrawText("Press R to reset timer", 20, 175, 15, GRAY);

            EndDrawing();
        }
        CloseWindow();
    }
}
