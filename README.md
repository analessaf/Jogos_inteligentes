# Game IA — Atividade prática de Jogos Inteligentes

Projeto libGDX adaptado do arquivo enviado, conforme o **slide 25** do PDF `game_IA_atualizado.pdf`.

## O que foi implementado

| Item do professor | Onde está no projeto | Comportamento |
|---|---|---|
| 1. Modelar agente | `core/src/main/java/br/mackenzie/Agente.java` | Posição, velocidade, `maxVelocidade`, `maxForca`, massa com `Vector2` |
| 2. Seek e Arrive | `Agente.seek()`, `Agente.arrive()` e `Main.atualizarNpc()` | Clique na tela; o NPC segue o ponto, desacelera e permanece parado ao chegar |
| 3. Wander + FSM | `Agente.wander()`, `Agente.pursuit()` e `Main.Estado` | NPC explora sozinho; ao ficar perto do jogador, muda para perseguição |
| 4. Bônus: Flocking | `Main.atualizarBando()` | 30 agentes usam separação, alinhamento e coesão |

O código implementa os conceitos da aula **sem biblioteca gdx-ai extra**: é mais fácil ver a lógica do `Vector2` em cada método.

## Abrindo e executando no Windows (VS Code)

1. Extraia o ZIP, depois abra no VS Code **a pasta que contém `gradlew.bat`, `settings.gradle`, `core` e `lwjgl3`**.
2. Use Java 17 no terminal (`java -version`). O projeto vem configurado com Gradle Wrapper 8.14.1.
3. No terminal aberto na pasta raiz, execute:

```powershell
.\gradlew.bat lwjgl3:run
```

O primeiro uso pode exigir conexão com a internet para baixar o Gradle e dependências. Se o VS Code mostrar erro no botão Play, execute pelo Gradle como acima, pois `Lwjgl3Launcher` depende do classpath preparado pelo projeto.

## Controles

- **Setas ou WASD:** mover o jogador (círculo azul).
- **Clique na tela:** mover o NPC rosa até o destino (Seek e depois Arrive).
- **Aproxime o jogador do NPC:** quando estão a menos de 3,2 unidades, o NPC entra em `PERSEGUINDO` e usa `Pursuit`.
- **Afaste o jogador:** a partir de 5 unidades, o NPC volta a `EXPLORANDO` e usa `Wander`.
- **C:** cancelar o destino atual e voltar a explorar.
- **B:** alternar para a tela do bônus com 30 agentes (Flocking).
- **V:** mostrar/ocultar os vetores de velocidade (verde) e força (amarelo).
- **R:** reiniciar as posições do jogador e do NPC.

## Entendendo a FSM

```text
EXPLORANDO --clique--> INDO_AO_CLIQUE --chegou--> PARADO
     ^                                               |
     +------------------- tecla C -------------------+
     | 
     +---- jogador longe (> 5 unidades) ---- PERSEGUINDO
                                       ^
                                       |
                   jogador perto (< 3,2 unidades)
```

Os três estados servem para decidir qual comportamento de movimentação utilizar. Quando ocorre um clique, esse comando tem prioridade até o NPC chegar ao destino. Ao chegar ele entra em `PARADO` e espera outro clique ou a tecla `C`.

## O que falar na gravação (até 1 minuto)

- **0–10 s:** "Criei uma classe Agente com vetores de posição e velocidade, força e velocidade máximas."
- **10–25 s:** Clique na tela: "Com Seek o NPC vai até o alvo; com Arrive ele desacelera perto do ponto."
- **25–40 s:** Mova o jogador perto e longe: "A máquina de estados alterna entre Wander, quando está explorando, e Pursuit, quando vê o jogador."
- **40–53 s:** Pressione `B`: "No bônus, 30 agentes combinam separação, alinhamento e coesão para formar um bando."
- **53–60 s:** Mostre a tela final e finalize.

## Arquivos principais

- `core/src/main/java/br/mackenzie/Main.java`: jogo e controle das demonstrações.
- `core/src/main/java/br/mackenzie/Agente.java`: cálculos dos comportamentos de IA.
- `lwjgl3/src/main/java/br/mackenzie/lwjgl3/Lwjgl3Launcher.java`: inicia a janela desktop.

**Observação:** a nave do modelo original foi substituída por círculos desenhados com `ShapeRenderer`. Assim, a atividade roda sem depender de `nave_rosa.jfif`, que não estava na pasta `assets` do material enviado. Você pode colocar imagens depois, mas não são necessárias para atender à atividade.

**Entrega pedida no slide:** código no repositório e vídeo de até 1 minuto.
