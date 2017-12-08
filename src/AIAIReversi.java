import java.awt.Point;
import java.util.*;
import java.io.*;
import reversi.*;

/**
	AI同士
	@author vividorange
*/
public class AIAIReversi
{
	static Scanner scanner;
	static PrintWriter pw;
	static BitBoard board;
	static AI oppAI;
	
	/** 棋譜 */
	static List<String> history = new ArrayList<>();
	
	/**
		起動時に呼ばれます
		@param args 無しなら黒がAI、1以上あるなら白がAI
	*/
	public static void main(String[] args)
	{
		scanner = new Scanner(System.in);
		board = new BitBoard();
		
		// AIの色を選ぶ
		oppAI = new AI(args.length==0);
		
		try
		{
			game();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.print("1+1=?");
			scanner.next();
		}
	}
	/**
		ゲームの本体です
		mainメソッドから呼ばれる必要があります
		@throws Exception いろいろな例外が出る可能性がありますが、main側でキャッチして表示するだけです
	*/
	public static void game() throws Exception
	{
		Point cell;
		boolean gameTurn = Rule.BLACK;
		// ゲーム本体
		while(!board.isFinished())
		{
			System.out.println(String.format("======= %2d ========",board.getTurnCount()+1));
			System.out.print(getBoardString());
			System.out.println(String.format("\n%c のターンです",gameTurn == Rule.BLACK?'x':'o'));
		
			// パス判定
			if(board.getReversibleCount(gameTurn) == 0)
			{
				history.add(String.format("%c pass",gameTurn == Rule.BLACK?'x':'o'));
				System.out.println("置ける場所がないためパスします。");
				gameTurn = !gameTurn;
				continue;
			}
			
			boolean putted1 = false;
			String timeString = "";
			do
			{
				if(putted1)
				{
					System.out.println("置けない場所です。打ちなおしてください");
				}
				// 打つ手を決める
				if(gameTurn == oppAI.color)
				{
					System.out.println("思考中");
					long startTime = System.currentTimeMillis();
					// AIが打つ手をcellに入れる
					cell = oppAI.select(board.copy());
					
					// 棋譜データ
					long elapsedTime = System.currentTimeMillis() - startTime;
					int sec = (int)(elapsedTime/1000);
					int msec = (int)(elapsedTime%1000);
					timeString = String.format(":%d分%d秒%d",sec/60,sec%60,msec);
					System.out.println(timeString);
				}
				else
				{
					// ガベージコレクションをお願いする
					System.gc();
					
					// 人が打つ手をcellに入れる
					//cell = input();
					
					Point[] cells = board.getReversibleCells(gameTurn);
					cell = cells[0];
					cell = new AI(gameTurn).select(board);
					
				}
				// 1回置こうとした
				putted1 = true;
				// ボードに石を置けなかったらここをループする
			}while(!board.putStone(gameTurn, cell));
			
			// 棋譜
			char cturn = gameTurn==Rule.BLACK?'x':'o';
			char cx = (char)(cell.x+'A');
			char cy = (char)(cell.y+'1');
			System.out.println(String.format("棋譜:%c は %c%C に打ちました\n",cturn,cx,cy));
			
			history.add(String.format("%c%c%s",cx,cy,timeString));
			history.add(getBoardString());
			
			gameTurn = !gameTurn;
		}
		System.out.println("====================");
		// 終わりました
		System.out.println(getBoardString());
		
		// 0なら引き分け
		// 正ならxの勝ち
		// 負ならoの勝ち
		int resultBlack = board.getStoneCount(Rule.BLACK);
		int resultWhite = board.getStoneCount(Rule.WHITE);
		int result = resultBlack - resultWhite;
		
		// 作成日時
		String date = new java.text.SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date());
		// ファイルパス
		File file = new File("../aiai"+date+".log");
		// ファイル出力するやつ
		pw = new PrintWriter(file,"UTF-8");
		// 棋譜を書き込む
		
		pw.println(String.format("AI:%c\n",oppAI.color == Rule.BLACK?'x':'o'));
		
		for(String s:history)
		{
			pw.println(s);
		}
		
		pw.println(getBoardString());
		
		pw.println(String.format("x:%d",resultBlack));
		pw.println(String.format("o:%d",resultWhite));
		
		System.out.format("x:%2d o:%2dで",resultBlack,resultWhite);
		if(result > 0)
		{
			System.out.print("xの勝ちです");
			pw.println("x WON");
			if(oppAI.color == Rule.BLACK)
			{
				pw.println("AI WON");
			}
			else
			{
				pw.println("AI LOSE");
			}
		}
		else if(result < 0)
		{
			System.out.print("oの勝ちです");
			pw.println("o WON");
			if(oppAI.color == Rule.WHITE)
			{
				pw.println("AI WON");
			}
			else
			{
				pw.println("AI LOSE");
			}
		}
		else
		{
			System.out.print("引き分けです");
			pw.println("DRAW");
			pw.println("DRAW");
		}
		System.out.format(" (you are:%c AI:%c\n",!oppAI.color == Rule.BLACK?'x':'o', oppAI.color == Rule.BLACK?'x':'o');
		pw.close();
	}
	
	/**
		[A-Ha-h][1-8]の形式で入力するとPointクラスのインスタンスを返す
		プログラム内で表示されるヘルプには[A-H][1-8]の形式しか表示しない
		quitかexitと入力すると強制終了する
		不正入力には正常入力ができるまでループで対応する
		@return Point 入力されたセル
	*/
	public static Point input()
	{
		Point p;
		while(true)
		{
			System.out.println("([A-H][1-8])で入力してください");
			try
			{
				String value = scanner.next();
				if(value.equals("exit") || value.equals("quit"))
				{
					System.exit(0);
				}
				value = value.toUpperCase();
				int x = value.charAt(0)-'A';
				int y = value.charAt(1)-'1';
				if(0<=x&&x<=7&&0<=y&&y<=7)
				{
					p = new Point(x,y);
					break;
				}
			}
			catch(Exception e)
			{
			}
		}
		return p;
	}

	/**
		ボードの状態をStringで返す
		@return String ボードを文字列で表現したもの
	*/
	public static String getBoardString()
	{
		long mask = 1L;
		long black = board.black;
		long white = board.white;
		String buffer = "";
		
		buffer += ("  A B C D E F G H\n");
		
		for(int i=0;i<64;i++){
			if(i%8 == 0){
				buffer += ""+((i/8)+1 +" ");
			}
			
			if((black&mask)==mask){
				buffer += ("x ");
			}else if((white&mask)==mask){
				buffer += ("o ");
			}else{
				buffer += (": ");
			}
			mask <<= 1;
			
			if(i%8 == 7){
				buffer += ""+((i/8)+1)+"\n";
			}
		}
		buffer += ("  A B C D E F G H\n");
		return buffer;
	}
	/**
		ビープ音を鳴らす
	*/
	public static void notice()
	{
		java.awt.Toolkit.getDefaultToolkit().beep();
	}
}