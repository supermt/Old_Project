package imusic.server.models;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class User extends Model<User> {
	public static final User dao = new User();
	
	public void active(){
		super.set("enable", 1).update();
	}
	
	public void deactive(){
		super.set("enable", 2).update();
	}
	
	public static boolean mailexists(String email){
		return !dao.find("select * from user where mail = ?", email).isEmpty();
	}
	
	public static boolean nameexists(String name){
		return !dao.find("select * from user where username = ?", name).isEmpty();
	}
	
	public static Record createUser(String username,String password){
		Record newUser = new Record();
		newUser.set("username", username);
		newUser.set("password", password);
		Db.save("user", newUser);
		return newUser;
	}
	
	public static Page<User> randomUser(String pageNum, String pageSize){
		Page<User> result = dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "Select * ", "from user where enable = 1 ORDER BY RAND()");
		for (User each : result.getList()){
			each.remove("token","safeanswer","safequestion","password");
		}
		
		return result;
	}
	
	public static Page<User> searchUser(String pageNum, String pageSize,String username){
		Page<User> result = dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "Select * ", "from user where enable = 1 AND username like ?","%"+username+"%");
		for (User each : result.getList()){
			each.remove("token","safeanswer","safequestion","password");
		}
		
		return result;
	}
}
