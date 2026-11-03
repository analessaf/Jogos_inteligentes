package br.mackenzie;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Agente {
    public final Vector2 posicao = new Vector2();
    public final Vector2 velocidade = new Vector2();
    public float maxVelocidade = 150f;
    public float maxForca = 380f;
    private float angulo = 0f;

    public Agente(float x, float y) {
        posicao.set(x, y);
        velocidade.set(50f, 0f);
    }

    // Seek
    public Vector2 seek(Vector2 alvo) {
        Vector2 desejada = new Vector2(alvo).sub(posicao);
        if (desejada.isZero()) return new Vector2(velocidade).scl(-1f);
        return desejada.nor().scl(maxVelocidade).sub(velocidade);
    }

    // Arrive
    public Vector2 arrive(Vector2 alvo, float raio) {
        Vector2 desejada = new Vector2(alvo).sub(posicao);
        float distancia = desejada.len();
        if (distancia < 0.01f) return new Vector2(velocidade).scl(-1f);
        float rapidez = maxVelocidade * Math.min(distancia / raio, 1f);
        return desejada.scl(rapidez / distancia).sub(velocidade).scl(4f);
    }

    // Wander
    public Vector2 wander(float delta) {
        angulo += MathUtils.random(-1.5f, 1.5f) * delta;
        Vector2 rumo = new Vector2(velocidade);
        if (rumo.isZero(1f)) rumo.set(1f, 0f);
        return rumo.nor().rotateRad(angulo)
                .scl(maxVelocidade * 0.75f).sub(velocidade);
    }

    // Pursuit
    public Vector2 preverPosicao(Agente jogador) {
        float tempo = posicao.dst(jogador.posicao)
                / (maxVelocidade + jogador.velocidade.len());
        tempo = Math.min(tempo, 1.2f);
        return new Vector2(jogador.posicao).mulAdd(jogador.velocidade, tempo);
    }

    public Vector2 pursuit(Agente jogador) {
        return seek(preverPosicao(jogador));
    }

    // Flocking
    public Vector2 flocking(Agente[] grupo) {
        Vector2 separacao = new Vector2();
        Vector2 alinhamento = new Vector2();
        Vector2 coesao = new Vector2();
        int vizinhos = 0;

        for (Agente outro : grupo) {
            if (outro == this) continue;
            float distancia = posicao.dst(outro.posicao);
            if (distancia > 0f && distancia < 90f) {
                vizinhos++;
                alinhamento.add(outro.velocidade);
                coesao.add(outro.posicao);       
                if (distancia < 30f)
                    separacao.add(new Vector2(posicao).sub(outro.posicao)
                            .scl(1f / (distancia * distancia))); 
            }
        }

        if (vizinhos == 0) return new Vector2();
        alinhamento.scl(1f / vizinhos).nor().scl(maxVelocidade).sub(velocidade);
        coesao.scl(1f / vizinhos);
        Vector2 aproximar = seek(coesao);
        if (!separacao.isZero())
            separacao.nor().scl(maxVelocidade).sub(velocidade);
        return separacao.scl(2.2f).add(alinhamento.scl(0.8f))
                .add(aproximar.scl(0.7f));
    }

    public void mover(Vector2 forca, float delta) {
        velocidade.mulAdd(new Vector2(forca).limit(maxForca), delta)
                .limit(maxVelocidade);
        posicao.mulAdd(velocidade, delta);
    }
}
