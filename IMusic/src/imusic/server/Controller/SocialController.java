package imusic.server.Controller;

import java.sql.Timestamp;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import imusic.server.aop.UserAuth;
import imusic.server.config.MainConfig;
import imusic.server.models.*;
import imusic.server.models.cacheUtils.JedisUtils;
@Before(UserAuth.class)
public class SocialController extends Controller implements EventNeedCache {
	
	private Integer getID(){
//		String token = this.getRequest().getHeader("token");
//		String userid = this.getSessionAttr("userID")==null ? null : this.getSessionAttr("userID").toString();
//		//System.out.println(userid);
//		if (userid == null && token!=null){
//			User temp = User.dao.findFirst("Select * from user where token = ?",token);
//			if (temp!=null)
//			return Integer.valueOf( temp.getInt("id"));
//			else return 0;
//		}
//		else if (userid == null) return 0;
//		else return Integer.valueOf(userid);
		return Integer.valueOf(this.getRequest().getAttribute("id").toString());
	}
	@ActionKey("/club")
	public void club(){
		Result result = new Result();
		if (this.getRequest().getMethod().equals("GET")){
			String ma = getPara("musicianid");
			if (ma == null){
				result.put("Code", 105);
				result.put("Msg", "Lack Of musicianID");
				renderJson(result);
				return ;
			}
			else{
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<Topic> List = Topic.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from topic where enable = 1 and musicianid = ? Order By addtime DESC,viewscount DESC",ma);
				result.put("Code", 200);
				result.put("Result", List);
			}
		}
		else{
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
		}
		renderJson(result);
	}//end club
	
	@ActionKey("/toptopic")
	public void topTopic(){
		Result result = new Result();
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		Page<Topic> List = Topic.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "SELECT *"," FROM topic order by viewscount DESC,replycount DESC");
		result.put("Code", 200);
		result.put("Result", List);
		renderJson(result);
	}
	
	@ActionKey("/freshtopic")
	public void freshTopic(){
		Result result = new Result();
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		Page<Topic> List = Topic.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "SELECT *"," FROM music.topic order by addtime DESC");
		result.put("Code", 200);
		result.put("Result", List);
		renderJson(result);
	}
	
	//topicAction
	public void index(){
		Result result = new Result();
		if (this.getRequest().getMethod().equals("GET")){
			String a = getPara("id");
			if (a == null){
				result.put("Code", 105);
				result.put("Msg", "Lack Of TopicID");
				renderJson(result);
				return ;
			}
			else{
				Topic temp = Topic.dao.findById(a);
				if (temp==null){
					result.put("Code", 104);
					result.put("Msg","Can not Find Object");
				}
				else{
					temp.set("replycount", Db.queryLong("SELECT count(*) FROM topicreply where topicid=?",temp.getInt("id"))).
					set("viewscount",temp.getInt("viewscount")+1).update();
					result.put("Result", temp);
					
				}
			}
		}
		else if (this.getRequest().getMethod().equals("POST")){
			String ma = getPara("musicianid");
			String title = getPara("title");
			String content = getPara("content");
			String id = getPara("id");
			if (id!=null){
				if (title != null && content != null){
					Topic temp = Topic.dao.findById(id);
					if (temp == null) {
						result.put("Code", 104);
						result.put("Msg","Can not Find Object");
						renderJson(result);
						return ;
					}
					temp.set("title", title).set("text", content);
					if (!temp.update()) {
						result.put("Code", 107);
						result.put("Msg", "Error Parameters lead Database Error");
						renderJson(result);
						return ;
					}
				}else {
					result.put("Code", 105);
					result.put("Msg", "Lack of Parameters");
				}
			}
			else if (title != null && content != null && ma != null){
				int uid = getID();
				if (uid == 0){
					result.put("Code", 101);
					result.put("Msg", "Need Auth");
					renderJson(result);
					return ;
				}
				Topic temp = new Topic();
				temp.set("musicianid", ma).set("title", title).set("text", content).set("userid",uid).set("enable", 1).set("addtime", new Timestamp(System.currentTimeMillis())).save();
				result.put("Result", temp);
				this.pushIntoRedis(temp.getInt("id"), "CREATE");
			}else {
				result.put("Code", 105);
				result.put("Msg", "Lack of Parameters");
			}
		}
		else{
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
		}
		renderJson(result);
	}
	//@ActionKey("/api/topic/reply")
	public void reply(){
		Result result = new Result();
		if (this.getRequest().getMethod().equals("GET")){
			String tid = getPara("topicid");
			if (tid == null){
				result.put("Code", 105);
				result.put("Msg", "Lack Of topicid");
				renderJson(result);
				return ;
			}
			else{
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<TopicReply> List = TopicReply.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from topicreply where enable = 1 and topicid = ? Order By addtime DESC",tid);
				result.put("Code", 200);
				result.put("Result", List);
			}
		}
		else if (this.getRequest().getMethod().equals("POST")){
			String ma = getPara("topicid");
			String content = getPara("content");
			String id = getPara("id");
			if (id!=null){
				if (content != null){
					TopicReply temp = TopicReply.dao.findById(id);
					if (temp == null) {
						result.put("Code", 104);
						result.put("Msg","Can not Find Object");
						renderJson(result);
						return ;
					}
					temp.set("text", content);
					temp.update();
					if (!temp.update()) {
						result.put("Code", 107);
						result.put("Msg", "Error Parameters lead Database Error");
					}
				}else {
					result.put("Code", 105);
					result.put("Msg", "Lack of Parameters");
				}
			}
			else if (content != null && ma != null){
				int uid = getID();
				if (uid == 0){
					result.put("Code", 101);
					result.put("Msg", "Need Auth");
					renderJson(result);
					return ;
				}
				TopicReply temp = new TopicReply();
				temp.set("topicid", ma).set("text", content).set("userid",uid).set("enable",1).set("addtime", new Timestamp(System.currentTimeMillis())).save();
				result.put("Result", temp);
			}else {
				result.put("Code", 105);
				result.put("Msg", "Lack of Parameters");
			}
		}
		else{
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
		}
		renderJson(result);
	}
	@Override
	public void pushIntoRedis(Integer eventID,String action) {
		// TODO Auto-generated method stub
		List<Record> targets = Db.find("SELECT * from relation_follow where userid = ? and enable = 1",this.getID().toString());
		for (Record target : targets){
			JedisUtils.pushInto("Topic",action,target.getInt("userid2"), eventID);
		}
	}
}
