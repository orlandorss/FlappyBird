package com.orlando.bird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Game extends ApplicationAdapter {


	//Textures
	private SpriteBatch batch;
	private Texture[] birds;
	private Texture background;
	private Texture obstacle;
	private Texture obstacleDown;
	private Texture gameOver;
	//Sons
	Sound flyingSound;
	Sound collisionSound;
	Sound pointsSound;

	//Configs
	float deviceHeight;
	float deviceWidth;
	private float gravitate = 0;
	private float posInitialY;
	private float movVariation = 0;
	private float posPlumberVertical, posPlumberHorizontal;
	private float gapBetweenPlumbers;
	private Random random;
	private int points = 0;
	private int highestPunctuation = 0;
	private boolean passedPlumber = false;
	private int gameState =0;
	//textos
	BitmapFont punctuation;
	BitmapFont restart;
	BitmapFont recorde;
	//formas colisao
	private ShapeRenderer shapeRenderer;
	private Circle circleBird;
	private Rectangle rectangleObstacleUp;
	private Rectangle rectangleObstacleDown;
	//salvar pontos
	Preferences preferences;
	//camera
	private OrthographicCamera camera;
	private Viewport viewport;


	@Override
	public void create () {
		textures();
		objects();
	}

	@Override
	public void render () {
		verifierEstate();
		drawTextures();
		validatePoints();
		detectCollisions();

	}


	private void validatePoints() {
		if (posPlumberVertical < 40 ){
			if (!passedPlumber){
				points++;
				pointsSound.play();
						passedPlumber = true;
			}
		}
		movVariation += Gdx.graphics.getDeltaTime()*15;
		if (movVariation > 3){
			movVariation = 0;
		}
	}

	private void verifierEstate(){
		boolean touchScreen = Gdx.input.justTouched();
		if (points > highestPunctuation){
			highestPunctuation = points;
			preferences.putInteger("pontuacao_recorde", highestPunctuation);
		}
	switch (gameState){
			case 0:
				/*APLICA EVENTO TOQUE NA TELA*/
				if (touchScreen){
					gravitate = -15;
					gameState =1 ;
						flyingSound.play();
				}
				break;
			case 1:
				/*	MOVIMENTA CANO */

				posPlumberVertical -= Gdx.graphics.getDeltaTime() *200;

				if (posPlumberVertical <-100){
					posPlumberVertical = deviceWidth;
					posPlumberHorizontal = random.nextInt(400) -200;
					passedPlumber = false;
				}
				if (movVariation > 3){
					movVariation = 0;
				}
				if (Gdx.input.justTouched()){
					flyingSound.play();
					gravitate = -15;
				}
				//gravidade no passaro
				if (posInitialY >0|| touchScreen)
					posInitialY -= gravitate;

				gravitate++;
				break;
			case 2:

				if (posInitialY >0|| touchScreen)
					posInitialY -= gravitate;
					gravitate++;
				if (touchScreen){
					gameState =0;
					points = 0;
					gravitate = 0;
					posInitialY = deviceHeight /2;
					posPlumberVertical = deviceWidth;
				}


				break;
		}
	}
	private void detectCollisions(){
		circleBird.set((float) (50+ birds[0].getWidth()/2.0), posInitialY + birds[0].getHeight(), (float) (birds[0].getWidth()/2.0));

		rectangleObstacleUp.set(posPlumberVertical, deviceHeight /2 + gapBetweenPlumbers /2 + posPlumberHorizontal, obstacleDown.getWidth(), obstacleDown.getHeight());
		rectangleObstacleDown.set(posPlumberVertical, deviceHeight /2 - obstacle.getHeight()- gapBetweenPlumbers /2 - posPlumberHorizontal, obstacle.getWidth(), obstacle.getHeight());

		if (Intersector.overlaps(circleBird, rectangleObstacleUp) ||Intersector.overlaps(circleBird, rectangleObstacleDown) ) {
			if (gameState ==1){
				collisionSound.play();
				gameState = 2;
			}
		}
		shapeRenderer.end();
	}
	private void drawTextures(){
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(background,0,0, deviceWidth, deviceHeight);
		batch.draw(birds[(int) movVariation],50, posInitialY);
		batch.draw(obstacle, posPlumberVertical, deviceHeight /2 - obstacle.getHeight()- gapBetweenPlumbers /2 - posPlumberHorizontal);
		batch.draw(obstacleDown, posPlumberVertical, deviceHeight /2 + gapBetweenPlumbers /2 + posPlumberHorizontal);
		punctuation.draw(batch,String.valueOf(points), deviceWidth /2, deviceHeight);

		if (gameState ==2 ){
			batch.draw(gameOver, (float) (deviceWidth /2 -  gameOver.getWidth()/2.0), deviceHeight /2);
			restart.draw(batch,"Toque para reiniciar", deviceWidth /2 -140, deviceHeight /2 - gameOver.getHeight());
			recorde.draw(batch,"MELHOR PONTUAÇÃO:  "+ highestPunctuation, deviceWidth /2 -140, deviceHeight /2 -25);
		}

		batch.end();
	}
	private void textures(){
		birds = new Texture[3];
		birds[0] = new Texture("passaro1.png");
		birds[1] = new Texture("passaro2.png");
		birds[2] = new Texture("passaro3.png");

		background = new Texture("fundo.png");

		obstacle = new Texture("cano_baixo_maior.png");

		obstacleDown = new Texture("cano_topo_maior.png");

		gameOver = new Texture("game_over.png");


	}

	private  void objects(){
		random = new Random();
		batch = new SpriteBatch();

		final float virtual_height = 1280;
		deviceHeight = virtual_height;
		final float virtual_Width = 720;
		deviceWidth = virtual_Width;
		posInitialY = deviceHeight /2;
		posPlumberVertical = deviceWidth;
		gapBetweenPlumbers = 400;

		// textos
		punctuation = new BitmapFont();
		punctuation.setColor(Color.BLUE);
		punctuation.getData().setScale(5);

		restart = new BitmapFont();
		restart.setColor(Color.GREEN);
		restart.getData().setScale(2);

		recorde = new BitmapFont();
		recorde.setColor(Color.RED);
		recorde.getData().setScale(2);

		//formas geometricas
		circleBird = new Circle();
		rectangleObstacleDown = new Rectangle();
		rectangleObstacleUp = new Rectangle();
		shapeRenderer = new ShapeRenderer();

		//sons
		flyingSound = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		collisionSound = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		pointsSound = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
		//preferencias
		preferences = Gdx.app.getPreferences("flappyBird");
		highestPunctuation = preferences.getInteger ("pontuacao_recorde",0);


		//config camera

		camera = new OrthographicCamera();
		camera.position.set(virtual_Width /2, virtual_height /2,0);
		viewport = new StretchViewport(virtual_Width, virtual_height,camera);


	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width,height);
	}

}
