<!-- Home page for the spliffy instance -->
<#import "defaultLayout.ftl" as layout>
<@layout.myLayout "Home" "home">

<header id="branding" role="banner">
    <hgroup>
        <h1 id="site-title"><span><a href="http://spliffy.org" title="Yellow!" rel="home">Spliffy!</a></span></h1>
        <h2 id="site-description">Admin console</h2>
        <h2 id="site-description">Manage your users and websites here!</h2>
    </hgroup>
</header>

<div id="main">
    <div id="primary">
        <div id="content" role="main">
            <ul>
                <li><img src="/_static/home/folder.png"/></li>

                <li><img src="/_static/home/contact.png"/></li>

                <li><img src="/_static/home/calendar.png"/></li>
            </ul>

        </div>
    </div>
</div>


</@layout.myLayout>