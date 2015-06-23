<html>
<head>
<#include "head.ftl">
</head>
</head>
<body>
<#include "navBar.ftl">
<div class="content">
<#include "graphsTable.ftl">
    <div id="graph" class="graph">
    </div>
<#if typeDisplayed == 'WITH_ROLES'>
    <#include "rolesSettingsPanel.ftl">
</#if>
</div>

</body>
<script src="scripts/graphs.js"></script>
</html>