var app = angular.module('sentinelDashboardApp');

app.controller('DegradeNacosCtl', ['$scope', '$stateParams', 'DegradeNacosService', 'ngDialog', 'MachineService',
  function ($scope, $stateParams, DegradeNacosService, ngDialog, MachineService) {
    //初始化
    $scope.app = $stateParams.app;
    $scope.rulesPageConfig = {
      pageSize: 10,
      currentPageIndex: 1,
      totalPage: 1,
      totalCount: 0,
    };
    $scope.intervalUnits = [{val: 0, desc: '秒'}, {val: 1, desc: '分'}];

    getDegradeRules();
    function getDegradeRules() {
      DegradeNacosService.queryDegradeRules($scope.app).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.rules = data.data;
            $scope.rulesPageConfig.totalCount = $scope.rules.length;
          } else {
            $scope.rules = [];
            $scope.rulesPageConfig.totalCount = 0;
          }
        });
    };
    $scope.getDegradeRules = getDegradeRules;

    var degradeRuleDialog;
    $scope.editRule = function (rule) {
      $scope.currentRule = angular.copy(rule);
      $scope.degradeRuleDialog = {
        title: '编辑降级规则',
        type: 'edit',
        confirmBtnText: '保存'
      };
      degradeRuleDialog = ngDialog.open({
        template: '/app/views/dialog/degrade-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

    $scope.addNewRule = function () {
      $scope.currentRule = {
        grade: 0,
        app: $scope.app,
        limitApp: 'default',
        statisticsTimeWindow: 1,
        intervalUnit: 0
      };
      $scope.degradeRuleDialog = {
        title: '新增降级规则',
        type: 'add',
        confirmBtnText: '新增'
      };
      degradeRuleDialog = ngDialog.open({
        template: '/app/views/dialog/degrade-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

    $scope.saveRule = function () {
      if (!DegradeNacosService.checkRuleValid($scope.currentRule)) {
        return;
      }
      if ($scope.degradeRuleDialog.type === 'add') {
        addNewRule($scope.currentRule);
      } else if ($scope.degradeRuleDialog.type === 'edit') {
        saveRule($scope.currentRule, true);
      }
    };

    function parseDegradeMode(grade) {
        switch (grade) {
            case 0:
              return 'RT比例';
            case 1:
              return '异常比例';
            case 2:
              return '异常数';
            default:
              return '未知';
        }
    }

    var confirmDialog;
    $scope.deleteRule = function (rule) {
      $scope.currentRule = rule;
      $scope.confirmDialog = {
        title: '删除降级规则',
        type: 'delete_rule',
        attentionTitle: '请确认是否删除如下降级规则',
        attention: '资源名: ' + rule.resource +
            ', 降级模式: ' + parseDegradeMode(rule.grade) + ', 阈值: ' + rule.count,
        confirmBtnText: '删除',
      };
      confirmDialog = ngDialog.open({
        template: '/app/views/dialog/confirm-dialog.html',
        scope: $scope,
        overlay: true
      });
    };

    $scope.confirm = function () {
      if ($scope.confirmDialog.type == 'delete_rule') {
        deleteRule($scope.currentRule);
      } else {
        console.error('error');
      }
    };

    function deleteRule(rule) {
      DegradeNacosService.deleteRule(rule).success(function (data) {
        if (data.code == 0) {
          setTimeout(getDegradeRules, 1000);
          confirmDialog.close();
        } else {
          alert('失败!');
        }
      });
    };

    function addNewRule(rule) {
      DegradeNacosService.newRule(rule).success(function (data) {
        if (data.code == 0) {
          setTimeout(getDegradeRules, 1000);
          degradeRuleDialog.close();
        } else {
          alert('失败!');
        }
      });
    };

    function saveRule(rule, edit) {
      DegradeNacosService.saveRule(rule).success(function (data) {
        if (data.code == 0) {
          setTimeout(getDegradeRules, 1000);
          if (edit) {
            degradeRuleDialog.close();
          } else {
            confirmDialog.close();
          }
        } else {
          alert('失败!');
        }
      });
    }
    //queryAppMachines();
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
  }]);
