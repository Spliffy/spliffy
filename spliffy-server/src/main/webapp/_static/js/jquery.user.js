/**
 *
 *  jquery.login.js
 *  
 *  Depends on user.js and common.js
 *  
 *  The target should be a div containing
 *  - a form
 *  - <p> with id validationMessage
 *  - input type text for the username
 *  - input type password for the password
 *
 * Config:
 * urlSuffix: is appended to the current page url to make the url to POST the login request to. Default /.ajax
 * afterLoginUrl: the page to redirect to after login. Default index.html.  3 possibilities 
 *      null = no redirect
 *      "something" or "" = a relative path, will be avaluated relative to the user's url (returned in cookie)
 *      "/dashboard" = an absolute path, will be used exactly as given
 * 
 */

(function( $ ) {
    $.fn.user = function(options) {
        log("init login plugin2", this);
        initUser();
        var config = $.extend( {
            urlSuffix: "/.ajax",
            afterLoginUrl: "index.html",
            logoutSelector: ".logout",
            valiationMessageSelector: "#validationMessage",
            loginFailedMessage: "Sorry, those login details were not recognised"
        }, options);  
  
        $(config.logoutSelector).click(function() {
            doLogout();
        });
  
        var container = this;
        $("form", this).submit(function() {
            log("login", window.location);
            
            $(config.valiationMessageSelector, this).hide(100);
            try {
                $.ajax({
                    type: 'POST',
                    url: config.urlSuffix,
                    data: {
                        _loginUserName: $("input[type=text]", container).val(),
                        _loginPassword: $("input[type=password]", container).val()
                    },
                    dataType: "json",
                    success: function(resp) {
                        log("login success", resp)
                        initUser();                
                        if( userUrl ) {
                            if( config.afterLoginUrl == null) {
                                // do nothing
                            } else if( config.afterLoginUrl.startsWith("/")) {
                                //alert("would redirect to: " + config.afterLoginUrl);
                                //return;
                                window.location = config.afterLoginUrl;
                            } else {
                                //alert("would redirect to: " + userUrl + config.afterLoginUrl);
                                //return;
                                window.location = userUrl + config.afterLoginUrl;
                            }
                        } else {
                            // null userurl, so login was not successful
                            $(config.valiationMessageSelector, container).text(config.loginFailedMessage);
                            log("set message", $(config.valiationMessageSelector, this), config.loginFailedMessage);
                            $(config.valiationMessageSelector, container).show(100);                            
                        }
                        //window.location = "/index.html";
                    },
                    error: function(resp) {
                        alert("err");
                        $(config.valiationMessageSelector, container).text(config.loginFailedMessage);
                        log("set message", $(config.valiationMessageSelector, this), config.loginFailedMessage);
                        $(config.valiationMessageSelector, container).show(100);
                    }
                });                
            } catch(e) {
                log("exception sending forum comment", e);
            }            
            return false;
        });    
    };
})( jQuery );

/** End jquery.login.js */