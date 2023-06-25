package game;

import java.net.URL;
import java.util.List;

import client.Client;
import dao.MusicDAOImpl;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

//자기 순서가 되었을 때 음악 실행시키고 제목 변경받음
public class MyTurn {

	// MediaView를 통해 재생되는 resource를 제어하는 객체
	MediaPlayer mediaPlayer;
	// 재생해야할 resource 정보를 저장하는 객체
	Media media;
	// 재생해야할 media path저장 객체
	// "/media/audio.mp3" 형태로 전달 받아야함.
	String path = "";
	// path에서 노래 제목
	String musicTitle = "";
	
	public MyTurn() {}
 
	public MyTurn(MediaPlayer mediaPlayer, Media media) {
		this.mediaPlayer = mediaPlayer;
		this.media = media;
	}

	public MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}

	public void setMediaPlayer(MediaPlayer mediaPlayer) {
		this.mediaPlayer = mediaPlayer;
	}

	public Media getMedia() {
		return media;
	}

	public void setMedia(Media media) {
		this.media = media;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	// 노래제목 db에서 꺼내기
	// path에서 잘라서 num변수에 넣고 MusicDAOImpl getTitle 호출
	public void setTitle(String path){
		int begin = path.indexOf("/media/")+7;
		int end = path.indexOf(".");
		if(begin != -1 && end != -1) {
			// begin 인덱스부터 end 인덱스 이전까지
			String str = path.substring(begin,end);
			int num = Integer.parseInt(str);
			MusicDAOImpl dao = new MusicDAOImpl();
			musicTitle = dao.getTitle(num);
			// 서버로 노래 제목 전송
			Client.mainClient.canvasController.send("t", musicTitle);
		}else {
			musicTitle = "제목load 실패";
		}
	}
	
	// 음악 재생 메소드 
	public void playMusic() {
		List<Integer> oldlist = Client.mainClient.canvasController.musiclist;
		boolean isRun = true;
		String path = "";
		int song = 0;
		
		// 기존 출제된 문제는 다시 반복문 실행(중복방지)
		while(isRun) {
			int i = (int)(Math.random()*20)+1;
			if(!oldlist.contains(i)) {
				oldlist.add(i);
				song = i;
				isRun = false;
			}
		}
		path ="/media/"+song+".mp3";
		// path = "/media/1.mp3" 형태
		URL url = getClass().getResource(path);
		media = new Media(url.toString());
		if(mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer = null;
		}
		mediaPlayer = new MediaPlayer(media);
		mediaPlayer.play();
		
		setTitle(path);
		
		//progressBar에 남은 시간 반영
		mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {

			@Override
			public void changed(ObservableValue<? extends Duration> observable,
				Duration oldValue, Duration newValue) {
				Duration duration = mediaPlayer.getTotalDuration();
				double totalTime = duration.toSeconds();
				double currentTime = newValue.toSeconds();
				double progress = currentTime / totalTime;
				String lblTxt = (int)currentTime+"/"+(int)totalTime;
				Platform.runLater(()->{
					Client.mainClient.canvasController.setProgress(progress, lblTxt);
				});
			}
		});
	}
	
	public void stopMusic() {
		mediaPlayer.stop();
	}
}
