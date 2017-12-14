((module) => {
    homeController.$inject = ['$http'];

    function homeController($http) {
        let ctrl = angular.extend(this, {
            loading: true,
            error: null,
            categories: [],
            months: []
        });

        let rowData = [];

        ctrl.$onInit = function () {
            $http.get('/data/categoryByMonthTable').then(data => {
                data.data.forEach(row => {
                    if (!ctrl.categories.includes(row.category)) {
                        ctrl.categories.push(row.category);
                    }

                    if (!ctrl.months.includes(row.month)) {
                        ctrl.months.push(row.month);
                    }

                    rowData.push(row);
                });
            }).catch(err => {
                ctrl.error = err;
                console.error(err);
            }).finally(() => ctrl.loading = false)
        };

        ctrl.getData = function(category, month) {
            const row = rowData.find(row => row.category == category && row.month == month);
            return row === undefined ? 0 : row.amount;
        };

        ctrl.getMonthTotal = function (month) {
            return rowData.filter(row => row.month == month).map(row => row.amount).reduce((sum, amount) => sum + amount);
        };
    }

    module.component('home', {
        templateUrl: '/js/components/home/home.html',
        css: '/js/components/home/home.css',
        controller: homeController,
        bindings: {}
    });
})(angular.module('cashflow'));