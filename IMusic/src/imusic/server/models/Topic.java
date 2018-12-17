package imusic.server.models;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

public class Topic extends Model<Topic> {
	public static final Topic dao = new Topic();
	
	public List<Topic> find(String sql, Object... paras){
		List<Topic> temp = super.find(sql, paras);
		for (Topic a : temp){
			a.addName();
		}
		return temp;
	}
	
	public Page<Topic> mostPopularTopic(Integer pageNumber,Integer pageSize){
		return paginate(pageNumber, pageSize, "select *", "from topic where enable = 1 order by replycount DESC");
	}
	
	public Page<Topic> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		Page<Topic> temp = super.paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
		for (Topic a : temp.getList()){
			a.addName();
		}
		return temp;
	}
	
	public Topic findFirst(String sql, Object... paras){
		Topic temp = super.findFirst(sql,paras);
		if (temp ==null){	
			return null;
		}
		temp.addName();
		return temp;
	}
	public Topic findById(Object idValue){
		Topic temp = super.findById(idValue);
		if (temp ==null){
			return null;
		}
		temp.addName();
		return temp;
	}
	
	public void addName(){
		super.put("musicianName", Musician.dao.findById(this.getInt("musicianid")).get("nickname"));
		super.put("authorNmae",User.dao.findById(this.getInt("userid")).get("username"));
		super.put("avatar", User.dao.findById(this.getInt("userid")).get("avatar"));
	}
	public void active(){
		super.set("enable", 1).update();
	}
	public void deactive(){
		super.set("enable", 2).update();
	}
}
