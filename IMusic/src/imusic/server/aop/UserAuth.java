package imusic.server.aop;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

import imusic.server.Controller.Result;
import imusic.server.Controller.utils.JWTSigner;
import imusic.server.Controller.utils.JWTVerifier;
import imusic.server.Controller.utils.JWTVerifyException;

public class UserAuth implements Interceptor {
	
	private static String[] freePath = {
	        "/api/song",
			"/api/ring","/api/musician",
			"/api/notice/news","/api/notice",
			"/api/notice/","/api/notice/activity",
			"/api/song/like",
			"/api/song/comment",
			"/api/club",
			"/api/topic",
			"/api/topic/reply",
			"/api/ring/"
	};
	
	public void intercept(Invocation inv) {
		Controller controller = inv.getController();
//		System.out.println(controller.getRequest().getRequestURI());
//		System.out.println(controller.getRequest().getMethod());
		for (String temp :freePath){
//		    System.out.println(temp);
//            System.out.println(controller.getRequest().getRequestURI().contains(temp));
			
		    if(controller.getRequest().getRequestURI().contains(temp) && controller.getRequest().getMethod().equals("GET")){
			    inv.invoke();
				return ;
			}
		}
		
        Integer userID = controller.getSessionAttr("userID");
        
//        System.out.println(userID);
        
        if (userID !=null && userID!=0){
        	controller.getRequest().setAttribute("id", userID);
        	inv.invoke();
        	//System.out.println("return here");
        	return ;
        }
        
        String preToken = controller.getRequest().getHeader("token");
        
        if (preToken == null){        	
        	controller.renderJson(new Result().needAuth());
        	return ;
        }
        
        
        String agent = controller.getRequest().getHeader("agent");
        String resToken;
        JWTVerifier verfier = new JWTVerifier("SuperMTNeverFallBack");
		Map result = null;
		try {
			result = verfier.verify(preToken);
		} catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException | SignatureException
				| IOException | JWTVerifyException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			result = null;
			controller.renderJson(new Result().tokenSynaticError());
			return ;
		}
		if (result == null) {
			controller.getRequest().setAttribute("id", userID==null?0:userID);
	        controller.getResponse().setHeader("token","Can not detetive Token Head");
	        inv.invoke();
	        return ;
        }
		if (agent == null) {
			agent = "WEB";
		}
		System.out.println("agent:"+agent);
		if (!result.get(agent).toString().equals("online")) {
			controller.getRequest().setAttribute("id", 0);
			JWTSigner signer = new JWTSigner("SuperMTNeverFallBack");
			resToken = signer.sign(result);
	        controller.getResponse().setHeader("token", resToken);
			inv.invoke();
			return ;
		}
		System.out.println(result);
		userID = Integer.valueOf(result.get("userid").toString());
		JWTSigner signer = new JWTSigner("SuperMTNeverFallBack");
		resToken = signer.sign(result);
        controller.getResponse().setHeader("token", resToken);
        controller.getRequest().setAttribute("id", userID);
        if (userID==0||userID==null){
        	controller.renderJson(new Result().needAuth());
        	return ;
        }
        inv.invoke();
    }
}
