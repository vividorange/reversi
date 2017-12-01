import java.awt.Point;
import java.util.*;
import java.io.*;
import reversi.*;

class Reversi
{
	static Scanner scanner;
	static BitBoard board;
	static boolean aiColor;
	public static void main(String[] args)
	{
		scanner = new Scanner(System.in);
		board = new BitBoard();
		aiColor = args.length == 0 || args[0].equals("white")?
			Rule.WHITE:
			Rule.BLACK;
		
		if(aiColor == Rule.BLACK)
		{
			System.out.println("x(先攻)はAIです");
		}
		else
		{
			System.out.println("o(後攻)はAIです");
		}
		try
		{
			game();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	static void game() throws IOException
	{
		Point cell;
		String date = new java.text.SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date());
		File file = new File(System.getProperty("user.dir")+"/棋譜"+date+".log");
		FileOutputStream fos = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		PrintWriter pw = new PrintWriter(osw);
		while(!board.isFinished())
		{
			showBoard();
			System.out.format("\n%cのターンです ",Rule.turn == Rule.BLACK?'x':'o');
			
			// パス判定
			if(board.getReversibleCount(Rule.turn) == 0)
			{
				System.out.println("置ける場所がないためパスしました");
				Rule.turn = !Rule.turn;
				continue;
			}
			
			// 打つ手を決める
			if(Rule.turn == aiColor)
			{
				// AIが打つ手をcellに入れる
				System.out.println("AI未実装");
				cell = input();
			}
			else
			{
				// 人が打つ手をcellに入れる
				cell = input();
			}
			
			// 打てない場所に打とうとしたら打ちなおさせる
			if(!board.putStone(Rule.turn,cell))
			{
				System.out.println("打ちなおしてください");
				continue;
			}
			else
			{
				// 棋譜
				pw.format("%c%c%c\n",Rule.turn==Rule.BLACK?'x':'o',(char)(cell.x+'A'),(char)(cell.y+'1'));
			}
			
			Rule.turn = !Rule.turn;
		}
		pw.close();
		showBoard();
	}
	
	/**
		[A-H][1-8]の形式で入力すると2桁の整数で返す
		AIと入力すると-1を返す
		不正入力には正常入力ができるまでループで対応する
	*/
	static Point input()
	{
		Point p;
		System.out.println("([A-H][1-8])で入力してください");
		while(true)
		{
			try
			{
				String value = scanner.next();
				int x = value.charAt(0)-'A';
				int y = value.charAt(1)-'1';
				if(0<=x&&x<=7&&0<=y&&y<=7)
				{
					p = new Point(x,y);
					break;
				}
				System.out.println("([A-H][1-8])または(AI)で入力してください");
			}
			catch(Exception e)
			{
				System.out.println("([A-H][1-8])または(AI)で入力してください");
			}
		}
		return p;
	}

	/**
		ボードを表示する
	*/
	static void showBoard()
	{
		long mask = 1L;
		long black = board.black;
		long white = board.white;
		
		System.out.println("  A B C D E F G H");
		
		for(int i=0;i<64;i++){
			if(i%8 == 0){
				System.out.print((i/8)+1 +" ");
			}
			
			if((black&mask)==mask){
				System.out.print("x ");
			}else if((white&mask)==mask){
				System.out.print("o ");
			}else{
				System.out.print(": ");
			}
			mask <<= 1;
			
			if(i%8 == 7){
				System.out.println((i/8)+1);
			}
		}
		System.out.println("  A B C D E F G H");
	}
	
}