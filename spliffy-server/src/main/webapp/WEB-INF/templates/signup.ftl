<#import "defaultLayout.ftl" as layout>
<@layout.myLayout "Signup">

<h1>Register a spliffy account</h1>

<form id="signup" method="POST" action="${page.name}" onsubmit="doSignup()">
    <fieldset>
        <legend>Please enter your login details</legend>
        <label for="name">User name</label>
        <input type="text" name="name" id="name" class="required" />
        <br/>

        <label for="password">Password</label>
        <input type="password" name="password" id="password" class="required" />
        <br/>

        <label for="confirmPassword">Confirm password</label>
        <input type="password" name="confirmPassword" id="confirmPassword" class="required" />
        <br/>

        <label for="email">Email</label>
        <input type="text" name="email" id="email" class="required" />
        <br/>

        <button>Signup now!</button>



    </fieldset>

    <button type="button" onclick="doSignup()">Signup now!</button>

</form>

<script type="text/javascript">
    function doSignup() {
        resetValidation();
        var container = $("#signup");
        if( !checkRequiredFields(container)) {
            return false;
        }
        try {
            $.ajax({
                type: 'POST',
                url: "${page.name}",
                data: container.serialize(),
                dataType: "json",
                success: function(resp) {
                    if( resp.status ) {
                        log("save success", resp)
                        //window.location.href = resp.nextUrl;
                    } else {
                        alert("Failed: " + resp.messages);
                    }
                },
                error: function(resp) {
                    alert("err");
                    $(config.valiationMessageSelector, container).text(config.loginFailedMessage);
                    log("set message", $(config.valiationMessageSelector, this), config.loginFailedMessage);
                    $(config.valiationMessageSelector, container).show(100);
                }
            });                
        } catch(e) {
            ajaxLoadingOff();
            log("exception sending forum comment", e);
        }        
        return false;
    }
</script>

</@layout.myLayout>