package com.onlineinteract.core.workbench;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.onlineinteract.core.Workspace;
import com.onlineinteract.core.dialog.ServiceDialog;
import com.onlineinteract.core.type.ServiceStatus;
import com.onlineinteract.core.type.TemplateType;

public class Template implements WorkbenchItem {

	UUID uuid;

	public static final int DOUBLE_CLICK_RANGE = 400;
	private static float BOX_OFFEST_X = 20;
	private static float LABEL_OFFSET_X = 10;
	private static float LABEL_OFFSET_Y = 90;
	private static int BOX_WIDTH = 180;
	public static int BOX_HEIGHT = 100;

	private ShapeRenderer shapeRenderer;
	private SpriteBatch batch;
	private BitmapFont font;
	private OrthographicCamera camera;
	private float x;
	private float y;
	private Workspace workspace;
	private Color color1;
	private Color color2;
	private String label;
	private String startupCommand;
	private String runningClause;
	private String servicePortNo;
	private ServiceDialog serviceDialog;
	private Skin skin;
	private Stage stage;
	private Process exec;
	private Runtime runtime;
	private TemplateType type;

	private ServiceStatus serviceStatus = ServiceStatus.SHUTDOWN;

	private float instanceOffsetX;
	private float instanceOffsetY;

	private long previousTimeMillis = -DOUBLE_CLICK_RANGE - 1;

	public Template() {
	}

	public Template(Workspace workspace, float y, Color color1, Color color2, String label, TemplateType type,
			UUID uuid) {
		this(workspace, BOX_OFFEST_X, y, color1, color2, label, type, uuid);
	}

	public Template(Workspace workspace, float x, float y, Color color1, Color color2, String label, TemplateType type,
			UUID uuid) {
		this.workspace = workspace;
		this.shapeRenderer = workspace.getShapeRenderer();
		this.batch = workspace.getBatch();
		this.font = workspace.getFont();
		this.camera = workspace.getCamera();
		this.skin = workspace.getSkin();
		this.stage = workspace.getStage();
		this.x = x;
		this.y = y;
		this.color1 = color1;
		this.color2 = color2;
		this.label = label;
		this.type = type;
		this.uuid = (uuid != null) ? uuid : UUID.randomUUID();
		runtime = Runtime.getRuntime();
	}

	public void draw() {
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setColor(color1);
		Gdx.gl.glLineWidth(2);
		shapeRenderer.rect(x, y, BOX_WIDTH, BOX_HEIGHT);
		shapeRenderer.end();
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.rect(x + 1, y + 1, BOX_WIDTH - 2, BOX_HEIGHT - 2);
		drawServiceStatus();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		font.setColor(color2);
		font.getData().setScale(1);
		font.draw(batch, label, x + LABEL_OFFSET_X, y + LABEL_OFFSET_Y);
		batch.end();
	}

	private void drawServiceStatus() {
		shapeRenderer.end();
		shapeRenderer.begin(ShapeType.Filled);

		switch (serviceStatus) {
		case SHUTDOWN:
			shapeRenderer.setColor(Color.FOREST);
			shapeRenderer.triangle(x + 10, y + 10, x + 10, y + 30, x + 30, y + 20);
			break;
		case LOADING:
			shapeRenderer.setColor(Color.GOLD);
			shapeRenderer.circle(x + 20, y + 20, 10, 100);
			break;
		case RUNNING:
			shapeRenderer.setColor(Color.FIREBRICK);
			shapeRenderer.rect(x + 10, y + 10, 20, 20);
			break;
		}

		shapeRenderer.end();
	}

	public boolean isClickWithinBoundary(float x, float y) {
		float clickX = x;
		float clickY = y;

		if (clickX >= this.x && clickX <= (this.x + BOX_WIDTH) && clickY >= this.y && clickY <= (this.y + BOX_HEIGHT)) {
			instanceOffsetX = clickX - this.x;
			instanceOffsetY = clickY - this.y;
			return true;
		}

		return false;
	}

	public void startStopService(float x, float y) {
		float clickX = x;
		float clickY = y;

		if (clickX >= this.x + 10 && clickX <= (this.x + 30) && clickY >= this.y + 10 && clickY <= (this.y + 30))
			determineStartStop();
	}

	private void determineStartStop() {
		switch (serviceStatus) {
		case SHUTDOWN:
			serviceStatus = ServiceStatus.LOADING;
			spawnServiceInstance();
			break;
		case RUNNING:
			serviceStatus = ServiceStatus.LOADING;
			destroyServiceInstance();
			break;
		default:
			break;
		}
	}

	/**
	 * TODO: wire up command and started hooks. ensure env variables
	 * are parsed appropriately. save. load. start all.
	 */
	private void spawnServiceInstance() {
		String launchCommand = replaceEnvVars(startupCommand);
		try {
			exec = runtime.exec(launchCommand);
		} catch (IOException e) {
			e.printStackTrace();
		}

		processInputStream();
		processErrorStream();
	}

	private void processInputStream() {
		new Thread(() -> {
			try {
				InputStream inputStream = exec.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

				String line = "";
				while ((line = bufferedReader.readLine()) != null) {
					System.out.println(line);
					if (line.contains(runningClause)) {
						System.out.println("Application launched successfully!");
						serviceStatus = ServiceStatus.RUNNING;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private void processErrorStream() {
		new Thread(() -> {
			try {
				InputStream errorStream = exec.getErrorStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));

				String line = "";
				while ((line = bufferedReader.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private void destroyServiceInstance() {
		exec.destroy();
		System.out.println("Process destroyed, exiting.");
		serviceStatus = ServiceStatus.SHUTDOWN;
	}

	protected String replaceEnvVars(String startupCommand) {
		String launchCommand = startupCommand;
		while (launchCommand.indexOf("%") != -1) {
			int startIndex = launchCommand.indexOf("%");
			int endIndex = launchCommand.indexOf("%", startIndex + 1);
			if (endIndex == -1)
				break;
			String envVar = launchCommand.substring(startIndex + 1, endIndex);
			launchCommand = launchCommand.replace("%" + envVar + "%", System.getenv(envVar));
		}
		return launchCommand;
	}

	public void renderServiceDialog() {
		Gdx.input.setInputProcessor(stage);
		serviceDialog = new ServiceDialog("Service Configuration", skin, workspace, this);
		if (label.equals("Áicroservice"))
			serviceDialog.getLabelTextField().setText("New Service");
		else
			serviceDialog.getLabelTextField().setText(label);
		serviceDialog.getStartupCommandTextField().setText(startupCommand);
		serviceDialog.getRunningClauseTextField().setText(runningClause);
		serviceDialog.getServicePortNoTextField().setText(servicePortNo);
		stage.act();
		serviceDialog.show(stage);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getInstanceOffsetX() {
		return instanceOffsetX;
	}

	public float getInstanceOffsetY() {
		return instanceOffsetY;
	}

	public long getPreviousTimeMillis() {
		return previousTimeMillis;
	}

	public void setPreviousTimeMillis(long previousTimeMillis) {
		this.previousTimeMillis = previousTimeMillis;
	}

	public String getStartupCommand() {
		return startupCommand;
	}

	public void setStartupCommand(String startupCommand) {
		this.startupCommand = startupCommand;
	}

	public String getRunningClause() {
		return runningClause;
	}

	public void setRunningClause(String runningClause) {
		this.runningClause = runningClause;
	}

	public String getServicePortNo() {
		return servicePortNo;
	}

	public void setServicePortNo(String servicePortNo) {
		this.servicePortNo = servicePortNo;
	}

	public TemplateType getType() {
		return type;
	}

	public void setType(TemplateType type) {
		this.type = type;
	}

	public UUID getUuid() {
		return uuid;
	}
}
