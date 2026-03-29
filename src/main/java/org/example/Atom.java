package org.example;

import static com.raylib.Raylib.*;

public class Atom {
    Vector2 position;
    int radius;
    Color color;
    Vector2 force;
    float mass;
    public Vector2 velocity;

    public Atom(float x, float y, float radius, Color color) {
        this.position = new Vector2().x(x).y(y);
        this.radius = (int)radius;
        this.color = color;
        this.force = new Vector2().x(0).y(0);
        this.mass = mass;
        this.velocity = new Vector2().x(0).y(0);
    }

    void ResetForce() {
        force.x(0);
        force.y(0);
    }

    void ApplyWallForces(float screenWidth, float screenHeight, float stiffness) {
        float left = radius;
        float right = screenWidth - radius;
        float top = radius;
        float bottom = screenHeight - radius;

        // Левая стена
        if (position.x() < left) {
            float penetration = left - position.x();
            float wallForce = stiffness * penetration * penetration;
            force.x(force.x() + wallForce);
        }
        // Правая стена
        else if (position.x() > right) {
            float penetration = position.x() - right;
            float wallForce = stiffness * penetration * penetration;
            force.x(force.x() - wallForce);
        }

        // Верхняя стена
        if (position.y() < top) {
            float penetration = top - position.y();
            float wallForce = stiffness * penetration * penetration;
            force.y(force.y() + wallForce);
        }
        // Нижняя стена
        else if (position.y() > bottom) {
            float penetration = position.y() - bottom;
            float wallForce = stiffness * penetration * penetration;
            force.y(force.y() - wallForce);
        }
    }

    public static void LenJons(Atom atom, Atom atom2){
        Vector2 rVec = Vector2Subtract(atom.position, atom2.position);

        float r = Vector2Length(rVec);
        if (r < 0.1f) r = 0.1f;
        Vector2 direction = Vector2Normalize(rVec);

        float minDist = atom.radius * 2 * 1.1f; // С небольшим запасом
        if (r < minDist) {
            // Раздвигаем частицы
            Vector2 correction = Vector2Scale(direction, (minDist - r) * 0.5f);
            atom.position = Vector2Add(atom.position, correction);
            atom2.position = Vector2Subtract(atom2.position, correction);
        }


        float forceMagnitude = (4*1.0f * ((float)Math.pow((1.5f / r), 12) - (float) Math.pow((1.5f / r),6)));

        atom.force.x(atom.force.x() + forceMagnitude * direction.x());
        atom.force.y(atom.force.y() + forceMagnitude * direction.y());
        atom2.force.x(atom2.force.x() - forceMagnitude * direction.x());
        atom2.force.y(atom2.force.y() - forceMagnitude * direction.y());
    }

    static void UpdateAtomSymplectic(Atom atom, float dt) {
        Vector2 acceleration = new Vector2()
                .x(atom.force.x() / atom.mass)
                .y(atom.force.y() / atom.mass);

        atom.velocity.x(atom.velocity.x() + acceleration.x() * dt);
        atom.velocity.y(atom.velocity.y() + acceleration.y() * dt);

        atom.position.x(atom.position.x() + atom.velocity.x() * dt);
        atom.position.y(atom.position.y() + atom.velocity.y() * dt);
    }

    public static void SimulateAtoms(Atom[] atoms, float dt, float screenWidth, float screenHeight) {
        int n = atoms.length;

        // Шаг 1: Сброс сил для всех частиц
        for (Atom a : atoms) {
            a.ResetForce();
        }

        // Шаг 2: Расчёт сил взаимодействия между всеми парами частиц
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                LenJons(atoms[i], atoms[j]);
            }
        }

        float stiffness = 500.0f; // Жёсткость границ (подберите под свою симуляцию)
        for (Atom a : atoms) {
            a.ApplyWallForces(screenWidth, screenHeight, stiffness);
        }

        // Шаг 3: Обновление позиций и скоростей каждой частицы
        for (Atom a : atoms) {
            UpdateAtomSymplectic(a, dt);
        }
    }
}
