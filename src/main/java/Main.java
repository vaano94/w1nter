import com.animal.Fox;
import com.animal.Mouse;
import com.bear.Bear;
import com.exception.ClasspathSearchException;
import com.exception.WinterAlreadyCreatedException;
import com.exception.WinterCreationDeniedException;
import org.apache.log4j.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            Winter winter = new Winter ("com.animal");
            Fox mindy = (Fox) winter.getSnowFlake("Mindy");
            System.out.println (mindy.age);

            Mouse mouse = (Mouse) winter.getSnowFlake("Jerry");
            System.out.println(mouse.age);

            winter = new Winter();
            winter.addSnowflakes("com.bear");
            Bear mikey = (Bear) winter.getSnowFlake("Mikey");
            System.out.println(mikey.age);

        } catch (WinterAlreadyCreatedException e) {
                logger.error("The class is already created :" + e.getMessage());
        } catch (WinterCreationDeniedException e) {
        } catch (ClasspathSearchException e) {
                logger.error("Can not search classes in main catalogue" + e);
        }

    }

}