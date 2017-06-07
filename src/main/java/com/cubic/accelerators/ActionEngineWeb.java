package com.cubic.accelerators;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.Color;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.cubic.genericutils.GenericConstants;
import com.cubic.reportengine.report.CustomReports;

public class ActionEngineWeb {
	private final Logger LOG = Logger.getLogger(this.getClass().getName());
	private final String msgClickSuccess = "Successfully Clicked On ";
	private final String msgClickFailure = "Unable To Click On ";
	private final String msgRightClickSuccess = "Successfully Mouse Right Clicked On ";
	private final String msgRightClickFailure = "Unable To Right Click On ";
	private final String msgTypeSuccess = "Successfully Entered value ";
	private final String msgTypeFailure = "Unable To Type On ";
	private final String msgIsElementFoundSuccess = "Successfully Found Element ";
	private final String msgIsElementFoundFailure = "Unable To Found Element ";
	private final String msgCheckboxisnotChecked = "Checkbox is not Selected";
	

	private WebDriver webDriver = null;
	private CustomReports customReports = null;
	private String testCaseName = null;
	
	private int timeValue  = Integer.parseInt(GenericConstants.GENERIC_FW_CONFIG_PROPERTIES.get("webdriver_dynamicwait_time"));
	
	public ActionEngineWeb(WebDriver webDriver, CustomReports customReports, String testCaseName) {
		this.webDriver = webDriver;
		this.customReports = customReports;
		this.testCaseName = testCaseName;
	}
	
	public ActionEngineWeb(CustomReports customReports, String testCaseName, String browserName, String seleniumGridUrl)
			throws IOException, InterruptedException {

		this.webDriver = getWebDriverForLocal(browserName, seleniumGridUrl);
		this.customReports = customReports;
		this.testCaseName = testCaseName;
	}
	
	public WebDriver getWebDriver (){
		return this.webDriver;
	}
	
	public static void flush(ActionEngineWeb actionEngineWeb){
		if(actionEngineWeb.webDriver!=null){
			actionEngineWeb.webDriver.quit();
		}
		
		if(actionEngineWeb!=null){
			actionEngineWeb = null;
		}
	}

	public void failureReport(String stepName, String description) {
		if (customReports != null) {
			File screenshotFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
			customReports.failureReportWeb(stepName, description, screenshotFile, testCaseName);
		}
	}

	public void warningReport(String stepName, String description) {
		if (customReports != null) {
			customReports.warningReport(stepName, description, testCaseName); 
		}
	}
	
	public void successReport(String stepName, String description) {
		if (customReports != null) {
			customReports.successReport(stepName, description, testCaseName);
		}
	}

	public void successReportForWeb(String stepName, String description) {
		if (customReports != null) {
			File screenshotFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
			customReports.successReportForWeb(stepName, description, screenshotFile, testCaseName);
		}
	}	
	//Don't "getWebDriverForLocal" to public, since this method should not be exposed outside and should be allowed to access with in the package. 
	static synchronized WebDriver getWebDriverForLocal(String browserName,String seleniumGridUrl) throws IOException, InterruptedException{
		WebDriver webDriver = null;
		DesiredCapabilities capabilities = null;
		int implicitlyWaitTime = Integer.parseInt(GenericConstants.GENERIC_FW_CONFIG_PROPERTIES.get("webdriver_implicitwait_time"));
		 
		switch (browserName) {
		
		case GenericConstants.FIREFOX_BROWSER:
			FirefoxProfile firefoxProfile = new FirefoxProfile();
			String firefoxDownloadDirPath = GenericConstants.GENERIC_FW_CONFIG_PROPERTIES.get("firefox_downloadDir_Path");
			
			//Below property is for Selenium 3.0(geckodriver)
			String firefoxDriverPath = GenericConstants.GENERIC_FW_CONFIG_PROPERTIES.get("firefox_driver_path");
			System.setProperty("webdriver.gecko.driver", firefoxDriverPath);
			
			//Download path is optional
			if(!(firefoxDownloadDirPath==null || firefoxDownloadDirPath.trim().length()==0)){
				firefoxProfile.setPreference("browser.download.dir", firefoxDownloadDirPath);
			}
			
			firefoxProfile.setPreference("browser.download.folderList", 2);
			firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk",
							  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;" 
							+ "application/pdf;"
							+ "application/vnd.openxmlformats-officedocument.wordprocessingml.document;" 
							+ "text/plain;"
							+ "text/csv");
			firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
			firefoxProfile.setPreference("pdfjs.disabled", true);

			capabilities = DesiredCapabilities.firefox();
			firefoxProfile.setPreference("xpinstall.signatures.required", false);
			capabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile);
			
			if(seleniumGridUrl == null || seleniumGridUrl.equalsIgnoreCase(GenericConstants.LOCAL)){
				webDriver = new FirefoxDriver(firefoxProfile);
			}
			
			webDriver.manage().window().maximize();
			webDriver.manage().timeouts().implicitlyWait(implicitlyWaitTime, TimeUnit.SECONDS);
			// LOG.info("Driver launch ::" + browser);		
			
			break;
			
		case GenericConstants.IE_BROWSER:
			String ieDriverPath = GenericConstants.GENERIC_FW_CONFIG_PROPERTIES.get("ie_driver_path");		
			System.setProperty("webdriver.ie.driver", ieDriverPath);
			capabilities = DesiredCapabilities.internetExplorer();
			
			// To disable popup blocker.
			capabilities.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR, true);
			
			// to enable protected mode settings
			capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			
			// to get window focus
			capabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
			
			// to set zoom level is set to 100% so that the native mouse events
			// can be set to the correct coordinates.
			capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
			capabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
			capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
			
			Process p = Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 255");
			p.waitFor();		
			
			if(seleniumGridUrl == null || seleniumGridUrl.equalsIgnoreCase(GenericConstants.LOCAL)){
				webDriver = new InternetExplorerDriver(capabilities);
			}
			
			webDriver.manage().window().maximize();
			webDriver.manage().timeouts().implicitlyWait(implicitlyWaitTime, TimeUnit.SECONDS);
			
			// LOG.info("Driver launch ::" + browser);	
			break;	
			
		case GenericConstants.CHROME_BROWSER:
			String chromeDriverPath = GenericConstants.GENERIC_FW_CONFIG_PROPERTIES.get("chrome_driver_path");
			String chromeDownloadDirPath = GenericConstants.GENERIC_FW_CONFIG_PROPERTIES.get("chrome_downloadDir_Path");
			
			HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
			chromePrefs.put("profile.default_content_settings.popups", 0);

			//Optional
			if(!(chromeDownloadDirPath==null || chromeDownloadDirPath.trim().length()==0)){
				chromePrefs.put("download.default_directory", chromeDownloadDirPath);
			}
			
			capabilities = DesiredCapabilities.chrome();
			
			System.setProperty("webdriver.chrome.driver", chromeDriverPath);
			ChromeOptions options = new ChromeOptions();

			options.setExperimentalOption("prefs", chromePrefs);
			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

			options.addArguments("test-type");
			options.addArguments("chrome.switches", "--disable-extensions");
			capabilities.setCapability(ChromeOptions.CAPABILITY, options);
			
			if(seleniumGridUrl == null ||seleniumGridUrl.equalsIgnoreCase(GenericConstants.LOCAL)){
				webDriver = new ChromeDriver(capabilities);
			}
			
			webDriver.manage().window().maximize();
			
			webDriver.manage().timeouts().implicitlyWait(implicitlyWaitTime, TimeUnit.SECONDS);
			// LOG.info("Driver launch ::" + browser);
			
			break;
			
		case GenericConstants.EDGE_BROWSER:
			String edgeDriverPath = GenericConstants.GENERIC_FW_CONFIG_PROPERTIES.get("edge_driver_path");
			System.setProperty("webdriver.edge.driver", edgeDriverPath);
			capabilities = DesiredCapabilities.edge();		
			
			if(seleniumGridUrl == null ||seleniumGridUrl.equalsIgnoreCase(GenericConstants.LOCAL)){
				webDriver = new EdgeDriver(capabilities);
			}
		
			webDriver.manage().window().maximize();
			webDriver.manage().timeouts().implicitlyWait(implicitlyWaitTime, TimeUnit.SECONDS);
			
			//LOG.info("Driver launch ::" + browser);
			break;

		case GenericConstants.SAFARI_BROWSER:

			for (int i = 1; i <= 10; i++) {
				try {
					if (seleniumGridUrl==null || seleniumGridUrl.trim().length()==0) {
						webDriver = new SafariDriver();
					}
					break;
				} catch (Exception e1) {
					continue;
				}
			}		
			
			webDriver.manage().window().maximize();
			webDriver.manage().timeouts().implicitlyWait(implicitlyWaitTime, TimeUnit.SECONDS);
			
			//LOG.info("Driver launch ::" + browser);
			
			break;			
		}
		
		if(seleniumGridUrl!=null && !seleniumGridUrl.equalsIgnoreCase(GenericConstants.LOCAL)){
			webDriver = new RemoteWebDriver(new URL(seleniumGridUrl), capabilities);
		}
		

		return webDriver;			
	}	
	
	public boolean navigateToUrl(String url) {
		boolean flag = false;
		try {
			webDriver.get(url);
			successReport("Navigated to url", "Navigated to url '" + url + "'");
			flag = true;
		} catch (Exception e) {
			failureReport("Navigated to url", "Unable to navigat to url '" + url + "'");
			flag = false;
		}
		return flag;
	}

	/**
	 * selectByIndex
	 * 
	 * @param locator
	 *            of (By)
	 * @param index
	 *            of (int)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean selectByIndex(By locator, int index, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			Select s = new Select(webDriver.findElement(locator));
			s.selectByIndex(index);
			flag = true;
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			return true;
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("Select Value from the Dropdown :: " + locatorName,
						"Option at index :: " + index + " is Not Select from the DropDown :: " + locatorName);
			} else {
				successReport("Select Value from the Dropdown :: " + locatorName,
						"Option at index :: " + index + "is Selected from the DropDown :: " + locatorName);
			}
		}
	}

	/**
	 * assertTrue
	 * 
	 * @param condition
	 *            of (boolean)
	 * @param message
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean assertTrue(boolean condition, String message) throws Throwable {
		try {
			if (condition)
				return true;
			else
				return false;
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
			return false;
		} finally

		{
			if (!condition) {
				failureReport("Expected :: " + message, message + " is :: " + condition);
			} else {
				successReport("Expected :: " + message, message + " is :: " + condition);
			}
		}
	}

	/**
	 * dynamicWaitByLocator
	 * 
	 * @param locator
	 *            of (By)
	 * @param time
	 *            of (int)
	 * @return void
	 * @throws InterruptedException
	 *             the throwable
	 */
	public void dynamicWaitByLocator(By locator, int time) throws InterruptedException {
		try {
			WebDriverWait wait = new WebDriverWait(webDriver, time);
			wait.until(ExpectedConditions.presenceOfElementLocated(locator));
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	/**
	 * dynamicWaitByLocator
	 * 
	 * @param locator
	 *            of (By)
	 * @return void
	 * @throws InterruptedException
	 *             the throwable
	 */
	public void dynamicWaitByLocator(By locator) throws InterruptedException {
		try {
			WebDriverWait wait = new WebDriverWait(webDriver,timeValue);
			wait.until(ExpectedConditions.presenceOfElementLocated(locator));
		} catch (Exception e) {
			LOG.info(e.getMessage());
		}
	}

	/**
	 * assertElementPresent
	 * 
	 * @param by
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean assertElementPresent(By by, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			Assert.assertTrue(isElementPresent(by, locatorName, true));
			flag = true;
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			e.printStackTrace();
			LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
		} finally {
			if (!flag) {
				failureReport("AssertElementPresent :: ", locatorName + " present in the page :: ");
				// return false;
			} else {
				successReport("AssertElementPresent :: ", locatorName + " is not present in the page :: ");
			}
		}
		return flag;
	}

	/**
	 * mouseHoverByJavaScript
	 * 
	 * @param locator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean mouseHoverByJavaScript(By locator, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			WebElement mo = webDriver.findElement(locator);
			String javaScript = "var evObj = document.createEvent('MouseEvents');"
					+ "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
					+ "arguments[0].dispatchEvent(evObj);";
			JavascriptExecutor js = (JavascriptExecutor) webDriver;
			js.executeScript(javaScript, mo);
			flag = true;
			LOG.info("MoveOver action is done on  :: " + locatorName);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			return true;
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("MouseOver :: ", "MouseOver action is not perform on :: " + locatorName);
			} else {
				successReport("MouseOver :: ", "MouserOver Action is Done on :: " + locatorName);
			}
		}
	}

	/**
	 * waitForVisibilityOfElement
	 * 
	 * @param by
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean waitForVisibilityOfElement(By by, String locatorName) throws Throwable {
		boolean flag = false;
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
		LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
		WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			flag = true;
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			return true;
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			e.printStackTrace();
			LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
			return false;
		} finally {
			if (!flag) {
				failureReport("Visible of element is false :: ", "Element :: " + locatorName + " is not visible");
			} else {
				successReport("Visible of element is true :: ", "Element :: " + locatorName + "  is visible");
			}
		}
	}

	/**
	 * clickUsingJavascriptExecutor
	 * 
	 * @param locator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean clickUsingJavascriptExecutor(By locator, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			WebElement element = webDriver.findElement(locator);
			isElementPresent(locator, locatorName);
			// internalServerErrorHandler();
			JavascriptExecutor executor = (JavascriptExecutor) webDriver;
			executor.executeScript("arguments[0].click();", element);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			flag = true;
			LOG.info("clicked : " + locatorName);
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
			flag = false;
			throw new RuntimeException(e);
		} finally {
			if (!flag) {

				failureReport("Click : " + locatorName, msgClickFailure + locatorName);
			} else {
				successReport("Click : " + locatorName, msgClickSuccess + locatorName);
			}

		}
		return flag;
	}

	/**
	 * selectByValue
	 * 
	 * @param locator
	 *            of (By)
	 * @param value
	 *            of (String)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean selectByValue(By locator, String value, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			Select s = new Select(webDriver.findElement(locator));
			s.selectByValue(value);
			flag = true;
			LOG.info("Successfully selected the value" + locatorName);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("Select",
						"Option with value attribute : " + value + " is Not Select from the DropDown : " + locatorName);
			} else {
				successReport("Select",
						"Option with value attribute : " + value + " is  Selected from the DropDown : " + locatorName);
			}
		}
	}

	/**
	 * selectByVisibleText
	 * 
	 * @param locator
	 *            of (By)
	 * @param visibleText
	 *            of (String)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean selectByVisibleText(By locator, String visibleText, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			Select s = new Select(webDriver.findElement(locator));
			s.selectByVisibleText(visibleText.trim());
			flag = true;
			return true;
		} catch (Exception e) {
			// return false;
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("Select", visibleText + " is Not Select from the DropDown" + locatorName);
			} else {
				successReport("Select", visibleText + "  is Selected from the DropDown" + locatorName);
			}
		}
	}

	/**
	 * isVisible
	 * 
	 * @param locator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean isVisible(By locator, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			// added loggers
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name :: " + getCallerClassName() + " Method name :: " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			// value = driver.findElement(locator).isDisplayed();
			flag = webDriver.findElement(locator).isDisplayed();
			// value = true;
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			flag = false;
		} finally {
			if (!flag) {
				failureReport("IsVisible : ", locatorName + " Element is Not Visible : ");
			} else {
				successReport("IsVisible : ", locatorName + " Element is Visible : ");
			}
		}
		return flag;
	}

	/**
	 * getElementsSize
	 * 
	 * @param locator
	 *            of (By)
	 * @return int
	 */
	public int getElementsSize(By locator) {
		int a = 0;
		try {
			List<WebElement> rows = webDriver.findElements(locator);
			a = rows.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return a;
	}

	/**
	 * assertTextMatching
	 * 
	 * @param by
	 *            of (By)
	 * @param text
	 *            of (String)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean assertTextMatching(By by, String text, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			String ActualText = getText(by, locatorName).trim();
			LOG.info("ActualText is : " + ActualText);

			if (ActualText.contains(text.trim())) {
				flag = true;
				LOG.info("String comparison with actual text :: " + "actual text is : " + ActualText
						+ "And expected text is : " + text);
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				return true;
			} else {
				LOG.info("String comparison with actual text :: " + "actual text is : " + ActualText
						+ "And expected text is : " + text);
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (!flag) {
				failureReport("Verify : " + locatorName, text + " is not present in the element : ");
				// return false;
			} else {
				successReport("Verify : " + locatorName, text + " is  present in the element : " + locatorName);
			}
		}
	}

	/**
	 * assertTextMatchingWithAttribute
	 * 
	 * @param by
	 *            of (By)
	 * @param text
	 *            of (String)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean assertTextMatchingWithAttribute(By by, String text, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			// added loggers
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			String ActualText = getAttributeByValue(by, text).trim();
			LOG.info("ActualText is" + ActualText);
			if (ActualText.contains(text.trim())) {
				flag = true;
				// added loggers
				LOG.info("String comparison with actual text :: " + "actual text is :" + ActualText
						+ "And expected text is : " + text);
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (!flag) {
				failureReport("Verify : " + locatorName, text + " is not present in the element : ");
				// return false;
			} else {
				successReport("Verify : " + locatorName, text + " is  present in the element : ");
			}
		}
	}

	/**
	 * assertTextStringMatching
	 * 
	 * @param actText
	 *            of (String)
	 * @param expText
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean assertTextStringMatching(String actText, String expText) throws Throwable {
		boolean flag = false;
		try {
			// added loggers
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			String ActualText = actText.trim();
			LOG.info("act - " + ActualText);
			LOG.info("exp - " + expText);
			if (ActualText.equalsIgnoreCase(expText.trim())) {
				LOG.info("in if loop");
				flag = true;
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				return true;
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (!flag) {
				failureReport("Verify : " + expText, actText + " is not present in the element : ");
				// return false;
			} else {
				successReport("Verify : " + expText, actText + " is  present in the element : ");
			}
		}
	}

	/**
	 * click
	 * 
	 * @param locator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean click(By locator, String locatorName) throws Throwable {
		boolean status = false;
		// isElementPresent(locator, locatorName);
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : click  ::  Locator : " + locatorName);
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			// internalServerErrorHandler();
			LOG.info("Waiting for element");
			// wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			LOG.info("Locator is Visible :: " + locator);
			wait.until(ExpectedConditions.elementToBeClickable(locator));
			LOG.info("Clicked on the Locator");
			webDriver.findElement(locator).click();
			LOG.info("identified the element :: " + locator);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			status = true;
		} catch (Exception e) {
			status = false;
			LOG.info(e.getMessage());
			e.printStackTrace();
			// failureReport("Click : " + locatorName, msgClickFailure
			// + locatorName);
			throw new RuntimeException(e);
		} finally {
			if (!status) {

				failureReport("Click : " + locatorName, msgClickFailure + locatorName);

			} else {
				successReport("Click : " + locatorName, msgClickSuccess + locatorName);
			}

		}
		return status;
	}

	/**
	 * isElementPresent
	 * 
	 * @param by
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @param expected
	 *            of (boolean)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean isElementPresent(By by, String locatorName, boolean expected) throws Throwable {
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			highlight(webDriver.findElement(by));
			((JavascriptExecutor)webDriver).executeScript("arguments[0].scrollIntoView(true)",webDriver.findElement(by));
			this.successReport("isElementPresent : " + locatorName, this.msgIsElementFoundSuccess + locatorName);
			status = true;
		} catch (Exception e) {
			status = false;
			e.printStackTrace();
			LOG.info(e.getMessage());
			
		} finally {
			if (!status) {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				failureReport("isElementPresent : ", msgIsElementFoundFailure + locatorName);
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				successReport("isElementPresent : ", locatorName + ", isElementPresent : true");
			}

		}
		return status;
	}

	/**
	 * isElementPresent
	 * 
	 * @param by
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean isElementPresent(By by, String locatorName) throws Throwable {
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			dynamicWait(by);
			highlight(webDriver.findElement(by));
			((JavascriptExecutor)webDriver).executeScript("arguments[0].scrollIntoView(true)",webDriver.findElement(by));
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			status = true;
		} catch (Exception e) {
			status = false;
			LOG.info(e.getMessage());
			// throw new RuntimeException(e);
		}
		return status;
	}

	/**
	 * scroll
	 * 
	 * @param by
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean scroll(By by, String locatorName) throws Throwable {
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName());
			WebElement element = this.webDriver.findElement(by);
			Actions actions = new Actions(this.webDriver);
			actions.moveToElement(element);
			actions.build().perform();
			LOG.info("Scroll is performed : " + locatorName);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			status = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * JSScroll
	 * 
	 * @param by
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean JSScroll(By by, String locatorName) throws Throwable {
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName());
			WebElement element = this.webDriver.findElement(by);
			JavascriptExecutor js = ((JavascriptExecutor) this.webDriver);
			js.executeScript("arguments[0].scrollIntoView(true);", element);
			LOG.info("Scroll is performed : " + locatorName);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			status = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * verifyElementPresent
	 * 
	 * @param by
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @param expected
	 *            of (boolean)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean verifyElementPresent(By by, String locatorName, boolean expected) throws Throwable {
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName());
			if (this.webDriver.findElement(by).isDisplayed()) {
				this.successReport("VerifyElementPresent : " + locatorName,
						this.msgIsElementFoundSuccess + locatorName);
				LOG.info("Element is available :: " + locatorName);
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				status = true;
			} else {
				status = false;
			}
		} catch (Exception e) {
			status = false;
			LOG.info(e.getMessage());
			// throw new RuntimeException(e);
		}
		return status;
	}

	/**
	 * type
	 * 
	 * @param locator
	 *            of (By)
	 * @param testData
	 *            of (String)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean sendKeys(By locator, String testData, String locatorName) throws Throwable {
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : Type  ::  Locator : " + locatorName + " :: Data :" + testData);
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			LOG.info("Waiting for element :");
			// wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			LOG.info("Locator is Visible :: " + locator);
			wait.until(ExpectedConditions.elementToBeClickable(locator));
			webDriver.findElement(locator).click();
			LOG.info("Clicked on the Locator : ");
			webDriver.findElement(locator).clear();
			LOG.info("Cleared the existing Locator data : ");
			webDriver.findElement(locator).sendKeys(testData);
			LOG.info("Typed the Locator data :: " + testData);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

			successReport("Enter text in :: " + locatorName, msgTypeSuccess + testData);
			status = true;
		} catch (Exception e) {
			status = false;
			LOG.info(e.getMessage());
			failureReport("Enter text in :: " + locatorName, msgTypeFailure + testData);
			throw new RuntimeException(e);
		}
		return status;
	}

	/**
	 * typeUsingJavaScriptExecutor
	 * 
	 * @param locator
	 *            of (By)
	 * @param testData
	 *            of (String)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean typeUsingJavaScriptExecutor(By locator, String testData, String locatorName) throws Throwable {
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + locatorName);
			WebElement searchbox = webDriver.findElement(locator);
			JavascriptExecutor myExecutor = ((JavascriptExecutor) webDriver);
			myExecutor.executeScript("arguments[0].value=' " + testData + "'; ", searchbox);
			successReport("Enter text in :: " + locatorName, msgTypeSuccess + locatorName);
			LOG.info("Clicked on  : " + locatorName);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			status = true;
		} catch (Exception e) {
			status = false;
			LOG.info(e.getMessage());
			failureReport("Enter text in :: " + locatorName, msgTypeFailure + locatorName);
			throw new RuntimeException(e);
		}
		return status;
	}

	/**
	 * Moves the mouse to the middle of the element. The element is scrolled
	 * into view and its location is calculated using getBoundingClientRect.
	 *
	 * @param locator
	 *            : Action to be performed on element (Get it from Object
	 *            repository)
	 */
	public boolean waitForTitlePresent(By locator) throws Throwable {
		boolean flag = false;
		boolean bValue = false;
		try {
			for (int i = 0; i < 200; i++) {
				if (webDriver.findElements(locator).size() > 0) {
					flag = true;
					bValue = true;
					break;
				} else {
					webDriver.wait(50);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("WaitForTitlePresent :: ", "Title is wrong : ");
			} else {
				successReport("WaitForTitlePresent :: ", "Launched successfully expected Title : ");
			}
		}
		return bValue;
	}

	/**
	 * getTitle
	 * 
	 * @return String
	 * @throws Throwable
	 *             the throwable
	 */
	public String getTitle() throws Throwable {
		String text = webDriver.getTitle();
		{
			successReport("Title :: ", "Title of the page is :: " + text);
		}
		return text;
	}

	/**
	 * assertText
	 * 
	 * @param by
	 *            of (By)
	 * @param text
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean assertText(By by, String text) throws Throwable {
		boolean flag = false;
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
		try {
			Assert.assertEquals(getText(by, text).trim(), text.trim());
			flag = true;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (!flag) {
				failureReport("AssertText :: ", text + " is not present in the element : ");
				return false;
			} else {
				successReport("AssertText :: ", text + " is  present in the element : ");
			}
		}
	}

	/**
	 * assertTitle
	 * 
	 * @param title
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean assertTitle(String title) throws Throwable {
		boolean flag = false;
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
		try {
			By windowTitle = By.xpath("//title[contains(text(),'" + title + "')]");
			if (waitForTitlePresent(windowTitle)) {
				Assert.assertEquals(getTitle(), title);
				flag = true;
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				return true;
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			if (!flag) {
				failureReport("AsserTitle :: ", "Page title is not matched with : " + title);
				return false;
			} else {
				successReport("AsserTitle :: ", " Page title is verified with : " + title);
			}
		}
	}

	/**
	 * getText
	 * 
	 * @param locator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return String
	 * @throws Throwable
	 *             the throwable
	 */
	public String getText(By locator, String locatorName) throws Throwable {
		String text = "";
		boolean flag = false;
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
		try {
			if (isElementPresent(locator, locatorName, true)) {
				text = webDriver.findElement(locator).getText();
				LOG.info("Locator is Visible and text is retrieved :: " + text);
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				warningReport("GetText :: ", "Unable to get Text from :: " +locatorName);
				LOG.info("GetText :: Unable to get Text from :: " + locatorName);
			} else {
				successReport("GetText :: " + locatorName, "" + locatorName + " is :" + text);
				LOG.info("Locator is Visible and text is retrieved :: " + text);
			}
		}
		return text;
	}

	/**
	 * getAttributeByValue
	 * 
	 * @param locator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return String
	 * @throws Throwable
	 *             the throwable
	 */
	public String getAttributeByValue(By locator, String locatorName) throws Throwable {
		String text = "";
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName());
			if (isElementPresent(locator, locatorName, true)) {
				text = webDriver.findElement(locator).getAttribute("value");
				LOG.info("Locator is Visible and attribute value is retrieved :: " + text);
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				warningReport("GetAttribute :: ", "Unable to get Attribute value from :: " + locatorName);
				LOG.info("GetAttribute :: Unable to get Attribute value from :: " + locatorName);
			} else {
				successReport("GetAttribute :: ", "" + locatorName + " is" + text);
				LOG.info("Locator is Visible and attribute value is retrieved :: " + text);
			}
		}
		return text;
	}

	/**
	 * getAttributeByValue
	 * 
	 * @param locator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return String
	 * @throws Throwable
	 *             the throwable
	 */
	public String getAttributeByClass(By locator, String locatorName) throws Throwable {
		String text = "";
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName());
			if (isElementPresent(locator, locatorName, true)) {
				text = webDriver.findElement(locator).getAttribute("class");
				LOG.info("Locator is Visible and attribute value is retrieved :: " + text);
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				warningReport("GetAttribute :: ", "Unable to get Attribute value from :: " + locatorName);
				LOG.info("GetAttribute :: Unable to get Attribute value from :: " + locatorName);
			} else {
				successReport("GetAttribute :: ", "" + locatorName + " is" + text);
				LOG.info("Locator is Visible and attribute value is retrieved :: " + text);
			}
		}
		return text;
	}

	/**
	 * Moves the mouse to the middle of the element. The element is scrolled
	 * into view and its location is calculated using getBoundingClientRect.
	 *
	 * @param locator
	 *            : Action to be performed on element (Get it from Object
	 *            repository)
	 * @param locatorName
	 *            : Meaningful name to the element (Ex:link,menus etc..)
	 */
	public boolean mouseHover(By locator, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Mouse over start :: " + locatorName);
			WebElement mo = this.webDriver.findElement(locator);
			new Actions(this.webDriver).moveToElement(mo).build().perform();
			flag = true;
			LOG.info("Mouse over End :: " + locatorName);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++");
			return true;
		} catch (Exception e) {
			// return false;
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("MouseOver :: ", "MouseOver action is not perform on ::" + locatorName);
			} else {
				this.successReport("MouseOver :: ", "MouserOver Action is Done on  :: " + locatorName);
			}
		}
	}

	/**
	 * JSClick
	 * 
	 * @param locator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean JSClick(By locator, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			// added the loggers for click method

			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			LOG.info("Method : click  ::  Locator : " + locatorName);
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			LOG.info("Waiting for element");
			// internalServerErrorHandler();
			// wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			LOG.info("Locator is Visible :: " + locator);
			wait.until(ExpectedConditions.elementToBeClickable(locator));

			WebElement element = this.webDriver.findElement(locator);
			JavascriptExecutor executor = (JavascriptExecutor) this.webDriver;
			executor.executeScript("arguments[0].click();", element);
			// LOG.info("Successfully clicked on :: " + locatorName);
			// driver.executeAsyncScript("arguments[0].click();", element);
			flag = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				LOG.info("Inside Finally block");
				failureReport("Click : " + locatorName, "Click is not performed on : " + locatorName);
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				this.successReport("Click : " + locatorName, "Successfully click on  : " + locatorName);
			}
		}
		return flag;
	}

	/**
	 * JSClickUntil
	 * 
	 * @param locator
	 *            of (By)
	 * @param waitLocator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean JSClickUntil(By locator, By waitLocator, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			// wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			wait.until(ExpectedConditions.elementToBeClickable(locator));
			WebElement element = this.webDriver.findElement(locator);
			JavascriptExecutor executor = (JavascriptExecutor) this.webDriver;
			int icounter = 0;
			do {
				try {
					if (isElementPresent(waitLocator, "Wait for Element : " + locatorName)) {
						flag = true;
						break;
					} else {
						icounter = icounter + 1;
					}
					if (icounter >= 3) {
						flag = false;
						break;
					}
					executor.executeScript("arguments[0].click();", element);
				} catch (Exception e) {
					LOG.info("Retrying for the object :: " + waitLocator + " :: Iteration : " + icounter);
				}

			} while (icounter <= 3);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("Click : ", "Click action is not perform on : " + locatorName);
				// return flag;
			} else {
				this.successReport("Click : ", "Clicked : " + locatorName);
				// return flag;
			}
		}
		return flag;
	}

	/**
	 * clickUntil
	 * 
	 * @param locator
	 *            of (By)
	 * @param waitLocator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean clickUntil(By locator, By waitLocator, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method :" + getCallerMethodName() + "  ::  Locator : " + locatorName);
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			LOG.info("Waiting for element");
			// internalServerErrorHandler();
			// wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			LOG.info("Locator is Visible :: " + locator);
			wait.until(ExpectedConditions.elementToBeClickable(locator));
			LOG.info("Clicked on the Locator");

			int icounter = 0;
			do {
				try {
					if (isElementPresent(waitLocator, "Wait for Element : " + locatorName)) {
						flag = true;
						break;
					} else {
						icounter = icounter + 1;
					}
					if (icounter >= 3) {
						flag = false;
						break;
					}
					webDriver.findElement(locator).click();
				} catch (Exception e) {
					LOG.info("Retrying for the object :: " + waitLocator + " :: Iteration : " + icounter);
				}

			} while (icounter <= 3);

			LOG.info("identified the element :: " + locator);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			successReport("Click : " + locatorName, msgClickSuccess + locatorName);
		} catch (Exception e) {
			flag = false;
			LOG.info(e.getMessage());
			failureReport("Click : " + locatorName, msgClickFailure + locatorName);
			throw new RuntimeException(e);
		}
		return flag;
	}

	/**
	 * jsMouseHover
	 * 
	 * @param locator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean jsMouseHover(By locator, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method :" + getCallerMethodName() + "  ::  Locator : " + locatorName);
			WebElement HoverElement = this.webDriver.findElement(locator);
			String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover',true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
			((JavascriptExecutor) this.webDriver).executeScript(mouseOverScript, HoverElement);
			LOG.info("JSmousehover is performed  on :: " + locatorName);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			flag = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("MouseOver : ", "MouseOver action is not perform on : " + locatorName);
				// return flag;
			} else {
				this.successReport("MouseOver : ", "MouserOver Action is Done on" + locatorName);
				// return flag;
			}
		}
		return flag;
	}

	/**
	 * getWebElementList
	 * 
	 * @param by
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return List<WebElement>
	 * @throws Throwable
	 *             the throwable
	 */
	public List<WebElement> getWebElementList(By by, String locatorName) throws Throwable {
		List<WebElement> elements = null;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method :" + getCallerMethodName() + "  ::  Locator : " + locatorName);
			WebDriverWait wait = new WebDriverWait(this.webDriver, timeValue);
			wait.until(ExpectedConditions.presenceOfElementLocated(by));
			elements = webDriver.findElements(by);
			LOG.info("Size of List ::" + elements.size());
			for (int i = 0; i < elements.size(); i++) {
				// elements = this.driver.findElements(by);
				LOG.info("List value are :: " + elements.get(i).getText());
			}
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return elements;
	}

	/**
	 * elementLoadingTime
	 * 
	 * @param locator
	 *            of (By)
	 * @return float
	 * @throws Throwable
	 *             the throwable
	 */
	public float elementLoadingTime(By locator) throws Throwable {

		float timeTaken = 0;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			long start = System.currentTimeMillis();
			LOG.info("start " + start);
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			wait.until(ExpectedConditions.elementToBeClickable(locator));
			// wait.until(ExpectedConditions.elementToBeClickable(locator));
			long stop = System.currentTimeMillis();
			timeTaken = (stop - start);
			LOG.info("The time taken for the page to load is : " + timeTaken + " milli seconds : ");
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return timeTaken;
	}

	/**
	 * elementVisibleTime
	 * 
	 * @param locator
	 *            of (By)
	 * @return void
	 * @throws Throwable
	 *             the throwable
	 */
	public void elementVisibleTime(By locator) throws Throwable {

		float timeTaken = 0;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			long start = System.currentTimeMillis();
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			long stop = System.currentTimeMillis();
			timeTaken = (stop - start) / 1000;
			LOG.info("Took : " + timeTaken + " secs to display the results : ");
			successReport("Total time taken for element visible :: ",
					"Time taken load the element :: " + timeTaken + " seconds");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Moves the mouse to the middle of the element. The element is scrolled
	 * into view and its location is calculated using getBoundingClientRect.
	 *
	 * @param destinationLocator
	 *            : Action to be performed on element (Get it from Object
	 *            repository)
	 * @param locatorName
	 *            : Meaningful name to the element (Ex:link,menus etc..)
	 */
	public boolean dragAndDrop(By souceLocator, By destinationLocator, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			Actions builder = new Actions(this.webDriver);
			WebElement souceElement = this.webDriver.findElement(souceLocator);
			WebElement destinationElement = this.webDriver.findElement(destinationLocator);
			/*
			 * Action dragAndDrop =
			 * builder.clickAndHold(souceElement).moveToElement(
			 * destinationElement) .release(destinationElement).build();
			 * dragAndDrop.perform();
			 */
			builder.dragAndDrop(souceElement, destinationElement).build().perform();
			flag = true;
			LOG.info("drag and drop performed ");
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("DragDrop : ", "Drag and Drop action is not performed on : " + locatorName);
			} else {
				this.successReport("DragDrop : ", "Drag and Drop Action is Done on : " + locatorName);
			}
		}
	}

	/**
	 * navigateTo
	 * 
	 * @param Url
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean navigateTo(String Url) throws Throwable {
		boolean flag = false;
		try {
			webDriver.navigate().to(Url);
			LOG.info("Navigated URL is : " + Url);
			flag = true;
			return flag;
		} catch (Exception e) {
			flag = false;
			LOG.info(e.getMessage());
		} finally {
			if (!flag) {
				failureReport("Unable to Open : ", Url);
				return false;
			} else {
				successReport("Successfully Opened : ", Url);
			}
		}
		return flag;
	}

	/**
	 * generateRandomNumber
	 * 
	 * @return int
	 * @throws Throwable
	 *             the throwable
	 */
	public int generateRandomNumber() throws Throwable {
		Random generator = new Random();
		int intRandom_number = generator.nextInt(9999) + 10000;
		return intRandom_number;
	}

	/**
	 * rightClick
	 * 
	 * @param locator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean rightClick(By locator, String locatorName) throws Throwable {
		boolean status;
		try {
			// added loggers
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			Actions action = new Actions(webDriver);
			action.contextClick(webDriver.findElement(locator)).build().perform();
			webDriver.findElement(locator).click();
			successReport("Click : " + locatorName, msgRightClickSuccess + locatorName);
			LOG.info("Right click performed  on :: " + locatorName);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			status = true;
		} catch (Exception e) {
			status = false;
			LOG.info(e.getMessage());
			failureReport("Click : " + locatorName, msgRightClickFailure + locatorName);
			throw new RuntimeException(e);
		}
		return status;
	}

	/**
	 * dynamicWait
	 * 
	 * @param locator
	 *            of (By)
	 * @return void
	 * @throws Throwable
	 *             the throwable
	 */
	public void dynamicWait(By locator) throws Throwable {
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locator);
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			wait.until(ExpectedConditions.presenceOfElementLocated(locator));
			LOG.info(locator + ":: displayed succussfully");
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			LOG.info(e.getMessage());
			// failureReport("Unable to find Element :: " + locator,
			// msgIsElementFoundFailure + locator);
			// throw new RuntimeException(e);

		}
	}

	/**
	 * getCallerClassName
	 * 
	 * @return String
	 */
	public static String getCallerClassName() {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		return stElements[3].getClassName();
	}

	/**
	 * getCallerMethodName
	 * 
	 * @return String
	 */
	public static String getCallerMethodName() {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		return stElements[3].getMethodName();
	}

	/**
	 * Double click the mouse to the middle of the element. The element is
	 * scrolled into view and its location is calculated using
	 * getBoundingClientRect.
	 *
	 * @param locator
	 *            : Action to be performed on element (Get it from Object
	 *            repository)
	 * @param locatorName
	 *            : Meaningful name to the element (Ex:link,menus etc..)
	 */
	public boolean mouseDoubleClick(By locator, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Mouse Double Click start :: " + locatorName);
			WebElement mo = this.webDriver.findElement(locator);
			new Actions(this.webDriver).moveToElement(mo).doubleClick(mo).build().perform();
			flag = true;
			LOG.info("Mouse Double Click :: " + locatorName);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++");
			return true;
		} catch (Exception e) {
			// return false;
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("double Click :: ", "double Click action is not perform on ::" + locatorName);
			} else {
				successReport("double Click :: ", "double Click Action is Done on  :: " + locatorName);
			}
		}
	}

	/**
	 * click the mouse to the middle of the element. The element is scrolled
	 * into view and its location is calculated using getBoundingClientRect.
	 *
	 * @param locator
	 *            : Action to be performed on element (Get it from Object
	 *            repository)
	 * @param locatorName
	 *            : Meaningful name to the element (Ex:link,menus etc..)
	 */
	public boolean mouseClick(By locator, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Mouse Double Click start :: " + locatorName);
			WebElement mo = this.webDriver.findElement(locator);
			new Actions(this.webDriver).click(mo).build().perform();
			flag = true;
			LOG.info("Mouse Double Click :: " + locatorName);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++");
			return true;
		} catch (Exception e) {
			// return false;
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("Click :: ", "Click action is not perform on ::" + locatorName);
			} else {
				this.successReport(" Click :: ", " Click Action is Done on  :: " + locatorName);
			}
		}
	}

	/**
	 * getYear, Function to get required year e.g: 0-Current year, 1-Next year,
	 * 
	 * @param number
	 *            of (int) Number to get year (e.g: -1,0,1 etc)
	 * @return int
	 * @throws Throwable
	 *             the throwable
	 */
	public int getYear(int number) throws Throwable {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR) + number;
		LOG.info("Year is : " + year);
		return year;
	}

	/**
	 * dateFormatVerification, Function to verify date format by giving actual
	 * date
	 * 
	 * @param actualDate
	 *            of (String) actual date e.g: 21-11-2015
	 * @param formatToVerify
	 *            of (String) format type e.g: dd-MM-yyyy
	 * @return boolean
	 */
	public boolean dateFormatVerification(String actualDate, String formatToVerify) {
		boolean flag = false;

		if (actualDate.toLowerCase().contains("am")) {
			flag = formatVerify(actualDate, formatToVerify);
		} else if (actualDate.toLowerCase().contains("pm")) {
			flag = formatVerify(actualDate, formatToVerify);
		} else if (!actualDate.toLowerCase().contains("am") || !actualDate.toLowerCase().contains("pm")) {
			flag = formatVerify(actualDate, formatToVerify);
		}
		return flag;
	}

	/**
	 * formatVerify, Reusable Function to verify date format by giving actual
	 * date
	 * 
	 * @return : boolean
	 * @param actualDate
	 *            of (String)e.g: 21-11-2015
	 * @param formatToVerify
	 *            of (String) type e.g: dd-MM-yyyy
	 */
	public boolean formatVerify(String actualDate, String formatToVerify) {
		boolean flag = false;
		try {
			SimpleDateFormat sdf;
			sdf = new SimpleDateFormat(formatToVerify);
			Date date = sdf.parse(actualDate);
			String formattedDate = sdf.format(date);
			if (actualDate.equals(formattedDate)) {
				flag = true;
			} else {
				flag = false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return flag;
	}

	/**
	 * replaceAll, Function to replace the regular expression values with client
	 * required values
	 * 
	 * @return : String
	 * @param text
	 *            of (String)
	 * @param pattern
	 *            of (String), regular expression of actual value
	 * @param replaceWith
	 *            of (String), value to replace the actual
	 */
	public String replaceAll(String text, String pattern, String replaceWith) {
		String flag = null;
		try {
			flag = text.replaceAll(pattern, replaceWith);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return flag;
	}

	/**
	 * subString, Function to get sub string of given actual string text
	 * 
	 * @return : String
	 * @param text
	 *            of (String), Actual text
	 * @param startIndex
	 *            of (int), Start index of sub string
	 * @param endIndex
	 *            of (int), end index of sub string
	 */
	public String subString(String text, int startIndex, int endIndex) {
		String flag = null;
		try {
			flag = text.substring(startIndex, endIndex);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return flag;
	}

	/**
	 * getCssValue, Function to get the value of a given CSS property (e.g.
	 * width)
	 * 
	 * @return : String
	 * @param locator
	 *            of (By)
	 * @param cssValue
	 *            of (String), CSS property
	 */
	public String getCssValue(By locator, String cssValue) {
		String result = "";
		try {
			result = this.webDriver.findElement(locator).getCssValue(cssValue);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * getBackGroundColor, Function to get the background color of a given web
	 * element (e.g. background-color)
	 * 
	 * @return : String
	 * @param locator
	 *            of (By)
	 * @param cssValue
	 *            of (String), CSS property (e.g. background-color)
	 */
	public String getBackGroundColor(By locator, String cssValue) {
		String hexColor = "";
		try {
			String bColor = this.webDriver.findElement(locator).getCssValue(cssValue);
			hexColor = Color.fromString(bColor).asHex();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return hexColor;
	}

	/**
	 * switchToFrame, Function to switch to frame
	 * 
	 * @return : void
	 * @param locator
	 *            of (By)
	 */
	public void switchToFrame(By locator) {
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
		WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
		LOG.info("Waiting for element");
		wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		LOG.info("Locator is Visible :: " + locator);
		wait.until(ExpectedConditions.presenceOfElementLocated(locator));
		webDriver.switchTo().frame(webDriver.findElement(locator));
	}

	/**
	 * getCurrentDateTime, Function to get current time in client required
	 * format
	 * 
	 * @param dateTimeFormat
	 *            of (String), format to get date and time (e.g: h:mm)
	 * @return : String
	 */
	public String getCurrentDateTime(String dateTimeFormat) throws Throwable {
		DateFormat dateFormat = new SimpleDateFormat(dateTimeFormat);
		Date date = new Date();
		return dateFormat.format(date);
	}

	/**
	 * getFutureDateTime, Function to get future or past date in client required
	 * format
	 * 
	 * @return : String
	 * @param dateTimeFormat
	 *            of (String), format to get date and time (e.g: MM/dd/yyyy)
	 * @param days
	 *            of (int), number to get date E.g. 1:Tomorrow date, -1:
	 *            Yesterday date
	 */
	public String getFutureDateTime(String dateTimeFormat, int days) throws Throwable {
		SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, days);
		Date tomorrow = calendar.getTime();
		return sdf.format(tomorrow);
	}

	/**
	 * assertTextStringContains, Assert text string matching.
	 * 
	 * @param actText
	 *            of (String)
	 * @param expText
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean assertTextStringContains(String actText, String expText) throws Throwable {
		boolean flag = false;
		try {
			// added loggers
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			String ActualText = actText.trim();
			LOG.info("act - " + ActualText);
			LOG.info("exp - " + expText);
			if (ActualText.contains(expText.trim())) {
				LOG.info("in if loop");
				flag = true;
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				return true;
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (!flag) {
				failureReport("Verify : " + expText, actText + " is not present in the element : ");
				return false;
			} else if (flag) {
				successReport("Verify : " + expText, actText + " is  present in the element : ");
			}
		}
	}

	/**
	 * deleteDirectory, Delete directory from local machine
	 *
	 * @param directoryPath
	 *            of (String), path for the directory to delete
	 * @return void
	 * @throws IOException
	 */
	public void deleteDirectory(String directoryPath) throws IOException {
		FileUtils.deleteDirectory(new File(directoryPath));
	}

	/**
	 * getRandomString, Get random String
	 *
	 * @param noOfCharacters
	 *            of (int), Number of characters to get randomly
	 * @return String
	 * @throws IOException
	 */
	public String getRandomString(int noOfCharacters) throws IOException {
		return RandomStringUtils.randomAlphabetic(noOfCharacters);
	}

	/**
	 * getRandomNumeric, Get random Numeric
	 *
	 * @param noOfCharacters
	 *            of (int), Number of characters to get randomly
	 * @return String
	 * @throws IOException
	 */
	public String getRandomNumeric(int noOfCharacters) throws IOException {
		return RandomStringUtils.randomNumeric(noOfCharacters);
	}

	/**
	 * getAttributeValue, Function to get the value of a given attribute (e.g.
	 * class)
	 * 
	 * @return : String
	 * @param locator
	 *            of (By)
	 * @param attributeName
	 *            of (String)
	 */
	public String getAttributeValue(By locator, String attributeName) {
		String result = "";
		try {
			result = this.webDriver.findElement(locator).getAttribute(attributeName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * refreshPage
	 * 
	 * @return void
	 * @throws Throwable
	 */
	public void refreshPage() throws Throwable {
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName());
			webDriver.navigate().refresh();
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			e.printStackTrace();
			LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
		}
	}

	/**
	 * clearData, Clear value from textBox
	 * 
	 * @param locator
	 *            of (By)
	 * @return void
	 * @throws Throwable
	 */
	public void clearData(By locator) throws Throwable {
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName());
			WebElement element = webDriver.findElement(locator);
			element.sendKeys(Keys.CONTROL + "a");
			element.sendKeys(Keys.DELETE);
			element.clear();
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			e.printStackTrace();
			LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
		}
	}

	/**
	 * keyBoardOperations
	 * 
	 * @param locator
	 *            of (By)
	 * @param testData
	 *            of (Keys)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 */
	public boolean keyBoardOperations(By locator, Keys testData, String locatorName) throws Throwable {
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : Type  ::  Locator : " + locatorName + " :: Data :" + testData);
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			LOG.info("Waiting for element :");
			// wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			LOG.info("Locator is Visible :: " + locator);
			wait.until(ExpectedConditions.elementToBeClickable(locator));
			webDriver.findElement(locator).sendKeys(testData);
			LOG.info("Typed the Locator data :: " + testData);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

			successReport("Enter text in :: " + locatorName, msgTypeSuccess + testData);
			status = true;
		} catch (Exception e) {
			status = false;
			LOG.info(e.getMessage());
			failureReport("Enter text in :: " + locatorName, msgTypeFailure + testData);
			throw new RuntimeException(e);
		}
		return status;
	}

	/**
	 * keyBoardOperations
	 * 
	 * @param locator
	 *            of (By)
	 * @param testData
	 *            of (String)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 */
	public boolean typeWitoutReport(By locator, String testData, String locatorName) throws Throwable {
		boolean status = false;
		/* try { */
		// Waittime();
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
		LOG.info("Method : Type  ::  Locator : " + locatorName + " :: Data :" + testData);
		// WebDriverWait wait = new WebDriverWait(driver, 10);
		LOG.info("Waiting for element :");
		webDriver.findElement(locator).click();
		LOG.info("Clicked on the Locator : ");
		webDriver.findElement(locator).clear();
		LOG.info("Cleared the existing Locator data : ");
		webDriver.findElement(locator).sendKeys(testData);
		LOG.info("Typed the Locator data :: " + testData);
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		status = true;
		/*
		 * } catch (Exception e) { status = false; LOG.info(e.getMessage()); }
		 */
		return status;
	}

	/**
	 * Switch to frame using index value
	 *
	 * @param index
	 *            of (int), frame number to switch
	 * @return void
	 */
	public void switchToFrameByIndex(int index) {
		webDriver.switchTo().frame(index);
	}

	/**
	 * come out from frame
	 * 
	 * @return void
	 */
	public void comeOutFromFrame() {
		webDriver.switchTo().defaultContent();
	}

	/**
	 * Click on OK button on alert
	 *
	 * @return void
	 */
	public void acceptAlert() {
		try {

			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			wait.until(ExpectedConditions.alertIsPresent());
			webDriver.switchTo().alert().accept();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * findWebElement
	 * 
	 * @param locator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return WebElement
	 * @throws Throwable
	 */
	public WebElement findWebElement(By locator, String locatorName) throws Throwable {
		WebElement element;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : click  ::  Locator : " + locatorName);
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			LOG.info("Waiting for element");
			LOG.info("Locator is Visible :: " + locator);
			wait.until(ExpectedConditions.elementToBeClickable(locator));
			LOG.info("Clicked on the Locator");
			element = webDriver.findElement(locator);
			LOG.info("identified the element :: " + locator);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			LOG.info(e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return element;
	}

	/**
	 * checkBoxIsChecked
	 * 
	 * @param by
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @param expected
	 *            of (boolean)
	 * @return boolean
	 * @throws Throwable
	 */
	public boolean checkBoxIsChecked(By by, String locatorName, boolean expected) throws Throwable {
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			webDriver.findElement(by).isSelected();
			this.successReport("checkBoxIsChecked : " + locatorName, this.msgIsElementFoundSuccess + locatorName);
			status = true;
		} catch (Exception e) {
			status = false;
			LOG.info(e.getMessage());

		} finally {
			if (!status) {

				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				failureReport("checkBoxIsChecked : ", msgCheckboxisnotChecked + locatorName);
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				successReport("checkBoxIsChecked : ", locatorName + ", checkBoxIsChecked : true");
			}

		}
		return status;
	}

	/**
	 * switchToWindow, Function to switch to latest window
	 * 
	 * @return : void
	 */
	public void switchToWindow() {
		for (String handle : webDriver.getWindowHandles()) {
			webDriver.switchTo().window(handle);
		}
	}

	/**
	 * switchToParentWindow, Function to switch to parent window
	 * 
	 * @return void
	 * @param handle
	 *            of (String), window handle to switch
	 */
	public void switchToParentWindow(String handle) {
		webDriver.switchTo().window(handle);
	}

	/**
	 * closeWindow, Function to close the currently focused window
	 * 
	 * @return : void
	 */
	public void closeWindow() {
		webDriver.close();
	}

	/**
	 * getWindowHandle, Function to get the current window handle
	 * 
	 * @return : String
	 */
	public String getWindowHandle() {
		return webDriver.getWindowHandle();
	}

	/**
	 * scrollToWebElement, Function to scroll to a particular element
	 * 
	 * @param element
	 *            of (By)
	 * @return : void
	 */
	public void scrollToWebElement(By element) {
		JavascriptExecutor jse = (JavascriptExecutor) webDriver;
		jse.executeScript("arguments[0].scrollIntoView(true);", webDriver.findElement(element));
	}

	public void deleteSpecificFile(String fileName) throws InterruptedException {
		try {
			File file = new File(fileName);
			if (file.delete()) {
				System.out.println(file.getName() + " is deleted!");
			} else {
				System.out.println("Delete operation is failed.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * findWebElementVisibility, Function returns WebElement
	 * 
	 * @return : WebElement
	 */

	public WebElement findWebElementVisibility(By locator, String locatorName) throws Throwable {
		WebElement element;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : click  ::  Locator : " + locatorName);
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			LOG.info("Waiting for element");
			LOG.info("Locator is Visible :: " + locator);
			wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			LOG.info("Element Found on the Locator");
			element = webDriver.findElement(locator);
			LOG.info("identified the element :: " + locator);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			LOG.info(e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return element;
	}

	public boolean isCheckBoxSelected(By locator) {
		boolean flag = false;
		flag = webDriver.findElement(locator).isSelected();
		return flag;
	}

	/**
	 * isVisible
	 * 
	 * @param locator
	 *            of (By)
	 * @param locatorName
	 *            of (String)
	 * @return boolean
	 * @throws Throwable
	 *             the throwable
	 */
	public boolean isVisibleOnly(By locator, String locatorName) throws Throwable {
		boolean flag = false;
		try {
			// added loggers
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name :: " + getCallerClassName() + " Method name :: " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			// value = driver.findElement(locator).isDisplayed();
			flag = webDriver.findElement(locator).isDisplayed();
			// value = true;
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			flag = false;

		}
		return flag;
	}

	/**
	 * isVisible locator of (By) locatorName of (String) boolean
	 * 
	 * @throws Throwable
	 *             the throwable
	 */
	public long differenceBetweenTwoDates(String date1, String date2, String dateFormat) throws Throwable {
		long diffDays = 0;
		try {
			SimpleDateFormat format = new SimpleDateFormat(dateFormat);
			Date d1 = format.parse(date1);
			Date d2 = format.parse(date2);
			long diff = d2.getTime() - d1.getTime();
			diffDays = diff / (24 * 60 * 60 * 1000) + 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return diffDays;
	}
	
	/**
	 * @FunctionName highlight
	 * @Description Function to highlight the element
	 * @param element
	 * @return void
	 * @throws Throwable
	 * @author PriyaBatchu
	 */
	public void highlight(WebElement element) {

		if (webDriver instanceof JavascriptExecutor) {
			((JavascriptExecutor) webDriver).executeScript(
					"arguments[0].style.border='4px solid green'", element);

		}
	}
		
}
