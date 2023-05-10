package org.jgl3;

import java.util.HashSet;

public class ResourceManager extends Resource {
    
    private final HashSet<Resource> resources = new HashSet<>();

    public final <T extends Resource> T manage(T resource) {
        resources.add(resource);

        return resource;
    }

    public final void unManage(Resource resource) throws Exception {
        resources.remove(resource);
        resource.destroy();
    }

    public void clear() throws Exception {
        for(Resource resource : resources) {
            resource.destroy();
        }
        resources.clear();
    }

    @Override
    public void destroy() throws Exception {
        clear();
        
        super.destroy();
    }
}
