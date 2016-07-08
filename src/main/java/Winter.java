import com.animal.Copied;
import com.animal.Denied;
import com.animal.Report;
import com.animal.SnowFlake;
import com.exception.ClasspathSearchException;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

/**
 * Winter is a simple representation of IoC container.
 */
public class Winter {

    /**
     * Class[] array for saving package classes.
     */
    private Class[] packageClasses;
    /**
     * List containing all snowflake names in a package.
     */
    private static List<String> snowflakeNames = new ArrayList<String>();
    /**
     * List containing all @SnowFlake create objects in application.
     */
    private static Map<String,Object> createdObjects = new HashMap<>();
    /**
     * Logger instance.
     */
    final static Logger logger = Logger.getLogger(Winter.class);

    public Winter() {}

    /**
     * Custom constructor with class package scanning.
     * @param classpath package path to find classes
     */
    public Winter(String classpath) {
        try {
            packageClasses = getClasses (classpath);
            logger.debug("Constructor call: Scanned for classes in package: " + classpath);
        }
        catch (ClassNotFoundException e) {
            logger.error("Could not find any classes in package: " + classpath, e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Scans package for classes.
     * @param classpath package path to find classes
     * @throws ClasspathSearchException
     */
    public void addSnowflakes(String classpath) throws ClasspathSearchException {
        if (classpath.equals("")) {
            throw new ClasspathSearchException();
        }
        try {
            packageClasses = getClasses(classpath);
            logger.debug("Method call: Scanned for classes in package: " + classpath);
        } catch (ClassNotFoundException e) {
            logger.error("Could not find any classes in package: " + classpath, e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates instance of a given class using Reflection API.
     * @param c Class
     * @return new Object of class
     */
    private Object createClassInstance(Class c) {
        try {
        return Class.forName(c.getName()).newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            logger.debug("Instantiation exception during class creation: " + c.getName() , e);
        } catch (ClassNotFoundException e) {
            logger.debug("Could not find class: " + c.getName(), e);
        }
        return null;
    }

    /**
     * Returns all data about class annotations, fields and methods.
     * @param clazz Class instance
     * @return String containing class data
     */
    private static String getClassReflectionData(Class clazz) {
        String classData = "";
        SnowFlake snowFlake = (SnowFlake) clazz.getAnnotation(SnowFlake.class);
        classData += "Class " + clazz.getSimpleName() + " with name " + snowFlake.value() + "\n";
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation: annotations) {
            classData += "annotation" + annotation.toString() + "\n";
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            for (Annotation field_ann: f.getDeclaredAnnotations()) {
                classData += field_ann.toString() + "\n";
            }
            classData += "field: " + f.toString() + "\n";
        }
        for (Method m: clazz.getDeclaredMethods()) {
            for (Annotation method_ann : m.getAnnotations()) {
                classData += method_ann.toString() + "\n";
            }
            classData += "method: " + m.toString() + "\n";
        }
        return classData;
    }

    /**
     * Writes class data to file.
     * @param path Path to write a file
     * @param clazz Class from which the data will be copied
     */
    private static void writeReflectionClassDataToFile(String path, Class clazz) {
        java.util.Properties properties = System.getProperties();

        // get Operating System home directory
        String home = properties.get("user.home").toString();

        // get Operating System separator
        String separator = properties.get("file.separator").toString();

        String pathName = path + ".txt";

        Path filepath = Paths.get(home+separator+pathName);
        logger.info("Created classpath for the file: " + filepath);

        try(BufferedWriter writer = Files.newBufferedWriter(filepath)) {
            // Get class data
            String classData = getClassReflectionData(clazz);
            writer.write(classData);
            logger.info("Finished writing " + clazz.getSimpleName() + " data in file");
        } catch (UnsupportedEncodingException e) {
            logger.error("UnsupportedEncoding", e);
        } catch (IOException e) {
            logger.error("Could not write file", e);
        }

    }

    /**
     * Returns  @SnowFlake annotate object with given String param.
     * @param name @SnowFlake parameter name
     * @return Snowflake object. Needs to be casted to called Class
     */
    public Object getSnowFlake(String name) {
        for (Class c : getPackageClasses()) {
            if (c.isAnnotationPresent(SnowFlake.class)) {
                logger.debug("Found class with @SnowFlake annotation: " + c.getCanonicalName());
                if (c.isAnnotationPresent(Report.class)) {
                    Report report = (Report) c.getAnnotation(Report.class);
                    String path = report.value();
                    writeReflectionClassDataToFile(path, c);
                }
                if (c.isAnnotationPresent(Denied.class)) {
                    logger.debug("Denied creation process of class: " + c.getSimpleName());
                }
                if (c.isAnnotationPresent(Copied.class)) {
                    logger.debug("Created new instance of class: " + c.getSimpleName());
                    return createClassInstance(c);
                }
                 else {
                    SnowFlake snowFlake = (SnowFlake) c.getAnnotation(SnowFlake.class);
                    // if annotation value equals entered value
                    if (snowFlake.value().equals(name)) {
                        // if class with this name has already been created
                        if (snowflakeNames.contains(name))
                        {
                            logger.debug("Class " + c.getSimpleName() + " is already in app context, returning existing instance");
                            createdObjects.get(name);
                        }
                        else
                        {
                            logger.debug("No Classes of " + c.getSimpleName() +
                                    " found. Creating new one");
                            snowflakeNames.add(name);
                            Object object = createClassInstance(c);
                            createdObjects.put(name, object);
                            return object;
                        }
                    }
                    else
                    {
                        logger.error("No classes with @Snowflake annotation found");
                    }
                }
            }
        }
        return null;
    }

    /**
     * This method returns an array of classes in a given packageName.
     * @param packageName String representation of package name
     * @return Class[] classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration resources = classLoader.getResources(path);
        List dirs = new ArrayList();
        while (resources.hasMoreElements()) {
            URL resource = (URL) resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList classes = new ArrayList();
        for (Object directory : dirs) {
            classes.addAll(findClasses((File) directory, packageName));
        }
        return (Class[]) classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursively finds classes in given directory.
     * Requires File directory and String packageName
     * @param directory Directory to find
     * @param packageName String representation of package name
     * @return List<Class></Class>
     * @throws ClassNotFoundException
     */
    private static List findClasses(File directory, String packageName) throws ClassNotFoundException {
        List classes = new ArrayList();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    /**
     * Getter for packageClasses.
     * @return packageClasses
     */
    public Class[] getPackageClasses() {
        return packageClasses;
    }

}
