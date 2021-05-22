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
    <div class="row">
        <div class="col-md-12 mt-5">
            <h1>${title}</h1>
            <h5 class="generated-at">Generated at ${generated_at}</h5>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12 mt-3">
            <h2>Alerts</h2>
            <p class="section-description">All alerts triggered in the last 24 hours.</p>

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
