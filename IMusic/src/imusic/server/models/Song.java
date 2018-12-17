package imusic.server.models;

import java.sql.Connection;
import java.util.List;
import java.util.Random;

import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class Song extends Model<Song> implements Counter{
	public static final Song dao = new Song();
	
	public List<Song> find(String sql, Object... paras){
		List<Song> temp = super.find(sql, paras);
		for (Song a : temp){
			a.addMusicianName();
			a.set("islegal", a.getInt("islegal")==1);
		}
		return temp;
	}
	
	public Page<Song> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
		Page<Song> temp = super.paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
		for (Song a : temp.getList()){
			a.addMusicianName();
			a.set("islegal", a.getInt("islegal")==1);
		}
		return temp;
	}
	
	public Song findFirst(String sql, Object... paras){
		Song temp = super.findFirst(sql,paras);
		if (temp == null) {
			return null;
		}
		temp.addMusicianName();
		//temp.set("islegal", temp.getInt("islegal")==1);
		viewIncrease(temp);
		return temp;
	}
	
	public void addMusicianName(){
		super.put("musicianName", Musician.dao.findById(this.getInt("musicianid")).get("nickname"));
	}
	
	public void active(){
		super.set("status", 2).update();
	}
	public void deactive(){
		super.set("status", 3).update();
	}
	
	public Page<Song> mostPopularSong(Integer pageNum,Integer pageSize){
		return this.paginate(pageNum, pageSize, "select *", "from song where status = 2 ORDER BY views DESC");
	}
	
	public Page<Song> guess(Integer pageNum,Integer pageSize,Integer userid){
		return this.paginate(pageNum, pageSize, "select *", "from song where status = 2 ORDER BY RAND()");
	}
	
}
