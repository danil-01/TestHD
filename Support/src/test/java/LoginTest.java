import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class LoginTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeClass
    public static void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:/chromedriver/chromedriver.exe");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20)); // Увеличено время ожидания до 20 секунд
    }

    @Test
    public void testLoginAndOpenTicket() {
        while (true) {
            try {
                // Открываем страницу входа
                driver.get("https://helpdesk.ag-ife.com/site/login");

                // Вводим логин и пароль
                driver.findElement(By.id("LoginForm_username")).sendKeys("danil_ivanov");
                driver.findElement(By.id("LoginForm_password")).sendKeys("passwOrd1@3");

                // Нажимаем кнопку входа
                wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-login"))).click();

                // Переходим на вкладку "Заявки"
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[.//span[contains(text(),'Заявки')]]"))).click();

                // Проверяем наличие заявки со статусом "Открыто" в первых 10 строках и кликаем по элементу
                boolean ticketFound = openTicketWithStatusInFirstTenRows("Открыто");

                if (ticketFound) {
                    // Кликаем по кнопке "Добавить комментарий"
                    clickAddCommentButton();

                    // Кликаем по выпадающему списку "Выберите значение"
                    clickSelect2Container();

                    // Кликаем по элементу с текстом "Принято в работу"
                    clickSelect2Option("Принято в работу");

                    // Выбираем опцию "Принято в работу" в выпадающем списке
                    selectCommentStatus("Принято в работу");

                    // Добавляем 5 секунд ожидания
                    waitForSeconds(5);

                    // Кликаем по кнопке "Добавить"
                    clickAddButton();

                    // Ожидаем 3 минуты перед следующим циклом
                    waitForSeconds(180);
                } else {
                    // Если заявок со статусом "Открыто" не найдено, ждем 3 минуты перед следующим циклом
                    System.out.println("Заявок со статусом 'Открыто' не найдено. Повтор через 3 минуты.");
                    waitForSeconds(180);
                }
            } catch (Exception e) {
                System.err.println("Ошибка во время выполнения цикла: " + e.getMessage());
                waitForSeconds(180);
            }
        }
    }

    private boolean openTicketWithStatusInFirstTenRows(String status) {
        try {
            // Ожидаем загрузки таблицы с заявками
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#request-grid-full2 > table")));

            // Получаем первые 10 строк таблицы
            List<WebElement> rows = driver.findElements(By.cssSelector("#request-grid-full2 > table > tbody > tr"));

            // Проверяем каждую из первых 10 строк (или меньше, если строк меньше 10)
            for (int i = 0; i < Math.min(10, rows.size()); i++) {
                WebElement row = rows.get(i);
                List<WebElement> columns = row.findElements(By.tagName("td"));

                if (columns.size() > 0) {
                    // Получаем элемент статуса из 8-й колонки (индекс 7)
                    WebElement statusCell = columns.get(7).findElement(By.xpath("./span"));

                    if (statusCell.getText().trim().equals(status)) {
                        // Ожидаем, пока элемент станет видимым и кликабельным, и кликаем по нему
                        wait.until(ExpectedConditions.visibilityOf(statusCell));
                        wait.until(ExpectedConditions.elementToBeClickable(statusCell)).click();
                        System.out.println("Открыта заявка со статусом: " + status);
                        return true; // Заявка найдена
                    }
                }
            }

            System.out.println("Заявки со статусом '" + status + "' не найдены в первых 10 строках.");
            return false; // Заявка не найдена
        } catch (StaleElementReferenceException e) {
            System.err.println("StaleElementReferenceException: Повторный поиск элемента.");
            return openTicketWithStatusInFirstTenRows(status);
        }
    }

    private void clickAddCommentButton() {
        try {
            // Ожидаем загрузки элемента "Добавить комментарий" и кликаем по нему
            WebElement addCommentButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li.search-button[title='Добавить комментарий'] a")));
            addCommentButton.click();
            System.out.println("Клик по кнопке 'Добавить комментарий' выполнен.");
        } catch (StaleElementReferenceException e) {
            System.err.println("StaleElementReferenceException: Повторный поиск элемента 'Добавить комментарий'.");
            clickAddCommentButton();
        }
    }

    private void clickSelect2Container() {
        try {
            // Ожидаем загрузки элемента выпадающего списка и кликаем по нему
            WebElement select2Container = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.select2-container#s2id_Comments_theme a.select2-choice")));
            select2Container.click();
            System.out.println("Клик по выпадающему списку 'Выберите значение' выполнен.");
        } catch (StaleElementReferenceException e) {
            System.err.println("StaleElementReferenceException: Повторный поиск элемента 'Выберите значение'.");
            clickSelect2Container();
        }
    }

    private void clickSelect2Option(String optionText) {
        try {
            // Ожидаем загрузки выпадающего списка
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("select2-drop")));

            // Ожидаем загрузки элемента с текстом "Принято в работу" и кликаем по нему
            WebElement option = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[contains(@class, 'select2-results-dept-0 select2-result select2-result-selectable')]//div[@class='select2-result-label' and text()='" + optionText + "']")));
            option.click();
            System.out.println("Клик по опции '" + optionText + "' выполнен.");
        } catch (StaleElementReferenceException e) {
            System.err.println("StaleElementReferenceException: Повторный поиск опции '" + optionText + "'.");
            clickSelect2Option(optionText);
        }
    }

    private void selectCommentStatus(String status) {
        try {
            // Ожидаем загрузки выпадающего списка статуса комментария
            WebElement statusDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("Comments_status")));

            // Создаем объект Select для взаимодействия с выпадающим списком
            Select select = new Select(statusDropdown);

            // Выбираем опцию по тексту
            select.selectByVisibleText(status);
            System.out.println("Выбрана опция '" + status + "' в выпадающем списке статуса комментария.");
        } catch (StaleElementReferenceException e) {
            System.err.println("StaleElementReferenceException: Повторный поиск выпадающего списка статуса комментария.");
            selectCommentStatus(status);
        }
    }

    private void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void clickAddButton() {
        try {
            // Ожидаем загрузки кнопки "Добавить" и кликаем по ней
            WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("create_btn")));
            addButton.click();
            System.out.println("Клик по кнопке 'Добавить' выполнен.");
        } catch (StaleElementReferenceException e) {
            System.err.println("StaleElementReferenceException: Повторный поиск кнопки 'Добавить'.");
            clickAddButton();
        }
    }


}