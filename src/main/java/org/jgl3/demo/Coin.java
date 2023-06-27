package org.jgl3.demo;

import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Sound;
import org.jgl3.scene.Animator;
import org.jgl3.scene.Node;
import org.jgl3.scene.Scene;

public class Coin extends Animator {
    
    private Node hero = null;
    private Sound collect = null;

    @Override
    public void init(Scene scene, Node node) throws Exception {
        if(App.getDemo() instanceof Editor) {
            return;
        }

        collect = Game.getInstance().getAssets().load(IO.file("assets/sound/collect.wav"));
        collect.setVolume(0.25f);
    }

    @Override
    public void animate(Scene scene, Node node) throws Exception {
        if(App.getDemo() instanceof Editor) {
            return;
        }

        if(hero == null) {
            scene.getRoot().traverse((n) -> {
                if(n.getAnimator() instanceof Hero) {
                    hero = n;
                }
                return true;
            });
        }

        if(hero != null && node.isVisible()) {
            if(hero.getPosition().distance(node.getPosition()) < 32) {
                node.setVisible(false);
                collect.play(false);
            }
        }

        float elapsed = Game.getInstance().getElapsedTime();

        node.getRotation().rotate((float)Math.toRadians(90) * elapsed, 0, 1, 0);
    }
}
