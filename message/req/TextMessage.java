package attendanceinfo.message.req;

public class TextMessage extends BaseMessage {
	private String Content;
	 
	public String Content(){
		return Content;
	}
	
	public void setContent(String content){
		Content = content;
	}
}
