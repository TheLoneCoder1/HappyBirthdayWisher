import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class FileUtil {

    private String fileName;

    public FileUtil(String fileName){
        this.fileName = fileName;
    }

    public void addValue(String key, String value){
        Path path = Paths.get(fileName);
        try {
            Files.writeString(path,key+"="+value+"\n",StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readValues(){
        Path path = Paths.get(fileName);
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
