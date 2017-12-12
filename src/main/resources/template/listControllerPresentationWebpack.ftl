${entityName}ListController.$inject = ['$scope', '${entityName}Service', 'gumgaController', '$gmdAlert'];

function ${entityName}ListController($scope, ${entityName}Service, gumgaController, $gmdAlert) {
  ${entityName}Service.resetDefaultState();
  gumgaController.createRestMethods($scope, ${entityName}Service, '${entityNameLowerCase}');


  $scope.${entityNameLowerCase}.execute('get');

  $scope.${entityNameLowerCase}.on('deleteSuccess', function() {
    $gmdAlert.success('Sucesso!', 'Seu registro foi removido!', 2000);
    $scope.${entityNameLowerCase}.execute('get');
  });

  $scope.actions = [
    { key: 'option1', label: 'option1' },
    { key: 'option2', label: 'option2' }
  ];

  $scope.search = function(field, param) {
    $scope.query = { searchFields: [field], q: param }
    $scope.${entityNameLowerCase}.methods.search(field,param)
  }

  $scope.advancedSearch = function(param) {
    $scope.${entityNameLowerCase}.methods.advancedSearch(param)
  }

  $scope.action = function(queryaction) {
    console.log(queryaction);
  }

  $scope.tableConfig = {
    columns: '${firstAttribute} ,button',
    checkbox: true,
    selection: 'multi',
    materialTheme: true,
    itemsPerPage: [5, 10, 15, 30],
    columnsConfig: [{
      name: '${firstAttribute}',
      title: '<span gumga-translate-tag="${entityNameLowerCase}.${firstAttribute}"> ${firstAttribute} </span>',
      content: '{{$value.${firstAttribute} }}',
      sortField: '${firstAttribute}'
    }, {
      name: 'button',
      title: ' ',
      content: '<span class="pull-right"><a class="btn gmd btn-link gmd-ripple" ui-sref="${entityNameLowerCase}.edit({id: $value.id })"><i class="glyphicon glyphicon-pencil"></i></a></span>'
    }]
  };

};

module.exports = ${entityName}ListController;
