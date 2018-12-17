package imusic.server.aop;

import java.util.Date;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import imusic.server.Controller.Result;

public class LogProducer implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        Integer adminID = controller.getSessionAttr("adminID");
        String importance = controller.getPara("importance");
        Record log = new Record();
        if (adminID==null){
            System.out.println("No user");
            Result result = new Result();
            result.put("Code", 101);
			result.put("Msg", "Need Auth");
			result.put("Result", "Requesting of "+controller.getRequest().getRequestURI()+" failed");
			controller.renderJson(result);
			return ;
        }
        if (importance == null || importance.equals("1")) {
        	log.set("type", "1");
        }
        else {
        	log.set("type", "2");
        }
        log.set("content", "Method: "+controller.getRequest().getMethod()+" to Action: "+controller.getRequest().getRequestURI()).set("adminid",adminID).set("addtime", new Date());
        Db.save("log", log);
        
        inv.invoke();
    }

}
