package com.nguyenviethien.exercise201;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.nguyenviethien.exercise201.config.DotEnvConfig;

@SpringBootApplication
public class Exercise201Application {

	public static void main(String[] args) {
		// #region agent log
		try {
			java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
			fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_main1\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"Exercise201Application.java:10\",\"message\":\"Main method entry\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\"}\n");
			fw.close();
		} catch (java.io.IOException ex) {}
		// #endregion
		
		SpringApplication app = new SpringApplication(Exercise201Application.class);
		// Add initializer để load .env file
		app.addInitializers(new DotEnvConfig());
		
		// #region agent log
		try {
			java.io.FileWriter fw = new java.io.FileWriter("d:\\DAT5\\.cursor\\debug.log", true);
			fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_main2\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"Exercise201Application.java:15\",\"message\":\"After adding DotEnvConfig initializer\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\"}\n");
			fw.close();
		} catch (java.io.IOException ex) {}
		// #endregion
		
		app.run(args);
	}

}
