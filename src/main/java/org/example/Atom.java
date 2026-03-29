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
        this.mass = 1.0f;
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

        if (position.x() < left) {
            float penetration = left - position.x();
            float wallForce = stiffness * penetration; // Линейная сила
            force.x(force.x() + wallForce);
        } else if (position.x() > right) {
            float penetration = position.x() - right;
            float wallForce = stiffness * penetration;
            force.x(force.x() - wallForce);
        }

        if (position.y() < top) {
            float penetration = top - position.y();
            float wallForce = stiffness * penetration;
            force.y(force.y() + wallForce);
        } else if (position.y() > bottom) {
            float penetration = position.y() - bottom;
            float wallForce = stiffness * penetration;
            force.y(force.y() - wallForce);
        }
    }


    public static void LenJons(Atom atom1, Atom atom2){
        Vector2 rVec = Vector2Subtract(atom2.position, atom1.position);
        float rSquared = rVec.x() * rVec.x() + rVec.y() * rVec.y();

        // Проверка на слишком малое расстояние (избегаем деления на ноль)
        if (rSquared < 0.25f) {
            rSquared = 0.25f;
            float r = 0.5f;
            Vector2 direction = Vector2Normalize(rVec);

            // Отталкивание при слишком близком расстоянии
            float forceMagnitude = 1000.0f; // Максимальная сила отталкивания

            atom1.force.x(atom1.force.x() + forceMagnitude * direction.x());
            atom1.force.y(atom1.force.y() + forceMagnitude * direction.y());
            atom2.force.x(atom2.force.x() - forceMagnitude * direction.x());
            atom2.force.y(atom2.force.y() - forceMagnitude * direction.y());
            return;
        }

        float r = (float) Math.sqrt(rSquared);
        Vector2 direction = Vector2Scale(rVec, 1.0f / r);

        float sigma = 1.5f;
        float epsilon = 0.1f; // Уменьшено для стабильности

        float sigmaOverR = sigma / r;
        float sigmaOverRPow6 = (float) Math.pow(sigmaOverR, 6);
        float sigmaOverRPow12 = sigmaOverRPow6 * sigmaOverRPow6;

        // Формула Леннарда-Джонса с ограничением
        float forceMagnitude = (24.0f * epsilon / r) * (2.0f * sigmaOverRPow12 - sigmaOverRPow6);

        // Ограничение максимальной силы
        float maxForce = 500.0f;
        if (Math.abs(forceMagnitude) > maxForce) {
            forceMagnitude = Math.signum(forceMagnitude) * maxForce;
        }

        atom1.force.x(atom1.force.x() + forceMagnitude * direction.x());
        atom1.force.y(atom1.force.y() + forceMagnitude * direction.y());
        atom2.force.x(atom2.force.x() - forceMagnitude * direction.x());
        atom2.force.y(atom2.force.y() - forceMagnitude * direction.y());
    }

    static void UpdateAtomSymplectic(Atom atom, float dt) {
        // Ограничение максимальной скорости
        float maxSpeed = 10.0f;
        float speed = Vector2Length(atom.velocity);
        if (speed > maxSpeed) {
            atom.velocity = Vector2Scale(Vector2Normalize(atom.velocity), maxSpeed);
        }

        Vector2 acceleration = new Vector2()
                .x(atom.force.x() / atom.mass)
                .y(atom.force.y() / atom.mass);

        // Полунеявный метод Эйлера (более стабильный)
        atom.velocity.x(atom.velocity.x() + acceleration.x() * dt);
        atom.velocity.y(atom.velocity.y() + acceleration.y() * dt);

        // Слабое демпфирование
        float damping = 0.995f;
        atom.velocity.x(atom.velocity.x() * damping);
        atom.velocity.y(atom.velocity.y() * damping);

        atom.position.x(atom.position.x() + atom.velocity.x() * dt);
        atom.position.y(atom.position.y() + atom.velocity.y() * dt);
    }

    public static void SimulateAtoms(Atom[] atoms, float dt, float screenWidth, float screenHeight, int countObjects) {
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

        // Шаг 3: Силы от границ экрана
        float stiffness = 80.0f; // Уменьшено для плавности
        for (Atom a : atoms) {
            a.ApplyWallForces(screenWidth, screenHeight, stiffness);
        }

        // Шаг 4: Обновление позиций и скоростей
        for (Atom a : atoms) {
            UpdateAtomSymplectic(a, dt);
        }
    }
}
