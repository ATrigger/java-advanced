package ru.ifmo.ctddev.kamenev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Created by kamenev on 09.03.16.
 */
public class Implementor implements Impler, JarImpler {

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.err.println("Not enough arguments! Must be at least 2");
            return;
        }
        if (args[0].equals("-jar") && args.length >= 3) {
            try {
                Class<?> c = Class.forName(args[1]);
                Implementor impl = new Implementor();
                impl.implementJar(c, Paths.get(args[2]));
            } catch (ClassNotFoundException e) {
                System.err.println("Cannot find class: " + args[1]);
            } catch (ImplerException e) {
                System.err.println("Cannot implement class: " + args[1] + " cause: " + e.getMessage());
            }
        } else if (!args[0].equals("-jar")) {
            try {
                Class<?> c = Class.forName(args[0]);
                Implementor impl = new Implementor();
                impl.implement(c, Paths.get(args[1]));
            } catch (ClassNotFoundException e) {
                System.err.println("Cannot find class: " + args[0]);
            } catch (ImplerException e) {
                System.err.println("Cannot implement class: " + args[0] + " cause: " + e.getMessage());
            }
        } else {
            System.err.println("Usage: \"-jar\" <ClassName> <JarName>");
            System.err.println("Usage: <ClassName> <Directory>");
        }
    }

    private Path resolvePackage(Class<?> aClass, Path path) {
        if (aClass.getPackage() == null) return path;
        else return path.resolve(aClass.getPackage().getName().replace(".", File.separator));
    }

    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        Objects.requireNonNull(aClass);
        Objects.requireNonNull(path);
        int mods = aClass.getModifiers();
        if (Modifier.isFinal(mods)) {
            throw new ImplerException("Cannot override final class : " + aClass.getName());
        }
        int count = aClass.getConstructors().length;
        if (count == 0) {
            for (Constructor c : aClass.getDeclaredConstructors()) {
                if (!Modifier.isPrivate(c.getModifiers())) {
                    count++;
                    break;
                }
            }
        }
        if (!aClass.isInterface() && count == 0) {
            throw new ImplerException("No constructor available : " + aClass.getName());
        }
        try {
            path = resolvePackage(aClass, path);
            Files.createDirectories(path);
        } catch (Exception e) {
            throw new ImplerException("Cannot create directories according to package name");
        }
        try (BufferedWriter out = Files.newBufferedWriter(
                path.resolve(aClass.getSimpleName() + "Impl.java"),
                Charset.defaultCharset())) {
            printClass(aClass, out);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ImplerException("Cannot write output");
        }

    }

    private static void printClass(Class<?> clazz, BufferedWriter to) throws IOException {
        if (clazz.getPackage() != null) {
            to.write("package " + clazz.getPackage().getName() + ";");
            to.newLine();
        }
        to.write("public class " + clazz.getSimpleName() + "Impl");
        Class[] ints = clazz.getInterfaces();
        if (!clazz.isInterface()) {
            to.write(" extends " + clazz.getSimpleName());
        } else {
            to.write(" implements " + clazz.getSimpleName());
        }
        to.write(" {");
        to.newLine();
        for (Constructor constructor : clazz.getDeclaredConstructors()) {
            if (!Modifier.isFinal(constructor.getModifiers()) && !Modifier.isPrivate(constructor.getModifiers())) {
                printConstructor(constructor, to, clazz.getSimpleName());
            }
        }
        Map<String, Method> hashMap = new HashMap<>();
        walkAncestors(clazz, hashMap);
        for (Method method : clazz.getMethods()) {
            int mods = method.getModifiers();
            if (Modifier.isAbstract(mods)) {
                hashMap.put(getHash(method), method);
            }
        }
        for (String s : hashMap.keySet()) {
            printMethod(hashMap.get(s), to);
        }
        to.write("}");
        to.newLine();
    }

    private static String getHash(Method method) {
        return method.getName() + Arrays.toString(method.getParameterTypes());
    }

    private static void printConstructor(Constructor c, BufferedWriter to, String className) throws IOException {
        to.write("    " +
                Modifier.toString(c.getModifiers() &
                        ~Modifier.ABSTRACT &
                        Modifier.constructorModifiers())
                + " " + className + "Impl(");
        printParameters(c.getParameters(), to);
        to.write(")");
        printExceptions(c.getExceptionTypes(), to);
        to.write("{");
        to.newLine();
        to.write("        super(");
        for (int i = 0; i < c.getParameterCount(); i++) {
            to.write("arg" + i);
            if (i + 1 < c.getParameterCount()) to.write(",");
        }
        to.write(");");
        to.newLine();
        to.write("    }");
        to.newLine();
    }

    private static void printMethod(Method m, BufferedWriter to) throws IOException {
        to.write("    " +
                Modifier.toString(m.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT) +
                " " +
                m.getReturnType().getCanonicalName() +
                " " + m.getName() + " ("
        );
        printParameters(m.getParameters(), to);
        to.write(")");
        printExceptions(m.getExceptionTypes(), to);
        if (m.getReturnType().equals(void.class)) {
            to.write("{}");
            to.newLine();
        } else {
            to.write("{");
            to.newLine();
            to.write("        return ");
            if (m.getReturnType().isPrimitive()) {
                if (m.getReturnType().equals(boolean.class)) {
                    to.write("false");
                } else {
                    to.write("0");
                }
            } else {
                to.write("null");
            }
            to.write(";");
            to.newLine();
            to.write("    }");
            to.newLine();
        }
    }

    private static void walkAncestors(Class c, Map<String, Method> map) {
        for (Method method : c.getDeclaredMethods()) {
            int mods = method.getModifiers();
            if (Modifier.isAbstract(mods) && !Modifier.isPrivate(mods)) {
                map.put(getHash(method), method);
            }
        }
        if (c.getSuperclass() != null) {
            walkAncestors(c.getSuperclass(), map);
        }
    }

    private static void printParameters(Parameter[] parameters, BufferedWriter out) throws IOException {
        for (int i = 0; i < parameters.length; ++i) {
            out.write(parameters[i].getType().getCanonicalName() + " arg" + i + (i + 1 < parameters.length ? ", " : ""));
        }
    }

    private static void printExceptions(Class[] exceptions, BufferedWriter out) throws IOException {
        out.write((exceptions.length > 0 ? " throws " : ""));
        for (int i = 0; i < exceptions.length; ++i) {
            out.write(exceptions[i].getName() + (i + 1 < exceptions.length ? ", " : " "));
        }
    }

    @Override
    public void implementJar(Class<?> aClass, Path output) throws ImplerException {
        Objects.requireNonNull(aClass);
        Objects.requireNonNull(output);
        Path p = Paths.get("");
        p.toFile().deleteOnExit();
        implement(aClass, p);
        Path target = resolvePackage(aClass, p);
        int exit = compilation(p, target.resolve(aClass.getSimpleName() + "Impl.java"));
        if (exit != 0) {
            throw new ImplerException("Cannot compile source: " + aClass.getSimpleName() + "Impl.java" + ". Exit code: " + exit);
        }
        archieve(output, target.resolve(aClass.getSimpleName() + "Impl.class"));
    }

    private static int compilation(Path dir, Path file) {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        List<String> args = new ArrayList<>();
        args.add(file.toAbsolutePath().toString());
        args.add("-cp");
        args.add(System.getProperty("java.class.path"));
        return javaCompiler.run(null, null, null, args.toArray(new String[args.size()]));
    }

    private static void archieve(Path to, Path what) throws ImplerException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (
                JarOutputStream output = new JarOutputStream(Files.newOutputStream(to),
                        manifest);
                InputStream input = Files.newInputStream(what);
                ) {
            output.putNextEntry(new ZipEntry(what.toString()));
            byte[] buff = new byte[1024];
            int counter=0;
            while((counter = input.read(buff))>0){
                output.write(buff,0,counter);
            }
            output.closeEntry();
        } catch (IOException e) {
            throw new ImplerException("Cannot create JAR file: "+e.getMessage());
        }
    }
}