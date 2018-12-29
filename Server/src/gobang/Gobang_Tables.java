/**
 * @author 张桓皓
 * @Time 2018-10-17
 *
 */
package gobang;

/**
 * @author Zhh
 */
import server.Write_Log;
import java.util.Hashtable;
import java.util.Vector;

public class Gobang_Tables {
	class Table {
		/** 从0开始 */
		int id;
		/** 位置信息，未进行除法运算，第一个是id，第二个是位置 */
		Hashtable<Integer, Integer> location;
		/** 线程信息 */
		Vector<Object> connections;
		/** 房间线程 */
		Game_Server gameserver;

		public Table(int id) {
			this.id = id;
			location = new Hashtable<Integer, Integer>(2);
			connections = new Vector<Object>(2);
			gameserver = new Game_Server(this);
			gameserver.setName("Gobang Table " + id);
		}

		void reinitial() {
			gameserver = new Game_Server(this);
			gameserver.setName("Gobang Table " + id);
			connections.clear();
		}
	}

	/** 桌子的数量 */
	private static final int TABLENUM = 99;

	/** 桌子 */
	private Table[] tables;
	/**
	 * 在线人数的状态
	 * 
	 * @param Integer
	 *            位置信息 String 个人信息
	 */
	private static volatile Hashtable<Integer, String> status;

	/**
	 * @param 初始化99张桌子
	 */
	public Gobang_Tables() {
		tables = new Table[TABLENUM];
		for (int i = 0; i < TABLENUM; i++) {
			tables[i] = new Table(i);
		}
		status = new Hashtable<Integer, String>(128);
	}

	public Hashtable<Integer, String> getStatus() {
		return status;
	}

	/**
	 * 加入某张桌子
	 * 
	 * @param position
	 *            桌子的编号
	 * @param acc
	 *            账号
	 */
	public boolean joinTable(int position, int id, String msg) {
		int tableid = position >> 2;
		try {
			Table table = tables[tableid];
			if (table.location.size() < 2 && !table.location.containsKey(id)) {
				table.location.put(id, position);
				status.put(position, msg);
				return true;
			}
		} catch (Exception e) {
			Write_Log.writeActivity("Gobang_Tables", "id: " + id + " join Table " + tableid + " failed!");
		}
		return false;
	}

	/**
	 * 离开桌子
	 */
	public boolean leaveTable(int position, int id) {
		int tableid = position >> 2;
		try {
			Table table = tables[tableid];
			table.location.remove(id);
			status.remove(position);
			if (!table.gameserver.Over())
				table.gameserver.info.remove(id);
			return true;
		} catch (Exception e) {
			Write_Log.writeActivity("Gobang_Tables", "id: " + id + " leave Table " + tableid + " failed!");
		}
		return false;
	}

	/**
	 * 准备
	 * 
	 * @return fasle 未全部准备 true 已全部准备并开始游戏！
	 */
	public boolean ready(int position, int id, String nickname, String head, Object output, Object input, String msg) {
		int tableid = position >> 2;
		try {
			Table table = tables[tableid];
			// 重新初始化新桌子
			if (table.gameserver.Over()) {
				table.reinitial();
				Write_Log.writeActivity("Gobang_Tables", "Table " + tableid + " reinitial.");
			}
			if (!table.gameserver.info.contains(id)) {
				table.gameserver.info.add(id, position, nickname, head, output, input);
				status.replace(position, msg);
				// 2人准备就启动房间线程
				if (table.gameserver.info.getnum() == 2) {
					table.gameserver.start();
				}
				return true;
			}
		} catch (Exception e) {
			Write_Log.writeActivity("Gobang_Tables", "id: " + id + " ready Table " + tableid + " failed!");
		}
		return false;
	}

	/**
	 * 开始游戏
	 */
	public boolean start(int position, Object con) {
		try {
			int tableid = position >> 2;
			Table table = tables[tableid];
			table.connections.add(con);
			table.gameserver.num++;
			return true;
		} catch (Exception e) {
		}
		return false;
	}
}
