package org.jgl3.demo;

import java.util.Random;

import org.jgl3.scene.Particle;
import org.jgl3.scene.ParticleEmitter;

public class FireLight implements ParticleEmitter {
    
    @Override
    public void emitParticle(Particle particle, Random random) {
        float c = 0.3f + random.nextFloat() * 0.3f;
        float s = 20 + random.nextFloat() * 40;
        float e = 4 + random.nextFloat() * 8;

        particle.startColorR = particle.startColorG = particle.startColorB = c;
        particle.endColorR = particle.endColorG = particle.endColorB = 0;
        particle.startSizeX = particle.startSizeY = s;
        particle.endSizeX = particle.endSizeY = e;
        particle.velocityX = -4 + random.nextFloat() * 8;
        particle.velocityY = -4 + random.nextFloat() * 8;
        particle.velocityZ = -4 + random.nextFloat() * 8;
        particle.lifeSpan = 0.5f + random.nextFloat() * 2;
    }
}
