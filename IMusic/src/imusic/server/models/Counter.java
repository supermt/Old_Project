package imusic.server.models;

import com.jfinal.plugin.activerecord.Model;

public interface Counter<M extends Model> {
	public default M viewIncrease(M temp){
		temp.set("views", temp.getInt("views")+1).update();
		return temp;
	}
}
