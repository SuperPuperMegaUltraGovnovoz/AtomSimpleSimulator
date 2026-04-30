package org.example;

import static com.raylib.Raylib.*;

public class Atom {
    Vector2 position;
    int radius;
    Color color;
    Vector2 force;
    float mass;
    public Vector2 velocity;

    public static final float SIGMA = 40.0f;
    public static final float EPSILON = 10.0f;
    public static final float MIN_DIST = 1.0f;
    public static final float CUTOFF = 150.0f;
    public static final float CUTOFF_SQ = CUTOFF * CUTOFF;

    // Параметры силы от мыши
    public static final float MOUSE_FORCE = 15000.0f;   // амплитуда отталкивания
    public static final float MOUSE_RADIUS = 250.0f;   // радиус действия
    public static final float MOUSE_RADIUS_SQ = MOUSE_RADIUS * MOUSE_RADIUS;

    public Atom(float x, float y, float radius, Color color) {
        this.position = new Vector2().x(x).y(y);
        this.radius = (int) radius;
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
            force.x(force.x() + stiffness * penetration);
        } else if (position.x() > right) {
            float penetration = position.x() - right;
            force.x(force.x() - stiffness * penetration);
        }

        if (position.y() < top) {
            float penetration = top - position.y();
            force.y(force.y() + stiffness * penetration);
        } else if (position.y() > bottom) {
            float penetration = position.y() - bottom;
            force.y(force.y() - stiffness * penetration);
        }
    }

    public static void LennardJones(Atom atom1, Atom atom2) {
        float dx = atom1.position.x() - atom2.position.x();
        float dy = atom1.position.y() - atom2.position.y();
        float rSq = dx * dx + dy * dy;

        if (rSq > CUTOFF_SQ) return;

        float r = (float) Math.sqrt(rSq);
        float invR = 1.0f / r;
        float unitX, unitY;

        if (r < MIN_DIST) {
            r = MIN_DIST;
            invR = 1.0f / r;
            if (rSq < 1e-6f) {
                unitX = 1.0f; unitY = 0.0f;
            } else {
                unitX = dx * invR;
                unitY = dy * invR;
            }
        } else {
            unitX = dx * invR;
            unitY = dy * invR;
        }

        float sigmaOverR = SIGMA * invR;
        float sigmaOverR2 = sigmaOverR * sigmaOverR;
        float sigmaOverR6 = sigmaOverR2 * sigmaOverR2 * sigmaOverR2;
        float sigmaOverR12 = sigmaOverR6 * sigmaOverR6;

        float forceMagnitude = 24.0f * EPSILON * invR *
                (2.0f * sigmaOverR12 - sigmaOverR6);

        atom1.force.x(atom1.force.x() + forceMagnitude * unitX);
        atom1.force.y(atom1.force.y() + forceMagnitude * unitY);
        atom2.force.x(atom2.force.x() - forceMagnitude * unitX);
        atom2.force.y(atom2.force.y() - forceMagnitude * unitY);
    }

    static void UpdateAtom(Atom atom, float dt) {
        float ax = atom.force.x() / atom.mass;
        float ay = atom.force.y() / atom.mass;

        atom.velocity.x(atom.velocity.x() + ax * dt);
        atom.velocity.y(atom.velocity.y() + ay * dt);
        atom.position.x(atom.position.x() + atom.velocity.x() * dt);
        atom.position.y(atom.position.y() + atom.velocity.y() * dt);
    }

    // Новая версия симуляции с поддержкой мыши
    public static void SimulateAtoms(Atom[] atoms, float dt, float screenWidth, float screenHeight,
                                     float mouseX, float mouseY, boolean mouseDown) {
        int n = atoms.length;
        int subSteps = 2;
        float dtSub = dt / subSteps;

        for (int step = 0; step < subSteps; step++) {
            for (Atom a : atoms) a.ResetForce();

            // Парные взаимодействия
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    LennardJones(atoms[i], atoms[j]);
                }
            }

            // Стенки
            float stiffness = 80.0f;
            for (Atom a : atoms) {
                a.ApplyWallForces(screenWidth, screenHeight, stiffness);
            }

            // Сила от мыши (только если нажата)
            if (mouseDown) {
                for (Atom a : atoms) {
                    float dx = a.position.x() - mouseX;
                    float dy = a.position.y() - mouseY;
                    float distSq = dx * dx + dy * dy;

                    if (distSq < MOUSE_RADIUS_SQ) {
                        float dist = (float) Math.sqrt(distSq);
                        if (dist < 1.0f) dist = 1.0f;  // избегаем деления на ноль
                        float invDist = 1.0f / dist;
                        float unitX = dx * invDist;
                        float unitY = dy * invDist;

                        // Сила отталкивания: обратно пропорциональна расстоянию (можно сделать обратно квадрату)
                        float forceMag = MOUSE_FORCE / (dist * dist);  // или MOUSE_FORCE / dist для более мягкого спада
                        a.force.x(a.force.x() + forceMag * unitX);
                        a.force.y(a.force.y() + forceMag * unitY);
                    }
                }
            }

            // Интегрирование
            for (Atom a : atoms) {
                UpdateAtom(a, dtSub);
            }
        }
    }

    // Старый метод без мыши оставлен для совместимости (если нужен)
    public static void SimulateAtoms(Atom[] atoms, float dt, float screenWidth, float screenHeight) {
        SimulateAtoms(atoms, dt, screenWidth, screenHeight, 0, 0, false);
    }

    public static float CalculateKineticEnergy(Atom[] atoms) {
        float kinetic = 0.0f;
        for (Atom a : atoms) {
            float vx = a.velocity.x();
            float vy = a.velocity.y();
            kinetic += 0.5f * a.mass * (vx * vx + vy * vy);
        }
        return kinetic;
    }

    public static float CalculatePotentialEnergy(Atom[] atoms, float screenWidth, float screenHeight, float wallStiffness) {
        float potentialPairs = 0.0f;
        int n = atoms.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                float dx = atoms[j].position.x() - atoms[i].position.x();
                float dy = atoms[j].position.y() - atoms[i].position.y();
                float rSq = dx * dx + dy * dy;
                if (rSq > CUTOFF_SQ) continue;

                float r = (float) Math.sqrt(rSq);
                if (r < MIN_DIST) r = MIN_DIST;

                float invR = 1.0f / r;
                float sigmaOverR = SIGMA * invR;
                float sigmaOverR2 = sigmaOverR * sigmaOverR;
                float sigmaOverR6 = sigmaOverR2 * sigmaOverR2 * sigmaOverR2;
                float sigmaOverR12 = sigmaOverR6 * sigmaOverR6;
                potentialPairs += 4.0f * EPSILON * (sigmaOverR12 - sigmaOverR6);
            }
        }

        float potentialWalls = 0.0f;
        for (Atom a : atoms) {
            float x = a.position.x();
            float y = a.position.y();
            float r = a.radius;
            float left = r, right = screenWidth - r, top = r, bottom = screenHeight - r;

            if (x < left) {
                float p = left - x;
                potentialWalls += 0.5f * wallStiffness * p * p;
            } else if (x > right) {
                float p = x - right;
                potentialWalls += 0.5f * wallStiffness * p * p;
            }
            if (y < top) {
                float p = top - y;
                potentialWalls += 0.5f * wallStiffness * p * p;
            } else if (y > bottom) {
                float p = y - bottom;
                potentialWalls += 0.5f * wallStiffness * p * p;
            }
        }
        return potentialPairs + potentialWalls;
    }
}
