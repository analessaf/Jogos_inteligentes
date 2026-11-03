package br.mackenzie;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Atividade de IA: NPC explora sozinho, segue cliques e persegue o jogador. */
public class Main extends ApplicationAdapter {
    private static final float LARGURA = 960f, ALTURA = 540f;
    private static final float VISAO = 175f, RAIO_ARRIVE = 105f;

    private enum Estado { EXPLORANDO, INDO_AO_CLIQUE, PARADO, PERSEGUINDO }

    private FitViewport tela;
    private ShapeRenderer formas;
    private SpriteBatch lote;
    private BitmapFont fonte;
    private Agente npc, jogador;
    private Vector2 destino;
    private Vector2 forca = new Vector2();
    private Estado estado = Estado.EXPLORANDO;
    private String comportamento = "Wander";
    private float pausa = 0f;

    @Override
    public void create() {
        tela = new FitViewport(LARGURA, ALTURA);
        formas = new ShapeRenderer();
        lote = new SpriteBatch();
        fonte = new BitmapFont();
        npc = new Agente(200f, 210f);
        jogador = new Agente(750f, 310f);
        jogador.velocidade.setZero();
    }

    @Override
    public void resize(int largura, int altura) {
        tela.update(largura, altura, true);
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        moverJogador(delta);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector2 clique = tela.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            if (clique.y > 75f && clique.y < 460f) {
                destino = clique;
                pausa = 0f;
            }
        }

        // FSM: a IA escolhe um comportamento automaticamente.
        if (npc.posicao.dst(jogador.posicao) < VISAO) {
            estado = Estado.PERSEGUINDO;
            comportamento = "Pursuit";
            forca = npc.pursuit(jogador);
        } else if (pausa > 0f) {
            pausa -= delta;
            estado = Estado.PARADO;
            comportamento = "Arrive concluido";
            forca = new Vector2();
        } else if (destino != null) {
            estado = Estado.INDO_AO_CLIQUE;
            float distancia = npc.posicao.dst(destino);
            comportamento = distancia > RAIO_ARRIVE ? "Seek" : "Arrive";
            forca = distancia > RAIO_ARRIVE
                    ? npc.seek(destino) : npc.arrive(destino, RAIO_ARRIVE);
            if (distancia < 7f && npc.velocidade.len() < 22f) {
                npc.velocidade.setZero();
                destino = null;
                pausa = 0.8f; // Para um instante e volta a explorar sozinho.
            }
        } else {
            estado = Estado.EXPLORANDO;
            comportamento = "Wander";
            forca = npc.wander(delta);
            // Evita que a exploracao deixe o NPC preso nas bordas.
            if (npc.posicao.x < 65f || npc.posicao.x > 895f ||
                    npc.posicao.y < 110f || npc.posicao.y > 435f)
                forca = npc.seek(new Vector2(480f, 270f));
        }

        npc.mover(forca, delta);
        npc.posicao.x = MathUtils.clamp(npc.posicao.x, 24f, 936f);
        npc.posicao.y = MathUtils.clamp(npc.posicao.y, 75f, 455f);
        desenhar();
    }

    private void moverJogador(float delta) {
        Vector2 direcao = new Vector2();
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) direcao.x--;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) direcao.x++;
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) direcao.y++;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) direcao.y--;
        if (!direcao.isZero()) direcao.nor().scl(185f);
        jogador.velocidade.set(direcao);
        jogador.posicao.mulAdd(direcao, delta);
        jogador.posicao.x = MathUtils.clamp(jogador.posicao.x, 24f, 936f);
        jogador.posicao.y = MathUtils.clamp(jogador.posicao.y, 75f, 455f);
    }

    private void desenhar() {
        ScreenUtils.clear(0.96f, 0.97f, 0.99f, 1f);
        tela.apply();
        formas.setProjectionMatrix(tela.getCamera().combined);

        // Visual diferente do exemplo: fundo claro, painel e personagens geometricos.
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(0.12f, 0.16f, 0.28f, 1f);
        formas.rect(0f, 478f, LARGURA, 62f);
        formas.setColor(0.90f, 0.92f, 0.96f, 1f);
        formas.rect(0f, 0f, LARGURA, 60f);
        formas.end();

        formas.begin(ShapeRenderer.ShapeType.Line);
        formas.setColor(0.87f, 0.89f, 0.93f, 1f);
        for (int x = 0; x <= 960; x += 48) formas.line(x, 60f, x, 478f);
        for (int y = 60; y <= 478; y += 48) formas.line(0f, y, LARGURA, y);

        // Radar ao redor do NPC; alvo do clique; previsao da perseguicao.
        formas.setColor(0.95f, 0.48f, 0.42f, 1f);
        formas.circle(npc.posicao.x, npc.posicao.y, VISAO, 64);
        if (destino != null) {
            formas.setColor(0.37f, 0.35f, 0.70f, 1f);
            formas.circle(destino.x, destino.y, 12f, 20);
            formas.line(destino.x - 18f, destino.y, destino.x + 18f, destino.y);
            formas.line(destino.x, destino.y - 18f, destino.x, destino.y + 18f);
            if (estado == Estado.INDO_AO_CLIQUE)
                formas.circle(destino.x, destino.y, RAIO_ARRIVE, 40);
        }
        if (estado == Estado.PERSEGUINDO) {
            Vector2 futuro = npc.preverPosicao(jogador);
            formas.setColor(0.61f, 0.34f, 0.83f, 1f);
            formas.circle(futuro.x, futuro.y, 8f, 16);
            formas.line(npc.posicao, futuro);
        }
        // Verde = velocidade, roxo = forca de direcao.
        formas.setColor(0.16f, 0.63f, 0.37f, 1f);
        formas.line(npc.posicao, new Vector2(npc.posicao).mulAdd(npc.velocidade, 0.32f));
        formas.setColor(0.56f, 0.34f, 0.77f, 1f);
        formas.line(npc.posicao, new Vector2(npc.posicao).mulAdd(forca, 0.12f));
        formas.end();

        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(0.98f, 0.40f, 0.37f, 1f); // NPC: coral
        formas.circle(npc.posicao.x, npc.posicao.y, 15f, 24);
        formas.setColor(Color.WHITE);
        Vector2 frente = new Vector2(npc.velocidade);
        if (!frente.isZero()) frente.nor().scl(6f);
        formas.circle(npc.posicao.x + frente.x, npc.posicao.y + frente.y, 4f, 12);
        formas.setColor(0.10f, 0.65f, 0.72f, 1f); // Jogador: losango turquesa
        float x = jogador.posicao.x, y = jogador.posicao.y;
        formas.triangle(x, y + 17f, x - 15f, y, x + 15f, y);
        formas.triangle(x, y - 17f, x - 15f, y, x + 15f, y);
        formas.end();

        lote.setProjectionMatrix(tela.getCamera().combined);
        lote.begin();
        fonte.setColor(Color.WHITE);
        fonte.draw(lote, "LABORATORIO DE IA  |  Steering Behaviors", 18f, 514f);
        fonte.draw(lote, "Estado: " + estado + "  (" + comportamento + ")", 530f, 514f);
        fonte.setColor(0.16f, 0.20f, 0.30f, 1f);
        lote.end();
    }

    @Override
    public void dispose() {
        formas.dispose();
        lote.dispose();
        fonte.dispose();
    }
}
