<#macro baseLayout title="Home" bodyClass="">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="Robots" content="index,follow" />
        <meta name="keywords" content="" />
        <link rel="stylesheet" type="text/css" href="/_static/themes/yellow/style.css" media="screen" />
        <link rel="stylesheet" type="text/css" href="/_static/base.css" media="screen" />
        <link rel="stylesheet" type="text/css" href="/_static/common.css" media="screen" />
        <link rel="stylesheet" type="text/css" href="/_static/validation.css" media="screen" />
        <link rel="stylesheet" type="text/css" href="/_static/jquery-ui-1.8.11.custom.css" media="screen" />        
        <link rel="stylesheet" type="text/css" href="/_static/fullcalendar.css" media="screen" />        
        <link rel="stylesheet" type="text/css" href="/_static/fullcalendar.print.css" media="print" />        

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
    <body class="blog two-column right-sidebar ${bodyClass}">
        <nav id="access" role="navigation">
            <h3 class="assistive-text">Main menu</h3>
            <div class="menu-yellow-container">
                <ul id="menu-yellow" class="menu">
                    <#if page.currentUser??>

                    <li class="nav-dashboard menu-item menu-item-type-custom menu-item-object-custom menu-item-home">
                        <a href="/${user.name}/">Dashboard</a>
                    </li>
                    <li class="nav-calendar menu-item menu-item-type-post_type menu-item-object-page">
                        <a href="/${user.name}/cal/" shape="rect">Calendar</a>
                    </li>
                    <li class="nav-contacts menu-item menu-item-type-post_type menu-item-object-page">
                        <a href="/${user.name}/abs/" shape="rect">Contacts</a>
                    </li>
                    <li class="nav-sharing menu-item menu-item-type-post_type menu-item-object-page">
                        <a href="/${user.name}/sharing/" shape="rect">Sharing</a>
                    </li>
<!--
                    <li id="menu-item-694" class="menu-item menu-item-type-post_type menu-item-object-page menu-item-694">
                        <a href="http://demo.wpcharity.com/yellow/?page_id=143">Archives</a>
                        <ul class="sub-menu">
                            <li id="menu-item-690" class="menu-item menu-item-type-post_type menu-item-object-page menu-item-690">
                                <a href="/${user.name}/cal/" shape="rect">Calendar</a>
                            </li>
                            <li id="menu-item-692" class="menu-item menu-item-type-post_type menu-item-object-page menu-item-692">
                                <a href="/${user.name}/abs/" shape="rect">Contacts</a>
                            </li>
                            <li id="menu-item-691" class="menu-item menu-item-type-post_type menu-item-object-page menu-item-691">
                                <a href="/${user.name}/sharing/" shape="rect">Sharing</a>
                            </li>
                        </ul>
                    </li>
-->
                    <#else>
                    <li class="nav-home menu-item menu-item-type-custom menu-item-object-custom current_page_item menu-item-home menu-item-688">
                        <a href="/">Home</a>
                    </li>
                    
                    <li class="menu-item menu-item-type-custom menu-item-object-custom current_page_item menu-item-home menu-item-688">
                        <a href="/login">Login</a>
                    </li>
                    <li class="nav-register menu-item menu-item-type-post_type menu-item-object-page menu-item-689">
                        <a href="/signup">Register</a>
                    </li>

                    </#if>                        

                </ul>
            </div>			

        </nav>
        <!-- #access -->
        <div class="clear"></div>

        <div id="page" class="hfeed">

                <#nested/>

        </div>

        <footer id="colophon" role="contentinfo">

            <div id="supplementary" class="three">
                <div id="first" class="widget-area" role="complementary">
                    <aside id="text-2" class="widget widget_text"><h3 class="widget-title">About</h3>			
                        <div class="textwidget">Spliffy is a personal cloud server, for accessing your files, calendars and contacts from any device.</div>
                    </aside>
                    <aside id="search-2" class="widget widget_search">
                        <h3 class="widget-title">Search</h3>	
                        <form method="get" id="searchform" action="http://demo.wpcharity.com/yellow/">
                            <label for="s" class="assistive-text">Search</label>
                            <input type="text" class="field" name="s" id="s" placeholder="Search" />
                            <input type="submit" class="submit" name="submit" id="searchsubmit" value="Search" />
                        </form>
                    </aside>	
                </div><!-- #first .widget-area -->

            </div>
            <div id="site-generator">
                <div id="site-info">

                    <a class="wordpress" href="http://spliffy.org">Spliffy</a>
                    <a class="name" href="http://demo.wpcharity.com/yellow/" title="Yellow!" rel="home">
                        Yellow!				
                    </a>
                </div><!-- #site-info -->
            </div><!-- site-generator -->
        </footer><!-- #colophon -->


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



<#macro myLayout title="Home" bodyClass="">
<@layout.baseLayout title bodyClass>
<div id="main">
    <div id="primary">
        <div id="content" role="main">
            
<#nested/>
           
        </div>
    </div>
</div>
</@layout.baseLayout>
</#macro>



<#macro dirLayout title="Home" bodyClass="">
<@layout.myLayout title bodyClass>
<h1>${title}</h1>
<div class="breadcrumbs">
<@breadCrumbs node=page path="."/>
</div>

<#nested/>

<table class="data">
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