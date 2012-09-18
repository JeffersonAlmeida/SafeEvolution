package br.edu.ufcg.dsc;
/**
@author Jefferson Almeida - jra at cin dot ufpe dot br
  
<p>Enum Approach possui as abordagens que são utilizadas para avaliar a evolução da linha.</p>
 <h3><strong>Abordagens</strong></h3>	
<strong>NAIVE_2_ICTAC - ALL PRODUCT PAIRS : </strong> Esta abordagem tenta diretamente aproximar a condição expressa pela definição de refinamento,
procurando por produtos correspondentes com comportamento observável compatíveis.<br></br>
1 - Primeiro verifica se todos os produtos da LPS original e da LPS Target são bem formados. <br></br>
2 - Depois,  é mapeado os produtos do source com os seus correspondentes produtos.<br></br>
3 - Verifica se os produtos e seus produtos correspondentes tem comportamento observado compatível usando o SafeRefactor.<br></br>
4 - Se eles não forem correspondentes, então será gerados todos os produtos na LPS target verificando se existem produtos com
comportamento observável compatível.<br></br> 
<strong>NAIVE_1_APROXIMACAO - ALL PRODUCTS : </strong> É bem similar à abordagem APP , entretanto tem uma diferença no passo 4.
Quando alguns produtos e seus respectivos produtos target não possuem comportamento observável compatíveis,
a abordagem assume um não refinamento e não compara o produto da SPL original com outros produtos da SPL Target.
<br></br> 
<h3><strong>Abordagens Otimizadas</strong></h3>	
<strong>ONLY_CHANGED_CLASSES - IMPACTED CLASSES : </strong>Primeiro, essa abordagem identifica os assets modificados usando análise sintática.
Para cada asset modificado, a abordagem computa suas dependências usando análise de fluxo de dados,
ou seja, identifica o conjunto de assets necessários para compilar o asset modificado.
Então, é checado, para cada sub produtos, se eles tem comportamento observável compatíveis, gerando testes somente para classe
afetadas com a mudança.<br></br> 
<strong>IMPACTED_FEATURES - IMPACTED PRODUCTS : </strong> Somente avalia a preservação de comportamento para produtos que contém assets que foram mudados,
desde que os demais produtos são exatamente os mesmos, ou seja têm os mesmos comportamentos.
Checando quais produtos contém esses nomes de assets, é identificado os produtos impactados pela mudança.
Então, são geradas e avaliadas produtos da SPL source contendo pelo menos um asset alterado.<br></br>
 */
public enum Approach {
	NAIVE_2_ICTAC, NAIVE_1_APROXIMACAO, ONLY_CHANGED_CLASSES, IMPACTED_FEATURES;
}
