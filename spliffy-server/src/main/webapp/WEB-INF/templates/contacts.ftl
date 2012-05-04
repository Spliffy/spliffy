<#import "defaultLayout.ftl" as layout>
<@layout.myLayout "Contacts">

<@layout.breadCrumbs node=page path="."/>

<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Telephone</th>
            <th>organisation</th>
        </tr>
    </thead>
    <tbody>
        <#list page.children as x>
        <tr>
            <td>${x.formattedName}</td> 
            <td>${x.email!}</td>
            <td>${x.telephone!}</td>
            <td>${x.organisation!}</td>
        </tr>
        </#list>  
    </tbody>
</table>

</@layout.myLayout>