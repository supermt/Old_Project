package imusic.server.models;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

public class News extends Model<News> implements Counter{
	public static final News dao = new News();

//	public Page<News> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
//		Page<News> temp = super.paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
//		for (News a : temp.getList()){
//			a.set("content", removeImgs(a));
//		}
//		return temp;
//	}
	
	public String removeImgs(News a){
		return a.getStr("content").replaceAll("<img[^>]*>", "【图片】");
//		if (result==null) return getStr("content");
//		else return result[0];
	}
	
	public Page<News> mostPopularNews(Integer pageNumber,Integer pageSize){
		return this.paginate(pageNumber, pageSize, "select *", "from news where type = 1 order by id DESC");
	}
	
	public Page<News> mostPopularActivity(Integer pageNumber,Integer pageSize){
		return this.paginate(pageNumber, pageSize, "select *", "from news where type = 2 order by id DESC");
	}

	public static Page<News> searchNews(String pageNum, String pageSize, String newsContent) {
		return dao.paginate(Integer.valueOf(pageNum), Integer.valueOf(pageSize), "select *", "from news where content like ? or title like ? order by id DESC","%"+newsContent+"%","%"+newsContent+"%");
	}
}
