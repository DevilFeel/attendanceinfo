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
		buffer.append("��л���Ĺ�ע��").append("\n\n");
		buffer.append("���ע������㺢����ѧУ��ѧϰ��").append("\n");
		buffer.append("�Ǿ���ظ���AA�� + ����ע��ѧ����ѧ�š��� �磺AA1121302114").append("\n\n");
		buffer.append("����Ѿ���ע�ɹ����ظ���100���ɲ鿴�����ڰ༶").append("\n");
		return buffer.toString();
	}
	
	public String getFirstCustomize(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("���ã���ظ���AA�� + ����ע��ѧ����ѧ�š����磺AA1121302114").append("\n\n");
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
			String respContent = "�������쳣�����Ժ��ԣ�";
			
			Map<String, String> requestMap = MessageUtil.parseXml(request);
			
			String fromUserName = requestMap.get("FromUserName");
			String toUserName = requestMap.get("ToUserName");
			String msgType = requestMap.get("MsgType");
			
			//�ظ��ı���Ϣ
			TextMessage textMessage = new TextMessage();
			textMessage.setToUserName(fromUserName);
			textMessage.setFromUserName(toUserName);
			textMessage.setCreateTime(new Date().getTime());
			textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
			textMessage.setFuncFlag(0);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			//�ظ��ı���Ϣ
			if(msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)){
				String reqContent =requestMap.get("Content").trim();
				//����
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
						respContent ="�������ѧ�Ŷ�Ӧ������Ϊ��" + result.getString("studentname") + " ����ɶ��ơ�\n" 
									+ "�ظ���100���ɲ鿴���ڰ༶" ;
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
						respContent = "��������ȷ��ѧ�ţ�";
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
				}else if(reqContent.startsWith("����")){
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
						tmpStr ="�������ѧ�Ŷ�Ӧ��ѧ��Ϊ��" 
						+ result.getString("studentid")
						+"\n �༶Ϊ��" + result.getString("classname") + "\n\n";
						respContent += tmpStr;
					}
					
					if(!flag){
						respContent = "�����������������";
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
