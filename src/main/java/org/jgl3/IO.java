package org.jgl3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class IO {

    public static byte[] readAllBytes(InputStream input) throws IOException {
        ByteArrayOutputStream output = null;
        byte[] bytes = null;
        byte[] b = new byte[1000];
        int n;

        try {
            output = new ByteArrayOutputStream(1000);
            while((n = input.read(b)) >= 0) {
                if(n > 0) {
                    output.write(b, 0, n);
                }
            }
            bytes = output.toByteArray();
        } finally {
            if(output != null) {
                output.close();
            }
        }
        return bytes;
    }

    public static byte[] readAllBytes(Class<?> cls, String name) throws IOException {
        InputStream input = null;
        byte[] bytes = null;

        try {
            bytes = readAllBytes(input = cls.getResourceAsStream(name));
        } finally {
            if(input != null) {
                input.close();
            }
        }
        return bytes;
    }

    public static byte[] readAllBytes(File file) throws IOException {
        InputStream input = null;
        byte[] bytes = null;

        try {
            bytes = readAllBytes(input = new FileInputStream(file));
        } finally {
            if(input != null) {
                input.close();
            }
        }
        return bytes;
    }

    public static void writeAllBytes(byte[] bytes, File file) throws IOException {
        FileOutputStream output = null;

        try {
            output = new FileOutputStream(file);
            output.write(bytes);
        } finally {
            if(output != null) {
                output.close();
            }
        }
    }

    public static File file(String path) {
        return new File(path.replace('/', File.separatorChar).replace('\\', File.separatorChar));
    }

    public static File file(File file, String path) {
        return new File(file, file(path).getPath());
    }
    
    public static String extension(File file) {
        String name = file.getName();
        String extension = "";
        int i = name.lastIndexOf('.');

        if(i >= 0) {
            extension = name.substring(i);
        }
        return extension;
    }

    public static String fileNameWithOutExtension(File file) {
        String name = file.getName();
        int i = name.lastIndexOf('.');

        if(i >= 0) {
            name = name.substring(0, i);
        }
        return name;
    }
}
