package attendanceinfo.service;

import java.sql.DriverManager;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.text.SimpleDateFormat;

import attendanceinfo.message.resp.TextMessage;
import attendanceinfo.util.MessageUtil;


public class CoreService {
	public static String TransactSQLInjection(String str)

    {

          return str.replaceAll(".*([';]+|(--)+).*", " ");

    }
	public String getCustomizeMenu(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("感谢您的关注！").append("\n\n");
		buffer.append("想关注你或者你孩子在学校的学习嘛").append("\n");
		buffer.append("那就请回复“AA” + “关注的学生的学号”。 如：AA1121302114").append("\n\n");
		buffer.append("如果已经关注成功，回复“100”可查看其所在班级").append("\n");
		return buffer.toString();
	}
	
	public String getFirstCustomize(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("您好，请回复“AA” + “关注的学生的学号”。如：AA1121302114").append("\n\n");
		return buffer.toString();
	}
	
	public String getClassName(String Studentid) throws SQLException{

		String dbUrl = "jdbc:mysql://w.rdc.sae.sina.com.cn:3307/app_attendanceinfo";
		String dbUser = "j0wo0yk3km";
		String dbPwd = "53iy444kkjl4xlwzwywxmijy3l0z3ixhh2lylz0j";
		
		Connection conn = null;
		conn = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
		Statement stmt;
		stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from student_try where studentid = '"+ Studentid +"'");
		while(rs.next()){
			return rs.getString("classname");
		}
		return null;
	}
	public static String processRequest(HttpServletRequest request) {
		String respMessage = null;
		String dbUrl = "jdbc:mysql://w.rdc.sae.sina.com.cn:3307/app_attendanceinfo";
		String dbUser = "j0wo0yk3km";
		String dbPwd = "53iy444kkjl4xlwzwywxmijy3l0z3ixhh2lylz0j";
		
		try{
			String respContent = "请求处理异常，请稍后尝试！";
			
			Map<String, String> requestMap = MessageUtil.parseXml(request);
			
			String fromUserName = requestMap.get("FromUserName");
			String toUserName = requestMap.get("ToUserName");
			String msgType = requestMap.get("MsgType");
			
			//回复文本消息
			TextMessage textMessage = new TextMessage();
			textMessage.setToUserName(fromUserName);
			textMessage.setFromUserName(toUserName);
			textMessage.setCreateTime(new Date().getTime());
			textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
			textMessage.setFuncFlag(0);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			//回复文本消息
			if(msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)){
				String reqContent =requestMap.get("Content").trim();
				//定制
				if(reqContent.startsWith("AA")){
					String reqStudentId = reqContent.substring(2);						
					Class.forName("com.mysql.jdbc.Driver").newInstance();
					Connection conn = null;
					conn =  DriverManager.getConnection(dbUrl, dbUser, dbPwd);
					reqStudentId = TransactSQLInjection(reqStudentId);
					String sql = "select * from student_try where studentid = '"+ reqStudentId +"'";
					Statement stmt;
					stmt = conn.createStatement();
					ResultSet result = stmt.executeQuery(sql);
					boolean flag = false;
					while(result.next()){
						flag = true;
						respContent ="您输入的学号对应的姓名为：" + result.getString("studentname") + " 已完成定制。\n" 
									+ "回复“100”可查看所在班级" ;
						stmt = conn.createStatement();
						stmt.executeUpdate("insert into user_info(openid, studentid, reqtime) values('"
								+ fromUserName 
								+ "','" 
								+ reqStudentId
								+ "','"
								+ sdf.format(new Date())
								+"')");
					}
					
					if(!flag){
						respContent = "请输入正确的学号！";
					}
					
					
					textMessage.setContent(respContent);
					respMessage = MessageUtil.textMessageToXml(textMessage);
				}
				else if(reqContent.equals("100")){
					String reqStudentId = null;
					CoreService className = new CoreService();
					
					Class.forName("com.mysql.jdbc.Driver").newInstance();
					Connection conn = null;
					conn = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
					Statement stmt;
					stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery("select * from user_info where openid = '"+ fromUserName +"'");
					while(rs.next()){
						respContent = className.getClassName(rs.getString("studentid"));
					}
					
					textMessage.setContent(respContent);
					respMessage = MessageUtil.textMessageToXml(textMessage);
				}else if(reqContent.startsWith("姓名")){
					String reqStudentName = reqContent.substring(2);						
					Class.forName("com.mysql.jdbc.Driver").newInstance();
					Connection conn = null;
					conn =  DriverManager.getConnection(dbUrl, dbUser, dbPwd);
					reqStudentName = TransactSQLInjection(reqStudentName);
					
					String sql = "select * from student_try where studentname = '"+ reqStudentName +"'";
					Statement stmt;
					stmt = conn.createStatement();
					ResultSet result = stmt.executeQuery(sql);
					respContent = null;
					boolean flag = false;
					while(result.next()){
						String tmpStr = null;
						flag = true;
						tmpStr ="您输入的学号对应的学号为：" 
						+ result.getString("studentid")
						+"\n 班级为：" + result.getString("classname") + "\n\n";
						respContent += tmpStr;
					}
					
					if(!flag){
						respContent = "您输入的姓名不存在";
					}
					
					
					textMessage.setContent(respContent);
					respMessage = MessageUtil.textMessageToXml(textMessage);
				}
				else{
					CoreService StrTemp = new CoreService();
					respContent = StrTemp.getFirstCustomize();
					textMessage.setContent(respContent);
					respMessage = MessageUtil.textMessageToXml(textMessage);
				}
				
			}
			
			else if(msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)){
				String eventType = requestMap.get("Event");
				if(eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)){
					CoreService StrTemp = new CoreService();
					respContent = StrTemp.getCustomizeMenu();
					
				}
			}
			
			textMessage.setContent(respContent);
			respMessage = MessageUtil.textMessageToXml(textMessage);
		} catch (Exception e){
			e.printStackTrace();
		}

		return respMessage;
	}
}
