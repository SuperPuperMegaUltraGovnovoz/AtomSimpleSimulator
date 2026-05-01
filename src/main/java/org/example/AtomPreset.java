package org.example;

public class AtomPreset {
    public String name;
    public float sigma;
    public float epsilon;
    public float cutoff;
    public float minDist;

    public AtomPreset(String name, float sigma, float epsilon, float cutoff, float minDist) {
        this.name = name;
        this.sigma = sigma;
        this.epsilon = epsilon;
        this.cutoff = cutoff;
        this.minDist = minDist;
    }

    public static final AtomPreset HYDROGEN = new AtomPreset(
            "Hydrogen",
            50.0f,    // sigma чуть больше – более рыхлое отталкивание
            0.2f,     // epsilon значительно уменьшена – слабое притяжение
            200.0f,   // cutoff
            20.0f     // minDist
    );

    public static final AtomPreset IRON = new AtomPreset(
            "Iron",
            50.0f,   // больше sigma – более плотная упаковка
            2.0f,    // более сильное притяжение
            250.0f,  // больший cutoff
            20.0f    // тот же minDist
    );

    public static final AtomPreset OXYGEN = new AtomPreset(
            "Oxygen",      55.0f, 0.3f, 220.0f, 20.0f
    );

    public static final AtomPreset NITROGEN = new AtomPreset(
            "Nitrogen",    55.0f, 0.35f, 220.0f, 20.0f
    );

    public static final AtomPreset TITANIUM = new AtomPreset(
            "Titanium",    45.0f, 2.5f, 260.0f, 20.0f
    );

    public static final AtomPreset POLONIUM = new AtomPreset(
            "Polonium",    48.0f, 3.0f, 270.0f, 20.0f
    );
}
