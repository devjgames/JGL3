package org.jgl3.scene;

import java.io.File;

import org.jgl3.AssetLoader;
import org.jgl3.Game;
import org.jgl3.IO;

public abstract class Animator {
    
    private File file = null;

    public Animator() {
    }

    public final File getFile() {
        return file;
    }

    public void init(Scene scene, Node node) throws Exception {
    }

    public void animate(Scene scene, Node node) throws Exception {
    }

    public final Animator newInstance() throws Exception {
        Animator animator = (Animator)getClass().getConstructors()[0].newInstance();

        animator.file = file;

        return animator;
    }

    private static class Loader implements AssetLoader {
        @Override
        public Object load(File file) throws Exception {
            String[] tokens = new String(IO.readAllBytes(file)).split("\\s+");
            Animator animator = (Animator)Class.forName(tokens[0]).getConstructors()[0].newInstance();

            animator.file = file;

            return animator;
        }
    }

    public static void registerAssetLoader() {
        Game.getInstance().getAssets().registerAssetLoader(".ani", 0, new Loader());
    }
}
