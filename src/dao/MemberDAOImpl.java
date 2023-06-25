package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import game.MemberVO;
import utils.DBUtil;

public class MemberDAOImpl implements MemberDAO {

	Connection conn;
	Statement stmt;
	PreparedStatement pstmt;
	ResultSet rs;
	
	public MemberDAOImpl() {
		conn = DBUtil.getConnection();
	}
	
	// 로그인시 db정보 확인
	@Override
	public MemberVO selectMember(String mId, String mPw) {
		MemberVO member = null;
		String sql = "SELECT * FROM member WHERE mId = ? AND mPw = ?";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, mId);
			pstmt.setString(2, mPw);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				member = new MemberVO(
					rs.getString("mId"),		
					rs.getString("mPw")			
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.close(rs,pstmt);
		}
		
		return member;
	}
	
	// 회원목록(id,점수)
	@Override
	public ArrayList<MemberVO> select() {
		ArrayList<MemberVO> list = new ArrayList<>();
		
		String sql = "SELECT * FROM member ORDER BY point DESC";
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while(rs.next()) {
				MemberVO member = new MemberVO();
				String mId = rs.getString(2);
				int point = rs.getInt(4);
				member.setmId(mId);
				member.setPoint(point);
				list.add(member);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			DBUtil.close(rs,stmt);
		}
		return list;
	}
	
	// 회원 점수 업데이트 
	@Override
	public String update(MemberVO member) {
		int result = 0;
		String answer = "";
		String sql = "UPDATE member SET point = ? WHERE mId = ?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, member.getPoint());
			pstmt.setString(2, member.getMId());
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			DBUtil.close(pstmt);
			answer = result+"개가 업데이트 되었습니다.";
		}
		return answer;
	}
	
	// member객체로 점수 찾기
	@Override
	public int selectPoint(MemberVO member) {
		int result = 0;
		String sql = "SELECT point FROM member WHERE mId = ?";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, member.getMId());
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				result = rs.getInt("point");	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.close(rs,pstmt);
		}
		return result;
	}

}




