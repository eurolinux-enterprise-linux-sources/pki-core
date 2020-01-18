<!-- --- BEGIN COPYRIGHT BLOCK ---
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; version 2 of the License.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License along
     with this program; if not, write to the Free Software Foundation, Inc.,
     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

     Copyright (C) 2013 Red Hat, Inc.
     All rights reserved.
     --- END COPYRIGHT BLOCK --- -->
<html>
<head>
    <title>Token Processing System</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="/pki/css/patternfly.css" rel="stylesheet" media="screen, print">
    <script src="/pki/js/jquery.js"></script>
    <script src="/pki/js/jquery.i18n.properties.js"></script>
    <script src="/pki/js/underscore.js"></script>
    <script src="/pki/js/backbone.js"></script>
    <script src="/pki/js/bootstrap.js"></script>
    <script src="/pki/js/pki.js"></script>
    <script src="/pki/js/pki-banner.js"></script>
    <script src="/tps/js/account.js"></script>
    <script>
$(function() {
    var account = new Account();
    $("form").submit(function(e) {
        account.login({
            success: function() {
                window.location.href = "/tps/ui";
            },
            error: function() {
                PKI.logout();
            }
        });
        e.preventDefault();
    });
});
    </script>
</head>
<body>

<nav id="navigation" class="navbar navbar-default navbar-pf" role="navigation">
<div class="navbar-header">
    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse-1">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
    </button>
    <a class="navbar-brand" href="/tps">
        <b>Token Processing Service</b>
    </a>
</div>
</nav>

<div id="content">

<form action="/tps/ui">
<div class="col-sm-5 col-md-6 col-lg-7 details">
<p><strong>Welcome to the Dogtag Token Processing System 10.3</strong>
<p>The Token Processing System (TPS) is the conduit between the user-centered Enterprise Security Client,
which interacts with the tokens, and the Certificate System backend subsystems, such as the Certificate Manager.
</p>
<button type="submit" class="btn btn-primary btn-lg" tabindex="4">Log In</button>
</div>
</form>

</div>

</body>
</html>
