package reversi;
import java.awt.Point;
import java.util.*;

/**
	リバーシAI
	実装はネガマックスアルファベータ法
	@author vividorange
*/
public class AI
{
	private int TIME_LIMIT = 295;
	private int NORMAL_DEPTH = 10;
	private int ORDERING_DEPTH = 5;
	private int MIDDLE_DEPTH = 40;
	private int COMPLETE_DEPTH = 23;
	private int evalMode = 0;
	private long startTime;
	private int leaf;
	/** AIが担当する石の色 */
	public final boolean color;
	
	/**
		引数で指定した色のAIを生成する
		@param color AIの色
	*/
	public AI(boolean color)
	{
		this.color = color;
	}
	/**
		turn1の時の定石
		@param board ボード
		@return ねずみは嫌です
	*/
	public Point j1(BitBoard board)
	{
		if(board.existStone(!color,2,3))
		{
			return new Point(4,2);
		}
		if(board.existStone(!color,3,2))
		{
			return new Point(2,4);
		}
		if(board.existStone(!color,5,4))
		{
			return new Point(3,5);
		}
		return new Point(5,3);
	}
	/**
		ボードから石を置くのに最適と判断したセルを返す
		@param board 石を置きたいボード
		@return Point 最適と判断したセル
	*/
	public Point select(BitBoard board)
	{
		// 探索開始時刻
		startTime = System.currentTimeMillis();
		// 探索した葉の数
		leaf = 0;
		
		int alpha = -Integer.MAX_VALUE;
		int beta = Integer.MAX_VALUE;
		int maxValue = -Integer.MAX_VALUE;
		int depth = NORMAL_DEPTH;
		Point[] cells = board.getReversibleCells(color);
		Point resultCell = cells[0];
		
		if(board.getEmptyCount() <= MIDDLE_DEPTH)
		{
			evalMode = 1;
		}
		if(board.getEmptyCount() <= COMPLETE_DEPTH)
		{
			evalMode = 2;
			depth = COMPLETE_DEPTH;
		}
		int nodeCount = board.getReversibleCount(color);
		
		switch(board.getTurnCount())
		{
			case 0: return cells[1];
			case 1: return j1(board);
			case 2:
		}
		
		
		
		moveOrdering(board, cells);
		
		for(Point cell:cells)
		{
			// 探索セル表示
			char cx = (char)(cell.x+'A');
			char cy = (char)(cell.y+'1');
			System.out.print(String.format("%2d:%c%c:",nodeCount--,cx,cy));
			
			// ひっくり返して評価して元に戻す
			long pos = board.toPos(cell);
			long rev = board.toRev(color,pos);
			board.reverse(color,pos,rev);
			int evalValue = -negaMax(board, !color, alpha, beta, depth);
			board.reverse(color,pos,rev);
			
			// 評価値表示
			System.out.println(evalValue);
			
			// 評価最大値のセルを選ぶ
			if(evalValue > maxValue)
			{
				maxValue = evalValue;
				resultCell = cell;
			}
		}
		System.out.println(String.format("%d leaves",leaf));
		return resultCell;
	}
	/**
		ネガマックス法
		アルファベータ法
		@param board ボード
		@param color 評価する色
		@param alpha アルファ
		@param beta ベータ
		@param depth 評価する深さ
		@return int 評価
	*/
	public int negaMax(BitBoard board, boolean color,int alpha, int beta, int depth)
	{
		// 終わりか？
		boolean finished = board.isFinished();
		// 評価深度が最深になったか終わったか
		if(System.currentTimeMillis() - startTime > 1000*TIME_LIMIT || depth == 0 || finished)
		{
			++leaf;/*
			// 負けで終わったか
			if(finished && board.getStoneCount(this.color) - board.getStoneCount(!this.color) <= 0)
			{
				// 負ける可能性は排除する
				return color == this.color?
					-Integer.MAX_VALUE:
					Integer.MAX_VALUE;
			}*/
			// 評価関数
			return eval(board, color);
		}
		// 置けるへしがないよ！
		if(board.getReversibleCount(color) == 0)
		{
			// パス
			return -negaMax(board, !color, -beta, -alpha, depth);
		}
		// 置けるセルを列挙
		Point[] cells = board.getReversibleCells(color);
		
		// それぞれのセルに対して
		for(Point cell:cells)
		{
			// 呼び出し元と同じなのでそっち見てね
			long pos = board.toPos(cell);
			long rev = board.toRev(color,pos);
			board.reverse(color,pos,rev);
			alpha = Math.max(alpha, -negaMax(board, !color, -beta, -alpha, depth - 1));
			board.reverse(color,pos,rev);
			
			if(alpha >= beta)
			{
				return beta;
			}
		}
		return alpha;
	}
	/**
		リストの並びをいい感じに並べ替えます
		浅い探索をしてその結果を使って着手可能セルリストを降順に並べ替えます
		@param board ボード
		@param cells 着手可能セルリスト
	*/
	public void moveOrdering(BitBoard board, Point[] cells)
	{
		int alpha = -Integer.MAX_VALUE;
		int beta = Integer.MAX_VALUE;
		int depth = ORDERING_DEPTH;
		int[] evalValue = new int[cells.length];
		
		for(int i=0;i<cells.length;++i)
		{
			// ひっくり返して評価して元に戻す
			long pos = board.toPos(cells[i]);
			long rev = board.toRev(color,pos);
			board.reverse(color,pos,rev);
			evalValue[i] = -negaMax(board, !color, alpha, beta, depth);
			board.reverse(color,pos,rev);
		}
		// ソートする
		Point p;
		int t;
		for(int i=1;i<cells.length;++i)
		{
			int j = i;
			while(j>0 && evalValue[j-1]<evalValue[j])
			{
				t = evalValue[j-1];
				p = cells[j-1];
				evalValue[j-1] = evalValue[j];
				cells[j-1] = cells[j];
				evalValue[j] = t;
				cells[j] = p;
				
				--j;
			}
		}
	}
    private int[][] table = {
		{
			/* 序盤 */
			 50,-10,2,-3,-3,2,-10, 50,
			-10,-10,-3,-2,-2,-3,-10,-10,
			 2, -3,-1,-2,-2,-1, -3, 2,
			 -3, -2,-2,-1,-1,-2, -2, -3,
			 -3, -2,-2,-1,-1,-2, -2, -3,
			 2, -3,-1,-2,-2,-1, -3, 2,
			-10,-10,-3,-2,-2,-3,-10,-10,
			 50,-10,2,-3,-3,2,-10, 50
		},
		{
			/* 中盤 */
			 50,-10,2,-3,-3,2,-10, 50,
			-10,-10,-3,-2,-2,-3,-10,-10,
			 2, -3,-1,-1,-1,-1, -3, 2,
			 -3, -2,-1, 6, 6,-1, -2, -3,
			 -3, -2,-1, 6, 6,-1, -2, -3,
			 2, -3,-1,-1,-1,-1, -3, 2,
			-10,-10,-3,-2,-2,-3,-10,-10,
			 50,-10,2,-3,-3,2,-10, 50
		},
		{
			/* 終盤 */
			 50,-10,10,-10,-10,10,-10, 50,
			-10, -8, 4,  2,  2, 4, -8,-10,
			 10,  4, 8,  1,  1, 8,  4, 10,
			-10,  2, 1, 10, 10, 1,  2,-10,
			-10,  2, 1, 10, 10, 1,  2,-10,
			 10,  4, 8,  1,  1, 8,  4, 10,
			-10, -8, 4,  2,  2, 4, -8,-10,
			 50,-10,10,-10,-10,10,-10, 50
		}
	};
	/**
		評価関数
		ボードと色を渡すとその色にとってのボードの評価を返す
		@param board ボード
		@param color 評価する色
		@return int ボードの評価
	*/
    public int eval(BitBoard board, boolean color)
	{
        int evaluation = 0;
        for(int k = 0; k < 64; ++k) {
            if(board.existStone(color, k))
			{
                evaluation += table[evalMode][k];
            }
			if(board.existStone(!color, k))
            {
				evaluation -= table[evalMode][k];
			}
        }

        return evaluation;
    }

}
