package imusic.server.aop;



import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

import imusic.server.Controller.Result;

public class PostMethod implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		// TODO Auto-generated method stub
		Controller controller = inv.getController();
		
		if (controller.getRequest().getMethod().equals("POST") && 
				controller.getRequest().getContentType() == null){
			controller.renderJson(new Result().lackOfPara());
			return ;
		}
		else if (controller.getRequest().getMethod().equals("POST") && 
		controller.getRequest().getContentType()
		.contains("form-urlencoded")){
				Result temp = new Result();
				temp.put("Code", 102);
				temp.put("Msg", "use x-www-form-urlencoded as the post method");
				//controller.getFile();
				controller.renderJson(temp);
				return ;
			}
		else{
			inv.invoke();
		}
	}
}
