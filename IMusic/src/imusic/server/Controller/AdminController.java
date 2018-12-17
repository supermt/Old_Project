package imusic.server.Controller;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Page;

import imusic.server.aop.LogProducer;
import imusic.server.models.*;
//@Before(AdminAuth.class)
@Before(LogProducer.class)
//@Before(RestProcesser.class)
public class AdminController extends Controller {
	@Clear
	@ActionKey("admin/login")
	public void login(){
		Result result = new Result();
		String admin=getPara("username");
		String pass=getPara("password");
		Admin a = Admin.dao.findFirst(
				"SELECT * FROM admin WHERE username =? AND password =?",
				admin,pass);
		if (a==null) {
			result.put("Code", 103);
			result.put("Msg", "Wrong Username Or Password");
			renderJson(result);
		}
		else {
			result.put("Result", "UserName:"+admin);
			renderJson(result);
			this.setSessionAttr("admin", admin);
			this.setSessionAttr("adminID", a.get("id"));
			}
		
	}
	
	@ActionKey("admin/logout")
	public void logout(){
		renderJson(new Result());
		this.removeSessionAttr("adminID");
		this.removeSessionAttr("admin");
	}
	
	public void show(){
		String extra = "";
		int i=1;
		while (extra!=null){
			extra = getPara(i);
			System.out.println(extra+i);
			i++;
		}
		renderText("adminID"+this.getSessionAttr("adminID")+this.getRequest().getMethod());
	}
	
	public void topic(){
		Result result = new Result();
		if (this.getRequest().getMethod().equals("GET")){
			String a = getPara("id");
			if (a!=null){
				Topic temp = Topic.dao.findById(a);
				//List<TopicReply> replyList = TopicReply.dao.find("Select * from topicreply where topicid = ?",a);
				if (temp!=null){
					result.put("Code", 100);
					result.put("Result",temp);
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}
			}
			else {
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<Topic> topicList = Topic.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from topic order by id DESC");
				result.put("Code", 200);
				result.put("Result", topicList);
				
			}
		}//End Get
		else if (this.getRequest().getMethod().equals("PUT")){
			String a = getPara("id");
			if (a!=null){
				Topic temp = Topic.dao.findById(a);
				if (temp!=null){
					temp.set("enable", "1").update();
					result.put("Code", 100);
					result.put("Result","Topic has been ENABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of parameters");
			}//end a==null
		}//End Put
		else if(this.getRequest().getMethod().equals("DELETE")){
			String a = getPara("id");
			if (a!=null){
				Topic temp = Topic.dao.findById(a);
				if (temp!=null){
					temp.set("enable", "2").update();
					result.put("Code", 100);
					result.put("Result","Topic has been DISABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of parameters");
			}//end a==null
		}else {
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
		}//Illegal method
		//返回结果
		renderJson(result);
	}
	
	public void news(){
		Result result = new Result();
		if (this.getRequest().getMethod().equals("GET")){
			String a = getPara("id");
			if (a!=null){
				News temp = News.dao.findById(a);
				if (temp!=null){
					result.put("Code", 100);
					result.put("Result",temp);
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}
			}
			else {
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<News> List = News.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from news order by id DESC");
				result.put("Code", 200);
				result.put("Result", List);
			}
		}//End Get
		else if(this.getRequest().getMethod().equals("DELETE")){
			String a = getPara("id");
			if (a!=null){
				News temp = News.dao.findById(a);
				if (temp!=null){
					temp.delete();
					result.put("Code", 100);
					result.put("Result","Object has been DELETED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of ID");
			}//end a==null
		}else 
		if(this.getRequest().getMethod().equals("POST")){
			String a = getPara("id");
			if (a == null){
				String title = getPara("title");
				String content = getPara("content");
				String type = getPara("type");
				if (title!=null && content!=null && type!=null)
					try{
						News temp = new News();
						temp.set("title", getPara("title")).set("content", getPara("content"))
						.set("type", getPara("type")).set("admin", getSessionAttr("adminID")).
						set("addtime", new Timestamp(System.currentTimeMillis())).save();
						result.put("Result", temp);
					}catch(ActiveRecordException e){
						result.put("Code", 106);
						result.put("Msg","Error Parameters Lead a SQL Exception");
					}
				else {
					result.put("Code", 105);
					result.put("Msg", "Lack Of Parameter");
				}
			}//end post news
			else {
				try {
					News temp =News.dao.findById(a);
					String title = getPara("title");
					String content = getPara("content");
					String type = getPara("type");
					if (title!=null && content!=null && type!=null)
					temp.set("title", getPara("title")).set("content", getPara("content"))
					.set("type", getPara("type")).update();
					else {
						result.put("Code", 105);
						result.put("Msg", "Lack Of Parameter");
					}
				}catch(NullPointerException e){
					result.put("Code", 104);
					result.put("Msg", "Can not Found Object");
				}
			}
			
		}
		else
		{
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
		}//Illegal method
		//返回结果
		renderJson(result);
	}
	
	public void musician(){
		Result result = new Result();
		if (this.getRequest().getMethod().equals("GET")){
			String a = getPara("id");
			if (a!=null){
				Musician temp = Musician.dao.findById(a);
				if (temp!=null){
					result.put("Code", 100);
					result.put("Result",temp);
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}
			}
			else {
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<Musician> List = Musician.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from musician order by id DESC");
				result.put("Code", 200);
				result.put("Result", List);
			}
		}//End Get
		//put方法中，激活音乐家，级联更新user表
		else if (this.getRequest().getMethod().equals("PUT")){
			String a = getPara("id");
			if (a!=null){
				Musician temp = Musician.dao.findById(a);
				if (temp!=null){
					temp.set("status", "3").update();
					int userid = temp.getInt("userid");
					User.dao.findById(userid).set("type", 2).update();
					result.put("Code", 100);
					result.put("Result","Object has been ENABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of MusicianID");
			}//end a==null
		}//End Put
		else if(this.getRequest().getMethod().equals("DELETE")){
			String a = getPara("id");
			if (a!=null){
				Musician temp = Musician.dao.findById(a);
				if (temp!=null){
					temp.set("status", "2").update();
					result.put("Code", 100);
					result.put("Result","Object has been DISABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of ID");
			}//end a==null
		}else {
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
		}//Illegal method
		//返回结果
		renderJson(result);
	}
	
	public void org(){
		Result result = new Result();
		if (this.getRequest().getMethod().equals("GET")){
			String a = getPara("id");
			if (a!=null){
				User temp = User.dao.findFirst("Select * from user where type = 3 and id = ?",a);
				if (temp!=null){
					result.put("Code", 100);
					result.put("Result",temp);
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}
			}
			else {
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<User> List = User.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from user where type = 3 order by id DESC" );
				result.put("Code", 200);
				result.put("Result", List);
			}
		}//End Get
		//激活，将enable字段设为1后
		else if (this.getRequest().getMethod().equals("PUT")){
			String a = getPara("id");
			if (a!=null){
				User temp = User.dao.findFirst("Select * from user where type = 3 and id = ?",a);
				if (temp!=null){
					temp.set("enable", "1").update();
					result.put("Code", 100);
					result.put("Result","Object has been ENABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of ID");
			}//end a==null
		}//End Put
		else if(this.getRequest().getMethod().equals("DELETE")){
			String a = getPara("id");
			if (a!=null){
				User temp = User.dao.findFirst("Select * from user where type = 3 and id = ?",a);
				if (temp!=null){
					temp.set("enable", "2").update();
					result.put("Code", 100);
					result.put("Result","Object has been DISABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of ID");
			}//end a==null
		}else {
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
		}//Illegal method
		//返回结果
		renderJson(result);
	}
	
	public void user(){
		Result result = new Result();
		if (this.getRequest().getMethod().equals("GET")){
			String a = getPara("id");
			if (a!=null){
				User temp = User.dao.findById(a);
				if (temp!=null){
					result.put("Code", 100);
					result.put("Result",temp);
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}
			}
			else {
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<User> List = User.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from user order by id DESC");
				result.put("Code", 200);
				result.put("Result", List);
			}
		}//End Get
		//激活，将enable字段设为1后
		else if (this.getRequest().getMethod().equals("PUT")){
			String a = getPara("id");
			if (a!=null){
				User temp = User.dao.findById(a);
				if (temp!=null){
					temp.active();
					result.put("Code", 100);
					result.put("Result","Object has been ENABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of ID");
			}//end a==null
		}//End Put
		else if(this.getRequest().getMethod().equals("DELETE")){
			String a = getPara("id");
			if (a!=null){
				User temp = User.dao.findById(a);
				if (temp!=null){
					temp.deactive();
					result.put("Code", 100);
					result.put("Result","Object has been DISABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of ID");
			}//end a==null
		}else {
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
		}//Illegal method
		//返回结果
		renderJson(result);
	}
	
	public void song(){
		Result result = new Result();
		if (this.getRequest().getMethod().equals("GET")){
			String a = getPara("id");
			if (a!=null){
				Song temp = Song.dao.findById(a);
				if (temp!=null){
					result.put("Code", 100);
					result.put("Result",temp);
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}
			}
			else {
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<Song> List = Song.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from song order by id DESC" );
				result.put("Code", 200);
				result.put("Result", List);

			}
		}//End Get
		//激活，将enable字段设为1后
		else if (this.getRequest().getMethod().equals("PUT")){
			String a = getPara("id");
			if (a!=null){
				Song temp = Song.dao.findById(a);
				if (temp!=null){
					temp.active();
					result.put("Code", 100);
					result.put("Result","Object has been ENABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of ID");
			}//end a==null
		}//End Put
		else if(this.getRequest().getMethod().equals("DELETE")){
			String a = getPara("id");
			if (a!=null){
				Song temp = Song.dao.findById(a);
				if (temp!=null){
					temp.deactive();
					result.put("Code", 100);
					result.put("Result","Object has been DISABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of ID");
			}//end a==null
		}else {
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
		}//Illegal method
		//返回结果
		renderJson(result);
	}
	
	public void topicreply(){
		Result result = new Result();
		if (this.getRequest().getMethod().equals("GET")){
			String topicid = getPara("topicid");
			if (topicid!=null){
				List<TopicReply> replyList = TopicReply.dao.find("Select * from topicreply where topicid = ?",topicid);
				if (replyList.isEmpty()){
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}else {
					result.put("Code", 200);
					result.put("Result", replyList);
				}
				renderJson(result);
				return ;
			}
			String a = getPara("id");
			if (a!=null){
				TopicReply temp = TopicReply.dao.findById(a);
				if (temp!=null){
					result.put("Code", 100);
					result.put("Result",temp);
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}
			}
			else {
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<TopicReply> List = TopicReply.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from topicreply order by id DESC");
				result.put("Code", 200);
				result.put("Result", List);
			}
		}//End Get
		else if (this.getRequest().getMethod().equals("PUT")){
			String a = getPara("id");
			if (a!=null){
				TopicReply temp = TopicReply.dao.findById(a);
				if (temp!=null){
					temp.set("enable", "1").update();
					result.put("Code", 100);
					result.put("Result","Object has been ENABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of MusicianID");
			}//end a==null
		}//End Put
		else if(this.getRequest().getMethod().equals("DELETE")){
			String a = getPara("id");
			if (a!=null){
				TopicReply temp = TopicReply.dao.findById(a);
				if (temp!=null){
					temp.set("enable", "2").update();
					result.put("Code", 100);
					result.put("Result","Object has been DISABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of ID");
			}//end a==null
		}else {
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
		}//Illegal method
		//返回结果
		renderJson(result);
	}
	
	public void songcomment(){
		Result result = new Result();
		if (this.getRequest().getMethod().equals("GET")){
			String songid = getPara("songid");
			if (songid!=null){
				List<SongComment> commentList = SongComment.dao.find("Select * from songcomment where songid = ?",songid);
				if (commentList.isEmpty()){
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}else {
					result.put("Code", 200);
					result.put("Result", commentList);
				}
				renderJson(result);
				return ;
			}
			String a = getPara("id");
			if (a!=null){
				SongComment temp = SongComment.dao.findById(a);
				if (temp!=null){
					result.put("Code", 100);
					result.put("Result",temp);
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}
			}
			else {
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<SongComment> List = SongComment.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from songcomment order by id DESC");
				result.put("Code", 200);
				result.put("Result", List);
			}
		}//End Get
		else if (this.getRequest().getMethod().equals("PUT")){
			String a = getPara("id");
			if (a!=null){
				SongComment temp = SongComment.dao.findById(a);
				if (temp!=null){
					temp.active();
					result.put("Code", 100);
					result.put("Result","Object has been ENABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of MusicianID");
			}//end a==null
		}//End Put
		else if(this.getRequest().getMethod().equals("DELETE")){
			String a = getPara("id");
			if (a!=null){
				SongComment temp = SongComment.dao.findById(a);
				if (temp!=null){
					temp.deactive();
					result.put("Code", 100);
					result.put("Result","Object has been DISABLED");
				}else {
					result.put("Code", 104);
					result.put("Msg", "Can Not Find Object");
				}//end temp == null
			}
			else {
				result.put("Code", 105);
				result.put("Msg", "Lack Of ID");
			}//end a==null
		}else {
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
		}//Illegal method
		//返回结果
		renderJson(result);
	}
	@ActionKey("/review/musician")
	public void reviewMusician(){
		Result result = new Result();
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		Page<Musician> List = Musician.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from musician where status = 1 order by id DESC");
		result.put("Code", 200);
		result.put("Result", List);
		renderJson(result);
	}
	
	@ActionKey("/failed/musician")
	public void failedMusician(){
		Result result = new Result();
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		Page<Musician> List = Musician.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from musician where status = 2 order by id DESC");
		result.put("Code", 200);
		result.put("Result", List);
		renderJson(result);
	}
	
	@ActionKey("/review/org")
	public void reviewOrg(){
		Result result = new Result();
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		Page<User> List = User.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from user where type = 3 and enable != 1 order by regtime DESC");
		result.put("Code", 200);
		result.put("Result", List);
		renderJson(result);
	}
	@ActionKey("/review/song")
	public void reviewSong(){
		Result result = new Result();
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		Page<Song> List = Song.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from song where status != 2 order by id DESC");
		result.put("Code", 200);
		result.put("Result", List);
		renderJson(result);
	}
	public void ring(){
		Result result = new Result();
		switch(this.getRequest().getMethod()){
			case "POST":{
				String id = getPara("id");
				if(id==null){//create
					String photo = getPara("photo");
					String ringname = getPara("ringname");
					String ringmaker = getPara("ringmaker");
					String url = getPara("url");
					if (photo == null || url == null || ringname == null || ringmaker == null){
						result.clear();
						result.put("success", false);
						result.put("msg", "Lack of Parameters");
						result.put("file_path",null);
						renderJson(result);
						return ;
					}else{
						try{
							Ring temp = new Ring();
							temp.set("ringname", ringname).set("ringmaker", ringmaker).set("photo", photo)
							.set("url", url).set("addtime", new Timestamp(System.currentTimeMillis())).save();
							result.put("Result", temp);
							renderJson(result);
							return ;
						}catch(Exception e){
							result.clear();
							result.put("success", false);
							result.put("msg", "Illegal Parameters");
							result.put("file_path",null);
							renderJson(result);
							return ;
						}
					}//end create
				}else{//modify
					Ring temp = Ring.dao.findById(id);
					if (temp==null){
						result.put("Code", 104);
						result.put("Msg", "Can not Object");
						renderJson(result);
						return;
					}
					String ringname = getPara("ringname");
					String ringmaker = getPara("ringmaker");
					String url = getPara("url");
					String photo = getPara("photo");
					if (ringname!=null) temp.set("ringname", ringname);
					if (ringmaker!=null) temp.set("ringmaker", ringmaker);
					if (url!=null) temp.set("url", url);
					if (photo!=null) temp.set("photo", photo);
					temp.update();
					renderJson(result);
				}
				break;
			}
			case "DELETE":{
				String id = getPara("id");
				if (id==null){
					result.lackOfPara();
					renderJson(result);
					return ;
				}
				else{
					
					Ring temp = Ring.dao.findById(id);
					if (temp==null) {
						result.nullObject();
						renderJson(result);
						return ;
					}
					Ring.dao.deleteById(id);
				}
				break;
			}
			default: {
				result.put("Code","102");
				result.put("Msg", "Illegal Action Parameter");
				break;
			}
		}
		renderJson(result);
	}
	public void log(){
		Result result = new Result();
		String a = getPara("id");
		if (a!=null){
			Log temp = Log.dao.findById(a);
			if (temp!=null){
				result.put("Code", 100);
				result.put("Result",temp);
			}else {
				result.put("Code", 104);
				result.put("Msg", "Can Not Find Object");
			}
		}
		else {
			String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
			String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
			Page<Log> List = Log.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from log order by id DESC");
			result.put("Code", 200);
			result.put("Result", List);
		}
		renderJson(result);
	}
	
}
