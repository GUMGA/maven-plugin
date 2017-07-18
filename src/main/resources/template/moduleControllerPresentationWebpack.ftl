require('../services/module');

module.exports = angular
        .module('app.${entityNameLowerCase}.controllers', ['app.${entityNameLowerCase}.services'])
        <#list controllers as controller>
        .controller('Modal${controller}Controller', require('./Modal${controller}Controller'))
        </#list>
        .controller('${entityName}FormController', require('./${entityName}FormController'))
        .controller('${entityName}ListController', require('./${entityName}ListController'));
