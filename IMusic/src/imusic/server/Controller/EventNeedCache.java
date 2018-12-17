package imusic.server.Controller;

public interface EventNeedCache {
	/**
	 * 这一段就是为了把刚刚产生的一个操作加入到redis缓存中方便后期调用（毛的后期，就是一个函数），
	 * 这个函数得把关注这个用户的所有用户ID提取出来，然后一次传入utils中的静态方法
	 * @param eventID 当前操作的产物ID
	 * @param action 当前操作类型，创建，修改或删除
	 * 
	 * */
	public void pushIntoRedis(Integer eventID,String action);
}
