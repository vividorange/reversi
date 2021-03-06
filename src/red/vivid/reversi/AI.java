package red.vivid.reversi;
import java.awt.Point;
import java.util.*;

/**
	リバーシAI
	実装はネガマックスアルファベータ法
	@author vividorange
*/
public class AI
{
	/** AIが担当する石の色 */
	public final boolean color;
	
	/**
		引数で指定した色のAIを生成します
		@param color AIの色
	*/
	public AI(boolean color)
	{
		this.color = color;
	}
	/**
		ボードから石を置くのに最適と判断したセルを返します
		@param board 石を置きたいボード
		@return Point 最適と判断したセル
	*/
	public Point select(BitBoard board)
	{
		return board.getReversibleCells(this.color)[0];
	}
}
