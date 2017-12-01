package reversi;
import java.awt.Point;
import java.util.*;

public class BitBoard
{
	public long black;
	public long white;
	public BitBoard(long black,long white)
	{
		this.black = black;
		this.white = white;
	}
	
	public BitBoard()
	{
		this(0x0000000810000000L,0x0000001008000000L);
	}
	
	public BitBoard copy(){
		return new BitBoard(black,white);
	}
	public boolean equals(BitBoard t){
		return black == t.black && white == t.white;
	}
	public long getBits(boolean color)
	{
		return color == Rule.BLACK?
			black:
			white;
	}
	public long getTurnCount()
	{
		return Long.bitCount(black) + Long.bitCount(white) - 4;
	}
	public long getEmptyCount()
	{
		return 64-getTurnCount();
	}
	public boolean existStone(boolean color, int x, int y)
	{
		return (((color == Rule.BLACK?black:white) >>> ((y << 3) + x)) & 1) == 1;
	}
	public Set<Point> makeReversibleCells(boolean color)
	{
		Set<Point> cells = new HashSet<>();
		long reversiblePos = color == Rule.BLACK?
			makeReversiblePos(black, white):
			makeReversiblePos(white, black);
		
		for(int i=0;i<64;i++)
		{
			if((reversiblePos>>i&1) == 1)
			{
				cells.add(new Point(i&7,i>>3));
			}
		}
		return cells;
	}
	public long getReversibleCount(boolean color)
	{
		long reversiblePos = color == Rule.BLACK?
			makeReversiblePos(black, white):
			makeReversiblePos(white, black);
		return Long.bitCount(reversiblePos);
		
	}
	public void setStone(boolean color, int x, int y)
	{
		long m = (1L << ((y << 3) + x));
		if(color == Rule.BLACK)black |= m;
		else white |= m;
	}
	public void clearStone(boolean color, int x, int y)
	{
		long m = (0xffffffffffffffffL ^ (1L << ((y << 3) + x)));
		if(color == Rule.BLACK)black &= m;
		else white &= m;
	}
	public boolean putStone(boolean color, Point cell)
	{
		return putStone(color, cell.x, cell.y);
	}
	public boolean putStone(boolean color, int x, int y)
	{
		long pos = 1L << ((y << 3) + x);
		long rev = color == Rule.BLACK?
				makeReverseBit(black, white, pos):
				makeReverseBit(white, black, pos);
		
		if(rev == 0)return false;
		
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
		return true;
	}
	public boolean isFinished()
	{
		return (makeReversiblePos(black, white) | makeReversiblePos(white, black))==0;
	}
	public boolean empty(Point cell)
	{
		return empty(cell.x, cell.y);
	}
	public boolean empty(int x, int y)
	{
		return !(existStone(Rule.BLACK,x,y)||existStone(Rule.WHITE,x,y));
	}
	public long makeReverseBit(long my, long opp, long pos)
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
	public long makeReversiblePos(long my, long opp)
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