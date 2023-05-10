package org.jgl3;

import java.nio.ByteBuffer;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;

public final class SoundEngine extends Resource {
    
    private long device;
    private long context;
    private boolean error = false;

    public SoundEngine() throws Exception {
        device = ALC10.alcOpenDevice((ByteBuffer)null);
        if(device == 0) {
            throw new Exception("Failed to create sound engine!");
        }

        ALCCapabilities caps = ALC.createCapabilities(device);

        context = ALC10.alcCreateContext(device, new int[] { 0 });
        if(context == 0) {
            ALC10.alcCloseDevice(device);
            throw new Exception("Failed to create sound engine!");
        }
        ALC10.alcMakeContextCurrent(context);
        AL.createCapabilities(caps);

        checkError("SoundEngine()");
    }

    public long getDevice() {
        return device;
    }

    public long getContext() {
        return context;
    }

    public void checkError(String tag) {
        if(!error) {
            int code = AL10.alGetError();

            if(code != AL10.AL_NO_ERROR) {
                Log.put(0, tag + ":" + code);
                error = true;
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        ALC11.alcMakeContextCurrent(0);
        ALC11.alcDestroyContext(context);
        ALC11.alcCloseDevice(device);
        super.destroy();
    }
}
