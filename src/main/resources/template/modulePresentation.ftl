define(function(require) {

  var APILocation = require('app/apiLocations');
  require('angular-ui-router');
  require('app/modules/${entityNameLowerCase}/services/module');
  require('app/modules/${entityNameLowerCase}/controllers/module');

  return require('angular')
    .module('app.${entityNameLowerCase}', [
      'ui.router',
      'app.${entityNameLowerCase}.controllers',
      'app.${entityNameLowerCase}.services',
      'gumga.core'
    ])
    .config(function($stateProvider, $httpProvider) {
      $stateProvider
        .state('${entityNameLowerCase}.list', {
          url: '/list',
          templateUrl: 'app/modules/${entityNameLowerCase}/views/list.html',
          controller: '${entityName}ListController'
        })
        .state('${entityNameLowerCase}.insert', {
          url: '/insert',
          templateUrl: 'app/modules/${entityNameLowerCase}/views/form.html',
          controller: '${entityName}FormController',
          resolve: {
            entity: ['$stateParams', '$http', function($stateParams, $http) {
              return $http.get(APILocation.apiLocation + '/api/${entityNameLowerCase}/new');
            }]
          }
        })
        .state('${entityNameLowerCase}.edit', {
          url: '/edit/:id',
          templateUrl: 'app/modules/${entityNameLowerCase}/views/form.html',
          controller: '${entityName}FormController',
          resolve: {
            entity: ['$stateParams', '$http', function($stateParams, $http) {
              return $http.get(APILocation.apiLocation + '/api/${entityNameLowerCase}/' + $stateParams.id);
            }]
          }
        });
    })
});