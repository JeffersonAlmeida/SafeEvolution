package br.edu.ufcg.dsc;
/**
@author Jefferson Almeida - jra at cin dot ufpe dot br
  
<p>Enum Approach possui as abordagens que s�o utilizadas para avaliar a evolu��o da linha.</p>
 <h3><strong>Abordagens</strong></h3>	
<strong>NAIVE_2_ICTAC - ALL PRODUCT PAIRS : </strong> Esta abordagem tenta diretamente aproximar a condi��o expressa pela defini��o de refinamento,
procurando por produtos correspondentes com comportamento observ�vel compat�veis.<br></br>
1 - Primeiro verifica se todos os produtos da LPS original e da LPS Target s�o bem formados. <br></br>
2 - Depois,  � mapeado os produtos do source com os seus correspondentes produtos.<br></br>
3 - Verifica se os produtos e seus produtos correspondentes tem comportamento observado compat�vel usando o SafeRefactor.<br></br>
4 - Se eles n�o forem correspondentes, ent�o ser� gerados todos os produtos na LPS target verificando se existem produtos com
comportamento observ�vel compat�vel.<br></br> 
<strong>NAIVE_1_APROXIMACAO - ALL PRODUCTS : </strong> � bem similar � abordagem APP , entretanto tem uma diferen�a no passo 4.
Quando alguns produtos e seus respectivos produtos target n�o possuem comportamento observ�vel compat�veis,
a abordagem assume um n�o refinamento e n�o compara o produto da SPL original com outros produtos da SPL Target.
<br></br> 
<h3><strong>Abordagens Otimizadas</strong></h3>	
<strong>ONLY_CHANGED_CLASSES - IMPACTED CLASSES : </strong>Primeiro, essa abordagem identifica os assets modificados usando an�lise sint�tica.
Para cada asset modificado, a abordagem computa suas depend�ncias usando an�lise de fluxo de dados,
ou seja, identifica o conjunto de assets necess�rios para compilar o asset modificado.
Ent�o, � checado, para cada sub produtos, se eles tem comportamento observ�vel compat�veis, gerando testes somente para classe
afetadas com a mudan�a.<br></br> 
<strong>IMPACTED_FEATURES - IMPACTED PRODUCTS : </strong> Somente avalia a preserva��o de comportamento para produtos que cont�m assets que foram mudados,
desde que os demais produtos s�o exatamente os mesmos, ou seja t�m os mesmos comportamentos.
Checando quais produtos cont�m esses nomes de assets, � identificado os produtos impactados pela mudan�a.
Ent�o, s�o geradas e avaliadas produtos da SPL source contendo pelo menos um asset alterado.<br></br>
 */
public enum Approach {
	NAIVE_2_ICTAC, NAIVE_1_APROXIMACAO, ONLY_CHANGED_CLASSES, IMPACTED_FEATURES, EIC;
}
