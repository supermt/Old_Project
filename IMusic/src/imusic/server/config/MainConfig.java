package imusic.server.config;

import java.io.File;

//import org.apache.catalina.User;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.handler.ContextPathHandler;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.render.ViewType;

import imusic.server.Controller.AdminController;
import imusic.server.Controller.AuthController;
import imusic.server.Controller.FileController;
import imusic.server.Controller.HomeController;
import imusic.server.Controller.NoticeController;
import imusic.server.Controller.SocialController;
import imusic.server.Controller.SongController;
import imusic.server.models.Admin;
import imusic.server.models.Musician;
import imusic.server.models.News;
import imusic.server.models.Photo;
import imusic.server.models.Ring;
import imusic.server.models.Song;
import imusic.server.models.SongComment;
import imusic.server.models.Speak;
import imusic.server.models.SpeakReply;
import imusic.server.models.Topic;
import imusic.server.models.TopicReply;
import imusic.server.models.User;

public class MainConfig extends JFinalConfig {
	
	public static String GIT_CLIENT_ID = "";
	
	public static String GIT_CLIENT_SECRET = "";
	
	public static String DOMAIN = "";
	
	public static String OAuthRedirect ="/";
	
	@Override
	public void configConstant(Constants me) {
		// TODO Auto-generated method stub
		me.setDevMode(true);
		me.setEncoding("UTF-8");
		me.setViewType(ViewType.JSP);
		me.setMaxPostSize(1000*1000*1000);
	}
	
	@Override
	public void configRoute(Routes me) {
		// TODO Auto-generated method stub
		//me.add("/files", FrontController.class,"/static");
		me.add("/auth",AuthController.class);
		me.add("/system",AdminController.class);
		me.add("/song",SongController.class);
		me.add("/topic",SocialController.class);
		me.add("/",HomeController.class);
		me.add("/file",FileController.class);
		me.add("/notice",NoticeController.class);
	}
	@Override
	public void configPlugin(Plugins me) {
		// TODO Auto-generated method stub
//		RedisPlugin testRedis = new RedisPlugin("test", "127.0.0.1",6379,1800);
//		me.add(testRedis);
		this.loadPropertyFile(new File(PathKit.getWebRootPath()+"/db.txt"));
		System.out.println(PathKit.getWebRootPath());
		System.out.println("user"+this.getProperty("user"));
		
		MainConfig.GIT_CLIENT_ID = this.getProperty("client_id");
		MainConfig.GIT_CLIENT_SECRET = this.getProperty("client_secret");
		MainConfig.DOMAIN = this.getProperty("domain");
		MainConfig.OAuthRedirect = this.getProperty("redirectURL");
		
		C3p0Plugin cp;
		if (this.getProperty("mysql_host")!=null){
			cp = new C3p0Plugin("jdbc:mysql://" + this.getProperty("mysql_host") +this.getProperty("dbname")+"?characterEncoding=utf-8",this.getProperty("user"),this.getProperty("password"));
			
		}else{
			cp = new C3p0Plugin("jdbc:mysql://localhost/"+this.getProperty("dbname")+"?characterEncoding=utf-8",this.getProperty("user"),this.getProperty("password"));
		}
		me.add(cp);
		ActiveRecordPlugin arp = new ActiveRecordPlugin(cp);
		me.add(arp);
		arp.addMapping("user","id", User.class);
		arp.addMapping("topic", Topic.class);
		arp.addMapping("topicreply", TopicReply.class);
		arp.addMapping("admin", Admin.class);
		arp.addMapping("musician", Musician.class);
		arp.addMapping("song",Song.class);
		arp.addMapping("songcomment", SongComment.class);
		arp.addMapping("speak",Speak.class);
		arp.addMapping("speakreply", SpeakReply.class);
		arp.addMapping("ring", Ring.class);
		arp.addMapping("news", News.class);
		arp.addMapping("log", imusic.server.models.Log.class);
		arp.addMapping("photo", Photo.class);
	}
	@Override
	public void configInterceptor(Interceptors me) {
		// TODO Auto-generated method stub
		//me.add(new TxByActionKeyRegex("/trans.*"));
		//me.add(new PostMethod());
	}

	@Override
	public void configHandler(Handlers me) {
		// TODO Auto-generated method stub
		me.add(new ContextPathHandler("basePath"));
	}
	
	@Override
	public void afterJFinalStart(){
		CacheIniter.init();
	};
	
	@Override
	public void beforeJFinalStop(){
		CacheIniter.shutdown();
	};

}
