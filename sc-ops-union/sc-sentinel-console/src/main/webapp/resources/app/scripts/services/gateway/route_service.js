var app = angular.module('sentinelDashboardApp');

app.service('GatewayRouteService', ['$http', function ($http) {
  this.queryLocalRoutes = function (app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };
    return $http({
      url: '/gateway/route/local/list.json',
      params: param,
      method: 'GET'
    });
  };
  this.queryRoutes = function (app) {
    var param = {
      app: app
    };
    return $http({
      url: '/gateway/route/list.json',
      params: param,
      method: 'GET'
    });
  };

  this.newRoute = function (route) {
    return $http({
      url: '/gateway/route/new.json',
      data: route,
      method: 'POST'
    });
  };

  this.saveRoute = function (route) {
    return $http({
      url: '/gateway/route/save.json',
      data: route,
      method: 'POST'
    });
  };

  this.deleteRoute = function (route) {
    var param = {
      id: route.id,
      app: route.app
    };
    return $http({
      url: '/gateway/route/delete.json',
      params: param,
      method: 'POST'
    });
  };

  this.checkRouteValid = function (route) {
    if(route === undefined || route == null) {
      return false;
    }
    if (route.id === undefined || route.id === '') {
      alert('路由ID不能为空');
      return false;
    }

    if (route.uri == null || route.uri === '') {
      alert('转发UIR不能为空');
      return false;
    }

    if(route.predicate == null || Object.keys(route.predicate).length < 1) {
      alert('至少需要配置一个断言');
      return false;
    }
    if(route.predicate.bodyRule != null && route.predicate.bodyRule.attrMap == null) {
      alert('Body断言至少需要配置一个属性值');
      return false;
    }
    if(route.filter != null && route.filter.headerFilter != null) {
      if(route.filter.headerFilter.headers == null) {
        alert("Header过滤器必须设置参数")
        return false
      }
      if(!(angular.isObject(route.filter.headerFilter.headers) && !angular.isArray(route.filter.headerFilter.headers))) {
        alert('header过滤器格式不正确,必须为map格式对象');
        return false;
      }
      if(Object.keys(route.filter.headerFilter.headers).length < 1) {
        alert('header过滤器至少需要设置一对参数');
        return false;
      }
    }
    if(route.predicate.pathRule != null && route.predicate.pathRule.path != null) {
      if(!angular.isArray(route.predicate.pathRule.path)) {
        alert('Path断言的路径参数必须是一个数组或使用','分隔');
        return false;
      }
    }

    return true;
  };
}]);
