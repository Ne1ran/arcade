package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class BallZGame extends ApplicationAdapter {
	private SpriteBatch batch;
	private BitmapFont font;

	OrthographicCamera camera;

	Texture background;
	Texture basketball;
	Texture volleyball;
	Texture golfball;
	Texture football;
	Texture bowlingball;
	Texture playerTexture;
	Texture enemyTexture;

	Sound lose;
	Sound hit;
	Sound enemyhit;
	Music music;

	long lastBallSpawnTime;
	int points = 0;
	boolean gameStarted = false;
	Array<Ball> balls = new Array<>();

	Rectangle player;
	Rectangle enemy;

	String scoreStr = "Score : ";
	String start = "Press ENTER to start";

	@Override
	public void create () {
		batch = new SpriteBatch();
		font = new BitmapFont();
		font.getData().setScale(2);

		hit = Gdx.audio.newSound(Gdx.files.internal("hit.wav"));
		lose = Gdx.audio.newSound(Gdx.files.internal("lose.mp3"));
		enemyhit = Gdx.audio.newSound(Gdx.files.internal("enemyhit.mp3"));

		music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
		music.setLooping(true);
		music.setVolume(0.01F);
		music.play();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 720);

	 	background = new Texture("background.png");
	 	basketball = new Texture("basketball.png");
	 	volleyball = new Texture("volleyball.png");
	 	golfball = new Texture("golfball.png");
	 	football = new Texture("football.png");
	 	bowlingball = new Texture("bowlingball.png");
	 	playerTexture = new Texture("player.png");
		enemyTexture = new Texture("enemy.png");

		player = new Rectangle();

		player.setWidth(playerTexture.getWidth());
		player.setHeight(playerTexture.getHeight());
		player.setX(1280-50-player.getWidth());
		player.setY(200 + player.getHeight());

		enemy = new Rectangle();
		enemy.setWidth(enemyTexture.getWidth());
		enemy.setHeight(enemyTexture.getHeight());
		enemy.setX(enemy.getWidth());
		enemy.setY(200 + enemy.getHeight());
	}

	private void spawnBall(){
		Ball ball = new Ball();
		int i = MathUtils.random(1, 100);
		if (i > 90){
			ball.setName("bowling");
			ball.setSpeed(1);
			ball.setTexture(bowlingball);
		} else if (i > 70){
			ball.setName("volleyball");
			ball.setSpeed(2);
			ball.setTexture(volleyball);
		} else if (i > 50){
			ball.setName("football");
			ball.setSpeed(2);
			ball.setTexture(football);
		} else if (i > 30){
			ball.setName("basketball");
			ball.setSpeed(2);
			ball.setTexture(basketball);
		} else {
			ball.setName("golf");
			ball.setSpeed(4);
			ball.setTexture(golfball);
		}
		ball.setDirection(1);
		ball.setX(75 + enemyTexture.getWidth());
		ball.setY(MathUtils.random(0, 720 - ball.getTexture().getHeight()));
		ball.setWidth(ball.getTexture().getWidth());
		ball.setHeight(ball.getTexture().getHeight());
		balls.add(ball);
		lastBallSpawnTime = TimeUtils.nanoTime();
	}

	@Override
	public void render () {

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		batch.draw(background, 0, 0);
		batch.draw(playerTexture, player.getX(), 720 - player.getY() - player.getHeight());
		batch.draw(enemyTexture, enemy.getX(), 720 - enemy.getY() - enemy.getHeight());
		font.draw(batch, start, 520, 360);
		if (gameStarted) {
			font.draw(batch, scoreStr + points, 580, 30);
			for (Ball ball : balls){
				batch.draw(ball.getTexture(), ball.getX(), 720 - ball.getY() - ball.getWidth());
			}
		}

		batch.end();

		if (!gameStarted){
			if(Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
				gameStarted = true;
				start = "";
				spawnBall();
			}
		}

		if (gameStarted){
			if(TimeUtils.nanoTime() - lastBallSpawnTime > 2100000000) spawnBall();

			if(Gdx.input.isKeyPressed(Input.Keys.W)) player.y -= 250 * Gdx.graphics.getDeltaTime();
			if(Gdx.input.isKeyPressed(Input.Keys.S)) player.y += 250 * Gdx.graphics.getDeltaTime();

			if(player.y < 0) player.y = 0;
			if(player.y > 720 - player.getHeight()) player.y = 720 - player.getHeight();

			Iterator<Ball> iter = balls.iterator();

			while (iter.hasNext()) {
				Ball ball = iter.next();
				String type = ball.getName();
				if (ball.getX() + ball.getTexture().getWidth() > 1280){
					iter.remove();
					if (!ball.getName().equals("bowling")){
						points -=3;
					} else points += 3;
				}
				if (ball.getX() + ball.getTexture().getWidth() < 0){
					iter.remove();
				}
				ball.x += (ball.getSpeed() * 150 * Gdx.graphics.getDeltaTime() * ball.getDirection());

				if (ball.overlaps(player)){
					if (type.equals("golf")){
						ball.setSpeed(ball.getSpeed() * 2);
						ball.setDirection(-1);
						hit.play();
						points += 5;
					} else if (type.equals("bowling")){
						lose.play();
						points -= 20;
						iter.remove();
					} else {
						ball.setSpeed(ball.getSpeed() * 2);
						ball.setDirection(-1);
						hit.play();
						points++;
					}
				}

				if (ball.overlaps(enemy)){
					enemy.setY(MathUtils.random(0, 720 - enemy.getHeight()));
					enemyhit.play(0.1F);
					points += 3;
					iter.remove();
				}

		}
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
	}
}
