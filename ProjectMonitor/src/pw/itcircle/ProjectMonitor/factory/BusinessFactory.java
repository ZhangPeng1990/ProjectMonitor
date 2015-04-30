package pw.itcircle.ProjectMonitor.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import pw.itcircle.ProjectMonitor.businessObject.Project;
import pw.itcircle.ProjectMonitor.tools.Log;

@SuppressWarnings("all")
public class BusinessFactory 
{
	private static BusinessFactory lock = null;
	private Document config;
	public static final String basePath = BusinessFactory.class.getClass().getResource("/").getPath() + File.separator;
	private final String configPath = basePath + "resource" + File.separator + "projectsConfig.xml";
	public final String reportTemplatePath = basePath + "resource" + File.separator + "Report_Template.html";
	private List<Project> projects = null;
	private WebClient webBrowser;
	private Map<String,Boolean> loginedWebMap = new HashMap<String, Boolean>();
	private Map<String,HtmlPage> openedWebPageMap = new HashMap<String, HtmlPage>();
	private final BrowserVersion browserVersion = BrowserVersion.INTERNET_EXPLORER_11;
	private String jiraUserName;
	private String jiraPassWord;
	private String jiraRrootUrl;
	private String jiraLoginUrl;
	
	private BusinessFactory()
	{
		Log.logPrintln("init BusinessFactory...");
		File file = new File(configPath);
		this.config = getDocumentByURI(file.getAbsolutePath());
		this.webBrowser = new WebClient(browserVersion);
		this.webBrowser.getOptions().setTimeout(100000);
		this.webBrowser.getCookieManager().setCookiesEnabled(true);
		
		Element rootElt = this.config.getRootElement();
		String defNamespace = rootElt.getNamespaceURI();
		XPath xpathSelector;
		
		Map<String, String> nameSpaceMap = new HashMap<String, String>();
		if(defNamespace != null)
		{
			nameSpaceMap.put("defu", defNamespace);
		}
			
		xpathSelector = DocumentHelper.createXPath("//jira//defu:userName");
		xpathSelector.setNamespaceURIs(nameSpaceMap);
		Element userNameE = (Element) xpathSelector.selectSingleNode(config);
		this.jiraUserName = userNameE.getTextTrim();
		
		xpathSelector = DocumentHelper.createXPath("//jira//defu:passWord");
		xpathSelector.setNamespaceURIs(nameSpaceMap);
		Element passWordE = (Element) xpathSelector.selectSingleNode(config);
		this.jiraPassWord = passWordE.getTextTrim();
		
		xpathSelector = DocumentHelper.createXPath("//jira//defu:loginUrl");
		xpathSelector.setNamespaceURIs(nameSpaceMap);
		Element loginUrlE = (Element) xpathSelector.selectSingleNode(config);
		this.jiraLoginUrl = loginUrlE.getTextTrim();
		
		xpathSelector = DocumentHelper.createXPath("//jira//defu:rootUrl");
		xpathSelector.setNamespaceURIs(nameSpaceMap);
		Element rootUrlE = (Element) xpathSelector.selectSingleNode(config);
		this.jiraRrootUrl = rootUrlE.getTextTrim();
			
		try {
			loginJiraSystem();
		} catch (Exception e) {
			e.printStackTrace();
			Log.logPrintln(Thread.currentThread().getName() + " log---> ","BusinessFactory 初始化登录jira系统失败！！！");
		} 
		
	}
	
	public synchronized void loginJiraSystem() throws FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		// 检查该网站是否已经登录过了， 如果没登陆先进行登录操作
		if(!isLogin(this.jiraLoginUrl))
		{
			HtmlPage loginPage;
			Log.logPrintln(Thread.currentThread().getName() + " log---> ","正在登录jira系统...");
			loginPage = openWebPage(false, false, this.jiraLoginUrl);
			HtmlInput userNameInput = (HtmlInput) loginPage.getElementById("username");
			userNameInput.setValueAttribute(this.jiraUserName);
			HtmlInput passWordInput = (HtmlInput) loginPage.getElementById("password");
			passWordInput.setValueAttribute(this.jiraPassWord);
			HtmlButton loginButton = (HtmlButton) loginPage.getElementById("login");
			loginButton.click();
			
			loginRegister(this.jiraLoginUrl, true);
		}
	}
	
	public synchronized static BusinessFactory init() throws FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		if(lock == null)
		{
			lock = new BusinessFactory();
		}
		return lock;
	}
	
	private static Document getDocumentByURI(String path){
		SAXReader reader = new SAXReader();
        Document document = null;
		try {
			document = reader.read(new FileInputStream(path));
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return document;
	}

	public synchronized List<Project> getProjects() throws FailingHttpStatusCodeException, MalformedURLException, IOException 
	{
		if(this.projects != null)
		{
			return projects;
		}
		
		Element rootElt = this.config.getRootElement();
		String defNamespace = rootElt.getNamespaceURI();
		XPath xpathSelector;
		
		projects = new ArrayList<Project>();
		Map<String, String> nameSpaceMap = new HashMap<String, String>();
		
		if(defNamespace != null)
		{
			nameSpaceMap.put("defu", defNamespace);
		}
		xpathSelector = DocumentHelper.createXPath("//defu:projects//defu:project");
		xpathSelector.setNamespaceURIs(nameSpaceMap);
		List<Element> listProject = xpathSelector.selectNodes(config);
		int i = 1;
		for(Element projectE : listProject)
		{
			Project p = new Project();
			
			xpathSelector = DocumentHelper.createXPath("//defu:project[" + i + "]//defu:name");
			xpathSelector.setNamespaceURIs(nameSpaceMap);
			Element nameE = (Element) xpathSelector.selectSingleNode(config);
			p.setName(nameE.getText());
			
			xpathSelector = DocumentHelper.createXPath("//defu:project[" + i + "]//defu:url");
			xpathSelector.setNamespaceURIs(nameSpaceMap);
			Element urlE = (Element) xpathSelector.selectSingleNode(config);
			p.setUrl(urlE.getText());
			
			xpathSelector = DocumentHelper.createXPath("//defu:project[" + i + "]//defu:alias");
			xpathSelector.setNamespaceURIs(nameSpaceMap);
			Element aliasE = (Element) xpathSelector.selectSingleNode(config);
			p.setAlias(aliasE.getText());
			
			xpathSelector = DocumentHelper.createXPath("//defu:project[" + i + "]//defu:webName");
			xpathSelector.setNamespaceURIs(nameSpaceMap);
			Element webNameE = (Element) xpathSelector.selectSingleNode(config);
			p.setWebName(webNameE != null ? webNameE.getText() : null);
			
			xpathSelector = DocumentHelper.createXPath("//defu:project[" + i + "]//defu:webContent");
			xpathSelector.setNamespaceURIs(nameSpaceMap);
			Element webContentE = (Element) xpathSelector.selectSingleNode(config);
			if(webContentE != null)
			{
				p.setCheckByHtmlContent(true);
				p.setHtmlContent(webContentE.getText());
			}
			
			xpathSelector = DocumentHelper.createXPath("//defu:project[" + i + "]//defu:jiraUrl");
			xpathSelector.setNamespaceURIs(nameSpaceMap);
			Element jiraUrlE = (Element) xpathSelector.selectSingleNode(config);
			if(jiraUrlE != null && jiraUrlE.getTextTrim().length() > 0)
			{
				p.setJiraUrl(jiraUrlE.getText());
			}
			
			projects.add(p);
			
			i++;
		}
		
		return projects;
	}
	
	private synchronized WebClient getWebBrowser(boolean cssEnabled, boolean javaScriptEnabled) 
	{
		if(this.webBrowser != null)
		{
			webBrowser.getOptions().setCssEnabled(cssEnabled);
			webBrowser.getOptions().setJavaScriptEnabled(javaScriptEnabled);
			return webBrowser;
		}
		
		Log.logPrintln("系统初始化异常,请重新运行！！！");
		return null;
	}
	
	public synchronized HtmlPage openWebPage(boolean cssEnabled, boolean javaScriptEnabled, String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		if(openedWebPageMap.get(url) != null)
		{
			return openedWebPageMap.get(url);
		}
		
		HtmlPage page = null;
		try {
			webBrowser = this.getWebBrowser(cssEnabled, javaScriptEnabled);
		} catch (Exception e) {
			Log.logPrintln(Thread.currentThread().getName() + " log--->", "浏览器初始化异常!!!");
			e.printStackTrace();
		}
		
		page = webBrowser.getPage(url);
		openedWebPageMap.put(url, page);
		return page;
	}
	
	public void loginRegister(String url, boolean isLogin)
	{
		if(isLogin)
		{
			try {
				Log.logPrintln(Thread.currentThread().getName() + " log--->", "成功登录" + new URL(url).getHost());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		this.loginedWebMap.put(url, isLogin);
	}
	
	public boolean isLogin(String url)
	{
		if(loginedWebMap.get(url) != null && this.loginedWebMap.get(url))
		{
			return true;
		}
		return false;
	}

	public String getJiraUserName() {
		return jiraUserName;
	}

	public String getJiraPassWord() {
		return jiraPassWord;
	}

	public String getJiraRrootUrl() {
		return jiraRrootUrl;
	}

	public String getJiraLoginUrl() {
		return jiraLoginUrl;
	}
	
}
