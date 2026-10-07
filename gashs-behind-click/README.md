# Gash's Behind Click

Mod client-side para Minecraft 1.21.1 com NeoForge 21.1.x.

## Uso

Segure o modificador configurado, por padrao `Alt esquerdo`, e pressione o botao
direito. O primeiro bloco sob a mira e ignorado, e a interacao normal e redirecionada
ao proximo bloco atingido pela mesma linha de mira dentro do alcance do jogador.

Se nao houver um segundo bloco, o clique e cancelado. O clique direito sem o
modificador nao e alterado.

O atalho aparece nos controles do Minecraft e pode ser remapeado.

## Compatibilidade

- Cada jogador que instalar o mod pode usar a funcao.
- O mod nao registra blocos, itens, menus, payloads ou canais de rede.
- Um cliente sem o mod pode entrar normalmente em um host ou servidor que possua o JAR.
- O servidor nao precisa ter o mod para aceitar o clique, pois a interacao enviada usa
  o pacote normal do Minecraft.

## Build local

O build usa somente as bibliotecas ja instaladas pelo CurseForge.

```powershell
.\build.ps1
```

Para compilar e instalar o JAR na pasta `mods` desta instancia:

```powershell
.\build.ps1 -Install
```
