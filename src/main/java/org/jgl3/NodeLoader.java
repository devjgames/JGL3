package org.jgl3;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.joml.Vector3f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class NodeLoader {
    
    public static interface NodeGenerator {

        Camera generateCamera(Vector3f eye, Vector3f target, Vector3f up) throws Exception;

        Node generateNode(String name) throws Exception;

        void visitNode(Node node, Element element) throws Exception;

        void visitNodeComponent(Node node, Element element) throws Exception;
    }

    public static Scene load(File file, NodeGenerator generator) throws Exception {
        Scene scene = new Scene();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        Element element = document.getDocumentElement();
        NodeList nodes = element.getChildNodes();
        String[] tokens = element.getAttribute("backgroundColor").split("\\s+");

        scene.getBackgroundColor().set(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
        scene.setLightMapLinear(Boolean.parseBoolean(element.getAttribute("lightMapLinear")));
        for(int i = 0; i != nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);

            if(node instanceof Element) {
                Element nodeElement = (Element)node;

                if(nodeElement.getTagName().equals("camera")) {
                    String[] eye = nodeElement.getAttribute("eye").split("\\s+");
                    String[] target = nodeElement.getAttribute("target").split("\\s+");
                    String[] up = nodeElement.getAttribute("up").split("\\s+");

                    scene.getRoot().addChild(generator.generateCamera(
                        new Vector3f(Float.parseFloat(eye[0]), Float.parseFloat(eye[1]), Float.parseFloat(eye[2])),
                        new Vector3f(Float.parseFloat(target[0]), Float.parseFloat(target[1]), Float.parseFloat(target[2])),
                        new Vector3f(Float.parseFloat(up[0]), Float.parseFloat(up[1]), Float.parseFloat(up[2]))
                    ));
                    ((Camera)scene.getRoot().getChild(0)).activate();
                } else if(nodeElement.getTagName().equals("node")) {
                    scene.getRoot().addChild(load(scene, nodeElement, generator));
                }
            }
        }
        scene.findActiveCamera();
        scene.getRoot().calcModel();
        
        return scene;
    }

    private static Node load(Scene scene, Element element, NodeGenerator generator) throws Exception {
        Game game = Game.getInstance();
        Node node = null;
        NodeList nodes = element.getChildNodes();

        if(Boolean.parseBoolean(element.getAttribute("isLight"))) {
            String[] lightColor = element.getAttribute("lightColor").split("\\s+");
            PointLight light = new PointLight();

            light.getColor().set(Float.parseFloat(lightColor[0]), Float.parseFloat(lightColor[1]), Float.parseFloat(lightColor[2]), Float.parseFloat(lightColor[3]));
            light.setRange(Float.parseFloat(element.getAttribute("lightRadius")));
            node = light;
        } else if(element.hasAttribute("vertices")) {
            DualTextureMesh mesh = new DualTextureMesh();
            String[] vertices = element.getAttribute("vertices").split("\\s+");
            String[] polygons = element.getAttribute("polygons").split("\\s+");

            for(int i = 0; i != vertices.length; i += 15) {
                mesh.getPipeline().pushVertex(
                    Float.parseFloat(vertices[i + 0]),
                    Float.parseFloat(vertices[i + 1]),
                    Float.parseFloat(vertices[i + 2]),
                    Float.parseFloat(vertices[i + 4]),
                    Float.parseFloat(vertices[i + 5]),
                    Float.parseFloat(vertices[i + 6]),
                    Float.parseFloat(vertices[i + 7])
                );
            }
            mesh.getPipeline().bufferVertices(VertexUsage.STATIC, true);
            
            for(String p : polygons) {
                String[] tokens = p.split(":");
                int[] indices = new int[tokens.length];

                for(int i = 0; i != tokens.length; i++) {
                    indices[i] = Integer.parseInt(tokens[i]);
                }
                mesh.getPipeline().pushFace(indices);
            }
            mesh.getPipeline().bufferIndices(VertexUsage.STATIC, true);

            node = mesh;
        } else if(element.hasAttribute("renderable")) {
            KeyFrameMesh mesh = game.getAssets().load(IO.file(element.getAttribute("renderable")));
            String[] sequence = element.getAttribute("sequence").split("\\s+");
            
            mesh.setSequence(Integer.parseInt(sequence[0]), Integer.parseInt(sequence[1]), Integer.parseInt(sequence[2]), Boolean.parseBoolean(sequence[3]));
            node = mesh.newInstance();
        } else {
            node = generator.generateNode(element.getAttribute("name"));
        }

        String[] position = element.getAttribute("position").split("\\s+");
        String[] scale = element.getAttribute("scale").split("\\s+");
        String[] rotation = element.getAttribute("rotation").split("\\s+");
        String[] ambientColor = element.getAttribute("ambientColor").split("\\s+");
        String[] diffuseColor = element.getAttribute("diffuseColor").split("\\s+");
        String[] color = element.getAttribute("color").split("\\s+");

        if(node == null) {
            node = new Node();
        }
        node.setName(element.getAttribute("name"));
        node.setVisible(Boolean.parseBoolean(element.getAttribute("visible")));
        node.getPosition().set(Float.parseFloat(position[0]), Float.parseFloat(position[1]), Float.parseFloat(position[2]));
        node.getScale().set(Float.parseFloat(scale[0]), Float.parseFloat(scale[1]), Float.parseFloat(scale[2]));
        node.getRotation().set(
            Float.parseFloat(rotation[0]), Float.parseFloat(rotation[1]), Float.parseFloat(rotation[2]), Float.parseFloat(rotation[3]),
            Float.parseFloat(rotation[4]), Float.parseFloat(rotation[5]), Float.parseFloat(rotation[6]), Float.parseFloat(rotation[7]),
            Float.parseFloat(rotation[8]), Float.parseFloat(rotation[9]), Float.parseFloat(rotation[10]), Float.parseFloat(rotation[11]),
            Float.parseFloat(rotation[12]), Float.parseFloat(rotation[13]), Float.parseFloat(rotation[14]), Float.parseFloat(rotation[15])
        );
        node.getRotation().transpose();
        
        if(node instanceof KeyFrameMesh) {
            KeyFrameMesh mesh = (KeyFrameMesh)node;

            mesh.getAmbientColor().set(Float.parseFloat(ambientColor[0]), Float.parseFloat(ambientColor[1]), Float.parseFloat(ambientColor[2]), Float.parseFloat(ambientColor[3]));
            mesh.getColor().set(Float.parseFloat(diffuseColor[0]), Float.parseFloat(diffuseColor[1]), Float.parseFloat(diffuseColor[2]), Float.parseFloat(diffuseColor[3]));

            if(element.hasAttribute("texture")) {
                mesh.setTexture(game.getAssets().load(IO.file(element.getAttribute("texture"))));
            }
        } else if(node instanceof ParticleSystem) {
            ParticleSystem particelSystem = (ParticleSystem)node;

            if(element.hasAttribute("texture")) {
                particelSystem.setTexture(game.getAssets().load(IO.file(element.getAttribute("texture"))));
            }
        } else if(node instanceof DualTextureMesh) {
            DualTextureMesh mesh = (DualTextureMesh)node;

            mesh.getColor().set(Float.parseFloat(color[0]), Float.parseFloat(color[1]), Float.parseFloat(color[2]), Float.parseFloat(color[3]));
            if(element.hasAttribute("texture")) {
                mesh.setTexture(game.getAssets().load(IO.file(element.getAttribute("texture"))));
            }
            if(element.hasAttribute("texture2")) {
                mesh.setTexture2(game.getAssets().load(IO.file(element.getAttribute("texture2"))));
                if(scene.isLightMapLinear()) {
                    mesh.getTexture2().toLinear(true);
                }
            }
        }
        if(node instanceof NodeState) {
            NodeState state = (NodeState)node;
            boolean depthWriteEnabled = Boolean.parseBoolean(element.getAttribute("depthWriteEnabled"));
            boolean depthTestEnabled = Boolean.parseBoolean(element.getAttribute("depthTestEnabled"));
            boolean blendEnabled = Boolean.parseBoolean(element.getAttribute("blendEnabled"));
            boolean additiveBlend = Boolean.parseBoolean(element.getAttribute("additiveBlend"));
            String cull = element.getAttribute("cullState");

            if(cull.equals("BACK")) {
                state.getState().setCullState(CullState.BACK);
            } else if(cull.equals("NONE")) {
                state.getState().setCullState(CullState.NONE);
            } else {
                state.getState().setCullState(CullState.FRONT);
            }
            if(blendEnabled) {
                if(additiveBlend) {
                    state.getState().setBlendState(BlendState.ADDITIVE);
                } else {
                    state.getState().setBlendState(BlendState.ALPHA);
                }
            } else {
                state.getState().setBlendState(BlendState.OPAQUE);
            }
            if(depthWriteEnabled && depthTestEnabled) {
                state.getState().setDepthState(DepthState.READWRITE);
            } else if(depthTestEnabled) {
                state.getState().setDepthState(DepthState.READONLY);
            } else {
                state.getState().setDepthState(DepthState.NONE);
            }
        }
        node.setZOrder(Integer.parseInt(element.getAttribute("zOrder")));

        generator.visitNode(node, element);

        for(int i = 0; i != nodes.getLength(); i++) {
            org.w3c.dom.Node child = nodes.item(i);

            if(child instanceof Element) {
                Element childElement = (Element)child;

                if(childElement.getTagName().equals("node")) {
                    node.addChild(load(scene, childElement, generator));
                } else {
                    generator.visitNodeComponent(node, childElement);
                }
            }
        }
        return node;
    }
}
