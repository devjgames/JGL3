package org.jgl3;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;
import java.util.Vector;

public final class ParticleSystem extends NodeState {

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
    private final VertexColorPipeline pipeline;
    private Texture texture = null;

    public ParticleSystem(float particlesPerSecond, int maxParticles, int seed, ParticleEmitter emitter) throws Exception {
        this.particlesPerSecond = particlesPerSecond;
        this.maxParticles = maxParticles;
        live = new Particle[maxParticles];
        dead = new Particle[maxParticles];
        temp = new Particle[maxParticles];
        random = new Random(seed);
        this.emitter = emitter;
        deadCount = maxParticles;

        for(int i = 0; i != maxParticles; i++) {
            dead[i] = new Particle();
        }

        getState().setDepthState(DepthState.READONLY);
        getState().setBlendState(BlendState.ADDITIVE);

        pipeline = Game.getInstance().getAssets().manage(new VertexColorPipeline());
        for(int i = 0, j = 0; i != maxParticles; i++, j += 4) {
            pipeline.pushFace(j, j + 1, j + 2, j + 3);
        }
        pipeline.bufferIndices(VertexUsage.STATIC, true);
    }

    public ParticleEmitter getEmitter() {
        return emitter;
    }

    public Vector3f getEmitPosition() {
        return position;
    }

    public int getMaxParticles() {
        return maxParticles;
    }

    public VertexColorPipeline getPipeline() {
        return pipeline;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    @Override
    public void update(Scene scene) throws Exception {
        Game game = Game.getInstance();

        seconds += particlesPerSecond * game.getElapsedTime();
        time = game.getTotalTime();
        while (seconds >= 1 && deadCount != 0) {
            Particle particle = dead[--deadCount];

            float c = 0.5f + random.nextFloat() * 0.5f;
            float s = 35 + random.nextFloat() * 35;
            float e = 0.5f + random.nextFloat();

            particle.velocityX = -8 + random.nextFloat() * 16;
            particle.velocityY = -8 + random.nextFloat() * 16;
            particle.velocityZ = -8 + random.nextFloat() * 16;
            particle.startPositionX = -1 + random.nextFloat() * 2;
            particle.startPositionY = -1 + random.nextFloat() * 2;
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

        matrix.set(scene.getCamera().getModel()).mul(getModel());

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
    public boolean isRenderable() {
        return true;
    }

    @Override
    public State render(Scene scene, Vector<Light> lights, Camera camera, State lastState) throws Exception   {

        if(liveCount == 0) {
            return lastState;
        }

        matrix.set(scene.getCamera().getModel()).mul(getModel());

        float rx = matrix.m00();
        float ry = matrix.m10();
        float rz = matrix.m20();
        float ux = matrix.m01();
        float uy = matrix.m11();
        float uz = matrix.m21();
        float fx = matrix.m02();
        float fy = matrix.m12();
        float fz = matrix.m22();
        float lr = Vector3f.length(rx, ry, rz);
        float lu = Vector3f.length(ux, uy, uz);
        float lf = Vector3f.length(fx, fy, fz);

        rx /= lr;
        ry /= lr;
        rz /= lr;

        ux /= lu;
        uy /= lu;
        uz /= lu;

        fx /= lf;
        fy /= lf;
        fz /= lf;

        getState().bind(lastState);

        pipeline.clearVertices();

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
            float x1 = px - rx * sx - ux * sy;
            float y1 = py - ry * sx - uy * sy;
            float z1 = pz - rz * sx - uz * sy;
            float x2 = px + rx * sx - ux * sy;
            float y2 = py + ry * sx - uy * sy;
            float z2 = pz + rz * sx - uz * sy;
            float x3 = px + rx * sx + ux * sy;
            float y3 = py + ry * sx + uy * sy;
            float z3 = pz + rz * sx + uz * sy;
            float x4 = px - rx * sx + ux * sy;
            float y4 = py - ry * sx + uy * sy;
            float z4 = pz - rz * sx + uz * sy;

            pipeline.pushVertex(
                x1, y1, z1,
                0, 0, 
                cr, cg, cb, ca
                );

            pipeline.pushVertex(
                x2, y2, z2,
                1, 0,
                cr, cg, cb, ca
            );

            pipeline.pushVertex(
                x3, y3, z3,
                1, 1,
                cr, cg, cb, ca
            );

            pipeline.pushVertex(
                x4, y4, z4,
                0, 1,
                cr, cg, cb, ca
            );
        }
        pipeline.bufferVertices(VertexUsage.DYNAMIC, false);

        pipeline.begin();
        pipeline.setTexture(texture);
        pipeline.setTransform(camera, this);
        pipeline.render(liveCount * 6);
        pipeline.end();

        return getState();
    }
}
