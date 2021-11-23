import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Translator {

    private FileUtil util = new FileUtil("names.properties");
    private PropertiesHandler commonPropertiesHandler = new PropertiesHandler("common.properties");
    private WebDriver driver;
    private String url = commonPropertiesHandler.readValue("translatorUrl");//"https://translate.google.co.il/#en/iw/";
    private ArrayList<String> namesToTranslate;// = new ArrayList<String>();
    private HashMap<Integer,Pair> mapOfNames = new HashMap<Integer,Pair>();
    private boolean needGoogleTranslate = false;
    private ArrayList<String> translatedNames = new ArrayList<String>();
    private HashMap<String,String> processedBeforeNames = new HashMap<>();
    private String name = commonPropertiesHandler.readValue("translatorName");//"//span[@class='VIiyi']";

    public Translator(ArrayList<String> namesToTranslate) {
        this.namesToTranslate = namesToTranslate;
        int index = 0;
        fetchProcessedNames();
        for (String name : namesToTranslate){
            String translatedName = fetchName(name);
            if (translatedName != null)
                mapOfNames.put(index++, new Pair(translatedName,  true));
            else{
                needGoogleTranslate = true;
                mapOfNames.put(index++, new Pair(name, false));
            }
        }
    }

    public ArrayList<String> process(){
        if (needGoogleTranslate){
            try {
                System.out.println("Translator is up");
                FirefoxOptions ffo = new FirefoxOptions();
                ffo.setPageLoadStrategy(PageLoadStrategy.NORMAL);

                driver = new FirefoxDriver(ffo);
                driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                System.out.println("Translator Driver ready");
                driver.get(url);

                Set<Integer> keySet = mapOfNames.keySet();
                for (int i : keySet){
                    Pair pair = mapOfNames.get(i);
                    if (!pair.translated){
                        String nameToTranslate = pair.name;

                        driver.get(url + nameToTranslate);

                        List<WebElement> translatedNameWebElement = driver.findElements(By.xpath(name));
                        String translatedName = translatedNameWebElement.get(0).getText();
                        if (!translatedNameWebElement.isEmpty()){
                            Driver.addComment("Translated "+nameToTranslate+" to "+translatedName);
                            System.out.println(translatedName);
                            mapOfNames.put(i,new Pair(translatedName, true));
                            util.addValue(nameToTranslate,translatedName);
                        }
                        else{
                            System.out.println("could not translate name: "+nameToTranslate);
                            Driver.addComment("could not translate name: "+nameToTranslate);
                            //translatedNames.add(nameToTranslate);
                            mapOfNames.put(i,new Pair(nameToTranslate, true));
                        }
                        humanPause(3);
                    }
                    translatedNames.add(mapOfNames.get(i).name);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            } finally {
                if (null != driver) {
                    System.out.println("Shutting Translator down");
                    driver.quit();
                }
            }
        }
        else{
            for (Integer i : mapOfNames.keySet()){
                translatedNames.add(mapOfNames.get(i).name);
            }
        }
        return translatedNames;
    }

    private void fetchProcessedNames(){
        String[] values = util.readValues().split("\n");
        for (String line : values) {
            String[] pair = line.split("=");
            processedBeforeNames.put(pair[0], pair[1]);
        }
    }

    private String fetchName(String name) {
        try{
            String fetchedName = processedBeforeNames.get(name);
            return fetchedName;
        }catch (NullPointerException e){
            return null;
        }
    }
    protected void humanPause(double seconds) throws InterruptedException {
        long time = (long) (seconds * 1000);
        Thread.sleep(time);
    }
    private class Pair{
        boolean translated = false;
        String name = null;

        public Pair(String name, boolean translated) {
            this.translated = translated;
            this.name = name;
        }
    }
}
