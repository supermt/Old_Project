package imusic.server.models;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

public class SpeakReply extends Model<SpeakReply> {
	public static final SpeakReply dao = new SpeakReply();
	public List<SpeakReply> find(String sql, Object... paras){
		List<SpeakReply> temp = super.find(sql, paras);
		for (SpeakReply a : temp){
			a.addName();
		}
		return temp;
	}
	
	public Page<SpeakReply> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		Page<SpeakReply> temp = super.paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
		for (SpeakReply a : temp.getList()){
			a.addName();
		}
		return temp;
	}
	
	public SpeakReply findFirst(String sql, Object... paras){
		SpeakReply temp = super.findFirst(sql,paras);
		if (temp==null){
			return null;
		}
		temp.addName();
		return temp;
	}
	public SpeakReply findById(Object idValue){
		SpeakReply temp = super.findById(idValue);
		if (temp ==null){
			return null;
		}
		temp.addName();
		return temp;
	}
	
	public void addName(){
		super.put("username", User.dao.findById(this.getInt("userid")).get("username"));
		super.put("avatar", User.dao.findById(this.getInt("userid")).get("avatar"));
	}
}
