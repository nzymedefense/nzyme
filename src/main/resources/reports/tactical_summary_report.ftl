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

    <div class="row mt-3"?>
        <div class="col-md-4">
            <div class="card text-center">
                <#if alerts_count == 0>
                    <div class="card-body bg-success">
                <#else>
                    <div class="card-body bg-warning">
                </#if>
                <p>Alerts</p>
                <span>${alerts_count}</span>
                </div>
            </div>
        </div>

    </div>

    <div class="row mt-3">
        <div class="col-md-12">
            <h2>Networks</h2>
            <p class="section-description">All networks seen in the last 24 hours. Highlighted networks were first
                seen within last 24 hours.</p>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <table class="table table-sm table-hover table-striped networks">
                <thead>
                <tr>
                    <th>SSID</th>
                    <th>First Seen</th>
                    <th>Last Seen</th>
                </tr>
                </thead>
                <tbody
                <#list networks as network>
                    <#if network.new_today >
                        <tr class="new-today">
                    <#else>
                        <tr>
                    </#if>
                        <td>${network.ssid}</td>
                        <td>${network.first_seen}</td>
                        <td>${network.last_seen}</td>
                    </tr>
                <#else>
                    <tr>
                        <td colspan="4" class="text-center">No networks seen in last 24 hours.</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>

    <div class="row mt-3">
        <div class="col-md-12">
            <h2>Alerts</h2>
            <p class="section-description">All alerts triggered in the last 24 hours.</p>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <table class="table table-sm table-hover table-striped">
                <thead>
                <tr>
                    <th>Type</th>
                    <th>First Seen</th>
                    <th>Last Seen</th>
                    <th>Frames</th>
                </tr>
                </thead>
                <tbody>
                <#list alerts as alert>
                    <tr>
                        <td>${alert.type}</td>
                        <td>${alert.first_seen}</td>
                        <td>${alert.last_seen}</td>
                        <td>${alert.frames}</td>
                    </tr>
                <#else>
                    <tr>
                        <td colspan="4" class="text-center">No alerts in last 24 hours.</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
</div>

</body>
</html>
