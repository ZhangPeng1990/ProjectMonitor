package pw.itcircle.ProjectMonitor.businessObject;

import java.io.IOException;
import java.net.MalformedURLException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

import pw.itcircle.ProjectMonitor.factory.BusinessFactory;


public class Start 
{
	public static void main(String[] args) throws InterruptedException, FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		Monitor monitor = new Monitor();
		
		int maxThreadNum = 20;
		maxThreadNum = (BusinessFactory.init().getProjects().size() <= maxThreadNum) ? BusinessFactory.init().getProjects().size() : maxThreadNum;
		for(int i = 0; i < maxThreadNum; i++)
		{
			new Thread(monitor, "检查者" + (i + 1)).start();
		}
	}
}
