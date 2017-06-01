package com.cubic.accelerators;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

import com.cubic.genericutils.GenericConstants;
import com.cubic.genericutils.TimeUtil;
import com.cubic.logutils.Log4jUtil;
import com.cubic.reportengine.bean.CustomReportBean;
import com.cubic.reportengine.bean.DetailedReportBean;
import com.cubic.reportengine.report.CustomReports;

/**
 * BaseWebTest have all the generic methods to execute to drive the webdriver browser web test cases.
 * 
 * @since 1.0
 */
public class TestEngineWeb {

	private Hashtable<String, WebDriver> wedDriverList = null;
	private Hashtable<String, ActionEngineWeb> actionEngineWebList = null;

	/**
	 * This method will be executed before the suite.
	 * CustomReport folder structure is created in this phase.
	 * 
	 * @param context
	 * @throws Exception
	 */
	@BeforeSuite
	public void beforeSuite(ITestContext context) throws Exception {
		Log4jUtil.configureLog4j(GenericConstants.LOG4J_FILEPATH);
		
		// Create custom report folder structure.
		createFolderStructureForCustomReport(context);

		context.setAttribute("wedDriverList", new Hashtable<String, WebDriver>());
		context.setAttribute("actionEngineWebList", new Hashtable<String, ActionEngineWeb>());
	}

	/**
	 * This method will be executed after the suite.
	 * Generating summary report and freeing up the custom report instances are done in this phase.
	 * 
	 * @param context
	 * @throws Exception
	 */
	@AfterSuite
	public void afterSuite(ITestContext context) throws Exception {
		// Generates the Summary report.
		generateSummaryReport(context);

		cleanUpCustomReports();
		
		//LOG.info("Before killing "+browser+" browser");
		Runtime.getRuntime().exec("taskkill /F /IM chromedriver_32Bit.exe");
		Runtime.getRuntime().exec("taskkill /F /IM IEDriverServer64bit.exe");
		Runtime.getRuntime().exec("taskkill /F /IM MicrosoftWebDriver.exe");
		//LOG.info("After killing "+browser+" browser");		
	}

	/**
	 * Browser(i.e. webdriver) intialization is done in this phase.
	 * 
	 * @param context
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@BeforeClass
	@Parameters({"browser","seleniumgridurl"})
	public void beforeClass(ITestContext context, String browser, String seleniumgridurl) throws Exception {
		customReports = (CustomReports) context.getAttribute("customReports");

		//Sets the browser name in bean
		CustomReportBean customReportBean = customReports.getCustomReportBean();
		customReportBean.setBrowserName(browser);
		try {
			//WebDriver webDriver = getWebDriverForLocal(browser, seleniumgridurl);
			WebDriver webDriver = ActionEngineWeb.getWebDriverForLocal(browser, seleniumgridurl);
			
			wedDriverList = (Hashtable<String, WebDriver>) context.getAttribute("wedDriverList");

			// Webdriver instance is set at class name level(i.e. webdriver for
			// each class)
			wedDriverList.put(this.getClass().getName(), webDriver);

			// For storing the action engine instance.
			actionEngineWebList = new Hashtable<String, ActionEngineWeb>();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Unable to perform browser intialization");
		}		
		
	}

	/**
	 * Closes the browser and webdriver instance. 
	 * 
	 * @param context
	 */
	@AfterClass
	public void afterClass(ITestContext context) {
		cleanUpWebdriver(context);
	}

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeBethod(ITestContext context) throws Exception {
		wedDriverList = (Hashtable<String, WebDriver>) context.getAttribute("wedDriverList");
	}
	
	/**
	 * Removes the webdriver instance.
	 * 
	 * @param context
	 */
	@SuppressWarnings("unchecked")
	private void cleanUpWebdriver(ITestContext context) {
		wedDriverList = (Hashtable<String, WebDriver>) context.getAttribute("wedDriverList");
		WebDriver webdriver = wedDriverList.get(this.getClass().getName());
		if (webdriver != null) {
			webdriver.quit();
			webdriver = null;
		}
	}

	/**
	 * Creates the actionEngineWeb instance. getActionEngineWeb() method should
	 * not be exposed outside, since each call to this method creates the
	 * actionEngineWeb instance.
	 * 
	 * @param testCaseName
	 * @return ActionEngineWeb
	 * @throws Exception 
	 */
	private ActionEngineWeb getActionEngineWeb(String testCaseName) throws Exception {
		ActionEngineWeb actionEngineWeb = null; 
		try{
			// Fetch webdriver instance based on class name.
			WebDriver webDriver = wedDriverList.get(this.getClass().getName());

			actionEngineWeb = new ActionEngineWeb(webDriver, customReports, testCaseName);
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Unable to execute the method 'getActionEngineWeb'");
		}
		return actionEngineWeb;
	}
	
	/**
	 * Prerequisite setup at test method level(@Test method level). Call to this
	 * method should be the first line in the test method(i.e. in @Test)
	 * 
	 * Ex:  String testCaseName = getClassNameWithMethodName(method, "Rest Sample test");
	 *      ActionEngineWeb actionEngineWeb = setupAutomationTest(context, testCaseName);
	 * 
	 * @param context
	 * @param testCaseName
	 * @return ActionEngineWeb
	 * @throws Exception
	 */
	public ActionEngineWeb setupAutomationTest(ITestContext context, String testCaseName) throws Exception {
		ActionEngineWeb actionEngineWeb = null;
		
		try{
			// For generating the detailed report for test case(i.e. test method)
			setupReport(context, testCaseName);

			actionEngineWeb = getActionEngineWeb(testCaseName);

			// Action Engine instance is created at test method level and placed in
			// actionEngineWebList
			actionEngineWebList.put(testCaseName, actionEngineWeb);
			
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("Unable to execute the method 'setupWebTest'.");
		}

		return actionEngineWeb;
	}

	/**
	 * Closing every associated instances related to the test(i.e. @Test method). 
	 * Call to this method should be the last line in the test method(i.e. in @Test)
	 *	
	 * Ex:  teardownAutomationTest(context, testCaseName);
	 * 
	 * @param context
	 * @param testCaseName
	 * @throws Exception
	 */
	public void teardownAutomationTest(ITestContext context, String testCaseName) throws Exception {
		try{
		
			// Captures the test case execution details like time taken for
			// executing the test case, test case status pass/fail, etc.
			// This details will be used for generating summary report.
			teardownReport(context, testCaseName);
		
			// Remove action engine object, after executing test method.
			actionEngineWebList.remove(testCaseName);	
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Unable to execute the method 'teardownWebTest'.");
		}
	}	
	
	
	public CustomReports customReports = null;
	
	/**
	 * Creates the custom report folder structure. 
	 * This method should be called in at suite level(i.e.before suite), 
	 * since custom folder structure before starting testing.
	 * 
	 * @param context
	 * @return boolean
	 * @throws Exception 
	 */
	protected void createFolderStructureForCustomReport(ITestContext context) throws Exception {
		try {
			customReports = new CustomReports();
			customReports.createFolderStructureForCustomReport();
			context.setAttribute("customReports", customReports);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Unable to create the folder structure for custom report");
		}
	}	
	
	/**
	 * Generates Summary Report.
	 * This method need to be called at the end of the suite.
	 * 
	 * @param context
	 * @throws Exception
	 */
	protected void generateSummaryReport(ITestContext context) throws Exception{
		customReports = (CustomReports) context.getAttribute("customReports");
		customReports.generateSummaryReport();
	}
	
	private String[] specialChars = new String[]{"\\", "/", ":", "*", "?", ">", "<", "|", "\""};
	protected String getClassNameWithMethodName(Method method, String description) {
		String className = this.getClass().getName();
		className = className.substring(className.lastIndexOf(".") + 1, className.length());
		String testMethodName = method.getName();
		String detailedReportName = className + "_" + testMethodName+"-"+description;		
		for(String specialChar : specialChars){
			try{
				detailedReportName = detailedReportName.replaceAll(specialChar, "");	
			}catch(Exception e){}			
		}
		
		return detailedReportName;
	}
	
	/** Initialize the detailed report for the test case(at test method level @Test) 
	 * 
	 * @param context
	 * @param testCaseName
	 * @return
	 */
	private boolean setupReport(ITestContext context, String testCaseName){
		boolean flag = false;
		try{
			customReports = (CustomReports) context.getAttribute("customReports");
			
			CustomReportBean customReportBean = customReports.getCustomReportBean();
			LinkedHashMap<String, DetailedReportBean> detailedReportMap = customReportBean.getDetailedReportMap();
			
			//Check test case is already present.
			if(detailedReportMap.get(testCaseName) == null){
				
				//Create the detailed report, holds information related to test case.
				 DetailedReportBean detailedReportBean = new DetailedReportBean();
				 detailedReportBean.setTestCaseName(testCaseName); 
				 detailedReportBean.setTestCaseStartTime(TimeUtil.getCurrentInstant());
				 
				 //Add the detailed report map having test case information to detailed report map.
				 detailedReportMap.put(testCaseName, detailedReportBean);
				 
				 customReports.intializeDetailedReport(testCaseName);
				 
				 customReportBean.setDetailedReportMap(detailedReportMap);
				 context.setAttribute("customReports", customReports);
			}			
			
			flag = true;
		}catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}
	
	/**
	 *  Collects the information like test case status pass/fail, total time taken to execute the test case after executing the test case
	 *  (i.e. at test method level @Test), this information will be used for generating the summary report. 
	 *  
	 * @param context
	 * @param testCaseName
	 * @return
	 */
	private boolean teardownReport(ITestContext context, String testCaseName){
		boolean flag = false;
		try{
			CustomReports customReports =(CustomReports) context.getAttribute("customReports");
			CustomReportBean customReportBean = customReports.getCustomReportBean();

			LinkedHashMap<String, DetailedReportBean> detailedReportMap = customReportBean.getDetailedReportMap();
			
			DetailedReportBean detailedReportBean = detailedReportMap.get(testCaseName);
			if(detailedReportBean != null){
				
				Instant endTime = TimeUtil.getCurrentInstant();
				Instant startTime = detailedReportBean.getTestCaseStartTime();
				String testCaseTotalTime = TimeUtil.getTimeDifference(startTime, endTime);
				detailedReportBean.setTestCaseEndTime(endTime);
				detailedReportBean.setTotalTimeForTestCase(testCaseTotalTime);
				
				long overallExecutionTimeInMillis = (long) customReportBean.getOverallExecutionTimeInMillis();
				int totalTestScriptsPassed = (int) customReportBean.getTotalTestScriptsPassed();
				int totalTestScriptsFailed = (int) customReportBean.getTotalTestScriptsFailed();
			
				if(GenericConstants.TEST_CASE_PASS.equalsIgnoreCase(detailedReportBean.getOverallStatus())){
					totalTestScriptsPassed = totalTestScriptsPassed + 1;
				}else{
					totalTestScriptsFailed = totalTestScriptsFailed + 1;
				}
				overallExecutionTimeInMillis = overallExecutionTimeInMillis + TimeUtil.getTimeDifferenceInMillis(startTime, endTime);

				customReportBean.setOverallExecutionTimeInMillis(overallExecutionTimeInMillis);
				customReportBean.setTotalTestScriptsPassed(totalTestScriptsPassed);
				customReportBean.setTotalTestScriptsFailed(totalTestScriptsFailed);
				context.setAttribute("customReports", customReports);
			}			
			
			flag = true;
		}catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		
		return flag;
	}

	/** Frees up the customReport instance.
	 *  This method should be called in after suite(i.e. at the end of the suite.) 
	 * 
	 */
	protected void cleanUpCustomReports() {
		customReports = null;
	}	
	
}
