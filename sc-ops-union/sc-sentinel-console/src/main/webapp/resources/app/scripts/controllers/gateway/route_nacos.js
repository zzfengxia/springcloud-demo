var app = angular.module('sentinelDashboardApp');

app.controller('GatewayRouteNacosCtl', ['$scope', '$stateParams', 'GatewayRouteService', 'ngDialog', 'MachineService',
  function ($scope, $stateParams, GatewayRouteService, ngDialog, MachineService) {
    $scope.app = $stateParams.app;
    $scope.routesPageConfig = {
      pageSize: 10,
      currentPageIndex: 1,
      totalPage: 1,
      totalCount: 0,
    };

    $scope.predicateCheckList = {
      bodyRule: false,
      pathRule: false,
      queryRule: false,
      headerRule: false,
      dateRule: false
    };

    $scope.filterCheckList = {
      pathFilter: false,
      headerFilter: false
    };

    $scope.strategyList = [{val: 'and', desc: 'AND'}, {val: 'or', desc: 'OR'}];
    $scope.respStrategy = [{val: 0, desc: 'SPTSM通用响应'}, {val: 1, desc: '订单系统通用响应'},
                           {val: 2, desc: '微信支付通用响应'}, {val: 3, desc: '银联支付通用响应'}];

    getRoutes();
    function getRoutes() {
      GatewayRouteService.queryRoutes($scope.app).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.routes = data.data;

            $scope.routes.forEach((route, index) => {
              if(route.predicate != null) {
                // predicate.bodyRule.attrMap 由map转为list存储
                if(route.predicate.bodyRule != null && route.predicate.bodyRule.attrMap != null) {
                  route.predicate.bodyRule.attrPatternList = []
                  for(let k in route.predicate.bodyRule.attrMap) {
                    route.predicate.bodyRule.attrPatternList.push({"key": k, "value": route.predicate.bodyRule.attrMap[k]})
                  }
                }

                // predicate.queryRule.queryMap 由map转为list存储
                if(route.predicate.queryRule != null && route.predicate.queryRule.queryMap != null) {
                  route.predicate.queryRule.queryMapList = []
                  for(let k in route.predicate.queryRule.queryMap) {
                    route.predicate.queryRule.queryMapList.push({"key": k, "value": route.predicate.queryRule.queryMap[k]})
                  }
                }

                if(route.filter != null && route.filter.headerFilter != null) {
                  if(route.filter.headerFilter.headers != null) {
                    route.filter.headerFilter.headers = JSON.stringify(route.filter.headerFilter.headers)
                  }
                }
              }
            });

            $scope.routesPageConfig.totalCount = data.data.length;
          } else {
            $scope.routes = [];
            $scope.routesPageConfig.totalCount = 0;
          }
        });
    };
    $scope.getRoutes = getRoutes;

    $scope.startDateOnSetTime = function() {
      $scope.$broadcast('start-date-changed');
    }

    $scope.endDateOnSetTime = function() {
      $scope.$broadcast('end-date-changed');
    }

    $scope.startDateBeforeRender = function($dates) {
      if ($scope.currentRoute.predicate.dateRule && $scope.currentRoute.predicate.dateRule.after) {
        var activeDate = moment($scope.currentRoute.predicate.dateRule.after);

        $dates.filter(function (date) {
          return date.localDateValue() >= activeDate.valueOf()
        }).forEach(function (date) {
          date.selectable = false;
        })
      }
    }

    $scope.endDateBeforeRender = function($view, $dates) {
      if ($scope.currentRoute.predicate.dateRule && $scope.currentRoute.predicate.dateRule.before) {
        var activeDate = moment($scope.currentRoute.predicate.dateRule.before).subtract(1, $view).add(1, 'minute');

        $dates.filter(function (date) {
          return date.localDateValue() <= activeDate.valueOf()
        }).forEach(function (date) {
          date.selectable = false;
        })
      }
    }

    $scope.addNewBodyMatch = function() {
      var total;
      if($scope.currentRoute.predicate.bodyRule == null) {
        $scope.currentRoute.predicate.bodyRule = {}
      }
      if ($scope.currentRoute.predicate.bodyRule.attrPatternList == null) {
        $scope.currentRoute.predicate.bodyRule.attrPatternList = [];
        total = 0;
      } else {
        total = $scope.currentRoute.predicate.bodyRule.attrPatternList.length;
      }
      $scope.currentRoute.predicate.bodyRule.attrPatternList.splice(total + 1, 0, {key: '', value: []});
    };

    $scope.removeBodyMatch = function($index) {
      if ($scope.currentRoute.predicate.bodyRule.attrPatternList.length <= 1) {
        alert('至少要有一个匹配规则');
        return;
      }
      $scope.currentRoute.predicate.bodyRule.attrPatternList.splice($index, 1);
    };

    $scope.addNewQueryMatch = function() {
      var total;
      if($scope.currentRoute.predicate.queryRule == null) {
        $scope.currentRoute.predicate.queryRule = {}
      }
      if ($scope.currentRoute.predicate.queryRule.queryMapList == null) {
        $scope.currentRoute.predicate.queryRule.queryMapList = [];
        total = 0;
      } else {
        total = $scope.currentRoute.predicate.queryRule.queryMapList.length;
      }
      $scope.currentRoute.predicate.queryRule.queryMapList.splice(total + 1, 0, {key: '', value: []});
    };

    $scope.removeQueryMatch = function($index) {
      if ($scope.currentRoute.predicate.queryRule.queryMapList.length <= 1) {
        alert('至少要有一个匹配规则');
        return;
      }
      $scope.currentRoute.predicate.queryRule.queryMapList.splice($index, 1);
    };

    function initPredicate(route) {
      // 初始化predicate checkbox状态
      $scope.predicateCheckList = {}
      // 判断predicate各属性是否为空
      if(route.predicate != null) {
        if(route.predicate.bodyRule != null) {
          $scope.predicateCheckList.bodyRule = true
        }
        if(route.predicate.pathRule != null) {
          $scope.predicateCheckList.pathRule = true
        }
        if(route.predicate.headerRule != null) {
          $scope.predicateCheckList.headerRule = true
        }
        if(route.predicate.queryRule != null) {
          $scope.predicateCheckList.queryRule = true
        }
        if(route.predicate.dateRule != null) {
          $scope.predicateCheckList.dateRule = true
        }
      } else {
        route.predicate = {}
      }

      $scope.filterCheckList= {}
      if(route.filter != null) {
        if(route.filter.pathFilter != null) {
          $scope.filterCheckList.pathFilter = true
        }
        if(route.filter.headerFilter != null) {
          $scope.filterCheckList.headerFilter = true
        }
      } else {
        route.filter = {}
      }
    }
    var gatewayRouteDialog;
    $scope.editRoute = function (route) {
      $scope.currentRoute = angular.copy(route);
      initPredicate($scope.currentRoute)

      $scope.gatewayRouteDialog = {
        title: '编辑网关路由',
        type: 'edit',
        confirmBtnText: '保存'
      };
      gatewayRouteDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/gateway-route-rule-dialog.html',
        width: 998,
        overlay: true,
        scope: $scope
      });
    };

    $scope.addNewRoute = function () {
      $scope.currentRoute = {
        app: $scope.app,
        predicate: {},
        filter: {}
      };
      $scope.predicateCheckList = {}
      $scope.filterCheckList= {}

      $scope.gatewayRouteDialog = {
        title: '新增网关路由',
        type: 'add',
        confirmBtnText: '新增'
      };
      gatewayRouteDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/gateway-route-rule-dialog.html',
        width: 998,
        overlay: true,
        scope: $scope
      });
    };

    function convertParam() {
      console.log(moment.defaultZone);

      let route = angular.copy($scope.currentRoute);
      if(route.predicate == null) {
        return null
      }

      // 通过勾选的断言或过滤器设置相应参数
      if(!$scope.predicateCheckList.bodyRule) {
        route.predicate.bodyRule = null
      }
      if(!$scope.predicateCheckList.pathRule) {
        route.predicate.pathRule = null
      }
      if(!$scope.predicateCheckList.headerRule) {
        route.predicate.headerRule = null
      }
      if(!$scope.predicateCheckList.queryRule) {
        route.predicate.queryRule = null
      }
      if(!$scope.predicateCheckList.dateRule) {
        route.predicate.dateRule = null
      }

      if(route.filter != null) {
        if(!$scope.filterCheckList.headerFilter) {
          route.filter.headerFilter = null
        }
        if(!$scope.filterCheckList.pathFilter) {
          route.filter.pathFilter = null
        }
      }

      if(route.predicate.bodyRule != null && route.predicate.bodyRule.attrPatternList != null) {
        // list(map)转为map
        route.predicate.bodyRule.attrMap = {}
        route.predicate.bodyRule.attrPatternList.forEach(attr => {
          if(!angular.isArray(attr.value)) {
            route.predicate.bodyRule.attrMap[attr.key] = attr.value.split(",")
          } else {
            route.predicate.bodyRule.attrMap[attr.key] = attr.value
          }
        })
        delete route.predicate.bodyRule.attrPatternList
      }

      // predicate.queryRule.queryMap 由map转为list存储
      if(route.predicate.queryRule != null && route.predicate.queryRule.queryMapList != null) {
        route.predicate.queryRule.queryMap = {}
        route.predicate.queryRule.queryMapList.forEach(attr => {
          route.predicate.queryRule.queryMap[attr.key] = attr.value
        })
        delete route.predicate.queryRule.queryMapList
      }

      if(route.predicate.pathRule != null && route.predicate.pathRule.path != null) {
        if(!angular.isArray(route.predicate.pathRule.path)) {
          route.predicate.pathRule.path = route.predicate.pathRule.path.split(",")
        }
      }

      if (route.predicate.dateRule && route.predicate.dateRule.after) {
        route.predicate.dateRule.after = new moment(route.predicate.dateRule.after).tz('Asia/Shanghai').format();
      }
      if (route.predicate.dateRule && route.predicate.dateRule.before) {
        route.predicate.dateRule.before = new moment(route.predicate.dateRule.before).tz('Asia/Shanghai').format();
      }

      if(route.filter != null && route.filter.headerFilter != null) {
        if(route.filter.headerFilter.headers != null) {
          try{
            route.filter.headerFilter.headers = JSON.parse(route.filter.headerFilter.headers)
          } catch(jsonerror) {
            alert("Header过滤器的header参数不是合法的json对象")
            return null
          }
        }
      }

      return route
    }
    $scope.saveRoute = function () {
      let route = convertParam();
      if (!GatewayRouteService.checkRouteValid(route)) {
        return;
      }

      if ($scope.gatewayRouteDialog.type === 'add') {
        addNewRoute(route);
      } else if ($scope.gatewayRouteDialog.type === 'edit') {
        saveRoute(route);
      }
    };

    function addNewRoute(route) {
      GatewayRouteService.newRoute(route).success(function (data) {
        if (data.code == 0) {
          // 延迟加载，数据发布到nacos需要一定时间
          setTimeout(getRoutes, 1000);
          gatewayRouteDialog.close();
        } else {
          alert('新增路由失败,' + data.msg);
        }
      });
    };

    function saveRoute(route) {
      GatewayRouteService.saveRoute(route).success(function (data) {
        if (data.code == 0) {
          setTimeout(getRoutes, 1000);
          gatewayRouteDialog.close();
        } else {
          alert('修改路由失败,' + data.msg);
        }
      });
    };

    var confirmDialog;
    $scope.deleteRoute = function (route) {
      $scope.currentRoute = route;
      $scope.confirmDialog = {
        title: '删除网关路由规则',
        type: 'delete_route',
        attentionTitle: '请确认是否删除如下路由',
        attention: '路由ID: ' + route.id,
        confirmBtnText: '删除',
      };
      confirmDialog = ngDialog.open({
        template: '/app/views/dialog/confirm-dialog.html',
        scope: $scope,
        overlay: true
      });
    };

    $scope.confirm = function () {
      if ($scope.confirmDialog.type == 'delete_route') {
        deleteRoute($scope.currentRoute);
      } else {
        console.error('error');
      }
    };

    function deleteRoute(route) {
      GatewayRouteService.deleteRoute(route).success(function (data) {
        if (data.code == 0) {
          setTimeout(getRoutes, 1000);
          confirmDialog.close();
        } else {
          alert('删除网关路由失败' + data.msg);
        }
      });
    };
  }]
);
