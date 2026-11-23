# Gash's Behind Click

Mod de cliente para Minecraft 1.20.1 (Forge), 1.21.1, 26.1.2 e 26.3 (NeoForge).

## Controles

| Atalho padrão | Ação |
| --- | --- |
| Alt esquerdo + clique direito | Faz um clique normal no próximo bloco atrás do primeiro, respeitando o item na mão e o estado de agachamento. |
| Alt direito + clique direito | Interage apenas com o bloco de trás, usando um espaço vazio da barra rápida. Funciona mesmo enquanto você está agachado. Se a barra estiver cheia, mostra um aviso. |
| Alt + Numpad Enter | **Accelerate Behind** aciona a aceleração do Timelesss no bloco de trás. Aparece com o Timelesss instalado no cliente. A aceleração exige o Timelesss também no servidor, ou no mundo solo, e segue suas regras normais de item e recursos. |

Os atalhos podem ser remapeados no menu de controles. Sem um segundo bloco dentro
do alcance, o atalho não aciona nenhuma interação. O clique direito comum funciona
como sempre. O Behind Click só precisa ser instalado no cliente, e o Timelesss é
opcional.

## Compilação

Na raiz do projeto, use o wrapper Forge com JDK 17 ou 21 e o wrapper NeoForge com
JDK 25. Para o alvo 1.21.1, disponibilize também o JDK 21 para a toolchain.

```powershell
.\forge-1.20.1\gradlew.bat -p forge-1.20.1 build
.\neoforge-26.3\gradlew.bat -p neoforge-1.21.1 build
.\neoforge-26.3\gradlew.bat -p neoforge-26.1.2 build
.\neoforge-26.3\gradlew.bat -p neoforge-26.3 build
```

Cada JAR é gerado em `build/libs` do alvo correspondente.
