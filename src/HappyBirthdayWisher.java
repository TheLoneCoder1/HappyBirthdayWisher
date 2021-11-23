import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebElement;

public class HappyBirthdayWisher {

    private PropertiesHandler propertiesHandler = PropertiesHandler.getInstance("common.properties");

    private WebDriver driver;
    private String url = propertiesHandler.readValue("url");
    private String birthdayPageUrl = propertiesHandler.readValue("birthdayPageUrl");
    private String email = propertiesHandler.readValue("user");
    private String pass = propertiesHandler.readValue("pass");

    private String birthdaySuperBlocks = propertiesHandler.readValue("birthdaySuperBlocks");
    private String birthdayBlock = propertiesHandler.readValue("birthdayBlock");
    private String xpathToName = propertiesHandler.readValue("xpathToName");

    private String xpathToTextField = propertiesHandler.readValue("xpathToTextField");

    private String sendMessageLine = propertiesHandler.readValue("sendMessageLine");

    private boolean shouldRun = true;
    private boolean isFirstRun = true;//is this the first run to gather names?
    private boolean didWeMakeAWish = false;
    ArrayList<String> namesToTranslate;
    ArrayList<String> translatedNames;
    private boolean shouldWeNameNames = true; //should we attempt to use the translated names or not

    private Set<String> whiteListNames = new HashSet<>();
    private Set<String> blackListNames = new HashSet<>();
    private ArrayList<String> wishList = new ArrayList<String>();

    public HappyBirthdayWisher(){
       loadListFromProperties(whiteListNames, "whitelist", ",");
       loadListFromProperties(wishList, "wishList", ";");
       loadListFromProperties(blackListNames, "blacklist", ";");
    }

    private void loadListFromProperties(Collection<String> collection, String keyword, String splitMark){
        int i=0;
        String line = propertiesHandler.readValue(keyword+i++);
        while (line != null){
            for (String str : line.split(splitMark)){
                collection.add(str);
            }
            line = propertiesHandler.readValue(keyword+i++);
        }
    }

    public void process() {
        while (shouldRun) {
            FirefoxOptions ffo = new FirefoxOptions();
            ffo.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            try {
                System.out.println("Happy Birthday wisher is up");
                if (isFirstRun)
                    Driver.addComment("Happy Birthday wisher is up "+new Date().toString());

                driver = new FirefoxDriver(ffo);
                driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                System.out.println("Driver ready");
                 driver.get(url);
                try {
                    login(email, pass);
                    System.out.println("login complete");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                driver.get(birthdayPageUrl);
                humanPause(3);

                if (isFirstRun){
                    gatherNames();
                }
                else{
                    processWishes();
                }

            } catch (Exception exp) {
                System.out.println("exception: " + exp.getMessage() + " at: " + new Date().toString());
                exp.printStackTrace();
                Driver.addComment("exception: " + exp.getMessage() + " at: " + new Date().toString());
            } finally {
                if (null != driver) {
                    System.out.println("Shutting driver down");
                    driver.quit();
                }
            }

            try {
                if (isFirstRun){
                    Translator translator = new Translator(namesToTranslate);
                    translatedNames = translator.process();
                    isFirstRun = false;
                }
                else{
                    String time = didWeMakeAWish ? "24" : "0.5";
                    createComment("Going down for "+time+" Hrs");
                    isFirstRun = true;
                    if (namesToTranslate!=null)
                        namesToTranslate.clear();
                    if (translatedNames!=null)
                        translatedNames.clear();
                    if (didWeMakeAWish){
                        Thread.sleep(86400000);
                        didWeMakeAWish=false;
                    }
                    else{
                        Thread.sleep(1800000);
                    }
                }
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
    }

    private void login(String email, String password) throws InterruptedException {
        driver.findElement(By.id("email")).sendKeys(email);
        humanPause(0.8);
        driver.findElement(By.id("pass")).sendKeys(password);
        humanPause(0.8);
        driver.findElement(By.name("login")).click();
    }

    protected void humanPause(double seconds) throws InterruptedException {
        long time = (long) (seconds * 1000);
        Thread.sleep(time);
    }

    private void gatherNames() {
        namesToTranslate = new ArrayList<String>();
        List<WebElement> birthdaySuperBlocksList = driver.findElements(By.xpath(birthdaySuperBlocks));
        gatherFromSuperBlock(birthdaySuperBlocksList.get(0));
        gatherFromSuperBlock(birthdaySuperBlocksList.get(1));
    }

    private void  gatherFromSuperBlock(WebElement superBirthdayBlock){
        List<WebElement> bithdayblockList = superBirthdayBlock.findElements(By.xpath(birthdayBlock));
        System.out.println("bithdayblockList size is: " + bithdayblockList.size());
        for (WebElement webElement : bithdayblockList) {
            if (isWriteableName(webElement)) {
                String name = webElement.findElements(By.xpath(xpathToName)).get(0).getText();
                createComment("gathered name: " + name);
                namesToTranslate.add(name.substring(0, name.indexOf(" ")));
            }
        }
    }

    private boolean isWriteableName(WebElement webElement){
        return webElement.getText().contains("Write on");
    }

    private void enableTextAreaWriting(){
        Object obj = ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('input');");

        ArrayList<RemoteWebElement> RemoteWebElementList = ((ArrayList<RemoteWebElement>)obj);
        for (RemoteWebElement element : RemoteWebElementList) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('style')", element);
        }
    }

    private void processWishes() throws InterruptedException {
        humanPause(2);
        List<WebElement> birthdayBlocks = driver.findElements(By.xpath(birthdayBlock));

        final List<WebElement> textAreas = driver.findElements(By.xpath(xpathToTextField));
        int textAreaIndex = 0;
        ArrayList<String> namesList = new ArrayList<String>();

        for (WebElement webElement : birthdayBlocks){
            if (isWriteableName(webElement)){
                String text = webElement.findElements(By.xpath(xpathToName)).get(0).getText();

                String name = text.split("\n")[0];
                System.out.println("name: "+name);
                namesList.add(name);
            }
        }
        int celebrators = textAreas.size();
        if (celebrators==0) didWeMakeAWish=true;
        shouldWeNameNames = decideIfToName(celebrators, translatedNames);
        enableTextAreaWriting();
        if (!didWeMakeAWish){

            for (int i = 0; i < namesList.size(); i++) {
                didWeMakeAWish=false;
                final int index = i;
                final int textAreaIndexFinal = textAreaIndex;
                //			final int postButtonsIndexFinal = postButtonsIndex;
                String name = namesList.get(i);
                if (!whiteListed(name)) {
                    humanPause(1);

                    if (isNoneWriteable(name)){
                        textAreaIndex++;
                        //postButtonsIndex++;
                        continue;
                    }
                    operateElement("Text Area", new Command(){

                        @Override
                        public void execute(Object data) {
                            WebElement webElement = textAreas.get(textAreaIndexFinal);
                            webElement.sendKeys((String)data + Keys.RETURN);
                            createComment("Wrote: "+((String)data));
                            didWeMakeAWish=true;
                        }
                    },createNamedWish(getNextName(index)));
                    textAreaIndex++;
                    humanPause(1.2);

                } else {
                    if (textAreaIndex+1 < textAreas.size())
                        textAreaIndex++;

                    if (!blackListed(name)) {
                        createComment(name + " is Whitelisted. sending mail now");
                        MailSender mailSender = new MailSender(name);
                        mailSender.sendMail();
                        didWeMakeAWish = true;
                    }
                }
            }
        }
    }

    private boolean decideIfToName(int celebrators, ArrayList<String> translatedNames) {
        if (translatedNames==null) return false;
        System.out.println("celebrators: "+celebrators+" translatedNames size: "+translatedNames.size());
        return translatedNames.size()==celebrators;
    }

    private String getNextName(int index){
        try{
            return translatedNames.get(index);
        }catch(IndexOutOfBoundsException ex){
            createComment("index out of bound for translatedNames. index: "+index);
            //System.out.println("index out of bound for translatedNames. index: "+index);
            return "";
        }
    }

    public boolean isNoneWriteable(String name) {
        try{
            name = name.substring(0, name.indexOf(" "));
            final List<WebElement> sendMessageLineElement = driver.findElements(By.xpath(sendMessageLine));
            for (WebElement webElement : sendMessageLineElement){
                String sendMessageLine = webElement.getText();
                if (sendMessageLine.startsWith("Send")){
                    System.out.println("send Message Line is: "+sendMessageLine+", name is: "+name);
                    if (sendMessageLine.contains(name)){
                        return true;
                    }
                }
            }
            return false;
        }
        catch(Exception exp){
            return false;
        }
    }

    private void operateElement(String debugPrint, Command command,Object data) throws InterruptedException {
        int attempts = 0;
        while (attempts < 10){
            try{
                System.out.println("Preparing to operate "+debugPrint);
                command.execute(data);
                attempts = 10;
            }catch (Exception exp){
                exp.printStackTrace();
                createComment("operating "+debugPrint+" failed. trying again. attempt: "+attempts);
                humanPause(2);
                attempts++;
            }
        }
    }

    private boolean whiteListed(String name) {
        boolean isWhiteListed = whiteListNames.contains(name.trim());
        createComment("name is " + name + (isWhiteListed ? " " : " not " + "whiteListed"));
        return isWhiteListed;
    }

    private boolean blackListed(String name) {
        boolean isBlackListed = blackListNames.contains(name.trim());
        createComment("name is " + name + (isBlackListed ? " " : " not " + "blackListed"));
        return isBlackListed;
    }




    private String getRandomWish() {
        int wishes = wishList.size();
        int randomWish = new Random().nextInt(wishes);
        return wishList.get(randomWish);
    }

    /**
     * create named wish if the selected wish is not in English
     * (once in every 3 names, the name will be deleted. just for random appearance.
     * @param name
     * @return named wish
     */
    public String createNamedWish(String name) {
        if (flip3SideCoin() || !shouldWeNameNames) {
            name = "";
        }

        String wish = getRandomWish();
        if (isEnglish(wish) || isEnglish(name)) {
            return wish;
        }
        return name + (name.equals("") ? "" : ", ") + wish;
    }

    private abstract class Command {
        public abstract void execute(Object data);
    }

    private boolean isEnglish(String str){
        if (str.length()>0){
            return str.charAt(0)>='A' && str.charAt(0)<='Z';
        }
        return false;
    }

    private boolean flip3SideCoin(){
        return new Random().nextInt(3)==0;
    }

    private void createComment(String comment){
        System.out.println(comment);
        Driver.addComment(comment);
    }
}
