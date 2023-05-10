package org.jgl3.scene;

import org.jgl3.AssetLoader;
import org.jgl3.AssetManager;
import org.jgl3.BoundingBox;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Renderer;
import org.jgl3.Triangle;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.File;
import java.util.Random;

public final class ParticleSystem implements Renderable {

    public static void registerAssetLoader() {
        Game.getInstance().getAssets().registerAssetLoader(".par", new Loader());
    }

    private static class Loader implements AssetLoader {
        @Override
        public Object load(File file, AssetManager assets) throws Exception {
            String[] tokens = new String(IO.readAllBytes(file)).split("\\s+");

            return new ParticleSystem(
                file,
                Integer.parseInt(tokens[0]),
                Integer.parseInt(tokens[1]),
                Integer.parseInt(tokens[2]),
                (ParticleEmitter)Class.forName(tokens[3]).getConstructors()[0].newInstance()
            );
        }
    }

    private final File file;
    private final Vector3f position = new Vector3f();
    private final int maxParticles;
    private final float particlesPerSecond;
    private Particle[] live;
    private final Particle[] dead;
    private Particle[] temp;
    private int liveCount = 0;
    private int deadCount = 0;
    private float time = 0;
    private float seconds = 0;
    private final Random random;
    private ParticleEmitter emitter;
    private final Matrix4f matrix = new Matrix4f();
    private final BoundingBox bounds = new BoundingBox();
    private final int seed;

    public ParticleSystem(File file, float particlesPerSecond, int maxParticles, int seed, ParticleEmitter emitter) throws Exception {
        this.file = file;
        this.particlesPerSecond = particlesPerSecond;
        this.maxParticles = maxParticles;
        live = new Particle[maxParticles];
        dead = new Particle[maxParticles];
        temp = new Particle[this.seed = maxParticles];
        random = new Random(seed);
        this.emitter = emitter;
        deadCount = maxParticles;
        for (int i = 0; i != deadCount; i++) {
            dead[i] = new Particle();
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    public Vector3f getPosition() {
        return position;
    }

    public int getMaxParticles() {
        return maxParticles;
    }

    @Override
    public int getTriangleCount() {
        return 0;
    }

    @Override
    public Triangle getTriangle(int i, Triangle triangle) {
        return triangle;
    }

    @Override
    public BoundingBox getBounds() {
        return bounds;
    }

    @Override
    public void update(Game game, Scene scene, Node node) throws Exception {
        seconds += particlesPerSecond * game.getElapsedTime();
        time = game.getTotalTime();
        while (seconds >= 1 && deadCount != 0) {
            Particle particle = dead[--deadCount];

            float c = 0.1f + random.nextFloat() * 0.1f;
            float s = 4 + random.nextFloat() * 8;
            float e = 0.5f + random.nextFloat() ;

            particle.velocityX = 0;
            particle.velocityY = 8 + random.nextFloat() * 8;
            particle.velocityZ = 0;
            particle.startPositionX = -1 + random.nextFloat() * 2;
            particle.startPositionY = random.nextFloat() * 2;
            particle.startPositionZ = -1 + random.nextFloat() * 2;
            particle.startSizeX = s;
            particle.startSizeY = s;
            particle.endSizeX = e;
            particle.endSizeY = e;
            particle.startColorR = c;
            particle.startColorG = c;
            particle.startColorB = c;
            particle.startColorA = 1;
            particle.endColorR = 0;
            particle.endColorG = 0;
            particle.endColorB = 0;
            particle.endColorA = 1;
            particle.lifeSpan = 0.5f + random.nextFloat() * 1.5f;

            emitter.emitParticle(particle, random);

            particle.startTime = time;
            particle.startPositionX += position.x;
            particle.startPositionY += position.y;
            particle.startPositionZ += position.z;
            particle.lifeSpan = (particle.lifeSpan < 0.1f) ? 0.1f : particle.lifeSpan;

            live[liveCount++] = particle;

            seconds -= 1;
        }

        int count = 0;

        for (int i = 0; i != liveCount; i++) {
            Particle particle = live[i];
            float s = time - particle.startTime;
            float t = s / particle.lifeSpan;

            if (t >= 0 && t < 1) {
                particle.positionX = particle.startPositionX + s * particle.velocityX;
                particle.positionY = particle.startPositionY + s * particle.velocityY;
                particle.positionZ = particle.startPositionZ + s * particle.velocityZ;
                particle.colorR = particle.startColorR + t * (particle.endColorR - particle.startColorR);
                particle.colorG = particle.startColorG + t * (particle.endColorG - particle.startColorG);
                particle.colorB = particle.startColorB + t * (particle.endColorB - particle.startColorB);
                particle.colorA = particle.startColorA + t * (particle.endColorA - particle.startColorA);
                particle.sizeX = particle.startSizeX + t * (particle.endSizeX - particle.startSizeX);
                particle.sizeY = particle.startSizeY + t * (particle.endSizeY - particle.startSizeY);

                temp[count++] = particle;
            } else {
                dead[deadCount++] = particle;
            }
        }

        liveCount = count;

        Particle[] tmp = live;

        live = temp;
        temp = tmp;

        matrix.set(scene.getCamera().getView()).mul(node.getModel());

        float rx = matrix.m00();
        float ry = matrix.m10();
        float rz = matrix.m20();
        float ux = matrix.m01();
        float uy = matrix.m11();
        float uz = matrix.m21();

        BoundingBox b = bounds;

        b.clear();

        for (int i = 0; i != liveCount; i++) {
            Particle particle = live[i];
            float px = particle.positionX;
            float py = particle.positionY;
            float pz = particle.positionZ;
            float sx = particle.sizeX / 2;
            float sy = particle.sizeY / 2;

            b.add(
                    px - rx * sx - ux * sy,
                    py - ry * sx - uy * sy,
                    pz - rz * sx - uz * sy
            );
            b.add(
                    px + rx * sx - ux * sy,
                    py + ry * sx - uy * sy,
                    pz + rz * sx - uz * sy
            );
            b.add(
                    px + rx * sx + ux * sy,
                    py + ry * sx + uy * sy,
                    pz + rz * sx + uz * sy
            );
            b.add(
                    px - rx * sx + ux * sy,
                    py - ry * sx + uy * sy,
                    pz - rz * sx + uz * sy
            );
        }
    }

    @Override
    public void render(Game game, Scene scene, Node node) throws Exception {

        if(liveCount == 0) {
            return;
        }

        matrix.set(scene.getCamera().getView()).mul(node.getModel());

        float rx = matrix.m00();
        float ry = matrix.m10();
        float rz = matrix.m20();
        float ux = matrix.m01();
        float uy = matrix.m11();
        float uz = matrix.m21();

        Renderer renderer = game.getRenderer();

        renderer.beginTriangles();

        for (int i = 0; i != liveCount; i++) {
            Particle particle = live[i];
            float px = particle.positionX;
            float py = particle.positionY;
            float pz = particle.positionZ;
            float sx = particle.sizeX / 2;
            float sy = particle.sizeY / 2;
            float cr = particle.colorR;
            float cg = particle.colorG;
            float cb = particle.colorB;
            float ca = particle.colorA;

            renderer.push(
                px - rx * sx - ux * sy,
                py - ry * sx - uy * sy,
                pz - rz * sx - uz * sy, 
                0, 0, 
                0, 0, 
                0, 0, 0, 
                cr, cg, cb, ca
                );

            renderer.push(
                px + rx * sx - ux * sy,
                py + ry * sx - uy * sy,
                pz + rz * sx - uz * sy,
                1, 0,
                0, 0,
                0, 0, 0,
                cr, cg, cb, ca
            );

            renderer.push(
                px + rx * sx + ux * sy,
                py + ry * sx + uy * sy,
                pz + rz * sx + uz * sy,
                1, 1,
                0, 0,
                0, 0, 0,
                cr, cg, cb, ca
            );

            renderer.push(
                px + rx * sx + ux * sy,
                py + ry * sx + uy * sy,
                pz + rz * sx + uz * sy,
                1, 1,
                0, 0,
                0, 0, 0,
                cr, cg, cb, ca
            );

            renderer.push(
                px - rx * sx + ux * sy,
                py - ry * sx + uy * sy,
                pz - rz * sx + uz * sy,
                0, 1,
                0, 0,
                0, 0, 0,
                cr, cg, cb, ca
            );

            renderer.push(
                px - rx * sx - ux * sy,
                py - ry * sx - uy * sy,
                pz - rz * sx - uz * sy, 
                0, 0, 
                0, 0, 
                0, 0, 0, 
                cr, cg, cb, ca
                );
        }
        renderer.endTriangles();
    }

    @Override
    public Renderable newInstance() throws Exception {
        return new ParticleSystem(file, particlesPerSecond, maxParticles, seed, emitter);
    }
}
