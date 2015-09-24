package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class GuiController implements Initializable {

	@FXML
	Button startButton, stopButton;
	@FXML
	TextField sField, nField, ipField;
	@FXML
	LineChart<Number, Number> chart;
	@FXML
	Label timeLabel, pingLabel;
	@FXML
	TextArea textArea;
	@FXML
	AnchorPane anchorPane;
	@FXML ProgressBar progressBar;

	ScheduledService<Number> scheduler;
	ObservableList<Data<Number, Number>> data;
	public static int totalCount = 0;

	private String getTime(double seconds) {
		int h = (int) (seconds / 3600);
		int m = (int) ((seconds % 3600) / 60);
		int s = (int) (seconds % 60);
		if (h == 0) {
			if (m == 0) {
				
				return String.format("%ds", s);
			}
		
			return String.format("%dm %ds", m, s);
		}
	
		return String.format("%dh %dm %ds", h, m, s);

	}

	public void start() {
		if (sField.getText().isEmpty() || Double.parseDouble(sField.getText()) < 0.1)
			sField.setText("0.1");
		data = FXCollections.observableArrayList();
		int size = Integer.parseInt(nField.getText());
		totalCount = -size;
		flip();
		progressBar.toFront();
		InitTask task = new InitTask(size);
		progressBar.progressProperty().bind(task.progressProperty());
		new Thread(task).start();
	}

	public void stop() {
		scheduler.cancel();
		flip();
		totalCount = 0;
	}

	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
	}

	public void flip() {
		ipField.setDisable(!ipField.isDisabled());
		nField.setDisable(!nField.isDisabled());
		sField.setDisable(!sField.isDisabled());
		startButton.setDisable(!startButton.isDisabled());
		stopButton.setDisable(!stopButton.isDisabled());
	}

	public void updatePingLabel(Number ping) {
		pingLabel.setText(ping + "ms");
		if (ping.intValue() < 80)
			pingLabel.setTextFill(Color.LAWNGREEN);
		if (ping.intValue() >= 80 && ping.intValue() < 150)
			pingLabel.setTextFill(Color.GOLD);
		if (ping.intValue() >= 150 && ping.intValue() < 400)
			pingLabel.setTextFill(Color.ORANGE);
		if (ping.intValue() >= 400)
			pingLabel.setTextFill(Color.RED);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		progressBar.toBack();
		this.textArea.prefWidthProperty().bind(anchorPane.widthProperty());
		this.textArea.prefHeightProperty().bind(anchorPane.heightProperty());
		chart.getXAxis().setVisible(false);
		chart.getXAxis().setAutoRanging(true);
		stopButton.setDisable(true);
		chart.getYAxis().setAutoRanging(true);
		((ValueAxis<Number>) this.chart.getXAxis())
				.setTickLabelFormatter((StringConverter<Number>) new XAxisLabelConverter(Double
						.parseDouble(this.sField.getText())));
		sField.textProperty().addListener(new ParamsChangeListener());
		nField.textProperty().addListener(new ParamsChangeListener());
		scheduler = new ScheduledService<Number>() {
			@Override
			protected Task<Number> createTask() {
				return new PingTask(ipField.getText());
			}
		};
		scheduler.messageProperty().addListener(
				(ChangeListener<String>) new ChangeListener<String>() {

					public void changed(ObservableValue<? extends String> observable,
							String oldValue, String newValue) {
						if (!(newValue == null || newValue.trim().isEmpty())) {
							GuiController.this.textArea.appendText(String.valueOf(newValue) + "\n");
						}
					}
				});
	}

	class ParamsChangeListener implements ChangeListener<String> {
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue,
				String newValue) {
			if (isNumeric(newValue))
				timeLabel.setText(getTime(Double.parseDouble(sField.getText())
						* Integer.parseInt(nField.getText())));
		}
	}
	
	class InitTask extends Task<XYChart.Series<Number, Number>>{

		int n;
		InitTask(int n){
			this.n = n;
		}
		@Override
		protected XYChart.Series<Number, Number> call() throws Exception {
			XYChart.Series<Number, Number> series = new Series<>();
			for (int i = 0; i < n; i++) {
				series.getData().add(new XYChart.Data<Number, Number>(totalCount++, 0));
				updateProgress(i,n);
			}
			return series;
		}
		
		@Override
		protected void succeeded() {
			chart.getData().clear();
			chart.getData().add(getValue());
			((ValueAxis<Number>) chart.getXAxis())
			.setTickLabelFormatter((StringConverter<Number>) new XAxisLabelConverter(Double
					.parseDouble(sField.getText())));
			scheduler.setPeriod(Duration.seconds(Double.parseDouble(sField.getText())));
			scheduler.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

				@Override
				public void handle(WorkerStateEvent event) {
					if (getValue().getData().size() >= n)
						getValue().getData().remove(0);
					getValue().getData().add(new XYChart.Data<>(totalCount++, scheduler.getValue()));
					updatePingLabel(scheduler.getValue());
				}
			});
			scheduler.restart();
			progressBar.toBack();
		}
	}

	class XAxisLabelConverter extends StringConverter<Number> {
		double interval;

		public XAxisLabelConverter(double interval) {
			this.interval = interval;

		}

		public Number fromString(String arg0) {
			return null;
		}

		public String toString(Number value) {
			if (value.intValue() < 0) {
				return "";
			}
		
			return getTime(value.doubleValue() * interval);
		}
	}

}