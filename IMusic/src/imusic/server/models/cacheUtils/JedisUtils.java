package imusic.server.models.cacheUtils;

import imusic.server.config.CacheIniter;
import redis.clients.jedis.Jedis;

public class JedisUtils {
	private static Jedis jedis = CacheIniter.jedis;
	
	/**
	 * 我就是想试试javadoc
	 * 
	 * @param type 事件类型
	 * @param target 事件的推送目标
	 * @param event 事件推送描述，可能会有id和事件吧 
	 */
	public static void pushInto(String type,String action,Integer targetID,Integer eventId){
		jedis.lpush("user"+targetID,type+";"+action+";"+eventId);
		//System.out.println(targetID);
		//jedis.ltrim("user"+targetID, 0, 9);
	}
	
	public static String popOut(String userid){
		return jedis.lpop("user"+userid);
	}
}
