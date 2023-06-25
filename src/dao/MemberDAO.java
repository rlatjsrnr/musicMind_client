package dao;

import java.util.ArrayList;

import game.MemberVO;

public interface MemberDAO {
		
		// 회원 검색
		// mId , mPw가 일치하는 사용자 검색
		MemberVO selectMember(String mId, String mPw);
		
		// 회원 목록 검색
		ArrayList<MemberVO> select();
		
		// 회원 점수 업데이트 
		String update(MemberVO member);
		
		// 회원 점수 조회
		int selectPoint(MemberVO member);
	
}
