package imusic.server.models;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

public class Ring extends Model<Ring> {
	public static final Ring dao = new Ring();

	public Page<Ring> best(Integer pageNum,Integer pageSize){
		return this.paginate(pageNum, pageSize, "select *", "from ring ORDER BY RAND()");
	}
}
