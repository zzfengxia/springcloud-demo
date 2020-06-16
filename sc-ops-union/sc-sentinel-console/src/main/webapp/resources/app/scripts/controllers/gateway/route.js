var app = angular.module('sentinelDashboardApp');

app.controller('GatewayRouteCtl', ['$scope', '$stateParams', 'GatewayRouteService', 'ngDialog', 'MachineService',
  function ($scope, $stateParams, GatewayRouteService, ngDialog, MachineService) {
    $scope.app = $stateParams.app;

    $scope.routesPageConfig = {
      pageSize: 10,
      currentPageIndex: 1,
      totalPage: 1,
      totalCount: 0,
    };
    $scope.macsInputConfig = {
      searchField: ['text', 'value'],
      persist: true,
      create: false,
      maxItems: 1,
      render: {
        item: function (data, escape) {
          return '<div>' + escape(data.text) + '</div>';
        }
      },
      onChange: function (value, oldValue) {
        $scope.macInputModel = value;
      }
    };

    getMachineRoutes();
    function getMachineRoutes() {
      if (!$scope.macInputModel) {
        return;
      }

      var mac = $scope.macInputModel.split(':');
      GatewayRouteService.queryLocalRoutes($scope.app, mac[0], mac[1]).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.routes = [];

            data.data.forEach(function(route) {
              let newItem = route;
              newItem["metadata"] = JSON.stringify(route["metadata"])
              newItem["filters"] = JSON.stringify(route["filters"])
              $scope.routes.push(newItem);
            });

            $scope.routesPageConfig.totalCount = data.data.length;
          } else {
            $scope.routes = [];
            $scope.routesPageConfig.totalCount = 0;
          }
        });
    };
    $scope.getMachineRoutes = getMachineRoutes;

    $scope.routeDetail = function (route) {
      $scope.currentRoute = angular.copy(route);
      $scope.gatewayRouteDetailDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/gateway-route-detail-dialog.html',
        width: 750,
        overlay: true,
        scope: $scope
      });
    };

    queryAppMachines();
    function queryAppMachines() {
      MachineService.getAppMachines($scope.app).success(
        function (data) {
          if (data.code == 0) {
            // $scope.machines = data.data;
            if (data.data) {
              $scope.machines = [];
              $scope.macsInputOptions = [];
              data.data.forEach(function (item) {
                if (item.healthy) {
                  $scope.macsInputOptions.push({
                    text: item.ip + ':' + item.port,
                    value: item.ip + ':' + item.port
                  });
                }
              });
            }
            if ($scope.macsInputOptions.length > 0) {
              $scope.macInputModel = $scope.macsInputOptions[0].value;
            }
          } else {
            $scope.macsInputOptions = [];
          }
        }
      );
    };
    $scope.$watch('macInputModel', function () {
      if ($scope.macInputModel) {
        getMachineRoutes();
      }
    });
  }]
);
