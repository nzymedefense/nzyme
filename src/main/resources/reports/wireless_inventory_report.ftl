<!doctype html>
<html>
<head>
    <meta name="viewport" content="width=device-width">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>${title}</title>

    <#include "partials/style.ftl">
</head>
<body>

<div class="container">
    <div class="row mt-5">
        <div class="col-md-12">
            <h1>${title}</h1>
            <h4>${time_range}</h4>
            <h5 class="generated-at">Generated at ${generated_at}</h5>
        </div>
    </div>

    <div class="row mt-3">
        <div class="col-md-12">
            <h2>Inventory</h2>
            <p class="section-description">All networks monitored by nzyme.</p>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
        <#list ssids as ssid,details>
            <div class="row">
                <div class="col-md-12">
                    <h4>SSID: ${ssid}</h4>

                    <dl>
                        <dt>Security:</dt>
                        <dd>${details["security"]}</dd>
                        <dt>Channels:</dt>
                        <dd>${details["channels"]}</dd>
                    </dl>
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <h5>Access Points</h5>

                    <table class="table table-sm table-hover table-striped networks">
                        <thead>
                        <tr>
                            <th>BSSID</th>
                            <th>Fingerprints</th>
                        </tr>
                        </thead>
                        <tbody>
                        <#list details["bssids"] as bssid>
                            <tr>
                                <td>${bssid["bssid"]}</td>
                                <td>${bssid["fingerprints"]}</td>
                            </tr>
                        <#else>
                            <tr>
                                <td colspan="2">
                                    No access points were configured.
                                </td>
                            </tr>
                        </#list>
                        </tbody>
                    </table>
                </div>
            </div>

            <hr />
        <#else>
            <div class="alert alert-warning">
                Nzyme was not configured to monitor any networks.
            </div>
        </#list>
        </div>
    </div>

    <div class="row mt-3">
        <div class="col-md-12">
            <h2>Alerts</h2>
            <p class="section-description">All enabled nzyme alerts.</p>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <ul>
            <#list enabled_alerts as alert>
                <li>${alert}</li>
            <#else>
                <li>No alerts were enabled.</li>
            </#list>
            </ul>
        </div>
    </div>

</div>

</body>
</html>
