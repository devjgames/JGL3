package org.jgl3.scene;

import java.io.File;
import java.util.Vector;

import org.jgl3.AssetLoader;
import org.jgl3.BinReader;
import org.jgl3.Game;
import org.jgl3.IO;

public final class KeyFrameMeshLoader implements AssetLoader {

    public static void registerAssetLoader() {
        Game.getInstance().getAssets().registerAssetLoader(".md2", Scene.ASSET_TAG, new KeyFrameMeshLoader());
    }

    @Override
    public final Object load(File file) throws Exception {
        Vector<KeyFrame> keyFrames = new Vector<>();
        BinReader reader = new BinReader(IO.readAllBytes(file));
        Header header = new Header(reader);
        Frame[] frames = new Frame[header.numFrames];
        Triangle[] tris = new Triangle[header.numTris];
        TexCoord[] texCoords = new TexCoord[header.numTexCoords];
        float[][] normals = cloneNormals();

        for(int i = 0; i != header.numFrames; i++) {
            reader.setPosition(header.offFrames + i * header.frameSize);
            frames[i] = new Frame(header, reader);
        }

        reader.setPosition(header.offTris);
        for(int i = 0; i != header.numTris; i++) {
            tris[i] = new Triangle(reader);
        }

        reader.setPosition(header.offTexCoords);
        for(int i = 0; i != header.numTexCoords; i++) {
            texCoords[i] = new TexCoord(reader);
        }

        for(Frame frame : frames) {
            float[] vertices = new float[header.numTris * 3 * 8];

            for(int i = 0, v = 0; i != header.numTris; i++) {
                for(int j = 2; j != -1; j--) {
                    vertices[v++] = frame.vertices[tris[i].vIndices[j]].vertex[0] * frame.scale[0] + frame.translation[0];
                    vertices[v++] = frame.vertices[tris[i].vIndices[j]].vertex[1] * frame.scale[1] + frame.translation[1];
                    vertices[v++] = frame.vertices[tris[i].vIndices[j]].vertex[2] * frame.scale[2] + frame.translation[2];
                    vertices[v++] = texCoords[tris[i].tIndices[j]].s / (float)header.skinW;
                    vertices[v++] = texCoords[tris[i].tIndices[j]].t / (float)header.skinH;
                    vertices[v++] = normals[frame.vertices[tris[i].vIndices[j]].normal][0];
                    vertices[v++] = normals[frame.vertices[tris[i].vIndices[j]].normal][1];
                    vertices[v++] = normals[frame.vertices[tris[i].vIndices[j]].normal][2];
                }
            }
            keyFrames.add(new KeyFrame(frame.name, vertices));
        }

        return new KeyFrameMesh(file, keyFrames);
    }
    
    public static final class Header {
        public final int id;
        public final int version;
        public final int skinW;
        public final int skinH;
        public final int frameSize;
        public final int numSkins;
        public final int numVertices;
        public final int numTexCoords;
        public final int numTris;
        public final int numGLCommands;
        public final int numFrames;
        public final int offSkins;
        public final int offTexCoords;
        public final int offTris;
        public final int offFrames;
        public final int offGLCommands;
        public final int offEnd;

        public Header(BinReader reader) {
            id = reader.readInt();
            version = reader.readInt();
            skinW = reader.readInt();
            skinH = reader.readInt();
            frameSize = reader.readInt();
            numSkins = reader.readInt();
            numVertices = reader.readInt();
            numTexCoords = reader.readInt();
            numTris = reader.readInt();
            numGLCommands = reader.readInt();
            numFrames = reader.readInt();
            offSkins = reader.readInt();
            offTexCoords = reader.readInt();
            offTris = reader.readInt();
            offFrames = reader.readInt();
            offGLCommands = reader.readInt();
            offEnd = reader.readInt();
        }
    }

    public static final class Vertex {
        public final int[] vertex = new int[3];
        public final int normal;

        
        public Vertex(BinReader reader) {
            vertex[0] = reader.readByte();
            vertex[1] = reader.readByte();
            vertex[2] = reader.readByte();
            normal = reader.readByte();
        }
    }

    public static final class Frame {
        public final float[] scale = new float[3];
        public final float[] translation = new float[3];
        public final String name;
        public final Vertex[] vertices;

        public Frame(Header header, BinReader reader) {
            scale[0] = reader.readFloat();
            scale[1] = reader.readFloat();
            scale[2] = reader.readFloat();
            translation[0] = reader.readFloat();
            translation[1] = reader.readFloat();
            translation[2] = reader.readFloat();
            name = reader.readString(16);
            vertices = new Vertex[header.numVertices];
            for(int i = 0; i != header.numVertices; i++) {
                vertices[i] = new Vertex(reader);
            }
        }
    }

    public static final class Triangle {
        public final int[] vIndices = new int[3];
        public final int[] tIndices = new int[3];

        public Triangle(BinReader reader) {
            vIndices[0] = reader.readShort();
            vIndices[1] = reader.readShort();
            vIndices[2] = reader.readShort();
            tIndices[0] = reader.readShort();
            tIndices[1] = reader.readShort();
            tIndices[2] = reader.readShort();
        }
    }

    public static final class TexCoord {
        public final int s;
        public final int t;

        public TexCoord(BinReader reader) {
            s = reader.readShort();
            t = reader.readShort();
        }
    }

    private final static float[][] normals = new float[][] {
        {-0.525731f, 0.000000f, 0.850651f},
        {-0.442863f, 0.238856f, 0.864188f},
        {-0.295242f, 0.000000f, 0.955423f},
        {-0.309017f, 0.500000f, 0.809017f},
        {-0.162460f, 0.262866f, 0.951056f},
        {0.000000f, 0.000000f, 1.000000f},
        {0.000000f, 0.850651f, 0.525731f},
        {-0.147621f, 0.716567f, 0.681718f},
        {0.147621f, 0.716567f, 0.681718f},
        {0.000000f, 0.525731f, 0.850651f},
        {0.309017f, 0.500000f, 0.809017f},
        {0.525731f, 0.000000f, 0.850651f},
        {0.295242f, 0.000000f, 0.955423f},
        {0.442863f, 0.238856f, 0.864188f},
        {0.162460f, 0.262866f, 0.951056f},
        {-0.681718f, 0.147621f, 0.716567f},
        {-0.809017f, 0.309017f, 0.500000f},
        {-0.587785f, 0.425325f, 0.688191f},
        {-0.850651f, 0.525731f, 0.000000f},
        {-0.864188f, 0.442863f, 0.238856f},
        {-0.716567f, 0.681718f, 0.147621f},
        {-0.688191f, 0.587785f, 0.425325f},
        {-0.500000f, 0.809017f, 0.309017f},
        {-0.238856f, 0.864188f, 0.442863f},
        {-0.425325f, 0.688191f, 0.587785f},
        {-0.716567f, 0.681718f, -0.147621f},
        {-0.500000f, 0.809017f, -0.309017f},
        {-0.525731f, 0.850651f, 0.000000f},
        {0.000000f, 0.850651f, -0.525731f},
        {-0.238856f, 0.864188f, -0.442863f},
        {0.000000f, 0.955423f, -0.295242f},
        {-0.262866f, 0.951056f, -0.162460f},
        {0.000000f, 1.000000f, 0.000000f},
        {0.000000f, 0.955423f, 0.295242f},
        {-0.262866f, 0.951056f, 0.162460f},
        {0.238856f, 0.864188f, 0.442863f},
        {0.262866f, 0.951056f, 0.162460f},
        {0.500000f, 0.809017f, 0.309017f},
        {0.238856f, 0.864188f, -0.442863f},
        {0.262866f, 0.951056f, -0.162460f},
        {0.500000f, 0.809017f, -0.309017f},
        {0.850651f, 0.525731f, 0.000000f},
        {0.716567f, 0.681718f, 0.147621f},
        {0.716567f, 0.681718f, -0.147621f},
        {0.525731f, 0.850651f, 0.000000f},
        {0.425325f, 0.688191f, 0.587785f},
        {0.864188f, 0.442863f, 0.238856f},
        {0.688191f, 0.587785f, 0.425325f},
        {0.809017f, 0.309017f, 0.500000f},
        {0.681718f, 0.147621f, 0.716567f},
        {0.587785f, 0.425325f, 0.688191f},
        {0.955423f, 0.295242f, 0.000000f},
        {1.000000f, 0.000000f, 0.000000f},
        {0.951056f, 0.162460f, 0.262866f},
        {0.850651f, -0.525731f, 0.000000f},
        {0.955423f, -0.295242f, 0.000000f},
        {0.864188f, -0.442863f, 0.238856f},
        {0.951056f, -0.162460f, 0.262866f},
        {0.809017f, -0.309017f, 0.500000f},
        {0.681718f, -0.147621f, 0.716567f},
        {0.850651f, 0.000000f, 0.525731f},
        {0.864188f, 0.442863f, -0.238856f},
        {0.809017f, 0.309017f, -0.500000f},
        {0.951056f, 0.162460f, -0.262866f},
        {0.525731f, 0.000000f, -0.850651f},
        {0.681718f, 0.147621f, -0.716567f},
        {0.681718f, -0.147621f, -0.716567f},
        {0.850651f, 0.000000f, -0.525731f},
        {0.809017f, -0.309017f, -0.500000f},
        {0.864188f, -0.442863f, -0.238856f},
        {0.951056f, -0.162460f, -0.262866f},
        {0.147621f, 0.716567f, -0.681718f},
        {0.309017f, 0.500000f, -0.809017f},
        {0.425325f, 0.688191f, -0.587785f},
        {0.442863f, 0.238856f, -0.864188f},
        {0.587785f, 0.425325f, -0.688191f},
        {0.688191f, 0.587785f, -0.425325f},
        {-0.147621f, 0.716567f, -0.681718f},
        {-0.309017f, 0.500000f, -0.809017f},
        {0.000000f, 0.525731f, -0.850651f},
        {-0.525731f, 0.000000f, -0.850651f},
        {-0.442863f, 0.238856f, -0.864188f},
        {-0.295242f, 0.000000f, -0.955423f},
        {-0.162460f, 0.262866f, -0.951056f},
        {0.000000f, 0.000000f, -1.000000f},
        {0.295242f, 0.000000f, -0.955423f},
        {0.162460f, 0.262866f, -0.951056f},
        {-0.442863f, -0.238856f, -0.864188f},
        {-0.309017f, -0.500000f, -0.809017f},
        {-0.162460f, -0.262866f, -0.951056f},
        {0.000000f, -0.850651f, -0.525731f},
        {-0.147621f, -0.716567f, -0.681718f},
        {0.147621f, -0.716567f, -0.681718f},
        {0.000000f, -0.525731f, -0.850651f},
        {0.309017f, -0.500000f, -0.809017f},
        {0.442863f, -0.238856f, -0.864188f},
        {0.162460f, -0.262866f, -0.951056f},
        {0.238856f, -0.864188f, -0.442863f},
        {0.500000f, -0.809017f, -0.309017f},
        {0.425325f, -0.688191f, -0.587785f},
        {0.716567f, -0.681718f, -0.147621f},
        {0.688191f, -0.587785f, -0.425325f},
        {0.587785f, -0.425325f, -0.688191f},
        {0.000000f, -0.955423f, -0.295242f},
        {0.000000f, -1.000000f, 0.000000f},
        {0.262866f, -0.951056f, -0.162460f},
        {0.000000f, -0.850651f, 0.525731f},
        {0.000000f, -0.955423f, 0.295242f},
        {0.238856f, -0.864188f, 0.442863f},
        {0.262866f, -0.951056f, 0.162460f},
        {0.500000f, -0.809017f, 0.309017f},
        {0.716567f, -0.681718f, 0.147621f},
        {0.525731f, -0.850651f, 0.000000f},
        {-0.238856f, -0.864188f, -0.442863f},
        {-0.500000f, -0.809017f, -0.309017f},
        {-0.262866f, -0.951056f, -0.162460f},
        {-0.850651f, -0.525731f, 0.000000f},
        {-0.716567f, -0.681718f, -0.147621f},
        {-0.716567f, -0.681718f, 0.147621f},
        {-0.525731f, -0.850651f, 0.000000f},
        {-0.500000f, -0.809017f, 0.309017f},
        {-0.238856f, -0.864188f, 0.442863f},
        {-0.262866f, -0.951056f, 0.162460f},
        {-0.864188f, -0.442863f, 0.238856f},
        {-0.809017f, -0.309017f, 0.500000f},
        {-0.688191f, -0.587785f, 0.425325f},
        {-0.681718f, -0.147621f, 0.716567f},
        {-0.442863f, -0.238856f, 0.864188f},
        {-0.587785f, -0.425325f, 0.688191f},
        {-0.309017f, -0.500000f, 0.809017f},
        {-0.147621f, -0.716567f, 0.681718f},
        {-0.425325f, -0.688191f, 0.587785f},
        {-0.162460f, -0.262866f, 0.951056f},
        {0.442863f, -0.238856f, 0.864188f},
        {0.162460f, -0.262866f, 0.951056f},
        {0.309017f, -0.500000f, 0.809017f},
        {0.147621f, -0.716567f, 0.681718f},
        {0.000000f, -0.525731f, 0.850651f},
        {0.425325f, -0.688191f, 0.587785f},
        {0.587785f, -0.425325f, 0.688191f},
        {0.688191f, -0.587785f, 0.425325f},
        {-0.955423f, 0.295242f, 0.000000f},
        {-0.951056f, 0.162460f, 0.262866f},
        {-1.000000f, 0.000000f, 0.000000f},
        {-0.850651f, 0.000000f, 0.525731f},
        {-0.955423f, -0.295242f, 0.000000f},
        {-0.951056f, -0.162460f, 0.262866f},
        {-0.864188f, 0.442863f, -0.238856f},
        {-0.951056f, 0.162460f, -0.262866f},
        {-0.809017f, 0.309017f, -0.500000f},
        {-0.864188f, -0.442863f, -0.238856f},
        {-0.951056f, -0.162460f, -0.262866f},
        {-0.809017f, -0.309017f, -0.500000f},
        {-0.681718f, 0.147621f, -0.716567f},
        {-0.681718f, -0.147621f, -0.716567f},
        {-0.850651f, 0.000000f, -0.525731f},
        {-0.688191f, 0.587785f, -0.425325f},
        {-0.587785f, 0.425325f, -0.688191f},
        {-0.425325f, 0.688191f, -0.587785f},
        {-0.425325f, -0.688191f, -0.587785f},
        {-0.587785f, -0.425325f, -0.688191f},
        {-0.688191f, -0.587785f, -0.425325f}
    };

    public static float[][] cloneNormals() {
        return normals.clone();
    }

    private static final Object[][] SEQUENCES  = new Object[][] {
        { "STAND",  0, 39, 9, true }, 
        { "RUN", 40, 45, 10, true },
        { "ATTACK", 46, 53,10, true }, 
        { "PAIN_A", 54, 57, 7, true }, 
        { "PAIN_B", 58, 61, 7, true }, 
        { "PAIN_C", 62, 65, 7, true }, 
        { "JUMP", 66, 71, 7, true }, 
        { "FLIP", 72, 83, 7, true }, 
        { "SALUTE", 84, 94, 7, true }, 
        { "FALLBACK", 95,111, 10, true }, 
        { "WAVE", 112, 122, 7, true }, 
        { "POINT", 123, 134, 6, true },
        { "CROUCH_STAND", 135, 153, 10, true }, 
        { "CROUCH_WALK", 154, 159, 7, true }, 
        { "CROUCH_ATTACK", 160, 168, 10, true }, 
        { "CROUCH_PAIN", 169, 172, 7, true },
        { "CROUCH_DEATH", 173, 177, 5, false }, 
        { "DEATH_FALLBACK", 178, 183, 7, false }, 
        { "DEATH_FALLFORWARD", 184, 189, 7, false },
        { "DEATH_FALLBACKSLOW", 190, 197, 7, false }
    };

    public static Object[][] cloneSequences() {
        return SEQUENCES.clone();
    }
}
