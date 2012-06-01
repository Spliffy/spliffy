<#import "defaultLayout.ftl" as layout>
<@layout.myLayout "Manage users" "userAdmin">
<h1>Manage users for ${page.organisation.name}</h1>
<p>Change organisation</p>
<#list page.childOrganisations as child>
<a href="${child.name}">${child.name}</a>
</#list>
<table>
    <thead>
        <tr>
            <th>First Name</th>
            <th>Last Name</th>
            <th>Email</th>
            <th>Contact</th>
            <th>State</th>
        </tr>        
    </thead>
    <tbody>
        <#list page.searchResults as u>
        <tr>
            <td>${u.firstName!}</td>
            <td>${u.surName!}</td>
            <td>${u.email}</td>
            <td>${u.phone!}</td>
            <td>${u.state!}</td>
        </tr>
        </#list>
    </tbody>
</table>
</@layout.myLayout>