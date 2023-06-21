package org.jgl3.scene;

import java.util.Random;

public interface ParticleEmitter {

    void emitParticle(Particle particle, Random random);
}
