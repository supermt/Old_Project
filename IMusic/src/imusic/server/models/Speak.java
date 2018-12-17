package imusic.server.models;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

public class Speak extends Model<Speak> {
	public static final Speak dao = new Speak();
	public List<Speak> find(String sql, Object... paras){
		List<Speak> temp = super.find(sql, paras);
		for (Speak a : temp){
			a.addName();
		}
		return temp;
	}
	
	public Page<Speak> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		Page<Speak> temp = super.paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
		for (Speak a : temp.getList()){
			a.addName();
		}
		return temp;
	}
	
	public Speak findFirst(String sql, Object... paras){
		Speak temp = super.findFirst(sql,paras);
		temp.addName();
		return temp;
	}
	
	public Speak findById(Object idValue){
		Speak temp = super.findById(idValue);
		temp.addName();
		return temp;
	}
	
	public void addName(){
		super.put("authorName", User.dao.findById(this.getInt("userid")).get("username"));
		super.put("avatar", User.dao.findById(this.getInt("userid")).get("avatar"));
	}
}
