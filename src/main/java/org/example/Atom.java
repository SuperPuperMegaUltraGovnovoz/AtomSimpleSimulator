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
    public static final float GRAVITY = 0.5f;
    public static final float CUTOFF = 150.0f;      // расстояние, дальше которого нет взаимодействия
    public static final float CUTOFF_SQ = CUTOFF * CUTOFF;

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

        if (rSq > CUTOFF_SQ) return;          // нет взаимодействия

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
        // Простое обновление без создания объектов
        float ax = atom.force.x() / atom.mass;
        float ay = atom.force.y() / atom.mass;

        atom.velocity.x(atom.velocity.x() + ax * dt);
        atom.velocity.y(atom.velocity.y() + ay * dt);
        atom.position.x(atom.position.x() + atom.velocity.x() * dt);
        atom.position.y(atom.position.y() + atom.velocity.y() * dt);
    }

    public static void SimulateAtoms(Atom[] atoms, float dt, float screenWidth, float screenHeight) {
        int n = atoms.length;
        int subSteps = 2;   // уменьшили до 2х
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

            // Гравитация
            for (Atom a : atoms) {
                a.force.y(a.force.y() + GRAVITY * a.mass);
            }

            // Интегрирование
            for (Atom a : atoms) {
                UpdateAtom(a, dtSub);
            }
        }
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
