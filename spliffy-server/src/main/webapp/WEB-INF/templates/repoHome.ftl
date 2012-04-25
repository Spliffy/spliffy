<#import "defaultLayout.ftl" as layout>
<@layout.dirLayout "User home">
listing...
	

<#list page.children as x>
  ${x.name}
</#list>  

</@layout.dirLayout>