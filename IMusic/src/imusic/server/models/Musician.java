package imusic.server.models;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

public class Musician extends Model<Musician> implements Counter{
	public static final Musician dao = new Musician();
	public List<Musician> find(String sql, Object... paras){
		List<Musician> temp = super.find(sql, paras);
		for (Musician a : temp){
			a.addAvatar();
		}
		return temp;
	}
	
	public Page<Musician> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		Page<Musician> temp = super.paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
		for (Musician a : temp.getList()){
			a.addAvatar();
		}
		return temp;
	}
	
	public Musician findFirst(String sql, Object... paras){
		Musician temp = super.findFirst(sql,paras);
		if (temp==null){
			return null;
		}
		temp.addAvatar();
		return temp;
	}
	
	public Musician findById(Object idValue){
		Musician temp = super.findById(idValue);
		if (temp ==null){
			return null;
		}
		temp.addAvatar();
		return temp;
	}
	
	public void addAvatar(){
		super.put("avatar", User.dao.findById(this.getInt("userid")).get("avatar"));
	}
	public void active(){
		super.set("status", 3).update();
	}
	public void deactive(){
		super.set("status", 2).update();
	}
}
