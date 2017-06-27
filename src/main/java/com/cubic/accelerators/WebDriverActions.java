package com.cubic.accelerators;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
import com.cubic.logutils.Log4jUtil;
import com.cubic.reportengine.report.CustomReports;

public class WebDriverActions {
	private final Logger LOG = Logger.getLogger(this.getClass().getName());
	private final String msgClickSuccess = "Successfully Clicked On ";
	private final String msgClickFailure = "Unable To Click On ";
	private final String msgRightClickSuccess = "Successfully Mouse Right Clicked On ";
	private final String msgTypeSuccess = "Successfully Entered value ";
	private final String msgTypeFailure = "Unable To Type On ";
	private final String msgIsElementFoundSuccess = "Successfully Found Element ";
	private final String msgIsElementFoundFailure = "Unable To Found Element ";
	private final String msgCheckboxisnotChecked = "Checkbox is not Selected";

	private WebDriver webDriver = null;
	private CustomReports customReports = null;
	private String testCaseName = null;
	
	private int timeValue  = Integer.parseInt(GenericConstants.GENERIC_FW_CONFIG_PROPERTIES.get("webdriver_dynamicwait_time"));
	
	/**
	 * This Constructor is used internally with in the Selenium Libarary(i.e. WebDriver engine).
	 * 
	 * @param webDriver reference variable is declared with in the class  
	 * @param customReports reference variable is declared with in the class 
	 * @param testCaseName reference variable is declared with in the class(testCaseName should be in the format &lt;&lt;TESTCASE_ID&gt;&gt; : &lt;&lt;TESTCASE DESCRIPTION&gt;&gt;)
	 */
	WebDriverActions(WebDriver webDriver, CustomReports customReports, String testCaseName) {
		this.webDriver = webDriver;
		this.customReports = customReports;
		this.testCaseName = testCaseName;
	}
	
	/**
	 * Constructor should be used explicitly, if you want to access the webDriver when the automation script doesn't extend the WebDriverEngine.<br>
	 * This need to be used while instantiating a separate webdriver object in script development where you are extending other Engines like  
	 * RESTEngine instead of extending WebDriverEngine.
	 * 
	 * @param customReports reference variable is declared with in the class
	 * @param testCaseName reference variable is declared with in the class(testCaseName should be in format &lt;&lt;TESTCASE_ID&gt;&gt; : &lt;&lt;TESTCASE DESCRIPTION&gt;&gt;)
	 * @param browserName to initialise the browser
	 * @param seleniumGridUrl url of seleniumGrid server
	 * @throws IOException java.io.IOException
	 * @throws InterruptedException java.lang.InterruptedException
	 */
	public WebDriverActions(CustomReports customReports, String testCaseName, String browserName, String seleniumGridUrl)
			throws IOException, InterruptedException {

		this.webDriver = getWebDriverForLocal(browserName, seleniumGridUrl);
		this.customReports = customReports;
		this.testCaseName = testCaseName;
	}
	
	/**
	 * Used to call selenium actions directly to use them in test scripts
	 * @return current webDriver instance
	 */
	public WebDriver getWebDriver (){
		return this.webDriver;
	}
	
	/**
	 * Tear down process to quit the webDriverAction instances
	 * @param webDriverActions instance to quit
	 */
	public static void flush(WebDriverActions webDriverActions){
		if(webDriverActions.webDriver!=null){
			webDriverActions.webDriver.quit();
		}
		
		if(webDriverActions!=null){
			webDriverActions = null;
		}
	}

	/**
	 * For adding the failure step to the detailed report
	 * Current web page instance will be captured as screenshot and linked to the step 
	 *   
	 *  @param stepName is description about action performed to display in customised detail report
	 * 	@param description is about the actual behaviour to display in customised detail report
	 */
	public void failureReport(String stepName, String description) {
		if (customReports != null) {
			File screenshotFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
			customReports.failureReportWeb(stepName, description, screenshotFile, testCaseName);
		}
	}

	/**
	 * For adding the warning step to the detailed report.
	 * 
	 * @param stepName is description about action performed to display in customised detail report 
	 * @param description is about the actual behaviour to display in customised detail report
	 */		
	public void warningReport(String stepName, String description) {
		if (customReports != null) {
			customReports.warningReport(stepName, description, testCaseName); 
		}
	}
	
	/**
	 * For adding the success step to the detailed report.
	 * 
	 * @param stepName is description about action performed to display in customised detail report 
	 * @param description is about the actual behaviour to display in customised detail report
	 */	
	public void successReport(String stepName, String description) {
		if (customReports != null) {
			customReports.successReport(stepName, description, testCaseName);
		}
	}

	/**
	 * For adding the success step to the detailed report with screenshot reference. 
	 * Current web page instance will be captured as screenshot and linked to the step.
	 *   
	 *  @param stepName is description about action performed to display in customised detail report
	 * 	@param description is about the actual behaviour to display in customised detail report
	 */
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
		
		case WebDriverConstants.FIREFOX_BROWSER:
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
			
			if(seleniumGridUrl == null || seleniumGridUrl.equalsIgnoreCase(WebDriverConstants.LOCAL)){
				webDriver = new FirefoxDriver(firefoxProfile);
			}
			
			break;
			
		case WebDriverConstants.IE_BROWSER:
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
			
			if(seleniumGridUrl == null || seleniumGridUrl.equalsIgnoreCase(WebDriverConstants.LOCAL)){
				webDriver = new InternetExplorerDriver(capabilities);
			}
			
			break;	
			
		case WebDriverConstants.CHROME_BROWSER:
			String chromeDriverPath = GenericConstants.GENERIC_FW_CONFIG_PROPERTIES.get("chrome_driver_path");
			String chromeDownloadDirPath = GenericConstants.GENERIC_FW_CONFIG_PROPERTIES.get("chrome_downloadDir_Path");
			
			HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
            chromePrefs.put("profile.default_content_settings.popups", 0);
            chromePrefs.put("credentials_enable_service", false);
            chromePrefs.put("profile.password_manager_enabled", false);
            
          

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
			options.addArguments("--disable-web-security");
			capabilities.setCapability(ChromeOptions.CAPABILITY, options);
			
			if(seleniumGridUrl == null ||seleniumGridUrl.equalsIgnoreCase(WebDriverConstants.LOCAL)){
				webDriver = new ChromeDriver(capabilities);
			}
			
			break;
			
		case WebDriverConstants.EDGE_BROWSER:
			String edgeDriverPath = GenericConstants.GENERIC_FW_CONFIG_PROPERTIES.get("edge_driver_path");
			System.setProperty("webdriver.edge.driver", edgeDriverPath);
			capabilities = DesiredCapabilities.edge();		
			
			if(seleniumGridUrl == null ||seleniumGridUrl.equalsIgnoreCase(WebDriverConstants.LOCAL)){
				webDriver = new EdgeDriver(capabilities);
			}
		
			break;

		case WebDriverConstants.SAFARI_BROWSER:

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
						
			break;			
		}
		
		if(seleniumGridUrl!=null && !seleniumGridUrl.equalsIgnoreCase(WebDriverConstants.LOCAL)){
			webDriver = new RemoteWebDriver(new URL(seleniumGridUrl), capabilities);
		}
		
		webDriver.manage().window().maximize();
		webDriver.manage().timeouts().implicitlyWait(implicitlyWaitTime, TimeUnit.SECONDS);
		
		return webDriver;			
	}	
	
	/**
	 * Launches web page with provided URL
	 * @param url of the web page
	 * @return boolean value indicating success of the operation          
	 */
	
	public boolean navigateToUrl(String url) {
		boolean flag = false;
		try {
			webDriver.get(url);
			successReport("Navigated to url", "Navigated to url '" + url + "'");
			flag = true;
		} catch (Exception e) {
			failureReport("Navigated to url", "Unable to navigate to url '" + url + "'");
			flag = false;
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		}
		return flag;
	}
	
	/**
	 * Selects a value from dropdown based on index of the value
	 * @param locator of element
	 * @param index of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean selectByIndex(By locator, int index, String locatorName)  {
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
			LOG.error(Log4jUtil.getStackTrace(e));
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
	 * Asserts the condition
	 * @param condition of boolean
	 * @param message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean assertTrue(boolean condition, String message)  {
		try {
			if (condition)
				return true;
			else
				return false;
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
			LOG.error(Log4jUtil.getStackTrace(e));
			return false;
		} finally{
			if (!condition) {
				failureReport("Expected :: " + message, message + " is :: " + condition);
			} else {
				successReport("Expected :: " + message, message + " is :: " + condition);
			}
		}
	}

	/**
	 * Waits for element presence
	 * @param locator of element
	 * @param time of integer
	 */
	public void dynamicWaitByLocator(By locator, int time){
		try {
			WebDriverWait wait = new WebDriverWait(webDriver, time);
			wait.until(ExpectedConditions.presenceOfElementLocated(locator));
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
		}
	}

	/**
	 * Waits for element presence
	 * @param locator of element
	 */
	public void dynamicWaitByLocator(By locator){
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locator);
			WebDriverWait wait = new WebDriverWait(webDriver,timeValue);
			wait.until(ExpectedConditions.presenceOfElementLocated(locator));
			LOG.info(locator + ":: displayed succussfully");
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
			
		}
	}

	/**
	 * Asserts a element presence  
	 * @param by locator of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean assertElementPresent(By by, String locatorName){
		boolean flag = false;
		try {
			Assert.assertTrue(isElementPresent(by, locatorName));
			flag = true;
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			LOG.error(Log4jUtil.getStackTrace(e));
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
	 * MouseHovers on a element using JavaScript Implementation
	 * @param locator of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean mouseHoverByJavaScript(By locator, String locatorName){
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
			LOG.error(Log4jUtil.getStackTrace(e));
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
	 * waits for visibility of element
	 * @param by locator of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation 
	 */
	public boolean waitForVisibilityOfElement(By by, String locatorName){
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
			LOG.error(Log4jUtil.getStackTrace(e));
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
	 * clicks an element using java script executor
	 * @param locator of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean clickUsingJavascriptExecutor(By locator, String locatorName) {
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
			LOG.error(Log4jUtil.getStackTrace(e));
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
	 * Selects value from drop down based on value
	 * @param locator of element
	 * @param value dropdown list value
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean selectByValue(By locator, String value, String locatorName){
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
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("Select " + value + " in "+ locatorName + " dropdown",
						value + " is Not Selected from the DropDown : " + locatorName);
			} else {
				successReport("Select " + value + " in "+ locatorName + " dropdown",
						value + " is selected from the DropDown : " + locatorName);
			}
		}
	}

	/**
	 * Selects value from dropdown based on visible text 
	 * @param locator of element
	 * @param visibleText of (String)
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean selectByVisibleText(By locator, String visibleText, String locatorName){
		boolean flag = false;
		try {
			Select s = new Select(webDriver.findElement(locator));
			s.selectByVisibleText(visibleText.trim());
			flag = true;
			return true;
		} catch (Exception e) {
			// return false;
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("Select " + visibleText + " in "+ locatorName + " dropdown", visibleText + " is Not Select from the DropDown" + locatorName);
			} else {
				successReport("Select " + visibleText + " in "+ locatorName + " dropdown", visibleText + "  is Selected from the DropDown" + locatorName);
			}
		}
	}

	

	/**
	 * Provides size of WebElements list
	 * @param locator of element
	 * @return integer value indicating the count of the web elements
	 */
	public int getElementsSize(By locator) {
		int listCount = 0;
		try {
			List<WebElement> rows = webDriver.findElements(locator);
			listCount = rows.size();
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
		}
		return listCount;
	}

	/**
	 * Verifies a text contains in an actual text of element 
	 * @param by locator of element 
	 * @param text expectedText text of (String)
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean assertTextContains(By by, String text, String locatorName){
		boolean flag = false;
		String ActualText = null;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			 ActualText = getText(by, locatorName).trim();
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
			LOG.error(Log4jUtil.getStackTrace(e));
			return false;
		} finally {
			if (!flag) {
				failureReport("Expected Text : " + text,  "Expected text "+text+" is not contains in ActualText of Webelement : " + ActualText );
			} else {
				successReport("Expected Text : " + text, "Expected text "+text+" is contains in ActualText of WebElement : " + ActualText);
			}
		}
	}

	/**
	 * Verifies a text contains in an actual text of attribute Value
	 * @param by locator of element 
	 * @param text expectedText text of (String)
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean assertTextMatchingContainsWithAttribute(By by, String text, String locatorName){
		boolean flag = false;
		String actualText = null;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			 actualText = getAttributeValue(by, text).trim();
			LOG.info("ActualText is" + actualText);
			if (actualText.contains(text.trim())) {
				flag = true;
				LOG.info("String comparison with actual text :: " + "actual text is :" + actualText
						+ "And expected text is : " + text);
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(Log4jUtil.getStackTrace(e));
			return false;
		} finally {
			if (!flag) {
				failureReport("Expected Text : " + text,  "Expected text "+text+" is not contains in ActualText of attribute : " + actualText);
			} else {
				successReport("Expected Text : " + text,  "Expected text "+text+" is  contains in ActualText of attribute : " + actualText);
			}
		}
	}

	/**
	 * Compares two string values
	 * @param actText text1
	 * @param expText text2
	 * @param value indicating to control the report generation
	 * @return boolean value indicating success of the operation
	 */
	public boolean assertTextStringMatching(String actText, String expText,boolean value){
		boolean flag = false;
		try {
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
			LOG.error(Log4jUtil.getStackTrace(e));
			return false;
		} finally {
			if(value){
			if (!flag) {
				failureReport("Actual Text :: "+ actText +" Should equal to :: " + expText , "Actual Text :: "+ actText +" is not equal to :: " + expText);
				} else {
					successReport("Actual Text :: "+ actText +" Should equal to :: " + expText , "Actual Text :: "+ actText +" is equal to :: " + expText);
				}
			}
		}
	}
	
	
	/**
	 * Clicks the element
	 * @param locator of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean click(By locator, String locatorName){
		boolean status = false;
		
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : click  ::  Locator : " + locatorName);
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			LOG.info("Waiting for element");
			LOG.info("Locator is Visible :: " + locator);
			wait.until(ExpectedConditions.elementToBeClickable(locator));
			LOG.info("Clicked on the Locator");
			webDriver.findElement(locator).click();
			LOG.info("identified the element :: " + locator);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			status = true;
		} catch (Exception e) {
			status = false;
			LOG.error(Log4jUtil.getStackTrace(e));
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
	 * Verifies presence of element in a web page
	 * @param by locator of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean isElementPresent(By by, String locatorName){
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			highlight(webDriver.findElement(by));
			((JavascriptExecutor)webDriver).executeScript("arguments[0].scrollIntoView(true)",webDriver.findElement(by));
			status = true;
		} catch (Exception e) {
			status = false;
			LOG.error(Log4jUtil.getStackTrace(e));
		} finally {
			if (!status) {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				failureReport("isElementPresent : ", msgIsElementFoundFailure + locatorName);
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				this.successReport("isElementPresent : " + locatorName, this.msgIsElementFoundSuccess + locatorName);
			}

		}
		return status;
	}
	

	/**
	 * Verifies presence of element in a web page 
	 * @param by locator of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean isElementPresentWithOutJS(By by, String locatorName,boolean expected){
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			webDriver.findElement(by);
			status = true;
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
		} finally {
			if(expected){
				if (!status) {
					LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					failureReport("isElementPresent : ", msgIsElementFoundFailure + locatorName);
				} else {
					LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					this.successReport("isElementPresent : " + locatorName, this.msgIsElementFoundSuccess + locatorName);
				}
			}
		}
		return status;
	}	

	/**
	 * Scrolls to Web Element
	 * @param by locator of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean scroll(By by, String locatorName){
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
			LOG.error(e.getStackTrace());
		}finally {
			if (!status) {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				failureReport("Scroll To element :"+locatorName, "Unable to Scroll to element " + locatorName);
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				this.successReport("Scroll To element :"+locatorName, "Scroll to element " + locatorName);
			}

		}
		return status;
	}

	/**
	 * Scrolls to element with JavaScript implementation
	 * @param by locator of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean JSScroll(By by, String locatorName)  {
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
			LOG.error(e.getStackTrace());
		}finally {
			if (!status) {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				failureReport("Scroll To element :"+locatorName, "Unable to Scroll to element " + locatorName);
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				this.successReport("Scroll To element :"+locatorName, "Scroll to element " + locatorName);
			}
		}
		return status;
	}

	/**
	 * Enters text into the text field
	 * @param locator of element
	 * @param testData of (String)
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean sendKeys(By locator, String testData, String locatorName){
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : Type  ::  Locator : " + locatorName + " :: Data :" + testData);
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			LOG.info("Waiting for element :");
			LOG.info("Locator is Visible :: " + locator);
			wait.until(ExpectedConditions.elementToBeClickable(locator));
			webDriver.findElement(locator).click();
			LOG.info("Clicked on the Locator : ");
			webDriver.findElement(locator).clear();
			LOG.info("Cleared the existing Locator data : ");
			webDriver.findElement(locator).sendKeys(testData);
			LOG.info("Typed the Locator data :: " + testData);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			status = true;
		} catch (Exception e) {
			status = false;
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		}finally {
			if (!status) {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				failureReport("Enter text in :: " + locatorName, msgTypeFailure + testData);
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				this.successReport("Enter text in :: " + locatorName, msgTypeSuccess + testData);
			}
		}
		return status;
	}

	/**
	 * Sends the keys to element with JavaScript Implementation
	 * @param locator of element
	 * @param testData of (String)
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean typeUsingJavaScriptExecutor(By locator, String testData, String locatorName){
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
			LOG.error(Log4jUtil.getStackTrace(e));
			failureReport("Enter text in :: " + locatorName, msgTypeFailure + locatorName);
			throw new RuntimeException(e);
		}finally {
			if (!status) {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				failureReport("Enter text in :: " + locatorName, msgTypeFailure + testData);
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				this.successReport("Enter text in :: " + locatorName, msgTypeSuccess + testData);
			}
		}
		return status;
	}

	/**
	 * Waits for Title of element should be present
	 * @param locator of element
	 * @param message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean waitForTitlePresent(By locator,String message){
		boolean flag = false;
		boolean bValue = false;
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
		LOG.info("Method : " + locator);
		try {
			for (int i = 0; i < 20; i++) {
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
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("Wait For Title Present :: "+message, "Successfully not found title: "+message);
			} else {
				successReport("Wait For Title Present :: "+message, "Successfully found title: "+message);
			}
		}
		return bValue;
	}

	/**
	 * Gets Title of the Page
	 * @return String value is the title of the web page
	 * 
	 */
	public String getTitle(){
		boolean flag = false;
		String text= null;
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
		try{
		 text = webDriver.getTitle();
		 flag=true;
		}catch(Exception e){
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("Expected Title Of the Page :: "+text, "Title Of the Page is not retrieved : ");
			} else {
				successReport("Expected Title Of the Page :: ", "Title of the page is :: " + text);
			}
		}
		return text;
	}

	
	/**
	 * Asserts text  with an actual text of element 
	 * @param by locator of element 
	 * @param text expectedText text of (String)
	 * @return boolean value indicating success of the operation
	 */
	public boolean assertText(By by, String text)  {
		boolean flag = false;
		String actualText = null;
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
		try {
			actualText = getText(by, text).trim();
			Assert.assertEquals(actualText, text.trim());
			flag = true;
			return true;
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
			return false;
		}  finally {
					if (!flag) {
						failureReport("Actual Text :: "+ actualText +" Should equal to :: " + text , "Actual Text :: "+ actualText +" is not equal to :: " + text);
					} else {
						successReport("Actual Text :: "+ actualText +" Should equal to :: " + text , "Actual Text :: "+ actualText +" is equal to :: " + text);
					}
		}
	}

	/**
	 * Gets the text of element
	 * @param locator of element
	 * @param locatorName message to be included in the execution report
	 * @return string value is the text of the element
	 */
	public String getText(By locator, String locatorName){
		String text = "";
		boolean flag = false;
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
		try {
			if (isElementPresent(locator, locatorName)) {
				text = webDriver.findElement(locator).getText();
				LOG.info("Locator is Visible and text is retrieved :: " + text);
				flag = true;
			}
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				warningReport("GetText :: ", "Unable to get Text from :: " +locatorName);
				LOG.info("GetText :: Unable to get Text from :: " + locatorName);
			} else {
				successReport("GetText From element :: " + locatorName,"The Value of the Element is :" + text);
				LOG.info("Locator is Visible and text is retrieved :: " + text);
			}
		}
		return text;
	}

	/**
	 * Gets the value of Attribute
	 * @param locator : Action to be performed on element
	 * @param attributeName message to be included in the execution report
	 * @return string value is indicating the actual value of the attribute
	 */
	public String getAttributeValue(By locator, String attributeName) {
		boolean flag = false;
		String result = "";
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
		LOG.info("Method : " + getCallerMethodName());
		try {
			result = this.webDriver.findElement(locator).getAttribute(attributeName);
			LOG.info("Locator is Visible and attribute value is retrieved :: " + result);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
		}finally {
			if (!flag) {
				warningReport("GetAttribute :: ", "Unable to get Attribute value from :: " + attributeName);
				LOG.info("GetAttribute :: Unable to get Attribute value from :: " + attributeName);
			} else {
				successReport("GetText From element :: " + attributeName,"The Value of the attribute is :" + result);
				LOG.info("Locator is Visible and attribute value is retrieved :: " + result);
			}
		}
		return result;
	}
	
	/**
	 * Gets the Value of class attribute of a html tag
	 * @param locator : Action to be performed on element
	 * @param locatorName message to be included in the execution report
	 * @return string value is indicating the actual value of class attribute
	 */
	public String getAttributeByClass(By locator, String locatorName)  {
		String text = "";
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName());
			if (isElementPresent(locator, locatorName)) {
				text = webDriver.findElement(locator).getAttribute("class");
				LOG.info("Locator is Visible and attribute value is retrieved :: " + text);
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				flag = true;
			}
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				warningReport("GetAttribute :: ", "Unable to get Attribute value from :: " + locatorName);
				LOG.info("GetAttribute :: Unable to get Attribute value from :: " + locatorName);
			} else {
				successReport("GetText From element :: " + locatorName,"The Value of the Class attribute is :" + text);
				LOG.info("Locator is Visible and attribute value is retrieved :: " + text);
			}
		}
		return text;
	}

	/**
	 * MouseHovers on the element
	 * @param locator : Action to be performed on element 
	 * @param locatorName : message to be included in the execution report
	 * @return : boolean value indicating success of the operation 
	 */
	public boolean mouseHover(By locator, String locatorName)  {
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
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("MouseOver on :: "+locatorName, "MouseOver action is not performed on ::" + locatorName);
			} else {
				this.successReport("MouseOver on :: "+locatorName, "MouserOver Action is performed  on  :: " + locatorName);
			}
		}
	}

	/**
	 * Click the element with JavaScript Implementation
	 * @param locator : Action to be performed on element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 * 
	 */
	public boolean JSClick(By locator, String locatorName)  {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			LOG.info("Method : click  ::  Locator : " + locatorName);
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			LOG.info("Waiting for element");
			LOG.info("Locator is Visible :: " + locator);
			wait.until(ExpectedConditions.elementToBeClickable(locator));
			WebElement element = this.webDriver.findElement(locator);
			JavascriptExecutor executor = (JavascriptExecutor) this.webDriver;
			executor.executeScript("arguments[0].click();", element);
			flag = true;
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				LOG.info("Inside Finally block");
				failureReport("Click : " + locatorName, "Click is not performed on : " + locatorName);
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				this.successReport("Click : " + locatorName, "Successfully clicked on  : " + locatorName);
			}
		}
		return flag;
	}

	/**
	 * MouseHovers on element with Javascript Implementation
	 * @param locator of element 
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean jsMouseHover(By locator, String locatorName)  {
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
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("MouseOver : ", "MouseOver action is not perform on : " + locatorName);
			} else {
				this.successReport("MouseOver : ", "MouserOver Action is Done on" + locatorName);
			}
		}
		return flag;
	}

	/**
	 * Gets the list of Web Elements
	 * @param by locator of element
	 * @param locatorName message to be included in the execution report
	 * @return List of web elements
	 */
	public List<WebElement> getWebElementList(By by, String locatorName)  {
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
			LOG.error(Log4jUtil.getStackTrace(e));
		}
		return elements;
	}

	/**
	 * Gets the time of element loading
	 * @param locator of element
	 * @param locatorName message to be included in the execution report
	 * @return float value is indicating loading time of element
	 */
	public float elementVisibleTime(By locator,String locatorName){
		boolean flag = false;
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
			LOG.error(Log4jUtil.getStackTrace(e));
			}finally {
			if (!flag) {
				failureReport("Expected loading time of element : "+locatorName, "Element is not Loaded");
			} else {
				this.successReport("Expected loading time of element : "+locatorName, "Element " + locatorName + " loading time is"+timeTaken);
			}
		}
		return timeTaken;
	}

	/**
	 * DragAndDrops the element
	 * @param souceLocator Action to be performed on element source 
	 * @param destinationLocator Action to be performed on element destination 
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean dragAndDrop(By souceLocator, By destinationLocator, String locatorName)  {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
			Actions builder = new Actions(this.webDriver);
			WebElement souceElement = this.webDriver.findElement(souceLocator);
			WebElement destinationElement = this.webDriver.findElement(destinationLocator);
			builder.dragAndDrop(souceElement, destinationElement).build().perform();
			flag = true;
			LOG.info("drag and drop performed ");
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			return true;
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
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
	 * Navigates to URL
	 * @param Url value of URL
	 * @return boolean value indicating success of the operation
	 */
	public boolean navigateTo(String Url){
		boolean flag = false;
		try {
			webDriver.navigate().to(Url);
			LOG.info("Navigated URL is : " + Url);
			flag = true;
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException();
			
		} finally {
			if (!flag) {
				failureReport("Unable to Open URL :: ", Url);
			} else {
				successReport("Successfully Navigates to URL :: ", Url);
			}
		}
		return flag;
	}

	/**
	 * Gets the random number integer value
	 * @return integer value indicating the random number
	 * 
	 */
	public int generateRandomNumber(){
		int intRandom_number=0;
		try{
		Random generator = new Random();
		 intRandom_number = generator.nextInt(9999) + 10000;
		
		}catch(Exception e){
			LOG.error(Log4jUtil.getStackTrace(e));
		}
		 return intRandom_number;
	}

	/**
	 * MouseRightClicks on element
	 * @param locator of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean rightClick(By locator, String locatorName)  {
		boolean status = false;
		try {
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
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		}finally {
			if (!status) {
				failureReport("Mouse Right click on element  : "+locatorName, "Unable to right click on element " + locatorName);
			} else {
				this.successReport("Mouse Right click on element  : "+locatorName, "Successfully right click on element " + locatorName);
			}
		}
		return status;
	}

	/**
	 * Gets the respective caller class name
	 * @return Gives the Respective ClassName of type (String)
	 */
	public static String getCallerClassName() {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		return stElements[3].getClassName();
	}

	/**
	 * Gets the respective caller method name
	 * @return Gives the respective method name of type (String)
	 */
	public static String getCallerMethodName() {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		return stElements[3].getMethodName();
	}

	/**
	 * MouseDoubleclicks  on element
	 * @param locator of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean mouseDoubleClick(By locator, String locatorName)  {
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
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("Double Click on :: "+locatorName, "Double Click action is not perform on ::" + locatorName);
			} else {
				successReport("Double Click :: "+locatorName, "double Click Action is performed on  :: " + locatorName);
			}
		}
	}

	/**
	 * MouseClicks on element
	 * @param locator of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	public boolean mouseClick(By locator, String locatorName)  {
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
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		} finally {
			if (!flag) {
				failureReport("Click :: ", "Mouse Click action is not perform on ::" + locatorName);
			} else {
				this.successReport(" Click :: ", "Mouse Click Action is Done on  :: " + locatorName);
			}
		}
	}

	

	/**
	 * get the CSS property value of element 
	 * @param locator of element
	 * @param cssValue of type(String), CSS property (eg:width etc)
	 * @return : String indicating the value css property of element 
	 */
	public String getCssValue(By locator, String cssValue) {
		boolean flag = false;
		String result = "";
		try {
			result = this.webDriver.findElement(locator).getCssValue(cssValue);
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
		}finally {
			if (!flag) {
				failureReport("Get the CSS Value of property  :: "+cssValue, "Unable to retrieve the value of CSSProperty "+cssValue);
			} else {
				this.successReport("Get the CSS Value of property  :: "+cssValue, "Retrieved the value of CSSProperty is :: "+ result);
			}
		}
		return result;
	}

	/**
	 * Gets the background colour of element (e.g. background-color)
	 * @param locator of element
	 * @param cssValue of type (String), CSS property (e.g. background-color)
	 * @return : String indicating the background color of element 
	 */
	public String getBackGroundColor(By locator, String cssValue) {
		boolean flag = false;
		String hexColor = "";
		try {
			String bColor = this.webDriver.findElement(locator).getCssValue(cssValue);
			hexColor = Color.fromString(bColor).asHex();
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
			
		}finally {
			if (!flag) {
				failureReport("Get the back ground colour of css property  :: "+cssValue, "Unable to retrieve the back ground color of CSSProperty "+cssValue);
			} else {
				this.successReport("Get the back ground colour of css property  :: "+cssValue, "Retrieved the back ground colour of CSSProperty is :: "+ hexColor);
			}
		}
		return hexColor;
	}

	/**
	 * Asserts text string contains.
	 * @param actText of type (String)
	 * @param expText of type (String)
	 * @return boolean value indicating success of the operation
	 */
	public boolean assertTextStringContains(String actText, String expText)  {
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
			LOG.error(Log4jUtil.getStackTrace(e));
			return false;
		}  finally {
			if (!flag) {
				failureReport("Actual Text :: "+ actText +" Should equal to :: " + expText , "Actual Text :: "+ actText +" is not equal to :: " + expText);
			} else {
				successReport("Actual Text :: "+ actText +" Should equal to :: " + expText , "Actual Text :: "+ actText +" is equal to :: " + expText);
			}
		}
	}

	/**
	 * Refresh the web page
	 */
	public void refreshPage()  {
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName());
			webDriver.navigate().refresh();
			flag =true;
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.error(Log4jUtil.getStackTrace(e));
			LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
		} finally {
			if (!flag) {
				failureReport("Page is refreshing " , "Page not refreshed");
			} else {
				successReport("Page is refreshing " , "Successfully refreshed page");
			}
		}
	}

	/**
	 *Clears the data of an element 
	 *@param locator of element
	 */
	public void clearData(By locator){
		boolean flag = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName());
			WebElement element = webDriver.findElement(locator);
			element.sendKeys(Keys.CONTROL + "a");
			element.sendKeys(Keys.DELETE);
			element.clear();
			flag =true;
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.error(Log4jUtil.getStackTrace(e));
			LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
		} finally {
			if (!flag) {
				failureReport("Clear the data of element " , "Not cleared the data of element");
			} else {
				successReport("Clear the data of element " , "Successfully cleared the data of element");
			}
		}
	}

	/**
	 * Switch to another frame
	 * @param locator of element
	 */
	public void switchToFrame(By locator) {
		boolean flag = false;
		try{
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
		WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
		LOG.info("Waiting for element");
		wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		LOG.info("Locator is Visible :: " + locator);
		wait.until(ExpectedConditions.presenceOfElementLocated(locator));
		webDriver.switchTo().frame(webDriver.findElement(locator));
		}catch(Exception e){
			LOG.error(Log4jUtil.getStackTrace(e));
		}finally {
			if (!flag) {
				failureReport("Swtich to frame  :: "+locator, "Unable to switch to frame "+locator);
			} else {
				this.successReport("Swtich to frame  :: "+locator, "Successfully switched to frame"+locator);
			}
		}
	}
	
	/**
	 * Switch to another frame by using index of frame
	 * @param index value of frame
	 */
	public void switchToFrameByIndex(int index) {
		boolean flag = false;
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
		try{
		webDriver.switchTo().frame(index);
		}catch(Exception e){
			LOG.error(Log4jUtil.getStackTrace(e));
		}finally {
			if (!flag) {
				failureReport("Swtich to frame  :: ", "Unable to switch to frame ");
			} else {
				this.successReport("Swtich to frame  :: ", "Successfully switched to frame");
			}
		}
	}

	/**
	 * Switch to default Content
	 */
	public void switchToDefaultContent() {
		boolean flag = false;
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
		try{
		webDriver.switchTo().defaultContent();
		}catch(Exception e){
			LOG.error(Log4jUtil.getStackTrace(e));
		}finally {
			if (!flag) {
				failureReport("Swtich to Parent Frame ", "Unable to switch to Parent frame ");
			} else {
				this.successReport("Swtich to Parent frame ", "Successfully switched to Parent frame");
			}
		}
	}

	/**
	 * Accepts the alert
	 */
	public void acceptAlert() {
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
			wait.until(ExpectedConditions.alertIsPresent());
			webDriver.switchTo().alert().accept();
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
		}
	}

	/**
	 * Finds the WebElemnt 
	 * @param locator of element
	 * @param locatorName message to be included in the execution report
	 * @return WebElement 
	 */
	public WebElement findWebElement(By locator, String locatorName){
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
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		}
		return element;
	}

	/**
	 * Verifies the check box is checked or not 
	 * @param by locator of element
	 * @param locatorName message to be included in the execution report
	 * @return boolean value indicating success of the operation
	 */
	
	public boolean checkBoxIsChecked(By by, String locatorName){
		boolean status = false;
		try {
		
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			webDriver.findElement(by).isSelected();
				status = true;
		} catch (Exception e) {
			status = false;
			LOG.error(Log4jUtil.getStackTrace(e));

		} finally {
			if (!status) {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				failureReport("checkBox Is Checked : ", msgCheckboxisnotChecked + locatorName);
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				successReport("check Box is checked", locatorName + ", checkBox is checked : true");
			}
		}
		return status;
	}

	/**
	 * Switch to latest window
	 */
		public void switchToWindow() {
			for (String handle : webDriver.getWindowHandles()) {
				webDriver.switchTo().window(handle);
			}
		}

	/**
	 * switch to parent window
	 * @param handle value of window handle to switch
	 */
	public void switchToParentWindow(String handle) {
		webDriver.switchTo().window(handle);
	}

	/**
	 * Close the current window
	 */
	public void closeWindow() {
		webDriver.close();
	}

	/**
	 * Gets the current window handle
	 * @return String indicating the value of current window handle
	 */
	public String getWindowHandle() {
		return webDriver.getWindowHandle();
	}

	/**
	 * Scrolls to web element with JavaScript implementation
	 * @param element of locator
	 * @param locatorName message to be included in the execution report
	 */
	public void scrollToWebElement(By element,String locatorName) {
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName());
			JavascriptExecutor jse = (JavascriptExecutor) webDriver;
			jse.executeScript("arguments[0].scrollIntoView(true);", webDriver.findElement(element));
			LOG.info("Scroll is performed : " +locatorName);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			status = true;
		} catch (Exception e) {
			LOG.error(Log4jUtil.getStackTrace(e));
		}finally {
			if (!status) {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				failureReport("Scroll To element :"+locatorName, "Unable to Scroll to element " + locatorName);
			} else {
				LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				this.successReport("Scroll To element :"+locatorName, "Scroll to element " + locatorName);
				}
			}
		}

	/**
	 * verifies the presence of element in a page
	 * @param locator of element
	 * @param locatorName message to be included in the execution report
	 * @return WebElement
	 */

	public WebElement findWebElementVisibility(By locator, String locatorName){
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
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		}
		return element;
	}

	
	/**
	 * Highlights the web element
	 * @param element locator of element
	 */
	public void highlight(WebElement element) {

		if (webDriver instanceof JavascriptExecutor) {
			((JavascriptExecutor) webDriver).executeScript(
					"arguments[0].style.border='4px solid green'", element);

		}
	}
	
	/**
	 * Asserts False of the condition
	 * @param condition of (boolean)
	 * @param message of (String)
	 * @return boolean value indicating success of the operation
	 */
	public boolean assertFalse(boolean condition, String message) {
		try {
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			if (!condition)
				return true;
			else
				return false;
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
			LOG.error(Log4jUtil.getStackTrace(e));
			return false;
		} finally{
			if (condition) {
				failureReport("Expected :: " + message, message + " is :: " + condition);
			} else {
				successReport("Expected :: " + message, message + " is :: " + condition);
			}
		}
	}
	
	/**
	 * Press Enter key from KeyBoard
	 * @param locator of element
	 */
	public void pressEnter(By locator) {
		boolean status = false;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : " + getCallerMethodName());
			WebElement element = webDriver.findElement(locator);
			element.sendKeys(Keys.ENTER);
			
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.error(Log4jUtil.getStackTrace(e));
			LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
		}finally {
			if (!status) {
				failureReport("Click Enter Key in Keyboard "  , "Unable to click Enter Key in Keyboard" );
		} else {
			successReport("Click Enter Key in Keyboard "  , "Successfully clicked Enter Key in Keyboard" );
		}
	}
	}
	
	/**
	 * Gets the WebElements  
	 * @param locator of element
	 * @param locatorName message to be included in the execution report
	 * @return List of web elements 
	 */
	public List<WebElement> findWebElements(By locator, String locatorName) {
		List<WebElement> element;
		try {
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
			LOG.info("Method : click  ::  Locator : " + locatorName);
			LOG.info("Waiting for element");
			LOG.info("Locator is Visible :: " + locator);
			LOG.info("Clicked on the Locator");
			element = webDriver.findElements(locator);
			LOG.info("identified the element :: " + locator);
			LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		} catch (Exception e) {
			LOG.info(e.getMessage());
			LOG.error(Log4jUtil.getStackTrace(e));
			throw new RuntimeException(e);
		}
		return element;
	}
	
		/**
	    * waits for inVisibility Of Element
	    * @param by locator of element
	    * @param locatorName message to be included in the execution report
	    * @return boolean value indicating success of the operation
	    */
	   public boolean waitForInVisibilityOfElement(By by, String locatorName)  {
	      boolean flag = false;
	      LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	      LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
	      LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
	      WebDriverWait wait = new WebDriverWait(webDriver, timeValue);
	      try {
	         wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
	         flag = true;
	         LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	         return true;
	      } catch (Exception e) {
	         LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
	         LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
	     	LOG.error(Log4jUtil.getStackTrace(e));
	         LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
	         return false;
	      } finally {
	         if (!flag) {
	            failureReport("Invisible of element is false :: ", "Element :: " + locatorName + " is visible");
	         } else {
	            successReport("Invisible of element is true :: ", "Element :: " + locatorName + "  is not visible");
	         }
	      }
	   }
	   
	   /**
	    * presence of element in local storage
	    * @param item of (String)
	    * @return boolean value indicating success of the operation
	    */
	   public boolean isItemPresentInLocalStorage(String item)  {
	      boolean flag = false;
	      try {
	         LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	         LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
	         LOG.info("Method : " + getCallerMethodName() + "  ::  StorageItem : " + item);
	         String javaScript = String.format("return window.localStorage.getItem('%s');", item);
	         JavascriptExecutor js = (JavascriptExecutor) webDriver;
	         flag = true;
	         LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	         return !(js.executeScript(javaScript) == null);
	      } catch (Exception e) {
	         LOG.info("++++++++++++++++++++++++++++Catch Block Start+++++++++++++++++++++++++++++++++++++++++++");
	         LOG.info("Class name" + getCallerClassName() + "Method name : " + getCallerMethodName());
	         LOG.info("++++++++++++++++++++++++++++Catch Block End+++++++++++++++++++++++++++++++++++++++++++");
	     	 LOG.error(Log4jUtil.getStackTrace(e));
	         throw new RuntimeException(e);
	      } finally {
	         if (!flag) {
	            failureReport("StorageItem :: ", "Read from local storage action failed on :: " + item);
	         } else {
	            successReport("StorageItem :: ", "Read from local storage action is done on :: " + item);
	         }
	      }
	   }
	   
	   /**
	     * Get the current URL of webPage
	     * @return String
	     * @throws Throwable Handled
	     */
		public String getCurrentURL() throws Throwable {
			String text = webDriver.getCurrentUrl();
			{
				successReport("Current URL :: ", "Current URL of the page is :: " + text);
			}
			return text;
		}
	
	   /**
	    * Verifies the invisibility of element  
	    * @param locator of element
	    * @param locatorName message to be included in the execution report
	    * @return boolean value indicating success of the operation
	    */
	   public boolean isNotVisible(By locator, String locatorName)  {
	      boolean flag = false;
	      try {
	         LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	         LOG.info("Class name :: " + getCallerClassName() + " Method name :: " + getCallerMethodName());
	         LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
	         flag = !webDriver.findElement(locator).isDisplayed();
	         LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	      } catch (Exception e) {
	    		LOG.error(Log4jUtil.getStackTrace(e));
	         flag = false;
	      } finally {
	         if (!flag) {
	            failureReport(locatorName +"is not Visible : ", locatorName + " Element is Visible");
	         } else {
	            successReport(locatorName+" is not Visible : ", locatorName + " Element is Not Visible as expected");
	         }
	      }
	      return flag;
	   }
	   
	   /**
	     * keyBoardOperations
	     * @param  locator of (By)
	     * @param  testData of (Keys)
	     * @param  locatorName of (String)
	     * @return boolean
	     * @throws Throwable
	     */
	    public boolean keyBoardOperations(By locator, Keys testData, String locatorName) throws Throwable {
	        boolean status = false;
	        try {
	            LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	            LOG.info("Class name : " + getCallerClassName() + "Method name : " + getCallerMethodName());
	            LOG.info("Method : Type  ::  Locator : " + locatorName + " :: Data :" + testData);
	            WebDriverWait wait = new WebDriverWait(webDriver, 30);
	            LOG.info("Waiting for element :");
	            //wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
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
	       * Verifies Visibility of element in a web page
	       * @param locator of element
	       * @param locatorName message to be included in the execution report
	       * @param value indicating to control the report part (eg:true report will append to detailed report)
	       * @return boolean value indicating success of the operation
	       */
	       public boolean isVisible(By locator, String locatorName,boolean value){
	              boolean flag = false;
	              try {
	                     LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	                     LOG.info("Class name :: " + getCallerClassName() + " Method name :: " + getCallerMethodName());
	                     LOG.info("Method : " + getCallerMethodName() + "  ::  Locator : " + locatorName);
	                     flag = webDriver.findElement(locator).isDisplayed();
	                     LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	              } catch (Exception e) {
	                     LOG.error(Log4jUtil.getStackTrace(e));
	                     flag = false;
	              } finally {
	                     if(value){
	                     if (!flag) {
	                           failureReport("IsVisible : " +locatorName, locatorName + " Element is Not Visible : ");
	                     } else {
	                           successReport("IsVisible : " +locatorName, locatorName + " Element is Visible : ");
	                           }
	                     }
	              }
	              return flag;
	       }
	       
	       /**
	   	 * Compares two string values
	   	 * @param actText text1 
	   	 * @param expText text2
	   	 * @return boolean value indicating success of the operation
	   	 */
	   	public boolean assertTextStringMatching(String actText, String expText){
	   		boolean flag = false;
	   		try {
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
	   			LOG.error(Log4jUtil.getStackTrace(e));
	   			return false;
	   		} finally {
	   			if (!flag) {
	   				failureReport("Actual Text :: "+ actText +" Should equal to :: " + expText , "Actual Text :: "+ actText +" is not equal to :: " + expText);
	   				} else {
	   					successReport("Actual Text :: "+ actText +" Should equal to :: " + expText , "Actual Text :: "+ actText +" is equal to :: " + expText);
	   			}
	   		}
	   	}
	   }
