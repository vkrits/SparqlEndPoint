/**
 * The angularJS application for handling the front-End / UI of the Blazegraph Endpoint
 * 
 * @author Vangelis Kritsotakis
 */

var app = angular.module('app', ['ngRoute','ngResource','ui.bootstrap', 'ngAnimate']);//'lazy-scroll']);
app.config(function($routeProvider){
    $routeProvider
        .when('/query',{
            templateUrl: '/views/query.html',
            controller: 'queryController'
        })
        .when('/roles',{
            templateUrl: '/views/roles.html',
            controller: 'rolesController'
        })
        .otherwise(
            { redirectTo: '/'}
        );
});

app.config(['$httpProvider', function ($httpProvider) {    
	$httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded; charset=UTF-8';
}]);

