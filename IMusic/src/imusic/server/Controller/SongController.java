package imusic.server.Controller;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import imusic.server.aop.PostMethod;
import imusic.server.aop.UserAuth;
import imusic.server.config.CacheIniter;
import imusic.server.models.*;
import imusic.server.models.cacheUtils.JedisUtils;


@Before(UserAuth.class)
public class SongController extends Controller implements EventNeedCache{
	
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
	
	/**
	 * get方法用于获取音乐
	 * post方法用于创建音乐
	 * delete方法用于禁用某歌
	 * */
	@Before(PostMethod.class)
	public void index(){
		Result result = new Result();
		
		switch(this.getRequest().getMethod()){
			case "GET":{
				String a = getPara("id");
				if (a!=null){
					Song temp = Song.dao.findFirst("Select * From song where id = ? and status = 2 ORDER BY views DESC",a);
					//List<TopicReply> replyList = TopicReply.dao.find("Select * from topicreply where topicid = ?",a);
					if (temp!=null){
						result.put("Code", 100);
						result.put("Result",temp);
						//temp.set("views", temp.getInt("views")+1).update();
					}else if (Song.dao.findFirst("Select * From song where id = ? and status = 1",a)!=null){
						result.put("Code", 104);
						result.put("Msg", "Object has been DISABLED");
					}else{
						result.nullObject();
					}
				}
				else {
					String musicianid  = getPara("musicianid");
					String mname = getPara("name");
					if (musicianid!=null){
						String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
						String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
						Page<Song> songList = Song.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from song where status = 2 and musicianid = ? ORDER BY views DESC",musicianid);
//						for(Song temp:songList.getList()){
//							temp.set("islegal", temp.getInt("islegal")==1);
//						}
						result.put("Code", 200);
						result.put("Result", songList);
					}
					else if (mname!=null){
						String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
						String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
						Page<Song> songList = Song.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from song where status = 2 and songname LIKE '%"+mname+"%' order by id DESC");
						
//						for(Song temp:songList.getList()){
//							temp.set("islegal", temp.getInt("islegal")==1);
//						}
						
						result.put("Code", 200);
						result.put("Result", songList);
					}else{
						String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
						String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
						Page<Song> songList = Song.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from song where status = 2 order by id DESC");
//						for(Song temp:songList.getList()){
//							temp.set("islegal", temp.getInt("islegal")==1);
//						}
						result.put("Code", 200);
						result.put("Result", songList);
					}
					
				}
				break;
			}
			case "POST":{
				Integer userID = getID();
				if (userID==0){
					result.needAuth();
					renderJson(result);
					return;
				}
				try{
					if (User.dao.findById(userID.toString()).getInt("type")!=2
						&& User.dao.findById(userID.toString()).getInt("type")!=3
						|| Musician.dao.findFirst("select * from musician where userid = ? ",userID).getInt("status")!=3){
						result.needAuth();
						result.put("Msg", "You are Not Musician");
						renderJson(result);
						return ;
					}
				}catch(Exception e){
					result.needAuth();
					result.put("Msg", "You are Not Musician");
					renderJson(result);
					return ;
				}
				
				File temp;File ptemp;String songName;
				boolean isLegal;String miguURL;
				try {
				 temp = getFile("filedata") == null ? null : getFile("filedata").getFile();
				 ptemp = getFile("picdata") == null ? null : getFile("picdata").getFile();
				 songName = getPara("songname");
				 isLegal = getParaToBoolean("islegal");
				 miguURL = getPara("miguurl");
				 
				 if (songName == null) songName = temp.getName();
				}catch (NullPointerException e){
					result.clear();
					result.put("success", false);
					result.put("msg", "Lack of Song file Or Photo file");	
					result.put("file_path",null);
					renderJson(result);
					return ;
				}
				String ltemp = getPara("lyric");
				if (isLegal){//正版，不能上传
					try{
						String b = FileController.moveFile(String.valueOf(userID),ptemp,"photo4music");
						Song newSong= new Song().set("musicianid", Musician.dao.findFirst("Select * from musician where userid = ?",userID).get("id")).set("songpath", null)
						.set("songname", songName).set("cover", b).set("islegal", 1).set("miguurl", miguURL).set("status", 1)
						.set("lyric", ltemp).set("views", 0).set("addtime", new Timestamp(System.currentTimeMillis()));
						newSong.save();
						new Photo().set("userid", userID).set("photopath", b).set("addtime", new Timestamp(System.currentTimeMillis())).save();
						result.put("success", true);
						result.put("msg", "Seems So GOOD");
						result.put("file_path", b);
						renderJson(result);
						this.pushIntoRedis(newSong.getInt("id"),"Create");
						break;
					}catch (NullPointerException e){
						result.clear();
						result.put("success", false);
						result.put("msg", "Lack of Song file Or Photo file");	
						result.put("file_path",null);
						renderJson(result);
						return ;
					}
					
				}
				else{
					if (temp == null || ptemp == null){
						result.clear();
						result.put("success", false);
						result.put("msg", "Lack of Song file Or Photo file");
						result.put("file_path",null);
						renderJson(result);
						return ;
					}
					else {
						String a = FileController.moveFile(String.valueOf(userID),temp,"music");
						String b = FileController.moveFile(String.valueOf(userID),ptemp,"photo4music");
						Song newSong= new Song().set("musicianid", Musician.dao.findFirst("Select * from musician where userid = ?",userID).get("id")).set("songpath", a)
						.set("songname", songName).set("cover", b).set("status", 1)
						.set("lyric", ltemp).set("views", 0).set("addtime", new Timestamp(System.currentTimeMillis()));
						newSong.save();
						new Photo().set("userid", userID).set("photopath", b).set("addtime", new Timestamp(System.currentTimeMillis())).save();
						result.put("success", true);
						result.put("msg", "Seems So GOOD");
						result.put("file_path", a+'\n'+b);
						renderJson(result);
						this.pushIntoRedis(newSong.getInt("id"),"Create");
					}
					break;
				}
			}
			case "DELETE":{
				String a = getPara("id");
				Integer userID = getID();
				if (userID==0 || !userID.equals(Song.dao.findById(a).getInt("musicianid"))){
					result.needAuth();
					renderJson(result);
					return;
				}
				if (a!=null){
					Song temp = Song.dao.findById(a);
					if (temp!=null){
						temp.set("status", 3);
						temp.update();
						result.put("Code", 104);
						result.put("Result","Object has been DISABLED");
						this.pushIntoRedis(Integer.valueOf(a),"DELETE");
					}else {
						result.nullObject();
					}//end temp == null
				}
				else {
					result.put("Code", 105);
					result.put("Msg", "Lack Of ID");
				}//end a==null
				break;
			}
			default: {
				result.illegalMethod();
			}
		}
		renderJson(result);
	}

	//@ActionKey("/api/song/comment")
	public void comment(){
		Result result = new Result();
		switch(this.getRequest().getMethod()){
			case "GET":{
				String ma = getPara("songid");
				if (ma == null){
					result.put("Code", 105);
					result.put("Msg", "Lack Of songID");
					renderJson(result);
					return ;
				}
				else {
					String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
					String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
					Page<SongComment> List = SongComment.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from songcomment where enable = 1 and songid = ? ORDER BY addtime DESC",ma);
					result.put("Code", 200);
					result.put("Result", List);
				}
				break;
			}
			case "POST":{
				String a = getPara("id");
				if (a == null){
					String songid = getPara("songid");
					String content = getPara("content");
					int userid = getID();
					if (userid == 0){
						result.needAuth();
						renderJson(result);
						return ;
					}
					if (songid!=null && content!=null){
							Song son= Song.dao.findById(songid);
							if ( son == null){
								result.nullObject();
							}
							else if(son.getInt("status")== null || son.getInt("status")!=2){
								result.nullObject();
							}
							else {
								SongComment temp = new SongComment();
								temp.set("songid", songid).set("content", content).set("userid",userid)
								.set("enable", 1).set("addtime", new Timestamp(System.currentTimeMillis())).save();
								result.put("Result", temp);
								}
							}
					else {
						result.lackOfPara();
					}
				}//end post songcomment
				else {
					try {
						SongComment temp =SongComment.dao.findById(a);
						String content = getPara("content");
						if (content!=null)
						temp.set("content", getPara("content")).update();
						else {
							result.lackOfPara();
						}
					}catch(NullPointerException e){
						result.nullObject();
					}
				}//end modify songcomment
				break;
			}
			default :{
				result.illegalMethod();
			}
		}
		//返回结果
		renderJson(result);
	}
	//Ring get 方法保持用户和核心层的一致
	@ActionKey("/ring")
	public void ring(){
		Result result = new Result();
		switch(this.getRequest().getMethod()){
			case "GET":{
				String a = getPara("id");
				String name = getPara("name");
				if (a!=null){
					Ring temp = Ring.dao.findById(a);
					if (temp!=null){
						result.put("Code", 100);
						result.put("Result",temp);
					}else{
						result.nullObject();
					}
				}
				else if (name !=null){
					String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
					String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
					Page<Ring> ringList = Ring.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from ring where ringname LIKE '%"+name+"%' order by id DESC");
					result.put("Code", 200);
					result.put("Result", ringList);
				}
				else {
					String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
					String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
					Page<Ring> ringList = Ring.dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from ring order by id DESC");
					result.put("Code", 200);
					result.put("Result", ringList);
				}
				break;
			}
			default: {
				result.illegalMethod();
			}
		}
		renderJson(result);
	}
	public void like(){
		Result result = new Result();
		switch(this.getRequest().getMethod()){
			case "GET":{
				String id = getPara("songid");
				if (id == null){
					result.lackOfPara();
					renderJson(result);
					return ;
				}
				String pageNum = getPara("pageNum")==null ? "1" : getPara("pageNum");
				String pageSize = getPara("pageSize") == null ? "20" : getPara("pageSize");
				Page<Record> pairlist = Db.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize),"SELECT *","from songlike where songid = ? and enable = 1",id);
				result.put("Code", 200);
				result.put("Result", pairlist);
				String targetid;
				try{
					for (Record a : pairlist.getList()){
						a.remove("songid");
						targetid = a.getInt("userid").toString();
						a.remove("userid");
						a.set("name", User.dao.findById(targetid).getStr("username"));
						a.set("avatar", User.dao.findById(targetid).getStr("avatar"));
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
			default:{result.illegalMethod();}
		}
		renderJson(result);
	}
	
	@Clear
	@ActionKey("/song/test")
	public void test(){
		Song newSong= new Song().set("musicianid",1234).set("songpath", "1234")
		.set("songname", "1234").set("cover", "1234")
		.set("lyric", "124").set("views", 0).set("addtime", new Timestamp(System.currentTimeMillis()));
		newSong.save();
		renderJson(newSong);
	}
	
	@Override
	public void pushIntoRedis(Integer eventID,String action) {
		// TODO Auto-generated method stub
		List<Record> targets = Db.find("SELECT * from relation_follow where userid = ? and enable = 1",this.getID().toString());
		for (Record target : targets){
			JedisUtils.pushInto("Song",action,target.getInt("userid2"), eventID);
		}
	}
}