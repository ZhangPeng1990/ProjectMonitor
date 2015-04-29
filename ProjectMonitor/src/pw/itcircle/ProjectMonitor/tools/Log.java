package pw.itcircle.ProjectMonitor.tools;

public class Log {

	private static boolean debug = true;
	
	public static void logPrint(String str)
	{
		if(debug)
		{
			System.out.print(str);
		}
	}
	
	public static void logPrintln(String str)
	{
		if(debug)
		{
			System.out.println(str);
		}
	}
	
	public static void logPrintln(String threadSpeak, String str)
	{
		if(debug)
		{
			System.out.println(threadSpeak + str);
		}
	}
}
