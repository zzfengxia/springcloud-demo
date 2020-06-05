var app = angular.module('sentinelDashboardApp');

app.service('DegradeNacosService', ['$http', function ($http) {
  this.queryDegradeRules = function (app) {
    var param = {
      app: app
    };
    return $http({
      url: '/v2/degrade/rules.json',
      params: param,
      method: 'GET'
    });
  };

  this.newRule = function (rule) {
    var param = {
      id: rule.id,
      app: rule.app,
      resource: rule.resource,
      limitApp: rule.limitApp,
      count: rule.count,
      timeWindow: rule.timeWindow,
      grade: rule.grade,
      statisticsTimeWindow: rule.statisticsTimeWindow,
      intervalUnit: rule.intervalUnit,
      minRequestAmount: rule.minRequestAmount,
      slowRt: rule.slowRt
    };
    return $http({
      url: '/v2/degrade/new.json',
      params: param,
      method: 'GET'
    });
  };

  this.saveRule = function (rule) {
    var param = {
      id: rule.id,
      app: rule.app,
      resource: rule.resource,
      limitApp: rule.limitApp,
      grade: rule.grade,
      count: rule.count,
      timeWindow: rule.timeWindow,
      statisticsTimeWindow: rule.statisticsTimeWindow,
      intervalUnit: rule.intervalUnit,
      minRequestAmount: rule.minRequestAmount,
      slowRt: rule.slowRt
    };
    return $http({
      url: '/v2/degrade/save.json',
      params: param,
      method: 'GET'
    });
  };

  this.deleteRule = function (rule) {
    var param = {
      id: rule.id,
      app: rule.app
    };
    return $http({
      url: '/v2/degrade/delete.json',
      params: param,
      method: 'GET'
    });
  };

  this.checkRuleValid = function (rule) {
      if (rule.resource === undefined || rule.resource === '') {
          alert('资源名称不能为空');
          return false;
      }
      if (rule.statisticsTimeWindow === undefined || rule.statisticsTimeWindow === '') {
        alert('统计时长不能为空');
        return false;
      }
      if (rule.grade === undefined || rule.grade < 0) {
          alert('未知的降级策略');
          return false;
      }
      if (rule.count === undefined || rule.count === '' || rule.count < 0) {
          alert('降级阈值不能为空或小于 0');
          return false;
      }
      if (rule.timeWindow === undefined || rule.timeWindow === '' || rule.timeWindow <= 0) {
          alert('降级时间窗口必须大于 0');
          return false;
      }
      // 异常比率类型.
      if (rule.grade != 2 && rule.count > 1) {
          alert('异常比率超出范围：[0.0 - 1.0]');
          return false;
      }
      return true;
  };
}]);
