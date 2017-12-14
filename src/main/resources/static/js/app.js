(() => angular.module('cashflow', ['ui.router', 'ui.bootstrap', 'angularCSS']).config(($stateProvider, $urlRouterProvider) => {
    $stateProvider
        .state('home', {
            url: '/',
            component: 'home'
        });

    $urlRouterProvider.otherwise('/');
}))();