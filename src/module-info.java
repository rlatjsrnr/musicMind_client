module project_client {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
	requires javafx.media;
	requires java.sql;

	opens client to javafx.graphics, javafx.fxml;
	opens dao to javafx.graphics, javafx.fxml;
	opens game to javafx.graphics, javafx.fxml;
	opens join to javafx.graphics, javafx.fxml;
	opens login to javafx.graphics, javafx.fxml;
	opens result to javafx.graphics, javafx.fxml;
    opens room to javafx.graphics, javafx.fxml;
    opens utils to javafx.graphics, javafx.fxml;
    opens waitingRoom to javafx.graphics, javafx.fxml;

    exports client;
    exports dao;
    exports game;
    exports join;
    exports login;
    exports result;
    exports room;
    exports utils;
    exports waitingRoom;
}
