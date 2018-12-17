package imusic.server.Controller;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import imusic.server.aop.UserAuth;
import imusic.server.models.Speak;
import imusic.server.models.SpeakReply;
import imusic.server.models.cacheUtils.JedisUtils;

@Before(UserAuth.class)
public class HomeController extends Controller implements EventNeedCache{
	//Get need no auth
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
	public void speak(){
		Result result = new Result();
		if (this.getRequest().getMethod().equals("GET")){
			String uid = getPara("userid");
			if (uid == null){
				result.put("Code", 105);
				result.put("Msg", "Lack Of userID");
				renderJson(result);
				return ;
			}
			else{
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<Speak> List = Speak.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from speak where enable = 1 and userid = ? Order By addtime DESC",uid);
				result.put("Code", 200);
				result.put("Result", List);
			}
		}else if (this.getRequest().getMethod().equals("POST")){//need auth
			int uid = getID();//need auth
			if (uid == 0){
				result.put("Code", 101);
				result.put("Msg", "Need Auth");
				renderJson(result);
				return ;
			}
			String sid = getPara("id");
			String content = getPara("content");
			if (content == null){
				result.put("Code", 105);
				result.put("Msg", "Lack Of content");
				renderJson(result);
				return ;
			}
			if (sid==null){
					Speak temp = new Speak();
					temp.set("userid", uid).set("content", content).set("enable", 1).set("addtime", new Timestamp(System.currentTimeMillis())).save();
					this.pushIntoRedis(temp.getInt("id"), "CREATE");
					result.put("Result", temp);
			}//end create
			else{
				//start modify,need Auth
				Speak temp = Speak.dao.findById(sid);
				if (temp==null){
					result.put("Code", 104);
					result.put("Msg", "Can not Find Object");
					renderJson(result);
					return ;
				}
				temp.set("content", content).update();
				this.pushIntoRedis(temp.getInt("id"), "UPDATE");
			}
		}
		else{
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
		}
		renderJson(result);
	}
	
	@ActionKey("/speak/reply")
	public void reply(){
		Result result = new Result();
		if (this.getRequest().getMethod().equals("GET")){
			String sid = getPara("speakid");
			if (sid == null){
				result.put("Code", 105);
				result.put("Msg", "Lack Of spaekID");
				renderJson(result);
				return ;
			}
			else{
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<SpeakReply> List = SpeakReply.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from speakreply where enable = 1 and speakid = ? Order By addtime DESC",sid);
				result.put("Code", 200);
				result.put("Result", List);
				
			}
		}//end get
		else if (this.getRequest().getMethod().equals("POST")){
			String sid = getPara("speakid");
			String rid = getPara("replyid");
			String content = getPara("content");
			if (sid == null && rid == null){
				result.put("Code", 105);
				result.put("Msg", "Lack Of speakid");
				renderJson(result);
				return ;
			}
			if (rid!=null){//post to modify
				SpeakReply temp = SpeakReply.dao.findById(rid);
				Integer uid = getID();
				if (uid==0||uid!=temp.getInt("userid")){
					result.needAuth();
					result.put("Msg", "May be this is not your reply,Or you need login in first");
					renderJson(result);
					return ;
				}
				if (content == null){
					result.put("Code", 105);
					result.put("Msg", "Lack Of Content");
					renderJson(result);
					return ;
				}
				
				temp.set("content", content).update();
			}
			else{
				SpeakReply temp = new SpeakReply();
				int uid = getID();
				if (uid == 0){
					result.put("Code", 101);
					result.put("Msg", "Need Auth");
					renderJson(result);
					return ;
				}
				if (sid!=null && content !=null){
					temp.set("speakid",sid).set("userid", uid).set("content", content).set("addtime", new Timestamp(System.currentTimeMillis())).set("enable", 1).save();
					result.put("Result", temp);
				}
				else{
					result.put("Code", 105);
					result.put("Msg", "Lack Of parameters");
					renderJson(result);
					return ;
				}
			}
			
		}//end create
		else if (this.getRequest().getMethod().equals("DELETE")){
			String rid = getPara("replyid");
			if (rid == null){
				result.put("Code", 105);
				result.put("Msg", "Lack Of replyid");
				renderJson(result);
				return ;
			}
			SpeakReply temp = SpeakReply.dao.findById(rid);
			if (temp == null){
				result.put("Code",104);
				result.put("Msg","Can not Find Object");
				renderJson(result);
				return;
			}
			temp.set("enable", 2).update();
		}//end delete
		else{
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
		}
		renderJson(result);
	}
	@ActionKey("/home/booth")	
	public void booth(){
		Result result = new Result();
		Integer uid = getID();
		if (uid == 0){
			result.put("Code", 101);
			result.put("Msg", "Need Auth");
			renderJson(result);
			return ;
		}
		File dir = new File(PathKit.getWebRootPath()+"/file/photo/"+uid);
		File[] photos = dir.listFiles();
		List paths = new Vector();
		String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
		String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
		int a = Integer.valueOf(pageNum);
		int b = Integer.valueOf(pageSize);
		try{
			for (int i = (a-1)*b;i<a*b && i < photos.length;i++){
				paths.add(photos[i].toURI().toString().replaceFirst("file:"+PathKit.getWebRootPath()+"/file", ""));
			}
			Page pathpage = new Page(paths,a,b,photos.length/b+1,photos.length);
			result.put("Result", pathpage);
			renderJson(result);
			return ;
		}catch(NullPointerException e){
			result.put("Code", 104);
			result.put("Msg", "Can not find any target in file system");
			renderJson(result);
			return ;
		}
	}
	
	@Override
	public void pushIntoRedis(Integer eventID,String action){
		// TODO Auto-generated method stub
		List<Record> targets = Db.find("SELECT * from relation_follow where userid = ? and enable = 1",this.getID().toString());
		targets.add(Db.findById("user",this.getID()));
		for (Record target : targets){
			JedisUtils.pushInto("Speak",action,target.getInt("userid2"), eventID);
		}
	}
	
}
