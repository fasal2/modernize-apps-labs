'use strict';

angular.module('app').config([ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/', {
    templateUrl : 'partials/home.html',
    controller : 'HomeController'
  }).when('/cart', {
    templateUrl : 'partials/cart.html',
    controller : 'CartController'
  }).when('/orders', {
    templateUrl : 'partials/orders.html',
    controller : 'OrdersController'
  }).when('/populars', {
    // -change-
    // route added
    templateUrl : 'partials/populars.html',
    controller : 'PopularsController'
  }).otherwise({
    redirectTo : '/'
  });
} ]);
