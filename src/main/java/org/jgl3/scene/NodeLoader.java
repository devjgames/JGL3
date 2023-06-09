package org.jgl3.scene;

import java.io.File;
import java.util.Vector;

import org.jgl3.AssetManager;
import org.jgl3.Game;
import org.jgl3.IO;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Hashtable;

public final class NodeLoader {

    private static Hashtable<String, Loader> loaders = new Hashtable<>();

    public static interface Loader {
        Node load(File file) throws Exception;
    }

    public static Vector<String> extensions() {
        return new Vector<String>(loaders.keySet());
    }

    public static void registerLoader(String extension, Loader loader) {
        loaders.put(extension, loader);
    }


    public static Node load(File file) throws Exception {
        return loaders.get(IO.extension(file)).load(file);
    }

    public static class OBJLoader implements Loader {
        public Node load(File file) throws Exception {
            AssetManager assets = Game.getInstance().getAssets();
            String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
            Vector<Vector3f> vList = new Vector<>();
            Vector<Vector2f> tList = new Vector<>();
            Vector<Vector3f> nList = new Vector<>();
            Hashtable<String, String> materials = new Hashtable<>();
            String material = "";
            Node root = new Node();

            for (String line : lines) {
                String tLine = line.trim();
                String[] tokens = tLine.split("\\s+");
                if(tLine.startsWith("mtllib ")) {
                    loadMaterials(new File(file.getParent(), tLine.substring(6).trim()), materials);
                } else if(tLine.startsWith("o ")) {
                    Node node = new Node();

                    node.setName(tLine.substring(1).trim());
                    root.addChild(node);
                } else if(tLine.startsWith("usemtl ")) {
                    Node node;

                    if(root.getChildCount() == 0) {
                        root.addChild(new Node());
                    }
                    material = materials.get(tLine.substring(6).trim());
                    node = root.getLastChild();
                    node.addChild(new Node());
                    node = node.getLastChild();
                    node.setTexture(assets.load(IO.file(material)));
                    node.setTextureLinear(true);
                    node.setName(IO.fileNameWithOutExtension(IO.file(material)));
                } else if (tLine.startsWith("v ")) {
                    Vector3f v = new Vector3f(
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]),
                        Float.parseFloat(tokens[3])
                    );
                    vList.add(v);
                } else if (tLine.startsWith("vt ")) {
                    Vector2f v = new Vector2f(
                        Float.parseFloat(tokens[1]),
                        1 - Float.parseFloat(tokens[2])
                    );
                    tList.add(v);
                } else if (tLine.startsWith("vn ")) {
                    Vector3f v = new Vector3f(
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]),
                        Float.parseFloat(tokens[3])
                    );
                    nList.add(v);
                } else if (tLine.startsWith("f ")) {
                    Node node;

                    if(root.getChildCount() == 0) {
                        root.addChild(new Node());
                    }
                    node = root.getLastChild();
                    if(node.getChildCount() == 0) {
                        node.addChild(new Node());
                    }
                    node = node.getLastChild();

                    int bV = node.getVertexCount();
                    int[] indices = new int[tokens.length - 1];

                    for (int i = 1; i != tokens.length; i++) {
                        String[] iTokens = tokens[i].split("[/]+");
                        int vI = Integer.parseInt(iTokens[0]) - 1;
                        int tI = Integer.parseInt(iTokens[1]) - 1;
                        int nI = Integer.parseInt(iTokens[2]) - 1;
                        Vector3f v = vList.get(vI);
                        Vector2f t = tList.get(tI);
                        Vector3f n = nList.get(nI);

                        node.push(
                            v.x, v.y, v.z,
                            t.x, t.y,
                            0, 0,
                            n.x, n.y, n.z,
                            1, 1, 1, 1
                        );

                        indices[i - 1] = bV + i - 1;
                    }
                    node.push(indices);
                }
            }
            root.setName(IO.fileNameWithOutExtension(file));
            
            return root;
        }

        private void loadMaterials(File file, Hashtable<String, String> materials) throws Exception {
            String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
            String name = null;

            for(String line : lines) {
                String tLine = line.trim();
                if(tLine.startsWith("newmtl ")) {
                    name = tLine.substring(6).trim();
                } else if(tLine.startsWith("map_Kd ")) {
                    materials.put(name, new File(file.getParent(), tLine.substring(6).trim()).getPath());
                }
            }
        }
    }
}
