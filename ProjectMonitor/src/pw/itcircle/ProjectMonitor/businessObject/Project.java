package pw.itcircle.ProjectMonitor.businessObject;

import java.io.Serializable;
import java.util.List;

public class Project implements Serializable
{
	private static final long serialVersionUID = 4598459213979507599L;
	
	private String name;
	private String url;
	private String alias;
	private String webName; //网页html源码title的内容
	private List<JiraProblem> jiraProblems;
	private boolean checkByHtmlContent = false;
	private String htmlContent;
	private String jiraUrl;
	
	public final int issueKeyIndex = 1;
	public final int summaryIndex = 2;
	public final int assigneeIndex = 3;
	public final int statusIndex = 6;
	public final int createdDateIndex = 8;
	
	private boolean beginedChecked = false;
	private boolean checkedFineshed = false;
	
	private boolean connectivity = false;//是否可以连接成功
	
	private int requestTime = 0;
	public StringBuffer log = new StringBuffer();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getJiraUrl() {
		return jiraUrl;
	}
	public void setJiraUrl(String jiraUrl) {
		this.jiraUrl = jiraUrl;
	}
	public List<JiraProblem> getJiraProblems() {
		return jiraProblems;
	}
	public void setJiraProblems(List<JiraProblem> jiraProblems) {
		this.jiraProblems = jiraProblems;
	}
	public String getWebName() {
		return webName;
	}
	public void setWebName(String webName) {
		this.webName = webName;
	}
	public boolean isCheckByHtmlContent() {
		return checkByHtmlContent;
	}
	public void setCheckByHtmlContent(boolean checkByHtmlContent) {
		this.checkByHtmlContent = checkByHtmlContent;
	}
	public String getHtmlContent() {
		return htmlContent;
	}
	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}
	
	public synchronized boolean isBeginedChecked() {
		if(requestTime > 0)
		{
			beginedChecked = true;
		}
		requestTime++;
		return beginedChecked;
	}
	public boolean isCheckedFineshed() {
		return checkedFineshed;
	}
	public void setCheckedFineshed(boolean checkedFineshed) {
		this.checkedFineshed = checkedFineshed;
	}
	public boolean isConnectivity() {
		return connectivity;
	}
	public void setConnectivity(boolean connectivity) {
		this.connectivity = connectivity;
	}
}
