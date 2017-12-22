package red.vivid.reversi;
import java.awt.Point;
import java.util.*;

/**
	リバーシAI
	実装はランダム
	@author vividorange
*/
public class RandomAI extends AI
{
	public RandomAI(boolean color)
	{
		super(color);
	}
	/**
		ボードから石を置くのに最適と判断したセルを返します
		@param board 石を置きたいボード
		@return Point 最適と判断したセル
	*/
	public Point select(BitBoard board)
	{
		int length = board.getReversibleCount(this.color);
		Point[] cells = board.getReversibleCells(this.color);
		int rnd = (int)(Math.random() * length);
		Point cell = cells[rnd];
		return cell;
	}
}
