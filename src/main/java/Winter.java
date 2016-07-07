import com.animal.Copied;
import com.animal.Denied;
import com.animal.SnowFlake;
import com.exception.ClasspathSearchException;
import com.exception.WinterAlreadyCreatedException;
import com.exception.WinterCreationDeniedException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Winter {

    private Class[] packageClasses;
    private static List<String> snowflakeNames = new ArrayList<String>();
    private static Map<String,Object> createdObjects = new HashMap<>();
    final static Logger logger = Logger.getLogger(Winter.class);

    public Winter() {}

    public Winter(String classpath) {
        try {
            packageClasses = getClasses (classpath);
            logger.debug("Scanned for classes in package: " + classpath);
        }
        catch (ClassNotFoundException e) {
            logger.error("Could not find any classes in package: " + classpath, e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSnowflakes(String classpath) throws ClasspathSearchException {
        if (classpath.equals("")) {
            throw new ClasspathSearchException();
        }
        try {
            packageClasses = getClasses(classpath);
            logger.debug("Scanned for classes in package: " + classpath);
        } catch (ClassNotFoundException e) {
            logger.error("Could not find any classes in package: " + classpath, e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public Object getSnowFlake(String name) throws WinterAlreadyCreatedException, WinterCreationDeniedException {
        for (Class c : getPackageClasses()) {
            System.out.println(c.getName());
            if (c.isAnnotationPresent(SnowFlake.class)) {
                logger.debug("Found class with @SnowFlake annotation: " + c.getCanonicalName());
                if (c.isAnnotationPresent(Denied.class)) {
                    logger.debug("Denied creation process of class: " + c.getSimpleName());
                    throw new WinterCreationDeniedException(c.getCanonicalName());
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
                            throw new WinterAlreadyCreatedException();
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



    public Class[] getPackageClasses() {
        return packageClasses;
    }

}
