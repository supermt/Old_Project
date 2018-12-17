
package imusic.server.Controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import imusic.server.aop.UserAuth;
import imusic.server.models.*;
import imusic.server.models.cacheUtils.JedisUtils;
@Before(UserAuth.class)
public class NoticeController extends Controller {
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
	
	public void index(){
		Result result = new Result();
		String a = getPara("id");
		if (a!=null){
			News temp = News.dao.findById(a);
			if (temp!=null){
				result.put("Result", temp);
				renderJson(result);
				return ;
			}else {
				result.nullObject();
			}
			renderJson(result);
			return ;
		}
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		Page<News> List = News.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *","from news order by id DESC");
		result.put("Code", 200);
		result.put("Msg", "OK");
		result.put("Result", List);
		renderJson(result);
	}
	
	public void news(){
		Result result = new Result();
		int days = getParaToInt("days") == null ? 0:getParaToInt("days");
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		if(days  == 0) {
			Page<News> List = News.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *",
					"from news where type = 1 order by id DESC");
			result.put("Code", 200);
			result.put("Result", List);
			renderJson(result);
			return ;
		}
		Page<News> List = News.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *",
				"from news where addtime > ? and type = 1 order by id DESC",new Timestamp(System.currentTimeMillis() - days*60*60*24*1000));
		result.put("Code", 200);
		result.put("Result", List);
		renderJson(result);
	}
	
	public void activity(){
		Result result = new Result();
		int days = getParaToInt("days") == null ? 0:getParaToInt("days");
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		if(days  == 0) {
			Page<News> List = News.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *",
					"from news where type = 2 order by id DESC");
			result.put("Code", 200);
			result.put("Result", List);
			renderJson(result);
			return ;
		}
		Page<News> List = News.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *",
				"from news where addtime > ? and type = 2 order by id DESC",new Timestamp(System.currentTimeMillis() - days*60*60*24*1000));
		result.put("Code", 200);
		result.put("Result", List);
		renderJson(result);
	}
	
	@ActionKey("/notice/guessulike")
	public void guessYouLike(){
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		Integer userid = 1;//this.getParaToInt("id");
		if(userid == null || userid == 0) {renderJson(new Result().needAuth());return ;}
		renderJson(new Song().guess(Integer.valueOf(pageNum),Integer.valueOf(pageSize),userid));
	}
	
	@Clear
	@ActionKey("/notice/bestrings")
	public void guess(){
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		Integer userid = 1;//this.getParaToInt("id");
		if(userid == null || userid == 0) {renderJson(new Result().needAuth());return ;}
		renderJson(new Ring().best(Integer.valueOf(pageNum),Integer.valueOf(pageSize)));
	}
	
	@Clear
	@ActionKey("/notice/dailymusic")
	public void dailyMusic(){
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		Integer userid = 1;//this.getParaToInt("id");
		if(userid == null || userid == 0) {renderJson(new Result().needAuth());return ;}
		renderJson(new Song().guess(Integer.valueOf(pageNum),Integer.valueOf(pageSize),userid));
	}
	
	
	@Clear
	@ActionKey("/mvp/song")
	public void mostPopularSong(){
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		renderJson(new Song().mostPopularSong(Integer.valueOf(pageNum),Integer.valueOf(pageSize)));
	}
	
	@Clear
	@ActionKey("/mvp/news")
	public void mostPopularNews(){
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		renderJson(new News().mostPopularNews(Integer.valueOf(pageNum),Integer.valueOf(pageSize)));
	}
	
	@Clear
	@ActionKey("/mvp/activity")
	public void mostPopularActivity(){
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		renderJson(new News().mostPopularActivity(Integer.valueOf(pageNum),Integer.valueOf(pageSize)));
	}
	
	@Clear
	@ActionKey("/mvp/topic")
	public void mostPopularTopic(){
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		renderJson(new Topic().mostPopularTopic(Integer.valueOf(pageNum),Integer.valueOf(pageSize)));
	}
	
	@Clear
	@ActionKey("/search/user")
	public void findUsers(){
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		String username = getPara("username");
		if (username==null){
			renderJson(new Result().lackOfPara());
			return ;
		}
		Page<User> users = User.searchUser(pageNum,pageSize,username);
		Result result = new Result().okForList();
		result.put("Msg", "OK");
		result.put("Result", users);
		renderJson(result);
	}
	
	@Clear
	@ActionKey("/search/news")
	public void findNews(){
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		String newsContent = getPara("keyword");
		if (newsContent==null){
			renderJson(new Result().lackOfPara());
			return ;
		}
		
		Page<News> news = News.searchNews(pageNum,pageSize,newsContent);
		Result result = new Result().okForList();
		result.put("Msg", "OK");
		result.put("Result", news);
		renderJson(result);
	}
	
	@Clear
	@ActionKey("/notice/randomUser")
	public void randomUsers(){
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		Page<User> users = User.randomUser(pageNum,pageSize);
		Result result = new Result().okForList();
		result.put("Msg", "OK");
		result.put("Result", users);
		renderJson(result);
	}
	
	@ActionKey("/collection/musician")
	public void followmusician(){
		Result result = new Result();
		Integer userid = getID();
		if(userid==0){
			result.needAuth();
			renderJson(result);
			return ;
		}
		switch(this.getRequest().getMethod()){
			case "GET":{
					String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
					String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
					//Page<Record> pairlist = Db.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "SELECT * from relation_fans where userid = ?",userid.toString());
					Page<Record> pairlist = Db.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize),"SELECT *","from relation_fans where userid = ? and enable = 1",userid.toString());
					String targetid;
					for (Record a : pairlist.getList()){
						a.remove("userid");
						targetid = a.getInt("musicianid").toString();
						a.remove("id");
						a.set("name", Musician.dao.findById(targetid).getStr("nickname"));
					}
					result.put("Code", 200);
					result.put("Result", pairlist);
					break;
				}
			case "POST":{
					String targetid = getPara("musicianid");
					if (targetid == null){
						result.put("Code","105");
						result.put("Msg","Lack Of MuisicianID");
						renderJson(result);
						return ;
					}
					Record temp = Db.findFirst("SELECT * From relation_fans where userid =? and musicianid =?",userid,targetid);
					if (temp == null){
						temp = new Record();
						temp.set("userid", userid).set("musicianid", targetid).set("enable", 1).set("addtime",new Timestamp(System.currentTimeMillis()));
						Db.save("relation_fans", temp);
					}
					else {
						temp.set("enable", 1);
						Db.update("relation_fans",temp);
					}
					break;
				}
			case "DELETE":{
					String targetid = getPara("musicianid");
					if (targetid == null){
						result.put("Code","105");
						result.put("Msg","Lack Of MuisicianID");
						renderJson(result);
						return ;
					}
					Record temp = Db.findFirst("SELECT * From relation_fans where userid =? and musicianid =?",userid,targetid);
					if (temp==null){result.nullObject();renderJson(result);return ;}
					temp.set("enable", 2);
					Db.update("relation_fans",temp);
					break;
				}
			default:{result.illegalMethod();}
		}
		renderJson(result);
	}
	
	/**
	 * get方法提取此用户收藏的用户
	 * post方法对一个用户进行关注
	 * delete方法，删除对一个用户的收藏
	 * get方法不可重用，建议重构
	 * <p>否决重构，一句话函数重构只会变得臃肿</p>
	 */
	
	@ActionKey("/collection/user")
	public void followuser(){
		Result result = new Result();
		Integer userid = getID();
		if(userid==0){
			result.needAuth();
			renderJson(result);
			return ;
		}
		switch(this.getRequest().getMethod()){
			case "GET":{
					String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
					String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
					//Page<Record> pairlist = Db.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "SELECT * from relation_fans where userid = ?",userid.toString());
					//提取被收藏的用户
					Page<User> pairlist = User.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize),"SELECT *","from user where id in ( select userid2 from relation_follow where userid = ? AND enable = 1)",userid.toString());
					
					for (User a : pairlist.getList()){
						a.remove("token","safeanswer","safequestion","password");
					}
					result.put("Code", 200);
					result.put("Result", pairlist);
					break;
				}
			case "POST":{
					String targetid = getPara("userid");
					if (targetid == null){
						result.put("Code","105");
						result.put("Msg","Lack Of targetID");
						renderJson(result);
						return ;
					}
					Record temp = Db.findFirst("SELECT * From relation_follow where userid =? and userid2 =?",userid,targetid);
					if (temp == null){
						temp = new Record();
						if (User.dao.findById(targetid)==null){
							result.put("Msg", "The User You Want Does Not Exists");
							result.put("Code", 104);
							renderJson(result);
							return ;
						}
						temp.set("userid", userid).set("userid2", targetid).set("enable", 1).set("addtime",new Timestamp(System.currentTimeMillis()));
						Db.save("relation_follow", temp);
					}
					else {
						temp.set("enable", 1);
						Db.update("relation_follow",temp);
					}
					break;
				}
			case "DELETE":{
					String targetid = getPara("userid");
					if (targetid == null){
						result.put("Code","105");
						result.put("Msg","Lack Of targetID");
						renderJson(result);
						return ;
					}
					Record temp = Db.findFirst("SELECT * From relation_follow where userid =? and userid2 =?",userid,targetid);
					if (temp==null){result.nullObject();renderJson(result);return ;}
					temp.set("enable", 2);
					Db.update("relation_follow",temp);
					break;
				}
			default:{result.illegalMethod();}
		}
		renderJson(result);
	}
	@ActionKey("/collection/song")
	public void followsong(){
		Result result = new Result();
		switch(this.getRequest().getMethod()){
			case "GET":{
				Integer userid = getID();
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<Record> pairlist = Db.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize),"SELECT *","from songlike where userid = ? and enable = 1",userid);
				result.put("Code", 200);
				result.put("Result", pairlist);
				String targetid;
				try{
					for (Record a : pairlist.getList()){
						a.remove("userid");
						targetid = a.getInt("songid").toString();
						a.remove("id");
						a.set("name", Song.dao.findById(targetid).getStr("songname"));
					}
				}catch(NullPointerException e){
					result.nullObject();
					renderJson(result);
					return ;
				}
				result.put("Code", 200);
				result.put("Result", pairlist);
				break;
			}
			case "POST":{
				String targetid = getPara("songid");
				if (targetid==null){
					result.lackOfPara();
					renderJson(result);
					return ;
				}
				Integer userid = getID();
				if (userid==0){
					result.needAuth();
					renderJson(result);
					return ;
				}
				Record temp = Db.findFirst("SELECT * From songlike where userid =? and songid =?",userid,targetid);
				if (temp == null){
					temp = new Record();
					temp.set("userid", userid).set("songid", targetid).set("enable", 1).set("addtime",new Timestamp(System.currentTimeMillis()));
					Db.save("songlike", temp);
				}
				else {
					temp.set("enable", 1);
					Db.update("songlike",temp);
				}
				break;
			}
			case "DELETE":{
				String targetid = getPara("songid");
				if (targetid==null){
					result.lackOfPara();
					renderJson(result);
					return ;
				}
				Integer userid = getID();
				if (userid==0){
					result.needAuth();
					renderJson(result);
					return ;
				}
				Record temp = Db.findFirst("SELECT * From songlike where userid =? and songid =?",userid,targetid);
				if (temp==null){result.nullObject();renderJson(result);return ;}
				temp.set("enable", 2);
				Db.update("songlike",temp);
				break;
			}
			default:{
				result.illegalMethod();
			}
		}
		renderJson(result);
	}

	@ActionKey("/tome/speak")
	public void recentspeakreply(){
		Result result = new Result();
		Integer userid = getID();
		if (userid ==0){
			result.needAuth();
			renderJson(result);
			return ;
		}
		if (this.getRequest().getMethod().equals("GET")){
			String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
			String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
			Page<SpeakReply> list = SpeakReply.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize),"SELECT *","from speakreply where enable = 1 and speakid in (SELECT id FROM speak where userid = ?) ORDER BY addtime DESC",userid);
			result.put("Result", list);
		}else{
			result.illegalMethod();
		}
		renderJson(result);
	}
	
	@ActionKey("/tome/topic")
	public void recenttopicreply(){
		Result result = new Result();
		Integer userid = getID();
		if (userid ==0){
			result.needAuth();
			renderJson(result);
			return ;
		}
		if (this.getRequest().getMethod().equals("GET")){
			String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
			String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
			Page<TopicReply> list = TopicReply.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize),"SELECT *","from topicreply where enable = 1 and topicid in (SELECT id FROM topic where userid = ?) ORDER BY addtime DESC",userid);
			result.put("Result", list);
		}else{
			result.illegalMethod();
		}
		renderJson(result);
	}
	
	@ActionKey("/me/care")
	public void speakICare(){
		//我关注的人最近发表的帖子
		Result result = new Result();
		Integer userid = getID();
		if (userid == 0){
			result.needAuth();
			renderJson(result);
			return ;
		}
		if (this.getRequest().getMethod().equals("GET")){
			String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");//页面量
			String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");//页面大小
//			String replyNum = getPara("replyAmount")==null ? "1" : getPara("pageAmount");//一个帖子的最大回帖数量
//			if (Integer.valueOf(replyNum) > 10)//限制返回的评论数量不然流量要爆炸
//			{
//				result.lackOfPara().put("Msg","Request For Too Much Reply");
//				renderJson(result);
//				return ;
//			}//end
			//List<Record> speakList = Db.find("SELECT * from relation_follow where userid = ? and enable = 1",userid.toString());
			Page<Speak> speakList = Speak.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from speak as sp where sp.userid in (SELECT userid2 FROM relation_follow where userid = ? )",this.getID());
			Page<Record> topicList = Db.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from topic as tp where tp.userid in (SELECT userid2 FROM relation_follow where userid = ? )",this.getID());

			
//			
//			Page<Object> resultPage = new Page<Object>(resultList, Integer.valueOf(pageNum), Integer.valueOf(pageSize), resultList.size()/Integer.valueOf(pageSize), resultList.size());
//			result.put("Code",200);
//			result.put("Result", resultPage);
//			renderJson(result);
			//renderText(JedisUtils.popOut(this.getID().toString()));
//			String eventInfo = JedisUtils.popOut(this.getID().toString());
//			List resultList = new ArrayList();
//			while (eventInfo!=null){
//				resultList.add(praseEvent(eventInfo));
//				eventInfo = JedisUtils.popOut(this.getID().toString());
//			}
			
			
			
			
			result.put("speaks", speakList);
			result.put("topics", topicList);
			//result.put("Result", resultList);
			result.okForList();
			renderJson(result);
			return ;
		}else{
			result.illegalMethod();
			renderJson(result);
			return ;
		}
	}
	
	private Model praseEvent(String eventInfo){
		String[] temps = eventInfo.split(";");
		switch(temps[0]){
		case "Speak":
			return Speak.dao.findById(temps[2]).put("Action", temps[1]);
		
		case "Topic":
			return Topic.dao.findById(temps[2]).put("Action", temps[1]);
		case "Song":
			return Song.dao.findById(temps[2]).put("Action", temps[1]);
		default : return null;
		}
	}
		
}
