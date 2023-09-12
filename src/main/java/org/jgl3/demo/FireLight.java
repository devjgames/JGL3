package org.jgl3.demo;

import org.jgl3.Game;
import org.jgl3.Node;
import org.jgl3.ParticleSystem;
import org.jgl3.Scene;

public class FireLight extends Node {
    
    @Override
    public void update(Scene scene) throws Exception {
        ParticleSystem particles = (ParticleSystem)getParent();
        Game game = Game.getInstance();

        particles.getEmitPosition().y = (float)Math.sin(game.getTotalTime() * 2) * 25;
    }
}
