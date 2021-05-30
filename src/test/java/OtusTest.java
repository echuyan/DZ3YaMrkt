import io.github.bonigarcia.wdm.DriverManagerType;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.openqa.selenium.interactions.Actions;
import  org.openqa.selenium.Point;


public class OtusTest {

    private Logger logger = LogManager.getLogger(OtusTest.class);
    protected static WebDriver driver;

    @Before
    public void startUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        logger.info("Драйвер поднят");
    }

    @After
    public void end() {
        if (driver != null) {
            driver.quit();
            logger.info("Драйвер погашен");
        }
    }


    /*Открыть в Chrome сайт Яндекс.Маркет - "Электроника"-> "Смартфоны"
Отфильтровать список товаров: Samsung и Xiaomi
Отсортировать список товаров по цене (от меньшей к большей)
Добавить первый в списке Samsung
Проверить, что отобразилась плашка "Товар {имя товара} добавлен к сравнению"
Добавить первый в списке Xiaomi
Проверить, что отобразилась плашка "Товар {имя товара} добавлен к сравнению"

Перейти в раздел Сравнение
Проверить, что в списке товаров 2 позиции*/
    @Test
    public void yaMrktTest() throws Exception{
        //читаем конфиг, открываем сайт
        ServerConfig cfg = ConfigFactory.create(ServerConfig.class);
        driver.get(cfg.hostnameYandex());
        driver.manage().window().maximize();
        logger.info("Сайт открыт");

        //Открываем страницу Электроника
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement electronics = wait.until(ExpectedConditions.visibilityOfElementLocated(By.ByXPath.xpath(".//span[text() = 'Электроника']")));
        electronics.click();
        logger.info("Открыта страница "+driver.getTitle());

        //Открываем страницу Смартфоны
        WebElement smartphones = wait.until(ExpectedConditions.visibilityOfElementLocated(By.ByXPath.xpath(".//a[text() = 'Смартфоны']")));
        smartphones.click();
        logger.info("Открыта страница "+driver.getTitle());

        //Ждем наличия фильтра на странице
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.ByXPath.xpath(".//legend[text() = 'Производитель']")));

        //Выбираем производителей в фильтре
        driver.findElement(By.ByXPath.xpath(".//span[text() = 'Samsung']")).click();
        driver.findElement(By.ByXPath.xpath(".//span[text() = 'Xiaomi']")).click();
        //Ждем отработки фильтра
        wait.until(ExpectedConditions.presenceOfElementLocated (By.ByXPath.xpath(".//div[text() = 'Найдено ']")));

        //Сортируем по цене от меньшей к большей и ждем обновления списка
        driver.findElement(By.ByXPath.xpath(".//button[text() = 'по цене']")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.ByXPath.xpath("//article[@data-autotest-id='product-snippet']//span[contains(text(),'Samsung')]/parent::a")));


        //Перегенерим страницу для обновления DOM
        driver.navigate().refresh();
        //Считываем и записываем в строку название первого самсунга и сяоми, дробим на части, чтобы затем вычленить основное название для сравнения
        //При этом избегаем блока Популярные предложения
        String firstSamsungName = driver.findElement(By.ByXPath.xpath("//a[not(contains(@href,'premiumOffers')) and not(contains(@rel,'nofollow noopener')) and contains(.,'Samsung')]/span")).getText();
        String[] shortNames = firstSamsungName.split(",");
        String messageExpectedSamsung = "Товар "+shortNames[0]+" добавлен к сравнению";

        String firstXiaomiName = driver.findElement(By.ByXPath.xpath("//a[not(contains(@href,'premiumOffers')) and contains(.,'Xiaomi')]/span")).getText();
        String[] shortXiNames = firstXiaomiName.split(",");
        String messageExpectedXiaomi = "Товар "+shortXiNames[0]+" добавлен к сравнению";

        //Добавляем к сравнению первый Самсунг и первый сяоми
        WebElement addFirstSamsung = driver.findElement(By.ByXPath.xpath("//article[contains(.,'Samsung')]/descendant::div[contains(@aria-label,'сравнению')]"));
        addFirstSamsung.click();
        //Сверяем сообщение
        logger.info("Ожидалось "+messageExpectedSamsung);
        String messageReal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.ByXPath.xpath("//div[contains(text(),'добавлен к сравнению')]"))).getText();
        logger.info("Фактически "+messageReal);
        Assert.assertEquals(messageExpectedSamsung,messageReal);

        //Добавляем к сравнению первый Сяоми
        WebElement addFirstXiaomi = driver.findElement(By.ByXPath.xpath("//article[contains(.,'Xiaomi')]/descendant::div[contains(@aria-label,'сравнению')]"));
        addFirstXiaomi.click();
        messageReal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.ByXPath.xpath("//div[contains(text(),'добавлен к сравнению')]"))).getText();

        //Сверяем сообщения
        logger.info("Ожидалось "+messageExpectedXiaomi);
        logger.info("Фактически "+messageReal);
        Assert.assertEquals(messageExpectedXiaomi,messageReal);

        //Переходим в раздел Сравнение
        wait.until(ExpectedConditions.visibilityOfElementLocated (By.ByXPath.xpath("//a[(contains(.,'Сравнить'))]")));
        wait.until(ExpectedConditions.elementToBeClickable(By.ByXPath.xpath("//a[(contains(.,'Сравнить'))]"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.ByXPath.xpath("//div[(contains(.,'Сравнение товаров'))]")));

        //Проверяем, что товаров два
        List<WebElement> list = driver.findElements(By.ByXPath.xpath("//a[(contains(.,'Смартфон '))]"));
        logger.info("Найдено " + list.size() + " товара");
        Assert.assertEquals(list.size(),2);

    }

}
