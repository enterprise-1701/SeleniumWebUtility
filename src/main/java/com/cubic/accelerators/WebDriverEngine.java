package com.cubic.accelerators;

import java.time.Instant;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Objects;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.cubic.genericutils.GenericConstants;
import com.cubic.genericutils.TimeUtil;
import com.cubic.logutils.Log4jUtil;
import com.cubic.reportengine.bean.CustomReportBean;
import com.cubic.reportengine.bean.DetailedReportBean;
import com.cubic.reportengine.report.CustomReports;
import com.cubic.testrail.TestRailUtil;

/**
 * BaseWebTest have all the generic methods to execute to drive the webdriver browser web test cases.
 * 
 * @since 1.0
 */
public class WebDriverEngine {

	private Hashtable<String, WebDriver> wedDriverList = null;
	private Hashtable<String, WebDriverActions> webDriverActionList = null;
	private Hashtable<String , String> propTable = GenericConstants.GENERIC_FW_CONFIG_PROPERTIES;
	private String testRailProjectID;
	private String testRailSuiteID;
	private String testRailRunID;
	private boolean testRailFlag;

	/**
	 * This method will be executed before the suite.
	 * CustomReport folder structure is created in this phase.
	 * 
	 * @param context org.testng.ITestContext
	 * @throws Exception java.lang.Exception
	 */
	@BeforeSuite
	@Parameters({"projectID","suiteID","runID","test_Rail_Integration_Enable_Flag"})
	public void beforeSuite(ITestContext context,
			@Optional String projectID,
			@Optional String suiteID,
			@Optional String runID,
			@Optional String test_Rail_Integration_Enable_Flag) throws Exception {
		try {
			//LOG.info("Before killing "+browser+" browser");
			Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");
			Runtime.getRuntime().exec("taskkill /F /IM geckodriver.exe");
			Runtime.getRuntime().exec("taskkill /F /IM IEDriverServer.exe");
			Runtime.getRuntime().exec("taskkill /F /IM MicrosoftWebDriver.exe");
			//LOG.info("After killing "+browser+" browser");
		}
		catch (Exception e) {
			System.out.println(e);
		}

		Log4jUtil.configureLog4j(GenericConstants.LOG4J_FILEPATH);
		try{
			testRailProjectID=TestRailUtil.getTestRailProjectID(projectID);
			testRailSuiteID=TestRailUtil.getTestRailSuiteID(suiteID);
			testRailFlag=TestRailUtil.getTestRailEnableFlag(test_Rail_Integration_Enable_Flag,testRailProjectID,testRailSuiteID);
			// Create custom report folder structure.
			createFolderStructureForCustomReport(context,testRailFlag);
			context.setAttribute("wedDriverList", new Hashtable<String, WebDriver>());
if(testRailFlag){
				
				if((runID==null) || (runID.equalsIgnoreCase("0") || runID.equalsIgnoreCase("%runID%") || runID.equalsIgnoreCase("${runID}") )){
					TestRailUtil.generateTestRunsForTestCases(testRailProjectID,testRailSuiteID,customReports.getCustomReportBean().getSuiteStartDateAndTime());
				}else if(runID!=null && !(runID.equals("0"))){
					testRailRunID = runID;
					TestRailUtil.setExistingTestRunID(testRailRunID);
				}
				
				}
		}catch (Exception e) {
			System.out.println(e);
		}
		

		
		
		//TODO need to remove below commented code after re-checking
		//context.setAttribute("webDriverActionList", new Hashtable<String, WebDriverActions>());
	
	}

	/**
	 * This method will be executed after the suite.
	 * Generating summary report and freeing up the custom report instances are done in this phase.
	 * 
	 * @param context org.testng.ITestContext
	 * @throws Exception org.testng.ITestContext
	 */
	@AfterSuite
	@Parameters({"projectID","suiteID"})
	public void afterSuite(ITestContext context,@Optional String projectID,@Optional String suiteID) throws Exception {
		// Generates the Summary report.
		generateSummaryReport(context,testRailFlag);
		
		if(testRailFlag){
			try{
				if((propTable.get("Test_Rail_Results_Update_End_of_Suite")==null)||(propTable.get("Test_Rail_Results_Update_End_of_Suite").equalsIgnoreCase("true"))){
					TestRailUtil.updateTestResultsinTestRail();
				}
				
			}catch (Exception e) {
		        e.printStackTrace();
		    }
		}

		cleanUpCustomReports();

		try {
			//LOG.info("Before killing "+browser+" browser");
			Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");
			Runtime.getRuntime().exec("taskkill /F /IM geckodriver.exe");
			Runtime.getRuntime().exec("taskkill /F /IM IEDriverServer.exe");
			Runtime.getRuntime().exec("taskkill /F /IM MicrosoftWebDriver.exe");
			//LOG.info("After killing "+browser+" browser");
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Browser(i.e. webdriver) intialization is done in this phase.
	 * 
	 * @param context org.testng.ITestContext
	 * @param browser name of the browser
	 * @param executionenv url of execution environment
	 * @throws Exception java.lang.Exception
	 */
	@SuppressWarnings("unchecked")
	@BeforeClass
	@Parameters({"browser","executionenv","platform","version"})
	public void beforeClass(ITestContext context, String browser, String executionenv,String platform,String version) throws Exception {
		customReports = (CustomReports) context.getAttribute("customReports");

		//Sets the browser name in bean
		CustomReportBean customReportBean = customReports.getCustomReportBean();
		customReportBean.setBrowserName(browser);
		try {
			WebDriver webDriver = WebDriverActions.getWebDriverForLocal(browser, executionenv,platform,version);
			
			wedDriverList = (Hashtable<String, WebDriver>) context.getAttribute("wedDriverList");

			// Webdriver instance is set at class name level(i.e. webdriver for
			// each class)
			wedDriverList.put(this.getClass().getName(), webDriver);

			// For storing the actions instance.
			webDriverActionList = new Hashtable<String, WebDriverActions>();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Unable to perform browser intialization");
		}		
		
	}

	/**
	 * Closes the browser and webdriver instance. 
	 * 
	 * @param context seleniumGrid server
	 */
	@AfterClass
	public void afterClass(ITestContext context) {
		cleanUpWebdriver(context);
	}

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod(ITestContext context) throws Exception {
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
	 * Creates the webDriverActions instance. getWebDriverActions() method should
	 * not be exposed outside, since each call to this method creates the
	 * webDriverActions instance.
	 * 
	 * @param testCaseName
	 * @return WebDriverActions
	 * @throws Exception 
	 */
	private WebDriverActions getWebDriverActions(String testCaseName) throws Exception {
		WebDriverActions webDriverActions = null; 
		try{
			// Fetch webdriver instance based on class name.
			WebDriver webDriver = wedDriverList.get(this.getClass().getName());

			webDriverActions = new WebDriverActions(webDriver, customReports, testCaseName);
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Unable to execute the method 'getWebDriverActions'");
		}
		return webDriverActions;
	}	
	
	/**
	 * Prerequisite setup at test method level(@Test method level). Call to this
	 * method should be the first line in the test method(i.e. in @Test)
	 * 
	 * Ex:  String testCaseName = "&lt;&lt;TESTCASE ID&gt;&gt; : &lt;&lt;TESTCASE DESCRIPTION&gt;&gt;"
	 *      WebDriverActions webDriverActions = setupAutomationTest(context, testCaseName);
	 * 
	 * Note: testCaseName(ex: "TC 01 : Sample Test") should be same when you are calling the method 'setupAutomationTest' and 'teardownAutomationTest'
	 * 
	 * @param context org.testng.ITestContext
	 * @param testCaseName should be &lt;&lt;TESTCASE_ID&gt;&gt; : &lt;&lt;TESTCASE DESCRIPTION&gt;&gt; format
	 * @return com.cubic.accelerators.WebDriverActions
	 * @throws Exception java.lang.Exception
	 */
	public WebDriverActions setupAutomationTest(ITestContext context, String testCaseName) throws Exception {
		WebDriverActions webDriverActions = null;
		
		try{
			// For generating the detailed report for test case(i.e. test method)
			setupReport(context, testCaseName);

			webDriverActions = getWebDriverActions(testCaseName);

			// action instance is created at test method level and placed in
			// webDriverActionList
			webDriverActionList.put(testCaseName, webDriverActions);
			
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("Unable to execute the method 'setupWebTest'.");
		}

		return webDriverActions;
	}

	/**
	 * Closing every associated instances related to the test(i.e. @Test method). 
	 * Call to this method should be the last line in the test method(i.e. in @Test), should be written in finally block.
	 *	
	 * Ex:  
	 * String testCaseName = "&lt;&lt;TESTCASE ID&gt;&gt; : &lt;&lt;TESTCASE DESCRIPTION&gt;&gt;"
	 * teardownAutomationTest(context, testCaseName);
	 * 
	 * Note: testCaseName(ex: "TC 01 : Sample Test") should be same when you are calling the method 'setupAutomationTest' and 'teardownAutomationTest'
	 * 
	 * @param context org.testng.ITestContext
	 * @param testCaseName should be in "&lt;&lt;TESTCASE ID&gt;&gt; : &lt;&lt;TESTCASE DESCRIPTION&gt;&gt;" format
	 * @throws Exception java.lang.Exception
	 */
	public void teardownAutomationTest(ITestContext context, String testCaseName) throws Exception {
		try{
		
			// Captures the test case execution details like time taken for
			// executing the test case, test case status pass/fail, etc.
			// This details will be used for generating summary report.
			teardownReport(context, testCaseName,TestRailUtil.testRailFlag);
		
			// Remove actions object, after executing test method.
			webDriverActionList.remove(testCaseName);
			// If test is fail then assert false, this is for testNG
			assert !Objects.equals(this.customReports.getCustomReportBean().getDetailedReportMap().get(testCaseName).getOverallStatus().toLowerCase(), "fail");
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
	protected void createFolderStructureForCustomReport(ITestContext context,boolean testRailFlag) throws Exception {
		try {
			customReports = new CustomReports();
			customReports.createFolderStructureForCustomReport(testRailFlag);
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
	protected void generateSummaryReport(ITestContext context,boolean testRailFlag) throws Exception{
		customReports = (CustomReports) context.getAttribute("customReports");
		customReports.generateSummaryReport(testRailFlag);
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
				 System.out.println("Test Case ID ::::"+testCaseName.split(":")[0]);
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
	private boolean teardownReport(ITestContext context, String testCaseName,boolean testRailFlag){
		boolean flag = false;
		String testCaseID = null;
		String finalResult = null;
		String comment=null;
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
				testCaseID = detailedReportBean.getTestCaseID();
				finalResult = detailedReportBean.getOverallStatus();
				comment=detailedReportBean.getFailStepDescription();
			}			
			
			flag = true;
		}catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}finally {
			System.out.println("Test Case ID ::::" + testCaseID);
			System.out.println("Test Status :::::" + finalResult);
			boolean testResultsUpdateFlag = false;
			
			if((propTable.get("Test_Rail_Results_Update_End_of_Suite")==null)||(propTable.get("Test_Rail_Results_Update_End_of_Suite").equalsIgnoreCase("true"))){
				testResultsUpdateFlag=false;
			}else if(propTable.get("Test_Rail_Results_Update_End_of_Suite").equalsIgnoreCase("false")){
				testResultsUpdateFlag=true;
			}

			if (testRailFlag) {
				try {
					
					if (testResultsUpdateFlag) {
						if(comment==null){
							comment="";
						}
						TestRailUtil.updateTestResultinTestRail(testCaseID, finalResult,comment);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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
