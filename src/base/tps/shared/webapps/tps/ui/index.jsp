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
    <link href="/pki/css/pki-ui.css" rel="stylesheet" type="text/css">
    <script src="/pki/js/jquery.js"></script>
    <script src="/pki/js/underscore.js"></script>
    <script src="/pki/js/backbone.js"></script>
    <script src="/pki/js/bootstrap.js"></script>
    <script src="/pki/js/patternfly.js"></script>
    <script src="/pki/js/pki.js"></script>
    <script src="/pki/js/pki-ui.js"></script>
    <script src="/pki/js/pki-banner.js"></script>
    <script src="/tps/js/tps.js"></script>
    <script src="/tps/js/account.js"></script>
    <script src="/tps/js/activity.js"></script>
    <script src="/tps/js/audit.js"></script>
    <script src="/tps/js/authenticator.js"></script>
    <script src="/tps/js/cert.js"></script>
    <script src="/tps/js/config.js"></script>
    <script src="/tps/js/connector.js"></script>
    <script src="/tps/js/group.js"></script>
    <script src="/tps/js/profile.js"></script>
    <script src="/tps/js/profile-mapping.js"></script>
<!--
    <script src="/tps/js/selftest.js"></script>
-->
    <script src="/tps/js/token.js"></script>
    <script src="/tps/js/user.js"></script>
    <script>
$(function() {

    function getAttribute(attributes, name) {
        for (var i=0; i<attributes.length; i++) {
            var attribute = attributes[i];
            if (name != attribute.name) continue;
            return attribute.value;
        }
        return null;
    }

    function getElementName(component) {

        if (component == "Generals") {
            return "config";

        } else if (component == "Authentication_Sources") {
            return "authenticators";

        } else if (component == "Subsystem_Connections") {
            return "connectors";

        } else if (component == "Profiles") {
            return "profiles";

        } else if (component == "Profile_Mappings") {
            return "profile-mappings";

        } else if (component == "Audit_Logging") {
            return "audit";

        } else {
            return null;
        }
    }

    var account = new Account();
    account.login({
        success: function(data, textStatus, jqXHR) {
            tps.user = data;
            var roles = tps.user.Roles.Role;

            var user = $("#navigation [name=account] [name=username]");
            user.text(data.FullName);

            var accounts_menu = $("#navigation [name=accounts]");
            var system_menu = $("#navigation [name=system]");

            if (_.contains(roles, "Administrators")) {
                accounts_menu.show();
            } else {
                accounts_menu.hide();
            }

            var attributes = tps.user.Attributes.Attribute;
            var values = getAttribute(attributes, "components");

            var components;
            if (values) {
                components = values.split(",");
            } else {
                components = [];
            }

            if (components.length > 0) {
                // display menu items for accessible components
                system_menu.show();
                for (var i=0; i<components.length; i++) {
                    var name = getElementName(components[i]);
                    if (!name) continue;
                    $("[name=" + name + "]", system_menu).show();
                }

            } else {
                system_menu.hide();
            }

            // homePage.update();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            window.location.href = "/tps";
        }
    });

    var content = $("#content");

    var router = new Backbone.Router();
/*
    var homePage = new HomePage({
        el: content,
        url: "home.html"
    });
*/
    var tokensPage = new TokensPage({
        el: content,
        url: "tokens.html"
    });

    router.route("", "home", function() {
        // homePage.open();
        tokensPage.open();
    });

    router.route("activities", "activities", function() {
        new ActivitiesPage({
            el: content,
            url: "activities.html"
        }).open();
    });

    router.route("activities/:id", "activity", function(id) {
        new ActivityPage({
            el: content,
            url: "activity.html",
            model: new ActivityModel({ id: id })
        }).open();
    });

    router.route("audit", "audit", function() {
        new AuditPage({
            el: content,
            url: "audit.html"
        }).open();
    });

    router.route("authenticators", "authenticators", function() {
        new AuthenticatorsPage({
            el: content,
            url: "authenticators.html"
        }).open();
    });

    router.route("authenticators/:id", "authenticator", function(id) {
        new ConfigEntryPage({
            el: content,
            url: "authenticator.html",
            model: new AuthenticatorModel({ id: id })
        }).open();
    });

    router.route("new-authenticator", "new-authenticator", function() {
        new ConfigEntryPage({
            el: content,
            url: "authenticator.html",
            model: new AuthenticatorModel(),
            mode: "add",
            title: "New Authenticator",
            editable: ["authenticatorID"],
            parentHash: "#authenticators"
        }).open();
    });

    router.route("certs", "certs", function() {
        new CertificatesPage({
            el: content,
            url: "certs.html",
            collection: new CertificateCollection()
        }).open();
    });

    router.route("certs/:id", "cert", function(id) {
        new CertificatePage({
            el: content,
            url: "cert.html",
            model: new CertificateModel({ id: id })
        }).open();
    });

    router.route("config", "config", function() {
        new ConfigPage({
            el: content,
            url: "config.html"
        }).open();
    });

    router.route("connectors", "connectors", function() {
        new ConnectorsPage({
            el: content,
            url: "connectors.html"
        }).open();
    });

    router.route("connectors/:id", "connector", function(id) {
        new ConfigEntryPage({
            el: content,
            url: "connector.html",
            model: new ConnectorModel({ id: id })
        }).open();
    });

    router.route("new-connector", "new-connector", function() {
        new ConfigEntryPage({
            el: content,
            url: "connector.html",
            model: new ConnectorModel(),
            mode: "add",
            title: "New Connector",
            editable: ["connectorID"],
            parentHash: "#connectors"
        }).open();
    });

    router.route("groups", "groups", function() {
        new GroupsPage({
            el: content,
            url: "groups.html"
        }).open();
    });

    router.route("groups/:id", "group", function(id) {
        new GroupPage({
            el: content,
            url: "group.html",
            model: new GroupModel({ id: id }),
            editable: ["description"]
        }).open();
    });

    router.route("new-group", "new-group", function() {
        new GroupPage({
            el: content,
            url: "group.html",
            model: new GroupModel(),
            mode: "add",
            title: "New Group",
            editable: ["groupID", "description"],
            parentHash: "#groups"
        }).open();
    });

    router.route("profiles", "profiles", function() {
        new ProfilesPage({
            el: content,
            url: "profiles.html"
        }).open();
    });

    router.route("profiles/:id", "profile", function(id) {
        new ConfigEntryPage({
            el: content,
            url: "profile.html",
            model: new ProfileModel({ id: id })
        }).open();
    });

    router.route("new-profile", "new-profile", function() {
        new ConfigEntryPage({
            el: content,
            url: "profile.html",
            model: new ProfileModel(),
            mode: "add",
            title: "New Profile",
            editable: ["profileID"],
            parentHash: "#profiles"
        }).open();
    });

    router.route("profile-mappings", "profile-mappings", function() {
        new ProfileMappingsPage({
            el: content,
            url: "profile-mappings.html"
        }).open();
    });

    router.route("profile-mappings/:id", "profile-mapping", function(id) {
        new ConfigEntryPage({
            el: content,
            url: "profile-mapping.html",
            model: new ProfileMappingModel({ id: id }),
        }).open();
    });

    router.route("new-profile-mapping", "new-profile-mapping", function() {
        new ConfigEntryPage({
            el: content,
            url: "profile-mapping.html",
            model: new ProfileMappingModel(),
            mode: "add",
            title: "New Profile Mapping",
            editable: ["profileMappingID"],
            parentHash: "#profile-mappings"
        }).open();
    });
/*
    router.route("selftests", "selftests", function() {
        new SelfTestsPage({
            el: content,
            url: "selftests.html"
        }).open();
    });

    router.route("selftests/:id", "selftest", function(id) {
        new SelfTestPage({
            el: content,
            url: "selftest.html",
            model: new SelfTestModel({ id: id })
        }).open();
    });
*/
    router.route("tokens", "tokens", function() {
        tokensPage.open();
    });

    router.route("tokens/:id", "token", function(id) {
        new TokenPage({
            el: content,
            url: "token.html",
            model: new TokenModel({ id: id }),
            editable: ["userID", "policy"]
        }).open();
    });

    router.route("tokens/:id/certs", "certs", function(id) {
        new CertificatesPage({
            el: content,
            url: "certs.html",
            collection: new CertificateCollection(null, { tokenID: id })
        }).open();
    });

    router.route("new-token", "new-token", function() {
        new TokenPage({
            el: content,
            url: "token.html",
            model: new TokenModel(),
            mode: "add",
            title: "New Token",
            editable: ["tokenID", "userID", "type", "appletID", "keyInfo", "policy"],
            parentHash: "#tokens"
        }).open();
    });

    router.route("users", "users", function() {
        new UsersPage({
            el: content,
            url: "users.html"
        }).open();
    });

    router.route("users/:id", "user", function(id) {
        new UserPage({
            el: content,
            url: "user.html",
            model: new UserModel({ id: id }),
            editable: ["fullName", "email", "tpsProfiles"]
        }).open();
    });

    router.route("users/:id/roles", "user-roles", function(id) {
        new UserRolesPage({
            el: content,
            url: "user-roles.html",
            collection: new UserRoleCollection(null, { userID: id })
        }).open();
    });

    router.route("users/:id/certs", "user-certs", function(id) {
        new UserCertsPage({
            el: content,
            url: "user-certs.html",
            collection: new UserCertCollection(null, { userID: id })
        }).open();
    });

    router.route("new-user", "new-user", function() {
        new UserPage({
            el: content,
            url: "user.html",
            model: new UserModel(),
            mode: "add",
            title: "New User",
            editable: ["userID", "fullName", "email", "tpsProfiles"],
            parentHash: "#users"
        }).open();
    });

    router.route("logout", "logout", function() {
        // destroy server session
        account.logout({
            success: function() {
                tps.user = null;
                // clear browser cache
                PKI.logout({
                    success: function() {
                        window.location.href = "/tps";
                    },
                    error: function() {
                        alert("Logout not supported by the browser. Please clear Active Logins or close the browser.");
                    }
                });
            },
            error: function() {
                alert("Logout failed. Please close the browser.");
            }
        });
    });

    Backbone.history.start();
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
<div class="collapse navbar-collapse navbar-collapse-1">
    <ul class="nav navbar-nav navbar-utility">
<!--
    <li name="status"><a href="#">Status</a></li>
-->
    <li name="account" class="dropdown">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
        <span class="pficon pficon-user"></span>
        <span name="username"></span><b class="caret"></b>
        </a>
        <ul class="dropdown-menu">
        <li name="logout"><a href="#logout">Logout</a></li>
        </ul>
    </li>
    </ul>
    <ul class="nav navbar-nav navbar-primary">
<!--
    <li name="home"><a href="#"><span class="glyphicon glyphicon-home"></span> Home</a></li>
-->
    <li name="tokens"><a href="#tokens">Tokens</a></li>
    <li name="certs"><a href="#certs">Certificates</a></li>
    <li name="activities"><a href="#activities">Activities</a></li>
    <li name="accounts" class="dropdown context" style="display: none;">
      <a href="#" class="dropdown-toggle" data-toggle="dropdown">
        Accounts
        <b class="caret"></b>
      </a>
      <ul class="dropdown-menu">
        <li><a href="#users">Users</a></li>
        <li><a href="#groups">Groups</a></li>
      </ul>
    </li>
    <li name="system" class="dropdown context">
      <a href="#" class="dropdown-toggle" data-toggle="dropdown">
        System
        <b class="caret"></b>
      </a>
      <ul class="dropdown-menu">
        <li name="config" style="display: none;"><a href="#config">General Configuration</a></li>
        <li name="authenticators" style="display: none;"><a href="#authenticators">Authentication Sources</a></li>
        <li name="connectors" style="display: none;"><a href="#connectors">Subsystem Connections</a></li>
        <li name="profiles" style="display: none;"><a href="#profiles">Profiles</a></li>
        <li name="profile-mappings" style="display: none;"><a href="#profile-mappings">Profile Mappings</a></li>
        <li name="audit" style="display: none;"><a href="#audit">Audit Logging</a></li>
<!--
        <li name="selftests" style="display: none;"><a href="#selftests">Self Tests</a></li>
-->
      </ul>
    </li>
    </ul>
</div>
</nav>

<div id="content">
</div>

<div id="confirm-dialog" class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    <span class="pficon pficon-close"></span>
                </button>
                <h4 class="modal-title">Confirmation</h4>
            </div>
            <div class="modal-body">
            </div>
            <div class="modal-footer">
                <button name="ok" class="btn btn-danger">OK</button>
                <button name="cancel" class="btn btn-default" data-dismiss="modal">Cancel</button>
            </div>
        </div>
    </div>
</div>

<div id="error-dialog" class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    <span class="pficon pficon-close"></span>
                </button>
                <h4 class="modal-title">Error</h4>
            </div>
            <div class="modal-body">
		        <fieldset>
		            <span name="content"></span>
		        </fieldset>
            </div>
            <div class="modal-footer">
                <button name="close" class="btn btn-primary">Close</button>
            </div>
        </div>
    </div>
</div>

</body>
</html>
