package imusic.server.Controller;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.NoUrlPara;
import com.jfinal.kit.PathKit;

public class FrontController extends Controller {
	@Before(NoUrlPara.class)
	public void index(){
	}
}
