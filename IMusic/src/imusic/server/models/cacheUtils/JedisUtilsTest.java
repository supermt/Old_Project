package imusic.server.models.cacheUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import redis.clients.jedis.Jedis;

public class JedisUtilsTest {
	
	static Jedis jedis = new Jedis("localhost");

	public void ping(){
		//String result = "ping";
		
		System.out.println(jedis.ping());
//		System.out.println(generateKey("Topic", "1", "10"));
//		jedis.set(generateKey("Topic", "1", "10"), String.valueOf(System.currentTimeMillis()));
//		System.out.println(jedis.get(generateKey("Topic", "1", "10")));
		jedis.zadd("1", 100, "队列测试");
		jedis.zadd("1", 12, "队列测试2");
		System.out.println(jedis.zcard("1"));
		jedis.zremrangeByScore("1", "13", "100");
		Set<String> resultSet = jedis.zrevrangeByScore("1", "+inf", "-inf");
		for (String result : resultSet){
			System.out.println(result);
		}
		return ;
	}
	
	@Test
	public void pushTest(){
		List<String> testStringArray = new ArrayList<String>();
		testStringArray.add("1");
		testStringArray.add("2");
		for (String a : testStringArray)
		jedis.lpush("list1", a);
		String temp = "";
		while (temp!=null){
			temp = jedis.lpop("list1");
			System.out.println(temp);
		}
		//System.out.println(temp==null);
	}
	
	public static String generateKey(String EventType,String EventAuthor,String EventID){
		String key=EventType+";"+EventAuthor+";"+EventID;
		return key;
	}
	
	public static Long pushInto(String userID,String type,String eventID){
		return jedis.zadd(userID,System.currentTimeMillis(), type+";"+eventID);
	}
	
	
}
