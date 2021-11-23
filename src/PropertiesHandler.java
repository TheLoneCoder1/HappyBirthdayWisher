import java.io.*;
import java.util.Properties;

public class PropertiesHandler {

    private String fileName;
    private static PropertiesHandler propertiesHandler;
    Properties p=new Properties();

    public static PropertiesHandler getInstance(String fileName){
        if (propertiesHandler==null){
            propertiesHandler = new PropertiesHandler(fileName);
        }
        return propertiesHandler;
    }

    public PropertiesHandler(String fileName){
        this.fileName = fileName;
    }

    public String readValue(String key)  {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new FileInputStream(fileName),"UTF-8");
            p.load(isr);
            return p.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            try {
                isr.close();
            } catch (IOException e) {
                return null;
            }
        }
    }

}
