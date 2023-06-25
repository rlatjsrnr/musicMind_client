package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import game.CanvasController;
import utils.DBUtil;

public class MusicDAOImpl implements MusicDAO {

	Connection conn;
	Statement stmt;
	PreparedStatement pstmt;
	ResultSet rs;
	
	public MusicDAOImpl() {
		conn = DBUtil.getConnection();
	}// 생성자 끝
	
	@Override
	public String getTitle(int num) {
		String sql = "SELECT name FROM music WHERE num = ?";
		String title = "";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				title = rs.getString(1);
			}else {
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			DBUtil.close(rs,pstmt);
		}
		
		return title;
	}
	
}
