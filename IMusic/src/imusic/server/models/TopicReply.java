package imusic.server.models;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

public class TopicReply extends Model<TopicReply> {
	public static final TopicReply dao = new TopicReply();
	public List<TopicReply> find(String sql, Object... paras){
		List<TopicReply> temp = super.find(sql, paras);
		for (TopicReply a : temp){
			a.addName();
		}
		return temp;
	}
	
	public Page<TopicReply> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		Page<TopicReply> temp = super.paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
		for (TopicReply a : temp.getList()){
			a.addName();
		}
		return temp;
	}
	
	public TopicReply findFirst(String sql, Object... paras){
		TopicReply temp = super.findFirst(sql,paras);
		if (temp ==null){
			return null;
		}
		temp.addName();
		return temp;
	}
	
	public TopicReply findById(Object idValue){
		TopicReply temp = super.findById(idValue);
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