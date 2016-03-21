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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 *
 * This class creates implementation of classes or interfaces
 * that you provide.
 *
 * @author Vladislav Kamenev
 * @see info.kgeorgiy.java.advanced.implementor.Impler
 * @see info.kgeorgiy.java.advanced.implementor.JarImpler
 */
public class Implementor implements Impler, JarImpler {
    /**
    *  Creates new instance of Implementor class
    *
    */
    public Implementor() {}
    /**
     *
     * Main method to execute in this class
     * <p>
     * If number of the arguments is valid, what means more or equal two,
     * this will command to make implementation of class given as the first argument
     * and whether the argument {@code -jar} is given the implementation will be
     * archieved to a JAR file
     *
     * @param args arguments from command line
     * @see #implement(Class, Path)
     * @see #implementJar(Class, Path)
     */
    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.err.println("Not enough arguments! Must be at least 2");
            System.err.println("Usage: \"-jar\" <ClassName> <JarName>");
            System.err.println("Usage: <ClassName> <Directory>");
            return;
        }
        Implementor impl = new Implementor();
        if (args[0].equals("-jar") && args.length >= 3) {
            try {
                Class<?> c = Class.forName(args[1]);
                impl.implementJar(c, Paths.get(args[2]));
            } catch (ClassNotFoundException e) {
                System.err.println("Cannot find class: " + args[1]);
            } catch (ImplerException e) {
                System.err.println("Cannot implement class: " + args[1] + " cause: " + e.getMessage());
            }
        } else if (!args[0].equals("-jar")) {
            try {
                Class<?> c = Class.forName(args[0]);
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

    /**
     *
     * Method to resolve path over package.
     * <p>
     * This method transforms {@code path} to valid dir
     * according to {@code aClass} package hierarchy
     *
     * @param aClass class, whose package method looks too
     * @param path   current working directory
     * @return if {@code aClass} in the default package method will return {@code path} unchanged.
     * Otherwise method returns new path with correct package hierarchy.
     * @throws InvalidPathException if token's package string cannot be converted to path
     */
    private Path resolvePackage(Class<?> aClass, Path path) throws InvalidPathException {
        if (aClass.getPackage() == null) return path;
        else return path.resolve(aClass.getPackage().getName().replace(".", File.separator));
    }

    /**
     *
     * Method to generate non-jar implementation
     * <p>
     * Creates a file that correctly implements or extends interface or class.
     * Output file is created in the folder that corresponds to the package of
     * the given class or interface. Output file contains java class that implements
     * or extends given class or interface.
     * Output files compiles without errors.
     * This class or interface must not contain generics
     *
     * @param aClass target class or interface, which is going to be implemented
     * @param path   directory where implementation should be placed to
     * @throws ImplerException if {@code aClass} is final or got no non-private constructors
     * or cannot create directories according to {@code aCLass}'s package
     * @see Impler
     * @see #implementJar(Class, Path)
     */
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

    /**
     *
     * Prints header and frame of the given class
     * <p>
     * Prints header and frame of the given class and calling other methods to fill it up.
     * Collects all methods of the given class and then passes them to {@link #printMethod(Method, BufferedWriter)}
     *
     * @param clazz target class
     * @param to    output file
     * @throws IOException thrown by BufferedWriter
     * @see BufferedWriter
     * @see #printConstructor(Constructor, BufferedWriter, String)
     * @see #printParameters(Parameter[], BufferedWriter)
     * @see #printExceptions(Class[], BufferedWriter)
     * @see #printMethod(Method, BufferedWriter)
     */
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

    /**
     *
     *
     * Makes hash-like string of a method
     * <p>
     * Makes hash-like string of a method, so we can use it as a key in a map.
     * Gets name and concatenates its arguments' types
     *
     * @param method target method to make a string of
     * @return string that contains name and all arguments types
     * @see #implement(Class, Path)
     */
    private static String getHash(Method method) {
        return method.getName() + Arrays.toString(method.getParameterTypes());
    }

    /**
     *
     * Prints declaration of given constructor
     * <p>
     * Method to print {@code constructor} of the class. Includes arguments and exceptions if they are neccesary.
     *
     * @param constructor constructor to print
     * @param to          to print to
     * @param className   simple name of the class
     * @throws IOException thrown by {@link BufferedWriter}
     * @see #printExceptions(Class[], BufferedWriter)
     * @see #printParameters(Parameter[], BufferedWriter)
     */
    private static void printConstructor(Constructor constructor, BufferedWriter to, String className) throws IOException {
        to.write("    " +
                 Modifier.toString(constructor.getModifiers() &
                                   ~Modifier.ABSTRACT &
                                   Modifier.constructorModifiers())
                 + " " + className + "Impl(");
        printParameters(constructor.getParameters(), to);
        to.write(")");
        printExceptions(constructor.getExceptionTypes(), to);
        to.write("{");
        to.newLine();
        to.write("        super(");
        for (int i = 0; i < constructor.getParameterCount(); i++) {
            to.write("arg" + i);
            if (i + 1 < constructor.getParameterCount()) to.write(",");
        }
        to.write(");");
        to.newLine();
        to.write("    }");
        to.newLine();
    }

    /**
     *
     * Prints method of the class
     * <p>
     * Method to print given method. Includes parameters and exceptions if they are neccesary.
     *
     * @param m  method to print
     * @param to to print to
     * @throws IOException thrown by {@link BufferedWriter}
     * @see #printExceptions(Class[], BufferedWriter)
     * @see #printParameters(Parameter[], BufferedWriter)
     */
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

    /**
     *
     * Method to walk all ancestors of the class
     * <p>
     * This method will walk to very beggining of {@code aClass} inheritance hierarchy and
     * fill up dictionary with method, which are abstract and non-private.
     *
     * @param aClass target class
     * @param map    dictionary to contain all methods on the {@code aClass} inheritance hierarchy
     */
    private static void walkAncestors(Class aClass, Map<String, Method> map) {
        for (Method method : aClass.getDeclaredMethods()) {
            int mods = method.getModifiers();
            if (Modifier.isAbstract(mods) && !Modifier.isPrivate(mods)) {
                map.put(getHash(method), method);
            }
        }
        if (aClass.getSuperclass() != null) {
            walkAncestors(aClass.getSuperclass(), map);
        }
    }

    /**
     *
     * Prints given parametres
     * <p>
     *
     * @param parameters array of parametres to print
     * @param out        to print to
     * @throws IOException thrown by {@link BufferedWriter}
     * @see #printMethod(Method, BufferedWriter)
     * @see #printConstructor(Constructor, BufferedWriter, String)
     */
    private static void printParameters(Parameter[] parameters, BufferedWriter out) throws IOException {
        for (int i = 0; i < parameters.length; ++i) {
            out.write(parameters[i].getType().getCanonicalName() + " arg" + i + (i + 1 < parameters.length ? ", " : ""));
        }
    }

    /**
     * Print given exceptions
     *
     * @param exceptions array of exceptions to print
     * @param out        to print to
     * @throws IOException thrown by {@link BufferedWriter}
     * @see #printMethod(Method, BufferedWriter)
     * @see #printConstructor(Constructor, BufferedWriter, String)
     */
    private static void printExceptions(Class[] exceptions, BufferedWriter out) throws IOException {
        out.write((exceptions.length > 0 ? " throws " : ""));
        for (int i = 0; i < exceptions.length; ++i) {
            out.write(exceptions[i].getName() + (i + 1 < exceptions.length ? ", " : " "));
        }
    }

    /**
     *
     * Provides implementation of class archieved in JAR
     * <p>
     * Takes given class. Makes implementation and tries to compile it.
     * If the compilation succedes tries to make JAR file of the compiled
     * byte-code
     *
     * @param aClass class to implement
     * @param output JAR to implement to
     * @throws ImplerException if compilation fails. {@link #compilation(Path)}
     * @see #archieve(Path, Path)
     */
    @Override
    public void implementJar(Class<?> aClass, Path output) throws ImplerException {
        Objects.requireNonNull(aClass);
        Objects.requireNonNull(output);
        Path p = Paths.get("");
        implement(aClass, p);
        Path target = resolvePackage(aClass, p);
        int exit = compilation(target.resolve(aClass.getSimpleName() + "Impl.java"));
        if (exit != 0) {
            throw new ImplerException("Cannot compile source: " + aClass.getSimpleName() + "Impl.java" + ". Exit code: " + exit);
        }
        archieve(output, target.resolve(aClass.getSimpleName() + "Impl.class"));
    }

    /**
     *
     * Provides basic usage of Java compiler
     * <p>
     * Requests an system Java compiler and passes {@code file} to it
     * After that returns result of compilation
     *
     * @param file to compile
     * @return exitcode of compiler
     */
    private static int compilation(Path file) {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        List<String> args = new ArrayList<>();
        args.add(file.toAbsolutePath().toString());
        args.add("-cp");
        args.add(System.getProperty("java.class.path"));
        return javaCompiler.run(null, null, null, args.toArray(new String[args.size()]));
    }

    /**
     *
     * Makes JAR of {@code what} and places it {@code to}
     * <p>
     * Makes JAR of {@code what} and places it {@code to} and provides manifest
     * that contains version attribute.
     *
     * @param to   path to store achieve to
     * @param what path of the file that is going to be archieved
     * @throws ImplerException thrown if any IOException occured.
     * @see #implementJar(Class, Path)
     */
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
            int counter = 0;
            while ((counter = input.read(buff)) > 0) {
                output.write(buff, 0, counter);
            }
            output.closeEntry();
        } catch (IOException e) {
            throw new ImplerException("Cannot create JAR file: " + e.getMessage());
        }
    }
}