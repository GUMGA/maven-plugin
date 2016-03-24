define([], function() {

  ${entityName}ListController.$inject = ['$scope', '${entityName}Service', 'gumgaController'];

  function ${entityName}ListController($scope, ${entityName}Service, gumgaController) {

    gumgaController.createRestMethods($scope, ${entityName}Service, '${entityNameLowerCase}');

    ${entityName}Service.resetDefaultState();
    $scope.${entityNameLowerCase}.execute('get');

    $scope.tableConfig = {
      columns: '${firstAttribute} ,button',
      checkbox: true,
      columnsConfig: [{
        name: '${firstAttribute}',
        title: '<span gumga-translate-tag="${entityNameLowerCase}.${firstAttribute}"> ${firstAttribute} </span>',
        content: '{{$value.${firstAttribute} }}',
        sortField: '${firstAttribute}'
      }, {
        name: 'button',
        title: ' ',
        content: '<span class="pull-right"><a class="btn btn-primary btn-sm" ui-sref="${entityNameLowerCase}.edit({id: {{$value.id}} })"><i class="glyphicon glyphicon-pencil"></i></a></span>'
      }]
    };

  };
  return ${entityName}ListController;
});
