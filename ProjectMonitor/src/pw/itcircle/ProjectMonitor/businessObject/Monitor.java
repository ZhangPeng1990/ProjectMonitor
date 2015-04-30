package pw.itcircle.ProjectMonitor.businessObject;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.conn.HttpHostConnectException;

import pw.itcircle.ProjectMonitor.factory.BusinessFactory;
import pw.itcircle.ProjectMonitor.tools.FileUtil;
import pw.itcircle.ProjectMonitor.tools.FreeMarkerUtil;
import pw.itcircle.ProjectMonitor.tools.Log;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class Monitor implements Runnable 
{

	public StringBuffer log = new StringBuffer();
	static final String CR = "\r\n";
	private boolean haveCreatedLog = false;
	
	@Override
	public void run() 
	{
		monit();
	}
	
	private void monit()
	{
		Log.logPrintln(Thread.currentThread().getName() + " log--->", "开始监测，需要连接网络，请等待...");
		
		List<Project> ps = null;
		try {
			ps = BusinessFactory.init().getProjects();
		} catch (Exception e1) {
			e1.printStackTrace();
			Log.logPrintln(Thread.currentThread().getName() + " log--->", "getProjects失败");
			return;
		}
		
		HtmlPage page;
		for(Project p : ps)
		{
			if(p.isBeginedChecked())
			{
				continue;
			}
			
			Log.logPrintln(Thread.currentThread().getName() + " log--->", "正在监测--->" + p.getName() + "--->" + p.getUrl());
			try {
				page = BusinessFactory.init().openWebPage(false, false, p.getUrl());
				String webTitle = page.getTitleText();
				if(!p.isCheckByHtmlContent() && !webTitle.equals(p.getWebName()))
				{
					p.log.append(p.getName()).append("------>未通过").append("测试地址--->").append(p.getUrl()).append(CR);
				}
				else if(p.isCheckByHtmlContent() && !page.asText().trim().equals(p.getHtmlContent()))
				{
					p.log.append(p.getName()).append("------>未通过").append("测试地址--->").append(p.getUrl()).append(CR);
				}
				else
				{
					p.log.append(p.getName()).append("------>OK, 测试地址--->").append(p.getUrl()).append(CR);
					p.setConnectivity(true);
				}
			} catch (HttpHostConnectException e) {
				p.log.append(p.getName()).append("------>存在异常，请检查!!").append("测试地址--->").append(p.getUrl()).append(CR);
			} catch (ConnectException e) {
				p.log.append(p.getName()).append("------>存在异常，请检查!!").append("测试地址--->").append(p.getUrl()).append(CR);
			} catch (UnknownHostException e) {
				p.log.append(p.getName()).append("------>未连接成功，请检查网络").append("测试地址--->").append(p.getUrl()).append(CR);
			} catch (FailingHttpStatusCodeException e) {
				p.log.append(p.getName()).append("------>未连接成功，请检查网站是否正常!!").append("测试地址--->").append(p.getUrl()).append(CR);
			} catch (MalformedURLException e) {
				p.log.append(p.getName()).append("------>存在异常，请检查!!").append("测试地址--->").append(p.getUrl()).append(CR);
			} catch (IOException e) {
				p.log.append(p.getName()).append("------>存在异常，请检查!!").append("测试地址--->").append(p.getUrl()).append(CR);
			} 
			
			if(p.getJiraUrl() != null)
			{
				try {
					Log.logPrintln(Thread.currentThread().getName() + " log--->", "开始检查" + p.getName() + "的jira问题");
					setJiraProblems(p);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
			String jira = "";
			if(p.getJiraProblems() != null && p.getJiraProblems().size() > 0)
			{
				jira += "-----------------------------------------------------------" + CR;
				jira += "项目" + p.getName() + "最新的5个jira问题是：" + CR;
				for(JiraProblem jp : p.getJiraProblems())
				{
					jira += jp.getName() + "--->" + jp.getAssigneeName() + "--->" + jp.getCreateTime() + "--->" + jp.getContent() + "--->" + jp.getStatus() + CR;
				}
				jira += "-----------------------------------------------------------" + CR;
				p.log.append(jira).append(CR);
			}
			
			p.setCheckedFineshed(true);
		}
		outLog();
	}
	
	public synchronized void setJiraProblems(Project p) throws FailingHttpStatusCodeException, MalformedURLException, IOException 
	{
		String jiraUrl = p.getJiraUrl();
		
		if(jiraUrl == null)
		{
			return;
		}
		
		if(jiraUrl != null && jiraUrl.length() > 0)
		{
			BusinessFactory.init().loginJiraSystem();
			
			HtmlPage issueListPage;
			
			issueListPage = BusinessFactory.init().openWebPage(false, false, jiraUrl);
			HtmlTable issueListTable = (HtmlTable) issueListPage.getElementById("issuetable");
			if(issueListTable == null)
			{
				Log.logPrintln(Thread.currentThread().getName() + " log---> ","issueListTable获取失败");
			}
			HtmlTableBody issueTbody = (HtmlTableBody) issueListTable.getLastElementChild();
			List<HtmlTableRow> rows = issueTbody.getRows();
			
			List<JiraProblem> jiraProblems = null;
			if(rows != null && rows.size() > 0)
			{
				jiraProblems = new ArrayList<JiraProblem>();
			}
			
			int time = 0;
			for(HtmlTableRow hr : rows)
			{
				if(time > 5)
				{
					break;
				}
				JiraProblem jp = new JiraProblem();
				
				HtmlTableCell issueKeyCell = hr.getCell(p.issueKeyIndex);
				String url = null;
				try {
					url = BusinessFactory.init().getJiraRrootUrl() + issueKeyCell.getFirstElementChild().getAttribute("href");
				} catch (Exception e) {
				}
				HtmlTableCell summaryCell = hr.getCell(p.summaryIndex);
				HtmlTableCell assigneeCell = hr.getCell(p.assigneeIndex);
				HtmlTableCell statusCell = hr.getCell(p.statusIndex);
				HtmlTableCell createdDateCell = hr.getCell(p.createdDateIndex);
				
				jp.setName(issueKeyCell.asText());
				jp.setUrl(url);
				jp.setContent(summaryCell.asText());
				jp.setCreateTime(createdDateCell.asText());
				jp.setStatus(statusCell.asText());
				jp.setAssigneeName(assigneeCell.asText());
				jiraProblems.add(jp);
				
				time ++;
			}
			p.setJiraProblems(jiraProblems);
		}
	}
	
	private boolean checkIsAllChecked(List<Project> ps)
	{
		boolean checkedAll = true;
		for(Project p : ps)
		{
			if(!p.isCheckedFineshed())
			{
				return false;
			}
		}
		return checkedAll;
	}
	
	private void outLog()
	{
		List<Project> ps = null;
		try {
			ps = BusinessFactory.init().getProjects();
		} catch (Exception e1) {
			e1.printStackTrace();
			Log.logPrintln(Thread.currentThread().getName() + " log--->", "getProjects 异常");
			return;
		}
		
		if(!checkIsAllChecked(ps))
		{
			return;
		}
		
		synchronized (this) 
		{
			if(this.haveCreatedLog)
			{
				return;
			}
			
			Log.logPrintln("开始生成监测日志报告...");
			String logStorePathFolder = BusinessFactory.basePath + "logs" + File.separator;
			File file = new File(logStorePathFolder);
			if(!file.exists())
			{
				file.mkdirs();
			}
			
			for(Project p : ps)
			{
				this.log.append(p.log);
			}
			
			Log.logPrintln("开始生成监测报告html文件...");
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ps", ps);
			String templateContent = null;
			try {
				templateContent = FileUtil.getFileContent(BusinessFactory.init().reportTemplatePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String reportHtml = FreeMarkerUtil.template2String(templateContent, map, true);
//			FileSystemView fsv = FileSystemView.getFileSystemView();//获取用户主页， windows系统获取到的是桌面路径
//			String deskPath = fsv.getHomeDirectory().getAbsolutePath() + File.separator; 
			String logSavePath = logStorePathFolder + "monitorLog-" + getTimeStr() + ".txt";
			FileUtil.setFileContent(logSavePath, log.toString(), "UTF-8");
			Log.logPrintln("日志生成完成，存储位置：--->" + logSavePath);
			String reportSavePath = logStorePathFolder + "monitorReport-" + getTimeStr() + ".html";
			FileUtil.setFileContent(reportSavePath, reportHtml, "UTF-8");
			Log.logPrintln("监测报告生成完成，存储位置：--->" + reportSavePath);
			
			
			this.haveCreatedLog = true;
		}
		
		
	}
	
	
	private static String getTimeStr()
	{
		Calendar c = Calendar.getInstance();
		return fillStr(c.get(Calendar.YEAR)) + fillStr(c.get(Calendar.MONTH) + 1) + fillStr(c.get(Calendar.DAY_OF_MONTH)) +
				fillStr(c.get(Calendar.HOUR_OF_DAY)) + fillStr(c.get(Calendar.MINUTE));
	}
	
	private static String fillStr(int str)
	{
		if(str < 10)
		{
			return "0" + str;
		}
		return String.valueOf(str);
	}
}
