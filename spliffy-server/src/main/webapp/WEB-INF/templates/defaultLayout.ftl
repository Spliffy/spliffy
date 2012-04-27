<#macro myLayout title="Home" showNav=true>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="Robots" content="index,follow" />
        <meta name="keywords" content="" />
        <link rel="stylesheet" type="text/css" href="/_static/theme.css" media="screen" />
        <link rel="stylesheet" type="text/css" href="/_static/base.css" media="screen" />
        <link rel="stylesheet" type="text/css" href="/_static/validation.css" media="screen" />
        <link rel="stylesheet" type="text/css" href="/_static/jquery-ui-1.8.11.custom.css" media="screen" />        

        <script type="text/javascript" src="/_static/js/10_jquery.js" >//</script>
        <script type="text/javascript" src="/_static/js/11_jquery.cookie.js" >//</script>
        <script type="text/javascript" src="/_static/js/common.js" >//</script>
        <script type="text/javascript" src="/_static/js/file.js" >//</script>
        <script type="text/javascript" src="/_static/js/jquery-ui-1.8.11.custom.min.js" >//</script>
        <script type="text/javascript" src="/_static/js/jquery.autoresize.js" >//</script>
        <script type="text/javascript" src="/_static/js/jquery.comments.js" >//</script>
        <script type="text/javascript" src="/_static/js/jquery.user.js" >//</script>
        <script type="text/javascript" src="/_static/js/types.js" >//</script>
        <script type="text/javascript" src="/_static/js/uploads.js" >//</script>
        <script type="text/javascript" src="/_static/js/user.js" >//</script>
        <script type="text/javascript" src="/_static/js/validation.js" >//</script>

        <script type="text/javascript" src="/_static/js/jquery.pjax.js" >//</script>        

        <script type="text/javascript" src="/_static/js/theme.js" >//</script>        

        <title> ${title}</title>        
    </head>
    <body>
        <!--wrapper-->
        <div class="wrapper">
            <!--header-->
            <div class="header">
                <!--logo-->
                <h1 class="logo"><a href="/index.html">Spliffy</a></h1>
                <!--headRight-->
                <div class="headRight">
                    <div class="formBox">
                        <div class="userBtn Login"> 
                            

                            <#if page.currentUser??>
                            <a class="Link" id="currentuser" href="#">Hi ${page.currentUser.name}</a>
                            <#else>
                            <a class="Link" href="/login">Login</a>
                            </#if>
                            <div class="dropBox">
                                <#if page.currentUser??>
                                <ul class="list">
                                    <li>
                                        <a class="logout">Logout</a> <!-- cant logout while using digest/basic auth -->
                                    </li>
                                </ul>
                                <#else>
                                <ul class="list sansuser">
                                    <li>
                                        <a href="/register.html">Register</a>
                                    </li>
                                </ul>      
                                <form method="post" action="" class="sansuser">
                                    <fieldset>
                                        <p id="validationMessage" style="display: none">.</p>
                                        <label for="email">Email</label>
                                        <input type="text" id="email" name="email" value="" class="Textbox" title="Email address" />
                                        <label for="password">Password</label>
                                        <input type="password" name="password" id="password" value="" class="Textbox" title="Password" />
                                        <button class="Login Button" ><span>Login</span></button>                                     
                                        <a href="/password-reset.html" title="Forgotten password" class="Forgot">Forgotten password</a>                                        
                                    </fieldset>
                                </form>
                                </#if>
                                <div class="accessBox">
                                    <div class="greyBox FontSize"> 
                                        <a class="linkText ZoomIn" href="#">A</a> 
                                        <a class="linkText linkTxt ZoomOut" href="#">A</a> 
                                        <a class="circle ZoomReset" href="#"><img src="/_static/images/circle.gif" alt="" /></a>
                                        <div class="clr"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!--
                        <div class="divider"></div>                        
                        <form action="http://www.google.com/search" method="get">
                            <fieldset>
                                <input type="hidden" name="domains" value="$targetPage.host.name"/>
                                <input type="hidden" name="sitesearch" value="$targetPage.host.name" />
                                <div class="inputBox">
                                    <input class="input" type="text" name="q" placeholder="Search"/>
                                    <button class="goBtn" name="btnG" type="submit">Go</button> 
                                </div>    
                            </fieldset>
                            <div class="clr"></div>
                        </form>
                        -->
                        <div class="clr"></div>
                    </div>
                </div>

                <div class="clr"></div>
                <!--nav-->
                <#if showNav>
                <div class="nav">                    
                    <ul>
                        <li class="nav-myDashboard">
                            <a href="/dashboard" shape="rect">Dashboard</a>
                        </li>                    
                        <li class="nav-myDashboard">
                            <a href="/files" shape="rect">Files</a>
                        </li>                    
                        <li class="nav-myDashboard">
                            <a href="/photos" shape="rect">Photos</a>
                        </li>                    
                        <li class="nav-myDashboard">
                            <a href="/music" shape="rect">Music</a>
                        </li>                    
                        <li class="nav-myDashboard">
                            <a href="/music" shape="rect">Videos</a>
                        </li>           
                        <li class="nav-myDashboard">
                            <a href="/music" shape="rect">Calendar</a>
                        </li>                              
                        <li class="nav-myDashboard">
                            <a href="/music" shape="rect">Contacts</a>
                        </li>                              
                        <li class="nav-myDashboard">
                            <a href="/music" shape="rect">Sharing</a>
                        </li>                              
                        
                    </ul>  
                    <div class="clr">.</div>
                </div>
                </#if>
                <!--navEnd-->
            </div>
            <!--headerEnd-->
            <!--content-->
            <div class="content">
                                               
                <#nested/>

                <div class="clr"></div>
            </div>
            <!--contentEnd-->
            <!--footer-->
            <div class="footerOuter">
                <div class="footer">
                    <ul class="fList">
                        <li>

                        </li>                    
                    </ul>
                    <div class="clr">.</div>      
                </div>
            </div>
            <!--footerEnd-->
        </div>
        <!--wrapperEnd-->
        <div id="ajaxLoading" style="display: none">
            <div>
                Processing, please wait...
                <img src="/_static/ajax-loader.gif" alt="Processing..." />
            </div>
        </div>
        <div id="thankyou" style="display: none">
            <h3>Thankyou</h3>
            <p>Message</p>
        </div>



        <script type="text/javascript">
            jQuery(function($) {
                initTheme();
            });            
        </script>
    </body>    
</html> 



</#macro>

<#macro dirLayout title="Home">
<@layout.myLayout title>

<@breadCrumbs node=page path="."/>

<#nested/>

<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Modified</th>
            <th>Created</th>
        </tr>
    </thead>
    <tbody>
        <#list page.children as x>
        <tr>
            <td>
                <#if x.dir >
                <a href="${x.name}/">${x.name}</a>
                <#else>
                <a href="${x.name}">${x.name}</a>
                </#if>
            </td>
            <td>${x.modifiedDate!}</td>
            <td>${x.createdDate!}</td>
        </tr>
        </#list>  
    </tbody>
</table>

</@layout.myLayout>
</#macro>





<#macro breadCrumbs node path>
<#if node.parent??>
    <#assign p = path + "/..">  
    <@breadCrumbs node=node.parent path=p/>
    / <a href="${path}">${node.name}</a>
<#else>
    / <a href="/">home</a>
</#if>




</#macro>