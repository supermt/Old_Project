package imusic.server.models;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

public class VIP extends Model<VIP> {
	public static final VIP dao = new VIP();
	public List<VIP> find(String sql, Object... paras){
		List<VIP> temp = super.find(sql, paras);
		for (VIP a : temp){
			a.addName();
		}
		return temp;
	}
	
	public Page<VIP> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		Page<VIP> temp = super.paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
		for (VIP a : temp.getList()){
			a.addName();
		}
		return temp;
	}
	
	public VIP findFirst(String sql, Object... paras){
		VIP temp = super.findFirst(sql,paras);
		temp.addName();
		return temp;
	}
	public VIP findById(Object idValue){
		VIP temp = super.findById(idValue);
		temp.addName();
		return temp;
	}
	
	public void addName(){
		super.put("username", Musician.dao.findById(this.getInt("userid")).get("username"));
		super.put("avatar", User.dao.findById(this.getInt("userid")).get("avatar"));
	}
}
