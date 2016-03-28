define(function (require) {
    var angular = require('angular');
    require('app/modules/${entityNameLowerCase}/services/module');
    require('angular-ui-router');

    return angular
            .module('app.${entityNameLowerCase}.controllers', ['app.${entityNameLowerCase}.services','ui.router'])
            <#list controllers as controller>
            .controller('Modal${controller}Controller', require('app/modules/${entityNameLowerCase}/controllers/Modal${controller}Controller'))
            </#list>
            .controller('${entityName}FormController', require('app/modules/${entityNameLowerCase}/controllers/${entityName}FormController'))
            .controller('${entityName}ListController', require('app/modules/${entityNameLowerCase}/controllers/${entityName}ListController'));
});