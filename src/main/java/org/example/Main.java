package org.example;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;

public class Main {
    // Цветовая шкала: синий (холодный) → зелёный → красный (горячий)
    static Color GetTemperatureColor(float speed, float maxSpeed) {
        // Избегаем деления на ноль
        if (maxSpeed < 1e-6f) return BLUE;
        float t = speed / maxSpeed;
        if (t > 1.0f) t = 1.0f;
        int r, g, b;
        if (t < 0.5f) {
            // Синий (0,0,255) → Зелёный (0,255,0)
            float s = t * 2.0f;
            r = 0;
            g = (int)(255 * s);
            b = (int)(255 * (1.0f - s));
        } else {
            // Зелёный (0,255,0) → Красный (255,0,0)
            float s = (t - 0.5f) * 2.0f;
            r = (int)(255 * s);
            g = (int)(255 * (1.0f - s));
            b = 0;
        }
        // Явно задаём альфа-канал 255 (непрозрачный)
        return new Color().r((byte) r).g((byte) g).b((byte) b).a((byte) 255);
    }

    public static void main(String[] args) {
        float screenWidth = 1280;
        float screenHeight = 720;
        SetConfigFlags(FLAG_WINDOW_RESIZABLE);
        InitWindow((int) screenWidth, (int) screenHeight, "LJ Thermal Map");

        SetTargetFPS(-1);
        int countObjects = 300;
        Rectangle spawnArea = new Rectangle().x(20).y(20).width(40).height(40);
        int cols = 30;

        Atom[] atoms = new Atom[countObjects];
        for (int i = 0; i < countObjects; i++) {
            int row = i / cols;
            int col = i % cols;
            atoms[i] = new Atom(
                    (int) spawnArea.x() + col * 45,
                    (int) spawnArea.y() + row * 45,
                    20, ColorAlpha(VIOLET, 0.2f)
            );
            atoms[i].velocity.x((float) (Math.random() * 1.0 - 0.5));
            atoms[i].velocity.y((float) (Math.random() * 1.0 - 0.5));
        }

        float screenWidth1 = 1280 + 500;
        float screenHeight1 = 720 + 500;

        Camera2D camera = new Camera2D();
        camera.target(new Vector2().x(spawnArea.x() + 200).y(spawnArea.y() + 200));
        camera.offset(new Vector2().x(screenWidth / 2).y(screenHeight / 2));
        camera.rotation(0.0f);
        camera.zoom(1.0f);

        float simulationSpeed = 1.0f;
        double startTime = GetTime();

        while (!WindowShouldClose()) {
            if (IsWindowResized()) {
                screenWidth = GetScreenWidth();
                screenHeight = GetScreenHeight();
                camera.offset(new Vector2().x(screenWidth / 2).y(screenHeight / 2));
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

            if (IsKeyPressed(KEY_R)) startTime = GetTime();

            Vector2 mouseScreen = GetMousePosition();
            Vector2 mouseWorld = GetScreenToWorld2D(mouseScreen, camera);
            boolean leftMouse = IsMouseButtonDown(MOUSE_BUTTON_LEFT);

            float dt = 0.016f * simulationSpeed;
            Atom.SimulateAtoms(atoms, dt, screenWidth1, screenHeight1,
                    mouseWorld.x(), mouseWorld.y(), leftMouse);

            // Максимальная скорость для цветовой шкалы (без искусственного минимума)
            float maxSpeed = 0.0001f; // крошечная не нулевая, чтобы избежать деления на 0
            for (Atom a : atoms) {
                float sp = Vector2Length(a.velocity);
                if (sp > maxSpeed) maxSpeed = sp;
            }

            float elapsed = (float)(GetTime() - startTime);
            float kinetic = Atom.CalculateKineticEnergy(atoms);
            float potential = Atom.CalculatePotentialEnergy(atoms, screenWidth1, screenHeight1, 80.0f);
            float total = kinetic + potential;

            BeginDrawing();
            ClearBackground(RAYWHITE);
            BeginMode2D(camera);

            DrawRectangleLines(0, 0, (int) screenWidth1, (int) screenHeight1, DARKGRAY);

            for (Atom atom : atoms) {
                Color tempCol = GetTemperatureColor(Vector2Length(atom.velocity), maxSpeed);
                DrawCircle((int) atom.position.x(), (int) atom.position.y(), atom.radius, tempCol);

                // Полупрозрачные стрелки
                DrawLine(
                        (int) atom.position.x(), (int) atom.position.y(),
                        (int) (atom.position.x() + atom.velocity.x() * 0.5f),
                        (int) (atom.position.y() + atom.velocity.y() * 0.5f),
                        ColorAlpha(GREEN, 0.4f)
                );
                DrawLine(
                        (int) atom.position.x(), (int) atom.position.y(),
                        (int) (atom.position.x() + atom.force.x() * 0.01f),
                        (int) (atom.position.y() + atom.force.y() * 0.01f),
                        ColorAlpha(RED, 0.4f)
                );
            }
            EndMode2D();

            DrawFPS(20, 20);
            DrawText("Sim speed: " + String.format("%.1f", simulationSpeed), 20, 40, 20, RED);
            DrawText("Objects: " + countObjects, 20, 60, 20, RED);
            DrawText(String.format("Kinetic: %.1f", kinetic), 20, 80, 20, RED);
            DrawText(String.format("Potential: %.1f", potential), 20, 100, 20, RED);
            DrawText(String.format("Total energy: %.1f", total), 20, 120, 20, RED);
            DrawText(String.format("Time: %.2f s", elapsed), 20, 150, 20, DARKBLUE);
            DrawText("LMB: push | MMB: pan | Wheel: zoom", 20, 170, 12, GRAY);
            DrawText("Max speed: " + String.format("%.1f", maxSpeed), 20, 190, 15, DARKGRAY);

            EndDrawing();
        }
        CloseWindow();
    }
}
