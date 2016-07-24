import constants.Constants;
import org.junit.Test;

/**
 * Created by cgz on 16-7-11.
 */
public class PlainTest {
    @Test
    public void testEncry(){
        String pwd = com.shove.security.Encrypt.MD5("123" + "Xco1tY5CDlhh5qFCDKb1uuKB42RmSk4u");
        System.out.print(pwd);
    }
}
