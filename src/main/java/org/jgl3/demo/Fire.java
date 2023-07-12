package org.jgl3.demo;

import java.util.Random;

import org.jgl3.scene.Particle;
import org.jgl3.scene.ParticleEmitter;

public class Fire implements ParticleEmitter {
    
    @Override
    public void emitParticle(Particle particle, Random random) {
        float c = 0.05f + random.nextFloat() * 0.1f;

        particle.startColorR = 
        particle.startColorG = 
        particle.startColorB = 
        particle.startColorA = c;

        particle.endColorR = 
        particle.endColorG = 
        particle.endColorB = 
        particle.endColorA = 0;
    }
}
