define(function(require) {
   require('angular')
   .module('app.${entityNameLowerCase}.services', [])
   .service('${entityName}Service', require('app/modules/${entityNameLowerCase}/services/${entityName}Service'));
});