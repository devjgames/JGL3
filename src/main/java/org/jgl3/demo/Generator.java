package org.jgl3.demo;

import org.jgl3.Camera;
import org.jgl3.DualTextureMesh;
import org.jgl3.Node;
import org.jgl3.ParticleSystem;
import org.jgl3.NodeLoader.NodeGenerator;
import org.joml.Vector3f;
import org.w3c.dom.Element;

public class Generator implements NodeGenerator {

    @Override
    public Camera generateCamera(Vector3f eye, Vector3f target, Vector3f up) throws Exception {
        PlayerCamera camera = new PlayerCamera();

        camera.getPosition().set(eye);
        camera.getTarget().set(target);
        camera.getUp().set(up);

        return camera;
    }

    @Override
    public Node generateNode(String name) throws Exception {
        System.out.println("generate " + name + " ?");
        if(name.equals("player")) {
            return new Player();
        } else if(name.equals("fireLight")) {
            return new ParticleSystem(10, 100, 1000, (p, r) -> { });
        }
        return null;
    }

    @Override
    public void visitNode(Node node, Element element) throws Exception {
        if(node instanceof DualTextureMesh) {
            if(Boolean.parseBoolean(element.getAttribute("collidable"))) {
                System.out.println(element.getAttribute("name") + " ...");
                Collidables.meshes.add((DualTextureMesh)node);
            }
        }
    }

    @Override
    public void visitNodeComponent(Node node, Element element) throws Exception {
        if(node instanceof Player && element.getTagName().equals("org.j3d.demo.Player")) {
            Player player = (Player)node;

            player.setJumpAmount(Integer.parseInt(element.getAttribute("jumpAmount")));
            player.setOffset(Float.parseFloat(element.getAttribute("offset")));
        } else if(node instanceof ParticleSystem && element.getTagName().equals("org.j3d.demo.FireLight")) {
            node.addChild(new FireLight());
        }
    }
    
}
