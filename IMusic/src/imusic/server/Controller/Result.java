package imusic.server.Controller;

import java.util.HashMap;

public class Result extends HashMap<String, Object> {
	public Result(){
		put("Code", 100);
		put("Msg", "OK");
	}
	
	public Result okForList(){
		put("Code",200);
		return this;
	}
	
	public Result needAuth(){
		put("Code", 101);
		put("Msg", "Need Auth");
		return this;
	}
	public Result illegalMethod(){
		put("Code", 102);
		put("Msg", "Illegal Action Parameters");
		return this;
	}
	public Result passError(){
		put("Code","103");
		put("Msg","User or Password Error");
		return this;
	}
	public Result nullObject(){
		put("Code","104");
		put("Msg","Can not Find Object");
		return this;
	}
	public Result lackOfPara(){
		put("Code", 105);
		put("Msg", "Lack Of Parameter");
		return this;
	}
	
	public Result tokenSynaticError(){
		this.put("Code",101);
		this.put("Msg","Token Synatic Error,use the token given by login");
		return this;
	}
}
