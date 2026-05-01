package org.example;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;

import java.util.Random;

public class Main {
    static final float REFERENCE_SPEED = 20.0f; // для абсолютной шкалы

    // Режим температурной шкалы: true = абсолютная (Q), false = относительная (W)
    static boolean useAbsoluteScale = true;

    // Абсолютная шкала: чем быстрее частица, тем краснее
    static Color GetAbsoluteColor(float speed) {
        float t = speed / REFERENCE_SPEED;
        if (t > 1.0f) t = 1.0f;
        int r, g, b;
        if (t < 0.5f) {
            float s = t * 2.0f;
            r = 0;
            g = (int)(255 * s);
            b = (int)(255 * (1.0f - s));
        } else {
            float s = (t - 0.5f) * 2.0f;
            r = (int)(255 * s);
            g = (int)(255 * (1.0f - s));
            b = 0;
        }
        return new Color().r((byte) r).g((byte) g).b((byte) b).a((byte) 255);
    }

    // Относительная шкала: цвет зависит от доли скорости от текущей максимальной
    static Color GetRelativeColor(float speed, float maxSpeed) {
        if (maxSpeed < 1e-6f) return BLUE;
        float t = speed / maxSpeed;
        if (t > 1.0f) t = 1.0f;
        int r, g, b;
        if (t < 0.5f) {
            float s = t * 2.0f;
            r = 0;
            g = (int)(255 * s);
            b = (int)(255 * (1.0f - s));
        } else {
            float s = (t - 0.5f) * 2.0f;
            r = (int)(255 * s);
            g = (int)(255 * (1.0f - s));
            b = 0;
        }
        return new Color().r((byte) r).g((byte) g).b((byte) b).a((byte) 255);
    }

    public static void main(String[] args) {
        float screenWidth = 1280;
        float screenHeight = 720;
        SetConfigFlags(FLAG_WINDOW_RESIZABLE);
        InitWindow((int) screenWidth, (int) screenHeight, "LJ Thermal Map (Q/W scale)");

        SetTargetFPS(60);
        int countObjects = 300;
        float screenWidth1 = 1280 + 500;
        float screenHeight1 = 720 + 500;

        long seed = 18922L;
        Random random = new Random(seed);

        Atom[] atoms = new Atom[countObjects];
        int atomRadius = 20;

        float minX = atomRadius;
        float maxX = screenWidth1 - atomRadius;
        float minY = atomRadius;
        float maxY = screenHeight1 - atomRadius;

        for (int i = 0; i < countObjects; i++) {
            float x = minX + random.nextFloat() * (maxX - minX);
            float y = minY + random.nextFloat() * (maxY - minY);
            atoms[i] = new Atom(x, y, atomRadius, ColorAlpha(VIOLET, 0.2f));

            atoms[i].velocity.x((float) (random.nextFloat() * 8 - 4));
            atoms[i].velocity.y((float) (random.nextFloat() * 8 - 4));
        }

        Camera2D camera = new Camera2D();
        camera.target(new Vector2().x(screenWidth1 / 2).y(screenHeight1 / 2));
        camera.offset(new Vector2().x(screenWidth / 2).y(screenHeight / 2));
        camera.rotation(0.0f);
        camera.zoom(1.0f);

        float simulationSpeed = 1.0f;
        double simTime = 0.0;

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

            if (IsKeyPressed(KEY_R)) simTime = 0.0;

            // Переключение пресетов
            if (IsKeyPressed(KEY_ONE))   Atom.currentPreset = AtomPreset.HYDROGEN;
            if (IsKeyPressed(KEY_TWO))   Atom.currentPreset = AtomPreset.IRON;
            if (IsKeyPressed(KEY_THREE))   Atom.currentPreset = AtomPreset.OXYGEN;
            if (IsKeyPressed(KEY_FOUR))    Atom.currentPreset = AtomPreset.NITROGEN;
            if (IsKeyPressed(KEY_FIVE))    Atom.currentPreset = AtomPreset.TITANIUM;
            if (IsKeyPressed(KEY_SIX))     Atom.currentPreset = AtomPreset.POLONIUM;

            // Переключение режима шкалы
            if (IsKeyPressed(KEY_Q)) useAbsoluteScale = true;
            if (IsKeyPressed(KEY_W)) useAbsoluteScale = false;

            Vector2 mouseScreen = GetMousePosition();
            Vector2 mouseWorld = GetScreenToWorld2D(mouseScreen, camera);
            boolean leftMouse = IsMouseButtonDown(MOUSE_BUTTON_LEFT);

            float dt = 0.016f * simulationSpeed;
            Atom.SimulateAtoms(atoms, dt, screenWidth1, screenHeight1,
                    mouseWorld.x(), mouseWorld.y(), leftMouse);

            simTime += dt;

            float maxSpeed = 0.0f;
            float totalSpeed = 0.0f;
            for (Atom a : atoms) {
                float sp = Vector2Length(a.velocity);
                if (sp > maxSpeed) maxSpeed = sp;
                totalSpeed += sp;
            }
            float avgSpeed = totalSpeed / countObjects;

            float wallStiffness = Atom.WALL_STIFFNESS;
            float kinetic = Atom.CalculateKineticEnergy(atoms);
            float potential = Atom.CalculatePotentialEnergy(atoms, screenWidth1, screenHeight1, wallStiffness);
            float total = kinetic + potential;

            BeginDrawing();
            ClearBackground(RAYWHITE);
            BeginMode2D(camera);

            DrawRectangleLines(0, 0, (int) screenWidth1, (int) screenHeight1, DARKGRAY);

            for (Atom atom : atoms) {
                Color tempCol;
                if (useAbsoluteScale) {
                    tempCol = GetAbsoluteColor(Vector2Length(atom.velocity));
                } else {
                    tempCol = GetRelativeColor(Vector2Length(atom.velocity), maxSpeed);
                }
                DrawCircle((int) atom.position.x(), (int) atom.position.y(), atom.radius, tempCol);

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
            DrawText(String.format("Sim Time: %.2f s", simTime), 20, 150, 20, DARKBLUE);
            DrawText("Preset: " + Atom.currentPreset.name, 20, 170, 15, DARKGREEN);

            // Информация о шкале
            String scaleMode = useAbsoluteScale ? "Absolute (Q) ref=" + REFERENCE_SPEED : "Relative (W)";
            DrawText("Scale: " + scaleMode, 20, 190, 15, DARKGRAY);
            DrawText("Avg speed: " + String.format("%.1f", avgSpeed), 20, 210, 15, DARKGRAY);
            DrawText("Keys: 1,2 presets | Q,W scale | LMB push | MMB pan", 20, 230, 10, GRAY);
            DrawText("Max speed: " + String.format("%.1f", maxSpeed), 20, 250, 15, GRAY);

            EndDrawing();
        }
        CloseWindow();
    }
}
