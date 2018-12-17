package imusic.server.models;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

public class SongComment extends Model<SongComment> {
	public static final SongComment dao = new SongComment();
	public List<SongComment> find(String sql, Object... paras){
		List<SongComment> temp = super.find(sql, paras);
		for (SongComment a : temp){
			a.addName();
		}
		return temp;
	}
	
	public Page<SongComment> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		Page<SongComment> temp = super.paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
		for (SongComment a : temp.getList()){
			a.addName();
		}
		return temp;
	}
	
	public SongComment findFirst(String sql, Object... paras){
		SongComment temp = super.findFirst(sql,paras);
		if (temp==null){
			return null;
		}
		temp.addName();
		return temp;
	}
	
	public SongComment findById(Object idValue){
		SongComment temp = super.findById(idValue);
		if (temp ==null){
			return null;
		}
		temp.addName();
		return temp;
	}
	
	
	public void addName(){
		super.put("authorName", User.dao.findById(this.getInt("userid")).get("username"));
		super.put("avatar", User.dao.findById(this.getInt("userid")).get("avatar"));
	}
	
	public void active(){
		super.set("enable", 1).update();
	}
	public void deactive(){
		super.set("enable", 2).update();
	}
	
}
