<!-- Home page for the spliffy instance -->
<#import "defaultLayout.ftl" as layout>
<@layout.myLayout "Home" false>

<h1>Welcome to spliffy!</h1>

<a href="login">Click here to login and go to your dashboard</a>

<br/><br/>

<a href="/signup">Or click here to create a new account</a>

</@layout.myLayout>