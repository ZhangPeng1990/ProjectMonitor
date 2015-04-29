package pw.itcircle.ProjectMonitor.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class FileUtil {
	
	public static Log log	= LogFactory.getLog(FileUtil.class);

	public static boolean uploadFile(InputStream is, String filePath) {

		boolean retCode = false;
		byte[] buffer = new byte[1024];
		FileOutputStream fos = null;

		try {
			File file=new File(filePath);
			if(!file.exists())
				file.createNewFile();
			
			fos = new FileOutputStream(file);

			int n = -1;
			while ((n = is.read(buffer, 0, buffer.length)) != -1) {
				fos.write(buffer, 0, n);
			}

			retCode = true;
			pw.itcircle.ProjectMonitor.tools.Log.logPrintln("upload file success...");
		} catch (FileNotFoundException fnfe) {
			pw.itcircle.ProjectMonitor.tools.Log.logPrintln("fnfe:" + fnfe);
		} catch (IOException ioe) {
			pw.itcircle.ProjectMonitor.tools.Log.logPrintln("ioe:" + ioe);
		} finally {
			if (fos != null) {
				try {
					fos.close();
					fos = null;
				} catch (IOException e) {
					log.error(e);
				}
			}
			if (is != null) {
				try {
					is.close();
					is = null;
				} catch (IOException e) {
					log.error(e);
				}

			}
		}

		return retCode;
	}

	public static String getXmlContent(File xmlFile) {
		try {
			Document document = new SAXReader().read(xmlFile);
			return document.asXML();
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static String getFileContent(String fileName) {
		return getFileContent(fileName,null);
	}

	public static String getFileContent(String fileName,String encoding) {
		try {
			return getFileContent(new FileInputStream(fileName),encoding);
		} catch (FileNotFoundException e1) {
			return "";
		}
	}
	
	public static String getFileContent(InputStream is) {
		return getFileContent(is, null);
	}

	public static String getFileContent(InputStream is,String encoding) {
		encoding = encoding ==null?"UTF-8":encoding;
		BufferedReader reader = null;
		StringBuilder fileContent = new StringBuilder();
		try {
			reader = new BufferedReader(new InputStreamReader(is,encoding));
			String line = "";
			while ((line = reader.readLine()) != null) {
				fileContent.append(line);
				fileContent.append("\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fileContent.toString();

	}
	
	public static boolean setFileContent(String path, String content,String encodeingType) {
		boolean flag = false;
		DataOutputStream dos = null;
		try {
			if (content != null && content.length() >= 0) {
				byte abyte[] = content.getBytes(encodeingType);
				dos = new DataOutputStream(new FileOutputStream(path));
				dos.write(abyte, 0, abyte.length);
				dos.flush();

				flag = true;
			}
		} catch (FileNotFoundException e) {
			log.error("fnfe:" + e);
		} catch (IOException e) {
			log.error("ioe:" + e);
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				dos = null;
			}
		}
		return flag;
	}
	
	public static boolean setFileContent(String path, String content) {
		return setFileContent(path,content,"UTF-8");
	}

	public static String getFileExt(String fileName) {
		String ext = "";

		if (fileName == null)
			return ext;
		
		int lastIndex = fileName.lastIndexOf(".");
		if (lastIndex >= 0) {
			ext = fileName.substring(lastIndex + 1).toLowerCase();
		}
		return ext;
	}
	
	public static Document readXml(String xml) throws UnsupportedEncodingException, DocumentException {

		try {
			ByteArrayInputStream xmlStream = new ByteArrayInputStream(xml.getBytes());
			return new SAXReader().read(xmlStream);
		} catch (DocumentException e) {
			ByteArrayInputStream xmlStream = new ByteArrayInputStream(xml.getBytes("UTF-16"));
			return new SAXReader().read(xmlStream);
		}

	}
	
	public static Document readXml(InputStream is) throws UnsupportedEncodingException, DocumentException {

		try {
			return new SAXReader().read(is);
		} catch (DocumentException e) {
			return null;
		}

	}
	
	public static void setContent(String path,OutputStream os){
		pw.itcircle.ProjectMonitor.tools.Log.logPrintln("physicalPath=" + path);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(path));

			byte[] buffer = new byte[1024];
			int n = -1;
			while ((n = fis.read(buffer, 0, buffer.length)) != -1) {
				os.write(buffer, 0, n);
			}

			os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fis = null;
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				os = null;
			}
		}
	}
	
	public static String getStringIO(String path){
		InputStream is=null;
		try{
			URL url=new URL(path);
			is=url.openStream(); 
			StringBuilder sb=new StringBuilder();
			 byte[] b = new byte[1024]; 
			 int n=-1;
			while ( (n = is.read(b)) != -1) {
				sb.append(new String(b,0,n));
			}
			return sb.toString();
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}finally{
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static InputStream getFileIO(String path){
		InputStream is = null;
		try {
			URL url=new URL(path);
			is = url.openStream();
			return is;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void removeFile(String path){
		File file=new File(path);
		if(file.exists())
	      file.delete();
	}
}
