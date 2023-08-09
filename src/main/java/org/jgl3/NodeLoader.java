package org.jgl3;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class NodeLoader {
    
    public static interface PipelineGenerator {
        MeshPipeline generate() throws Exception;
    }

    public static Node load(File file, float pixelOffset, PipelineGenerator generator) throws Exception {
        Node root = new Node();
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        Vector<Vector3f> vList = new Vector<>();
        Vector<Vector2f> tList = new Vector<>();
        Vector<Vector3f> nList = new Vector<>();
        Hashtable<String, Texture> textures = new Hashtable<>();
        Hashtable<String, Mesh> meshes = new Hashtable<>();
        String texture = "";
        Game game = Game.getInstance();

        for(String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");

            if(tLine.startsWith("mtllib ")) {
                String[] mLines = new String(IO.readAllBytes(IO.file(file.getParentFile(), tLine.substring(6).trim()))).split("\\n+");
                String name = "";

                for(String mLine : mLines) {
                    String tMLine = mLine.trim();
                    
                    if(tMLine.startsWith("newmtl ")) {
                        name = tMLine.substring(6).trim();
                    } else if(tMLine.startsWith("map_Kd ")) {
                        textures.put(name, game.getAssets().load(IO.file(file.getParentFile(), tMLine.substring(6).trim())));
                    }
                }
            } else if(tLine.startsWith("usemtl ")) {
                Texture t = null;

                texture = tLine.substring(6).trim();
                if(textures.containsKey(texture)) {
                    t = textures.get(texture);
                    texture = IO.fileNameWithOutExtension(t.getFile());
                } else {
                    texture = "";
                }
                if(!meshes.containsKey(texture)) {
                    MeshPipeline pipeline = game.getAssets().manage(generator.generate());
                    Mesh mesh = new Mesh(pipeline);

                    mesh.setTexture(t);
                    meshes.put(texture, mesh);
                    root.addChild(mesh);
                }
            } else if(tLine.startsWith("v ")) {
                vList.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if(tLine.startsWith("vt ")) {
                tList.add(new Vector2f(Float.parseFloat(tokens[1]), 1 - Float.parseFloat(tokens[2])));
            } else if(tLine.startsWith("vn ")) {
                nList.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if(tLine.startsWith("f")) {
                Mesh mesh = null;

                if(!meshes.containsKey(texture)) {
                    MeshPipeline pipeline = game.getAssets().manage(generator.generate());

                    mesh = new Mesh(pipeline);
                    meshes.put(texture, mesh);
                    root.addChild(mesh);
                } else {
                    mesh = meshes.get(texture);
                }

                int[] indices = new int[tokens.length - 1];
                int baseVertex = mesh.getPipeline().getVertexCount();
                Texture tex = mesh.getTexture();

                for(int i = 1; i != tokens.length; i++) {
                    String[] iTokens = tokens[i].split("/");
                    Vector3f v = vList.get(Integer.parseInt(iTokens[0]) - 1);
                    Vector2f t = tList.get(Integer.parseInt(iTokens[1]) - 1);
                    Vector3f n = nList.get(Integer.parseInt(iTokens[2]) - 1);

                    mesh.getPipeline().pushVertex(v.x, v.y, v.z, t.x, t.y, n.x, n.y, n.z);

                    indices[i - 1] = baseVertex + (i - 1);
                }
                if(pixelOffset > 0.0000001 && tex != null) {
                    float x1 = Float.MAX_VALUE;
                    float y1 = Float.MAX_VALUE;
                    float x2 = -x1;
                    float y2 = -y1;
                    float px = 1.0f / tex.getWidth();
                    float py = 1.0f / tex.getHeight();

                    for(int i = 0; i != indices.length; i++) {
                        float s = mesh.getPipeline().getVertexComponent(indices[i], 3);
                        float t = mesh.getPipeline().getVertexComponent(indices[i], 4);

                        x1 = Math.min(x1, s);
                        y1 = Math.min(y1, t);
                        x2 = Math.max(x2, s);
                        y2 = Math.max(y2, t);
                    }

                    x1 += px * pixelOffset;
                    y1 += py * pixelOffset;
                    x2 -= px * pixelOffset;
                    y2 -= py * pixelOffset;

                    for(int i = 0; i != indices.length; i++) {
                        float s = mesh.getPipeline().getVertexComponent(indices[i], 3);
                        float t = mesh.getPipeline().getVertexComponent(indices[i], 4);

                        s = Math.max(x1, s);
                        t = Math.max(y1, t);
                        s = Math.min(x2, s);
                        t = Math.min(y2, t);

                        mesh.getPipeline().setVertexComponent(indices[i], 3, s);
                        mesh.getPipeline().setVertexComponent(indices[i], 4, t);
                    }
                }
                mesh.getPipeline().pushFace(indices);
            }
        }

        root.traverse(null, (scene, node) -> {
            if(node instanceof Mesh) {
                Mesh m = (Mesh)node;

                m.getPipeline().bufferVertices(VertexUsage.STATIC, true);
                m.getPipeline().bufferIndices(VertexUsage.STATIC, true);
            }
            return true;
        });
        if(root.getChildCount() == 1) {
            root = root.getChild(0);
        }
        return root;
    }
}
