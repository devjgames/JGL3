package org.jgl3;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

public final class AssetManager extends ResourceManager {

    private final Hashtable<String, Object> assets = new Hashtable<>();
    private final Hashtable<String, AssetLoader> assetLoaders = new Hashtable<>();

    public AssetManager() {
        registerAssetLoader(".png", new Texture.Loader());
        registerAssetLoader(".jpg", new Texture.Loader());
        registerAssetLoader(".wav", new Sound.SoundLoader());
        registerAssetLoader(".md2", new KeyFrameMeshLoader());
    }
    
    public void registerAssetLoader(String extension, AssetLoader assetLoader) {
        assetLoaders.put(extension, assetLoader);
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T load(File file) throws Exception {
        String key = file.getPath();

        if(!assets.containsKey(key)) {
            Log.put(1, "Loading assets - '" + key + "' ...");

            assets.put(key, assetLoaders.get(IO.extension(file)).load(file));
        }
        return (T)assets.get(key);
    }

    public void unLoad(File file) throws Exception {
        String key = file.getPath();

        if(assets.containsKey(key)) {
            Object asset = assets.get(key);

            if(asset instanceof Resource) {
                ((Resource)asset).destroy();
            }
            assets.remove(key);
        }
    }

    @Override
    public void clear() throws Exception {
        Enumeration<String> keys = assets.keys();

        while(keys.hasMoreElements()) {
            Object asset = assets.get(keys.nextElement());

            if(asset instanceof Resource) {
                ((Resource)asset).destroy();
            }
        }
        assets.clear();
        
        super.clear();
    }
}
