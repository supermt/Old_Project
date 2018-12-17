package imusic.server.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import imusic.server.models.User;
import redis.clients.jedis.Jedis;

public class CacheIniter {
	static public Jedis jedis = new Jedis();

	//@Test	
	public static void init(){
		//System.out.println(jedis.ping());
		Map<String, String> allUserMap = new HashMap<String,String>();
		Long value = System.currentTimeMillis();
		List<User> userList = User.dao.find("Select * from user");
		//List<String> userIDs = new ArrayList<String>();
		for (User temp : userList){
			allUserMap.put(temp.getInt("id").toString(), value.toString());
		}
		
		//System.out.println(allUserMap);
		
		jedis.hmset("LastSearch", allUserMap);
		//jedis.hset("LastSearch","1","1");
		
	}
	
	public static void shutdown(){
		jedis.del("LastSearch");
	}
	
}
