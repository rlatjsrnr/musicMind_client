package dao;

import game.CanvasController;

public interface MusicDAO {

	// DB의 music table을 사용
	
	//client 제목칸에 보내기
	String getTitle(int num);
}
