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
	private int NORMAL_DEPTH = 9;
	private int COMPLETE_DEPTH = 22;
	private int evalMode = 0;
	private Random rand;
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
		rand = new Random();
	}
	/**
		ボードから石を置くのに最適と判断したセルを返す
		@param board 石を置きたいボード
		@return Point 最適と判断したセル
	*/
	public Point select(BitBoard board)
	{
		startTime = System.currentTimeMillis();
		leaf = 0;
		
		int turnCount = board.getTurnCount();
		List<Point> cells = board.getReversibleCells(color);
		Point resultCell = cells.get(0);
		
		if(turnCount<60)
		{
			int alpha = -Integer.MAX_VALUE;
			int beta = Integer.MAX_VALUE;
			int maxValue = -Integer.MAX_VALUE;
			int depth = NORMAL_DEPTH;
			if(board.getEmptyCount() <= COMPLETE_DEPTH)
			{
				evalMode = 1;
				depth = COMPLETE_DEPTH;
			}
			int nodeCount = board.getReversibleCount(color);
			
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
		}else{
			System.out.println("ランダムさん");
			resultCell = cells.get(rand.nextInt(cells.size()));
		}
		System.out.println(""+leaf+" leaves");
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
		if(alpha <= beta == false)
		{
			System.out.println("アルファベータがおかしい");
			System.exit(1);
		}
		// 終わりか？
		boolean finished = board.isFinished();
		// 評価深度が最深になったか終わったか
		if(System.currentTimeMillis() - startTime > 1000*290 || depth == 0 || finished)
		{
			leaf++;
			// 負けで終わったか
			if(finished && board.getStoneCount(this.color) - board.getStoneCount(!this.color) <= 0)
			{
				// 負ける可能性は排除する
				return color == this.color?
					-Integer.MAX_VALUE:
					Integer.MAX_VALUE;
			}
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
		List<Point> cells = board.getReversibleCells(color);
		
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
    private int[][] table = {
		{
			/* 序盤はあまりとりすぎないように内側に負が多くあり
			角を取られたくないので角は大きめで
			角から１つ離れたところは相手にとってほしいから小さめの負*/
			100,-12,0,-1,-1,0,-12,100,
			-12,-15,-3,-3,-3,-3,-15,-12,
			0,-3,0,-1,-1,0,-3,0,
			-1,-3,-1,-1,-1,-1,-3,-1,
			-1,-3,-1,-1,-1,-1,-3,-1,
			0,-3,0,-1,-1,0,-3,0,
			-12,-15,-3,-3,-3,-3,-15,-12,
			100,-12,0,-1,-1,0,-12,100
		},/*中盤は作るべきかな*/{
			/* 終盤は内側に相手の石があったら外が取られるよね
			周りに相手の石があったら一気に8個取られちゃうかもね
			ということで回りと内側を大きめにしてる
			できれば全部取りたいので正の値のみ */
			30,12,10,5,5,10,12,30,
			12,8,4,2,2,4,8,12,
			10,4,1,1,1,1,4,10,
			5,2,1,10,10,1,2,5,
			5,2,1,10,10,1,2,5,
			10,4,1,1,1,1,4,10,
			12,8,4,2,2,4,8,12,
			30,12,10,5,5,10,12,30
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
        for(int k = 0; k < 64; k++) {
            int y = k >> 3;
            int x = k & 7;
            if(board.existStone(color, x, y))
			{
                evaluation += table[evalMode][k];
            }
			if(board.existStone(!color, x, y))
            {
				evaluation -= table[evalMode][k];
			}
        }

        return evaluation;
    }

}
