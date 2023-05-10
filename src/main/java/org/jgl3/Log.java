package org.jgl3;

public class Log {

    public static int level = 2;
    
    private static Log instance = new Log();

    public Log() {
        instance = this;
    }

    protected void put(Object arg) {
        System.out.println(arg);
    }

    public static void put(int level, Object arg) {
        if(level <= Log.level) {
            instance.put(arg);
        }
    }
}
