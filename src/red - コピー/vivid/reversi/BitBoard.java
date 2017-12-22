package red.vivid.reversi;
import java.awt.Point;
import java.util.*;

/**
	ビットボード実装
*/
public class BitBoard
{
	/** 黒い石を64ビットで表現 */
	public long black;
	/** 白い石を64ビットで表現 */
	public long white;
	/** 座標セル対応表 */
	public Point[] cellTable = new Point[64];
	/**
		BitBoardを生成し、座標セル対応表を初期化します
		@param black 黒い石
		@param white 白い石
	*/
	public BitBoard(long black,long white)
	{
		this.black = black;
		this.white = white;
		
		for(int i=0;i<64;++i)
		{
			int y = i >> 3;
			int x = i & 7;
			cellTable[i] = new Point(x,y);
		}
	}
	/**
		初期配置でBitBoardを生成します
	*/
	public BitBoard()
	{
		this(0x0000000810000000L,0x0000001008000000L);
	}
	/**
		このインスタンスを新しいインスタンスにコピーします
		@return BitBoard このインスタンスをコピーした新しいBitBoard
	*/
	public BitBoard copy(){
		return new BitBoard(black,white);
	}
	/**
		引数のBitBoardと等しいかを返します
		@param t 評価するBitBoard
		@return boolean 等しいかどうか
	*/
	public boolean equals(BitBoard t){
		return black == t.black && white == t.white;
	}
	/**
		引数の色に対応する石の数を返します
		@param color 評価する色
		@return 石の数
	*/
	public int getStoneCount(boolean color)
	{
		return Long.bitCount(color==Rule.BLACK?black:white);
	}
	/**
		初期配置を除く石を置いた数を返します
		@return int 石を置いた数
	*/
	public int getTurnCount()
	{
		return getStoneCount(Rule.BLACK) + getStoneCount(Rule.WHITE) - 4;
	}
	/**
		空いているセルの数を返します
		@return int 空いているセルの数
	*/
	public int getEmptyCount()
	{
		return 64-getTurnCount();
	}
	/**
		その位置に石があるかを返します
		@param color 評価する色
		@param x x座標
		@param y y座標
		@return boolean 石が存在するか
	*/
	public boolean existStone(boolean color, int x, int y)
	{
		return (((color == Rule.BLACK?black:white) >>> ((y << 3) + x)) & 1) == 1;
	}
	/**
		その位置に石があるかを返します
		@param color 評価する色
		@param k kビット目
		@return boolean 石が存在するか
	*/
	public boolean existStone(boolean color, int k)
	{
		return (((color == Rule.BLACK?black:white) >>> k) == 1);
	}
	/**
		相手の石を返せるセルのリストを返します
		@param color 評価する色
		@return Point[] 返せる石のリスト(Point配列)
	*/
	public Point[] getReversibleCells(boolean color)
	{
		int z = getReversibleCount(color);
		Point[] cells = new Point[z];
		long reversiblePos = color == Rule.BLACK?
			getReversiblePos(black, white):
			getReversiblePos(white, black);
		
		for(int i=0;i<64;++i)
		{
			if((reversiblePos>>i&1) == 1)
			{
				cells[--z] = cellTable[i];
			}
		}
		return cells;
	}
	/**
		相手の石を返せるセルの数を返します
		@param color 評価する色
		@return int 返せる石の数
	*/
	public int getReversibleCount(boolean color)
	{
		long reversiblePos = color == Rule.BLACK?
			getReversiblePos(black, white):
			getReversiblePos(white, black);
		return Long.bitCount(reversiblePos);
		
	}
	/**
		指定した場所に石を置きます
		@param color 評価する色
		@param x x座標
		@param y y座標
	*/
	public void setStone(boolean color, int x, int y)
	{
		long m = (1L << ((y << 3) + x));
		if(color == Rule.BLACK)black |= m;
		else white |= m;
	}
	/**
		指定した場所の石を消します
		@param color 評価する色
		@param x x座標
		@param y y座標
	*/
	public void clearStone(boolean color, int x, int y)
	{
		long m = (0xffffffffffffffffL ^ (1L << ((y << 3) + x)));
		if(color == Rule.BLACK)black &= m;
		else white &= m;
	}
	/**
		Pointで指定したセルに石を置きます
		@param color 評価する色
		@param cell セル
		@return boolean 石を置けたか
	*/
	public boolean putStone(boolean color, Point cell)
	{
		return putStone(color, cell.x, cell.y);
	}
	/**
		座標で指定したセルに石を置きます
		@param color 評価する色
		@param x x座標
		@param y y座標
		@return boolean 石を置けたか
	*/
	public boolean putStone(boolean color, int x, int y)
	{
		if(existStone(color,x,y))return false;
		long pos = toPos(x,y);
		long rev = toRev(color,pos);
		return putStone(color, pos, rev);
	}
	/**
		64ビット論理演算で指定した位置に石を置きます
		@param color 評価する色
		@param pos 石を置く場所
		@param rev 裏返す石
		@return boolean 石を置けたか
	*/
	public boolean putStone(boolean color, long pos, long rev)
	{
		if(rev == 0)return false;
		reverse(color, pos, rev);
		return true;
	}
	/**
		位置から裏返る石に変換します
		@param color 評価する色
		@param pos 石を置く場所
		@return long 裏返る石
	*/
	public long toRev(boolean color, long pos)
	{
		return color == Rule.BLACK?
				getReverseBit(black, white, pos):
				getReverseBit(white, black, pos);
	}
	/**
		Pointを位置に変換します
		@param cell 石を置くセル
		@return long 64ビット情報に変換したもの
	*/
	public long toPos(Point cell)
	{
		return toPos(cell.x, cell.y);
	}
	/**
		座標を位置に変換します
		@param x x座標
		@param y y座標
		@return long 64ビット情報に変換したもの
	*/
	public long toPos(int x, int y)
	{
		return 1L << ((y << 3) + x);
	}
	/**
		石の色と石を置く場所posと返る石revを渡して石を裏返します
		@param color 評価する色
		@param pos 石を置く場所
		@param rev 返る石
	*/
	public void reverse(boolean color, long pos, long rev)
	{
		if(color == Rule.BLACK)
		{
			black ^= pos|rev;
			white ^= rev;
		}
		else
		{
			white ^= pos|rev;
			black ^= rev;
		}
	}
	/**
		ゲームが終了しているかを返します
		@return boolean ゲームが終了しているか
	*/
	public boolean isFinished()
	{
		return (getReversiblePos(black, white) | getReversiblePos(white, black))==0;
	}
	/**
		セルが空かを返します
		@param cell セル
		@return 空か
	*/
	public boolean empty(Point cell)
	{
		return empty(cell.x, cell.y);
	}
	/**
		座標が空かを返します
		@param x x座標
		@param y y座標
		@return boolean 空か
	*/
	public boolean empty(int x, int y)
	{
		return !(existStone(Rule.BLACK,x,y)||existStone(Rule.WHITE,x,y));
	}
	/**
		裏返す色の情報と返される色の情報と石を置く場所を渡して返せる石のビットを返します
		@param my 裏返す色
		@param opp 裏返される色
		@param pos 石を置く場所
		@return long 裏返せる石 rev
	*/
	public long getReverseBit(long my, long opp, long pos)
	{
		long rev = 0, tmp, mask;
		
        // 右
        tmp=0;
        mask = (pos >>> 1) & 0x7f7f7f7f7f7f7f7fL;
        while(mask!=0 && (mask & opp)!=0)
		{
            tmp |= mask;
            mask = (mask >>> 1) & 0x7f7f7f7f7f7f7f7fL;
        }
        if((mask & my)!=0) rev |= tmp;

        // 左
        tmp=0;
        mask = (pos << 1) & 0xfefefefefefefefeL;
        while(mask!=0 && (mask & opp)!=0)
		{
            tmp |= mask;
            mask = (mask << 1) & 0xfefefefefefefefeL;
        }
        if((mask & my)!=0) rev |= tmp;

        // 上
        tmp=0;
        mask = (pos << 8);
        while(mask!=0 && (mask & opp)!=0)
		{
            tmp |= mask;
            mask = (mask << 8);
        }
        if((mask & my)!=0) rev |= tmp;

        // 下
        tmp=0;
        mask = (pos >>> 8);
        while(mask!=0 && (mask & opp)!=0)
		{
            tmp |= mask;
            mask = (mask >>> 8);
        }
        if((mask & my)!=0) rev |= tmp;

        // 右上
        tmp=0;
        mask = (pos << 7) & 0x7f7f7f7f7f7f7f7fL;
        while(mask!=0 && (mask & opp)!=0)
		{
            tmp |= mask;
            mask = (mask << 7) & 0x7f7f7f7f7f7f7f7fL;
        }
        if((mask & my)!=0) rev |= tmp;

        // 左上
        tmp=0;
        mask = (pos << 9) & 0xfefefefefefefefeL;
        while(mask!=0 && (mask & opp)!=0)
		{
            tmp |= mask;
            mask = (mask << 9) & 0xfefefefefefefefeL;
        }
        if((mask & my)!=0) rev |= tmp;

        // 右下
        tmp=0;
        mask = (pos >>> 9) & 0x7f7f7f7f7f7f7f7fL;
        while(mask!=0 && (mask & opp)!=0)
		{
            tmp |= mask;
            mask = (mask >>> 9) & 0x7f7f7f7f7f7f7f7fL;
        }
        if((mask & my)!=0) rev |= tmp;

        // 左下
        tmp=0;
        mask = (pos >>> 7) & 0xfefefefefefefefeL;
        while(mask!=0 && (mask & opp)!=0)
		{
            tmp |= mask;
            mask = (mask >>> 7) & 0xfefefefefefefefeL;
        }
        if((mask & my)!=0) rev |= tmp;


        return rev;
	}
	/**
		裏返す石の情報と裏返される石の情報を渡して相手の石を裏返せるセルの位置のビットを返します
		@param my 裏返す色
		@param opp 裏返される色
		@return long 裏返る場所 pos
	*/
	public long getReversiblePos(long my, long opp)
	{
        long blank = ~(my | opp);
        long mobility = 0, t, w;

        // 右
        w = opp & 0x7e7e7e7e7e7e7e7eL;
        t = w & (my << 1);
        t |= w & (t << 1); t |= w & (t << 1); t |= w & (t << 1);
        t |= w & (t << 1); t |= w & (t << 1);
        mobility |= blank & (t << 1);

        // 左
        w = opp & 0x7e7e7e7e7e7e7e7eL;
        t = w & (my >>> 1);
        t |= w & (t >>> 1); t |= w & (t >>> 1); t |= w & (t >>> 1);
        t |= w & (t >>> 1); t |= w & (t >>> 1);
        mobility |= blank & (t >>> 1);

        // 上
        w = opp & 0x00ffffffffffff00L;
        t = w & (my << 8);
        t |= w & (t << 8); t |= w & (t << 8); t |= w & (t << 8);
        t |= w & (t << 8); t |= w & (t << 8);
        mobility |= blank & (t << 8);

        // 下
        w = opp & 0x00ffffffffffff00L;
        t = w & (my >>> 8);
        t |= w & (t >>> 8); t |= w & (t >>> 8); t |= w & (t >>> 8);
        t |= w & (t >>> 8); t |= w & (t >>> 8);
        mobility |= blank & (t >>> 8);

        // 右上
        w = opp & 0x007e7e7e7e7e7e00L;
        t = w & (my << 7);
        t |= w & (t << 7); t |= w & (t << 7); t |= w & (t << 7);
        t |= w & (t << 7); t |= w & (t << 7);
        mobility |= blank & (t << 7);

        // 左上
        w = opp & 0x007e7e7e7e7e7e00L;
        t = w & (my << 9);
        t |= w & (t << 9); t |= w & (t << 9); t |= w & (t << 9);
        t |= w & (t << 9); t |= w & (t << 9);
        mobility |= blank & (t << 9);

        // 右下
        w = opp & 0x007e7e7e7e7e7e00L;
        t = w & (my >>> 9);
        t |= w & (t >>> 9); t |= w & (t >>> 9); t |= w & (t >>> 9);
        t |= w & (t >>> 9); t |= w & (t >>> 9);
        mobility |= blank & (t >>> 9);

        // 左下
        w = opp & 0x007e7e7e7e7e7e00L;
        t = w & (my >>> 7);
        t |= w & (t >>> 7); t |= w & (t >>> 7); t |= w & (t >>> 7);
        t |= w & (t >>> 7); t |= w & (t >>> 7);
        mobility |= blank & (t >>> 7);

        return mobility;
    }
}