require('./services/module');
require('./controllers/module');

module.exports = angular.module('app.${entityNameLowerCase}', [
    'app.${entityNameLowerCase}.controllers',
    'app.${entityNameLowerCase}.services'
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