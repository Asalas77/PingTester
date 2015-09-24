/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  javafx.concurrent.Task
 */
package application;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javafx.concurrent.Task;

public class PingTask extends Task<Number> {
	int time;
	String address;

	public PingTask(String text) {
		this.address = text;
	}

	protected Integer call() throws Exception {
		try {
			String cmd = "";
			cmd = System.getProperty("os.name").startsWith("Windows") ? "ping -n 1 " + this.address
					: "ping -c 1 " + this.address;
			Process process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String firstLine = in.readLine();
			String secondLine = in.readLine();
			String thirdLine = in.readLine();

			if (thirdLine != null && thirdLine.startsWith("Reply from")) {
				String[] parts = thirdLine.split("[ =ms]");
				this.time = Integer.parseInt(parts[9]);
				this.updateMessage(thirdLine);
			} else if (secondLine != null) {
				this.updateMessage(secondLine);
			} else if (firstLine != null) {
				this.updateMessage(firstLine);
			} else {
				this.updateMessage("Shit.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.time;
	}
}
