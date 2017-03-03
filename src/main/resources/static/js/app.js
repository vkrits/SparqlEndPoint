/**
 * The angularJS application for handling the front-End / UI of the Blazegraph Endpoint
 * 
 * @author Vangelis Kritsotakis
 */

var app = angular.module('app', ['ngRoute','ngResource','ui.bootstrap', 
                                 'ngAnimate', 'ui.codemirror', 'app.mainServices']);//'lazy-scroll']);
/*
app.filter('encodeURIComponent', function() {
    return window.encodeURIComponent;
});
*/
app.filter('encodeURIComponent', function() {
    return function(input) {
        return encodeURIComponent(encodeURIComponent(input));
    }; 
})
app.filter('decodeURIComponent', function(input) {
    return decodeURIComponent(input);
});

app.config(function($routeProvider, $locationProvider) {
	
	//$locationProvider.hashPrefix(); // Removes index.html in URL
	
    $routeProvider
	    .when('/query',{
            templateUrl: '/views/query.html',
            controller: 'queryController'
        })
        // By simply using :afterbase I can use dynamic route (it could be :whatever)
        .when('/:afterbase/:uriToResolve',{
            templateUrl: '/views/explore.html',
            controller: 'exploreController'
        })
        .when('/roles',{
            templateUrl: '/views/roles.html',
            controller: 'rolesController'
        })
        .when('/explore',{
            templateUrl: '/views/explore.html',
            controller: 'exploreController'
        })
        .when('/exploreInit/:type/:uri/:init',{
            templateUrl: '/views/explore.html',
            controller: 'exploreController'/*
            resolve: {
            	testObj:  'submitQueryService.getOutgoingIncomingResults({uri: "http://purl.obolibrary.org/obo/BFO_0000051", itemsPerPage: 20})'
            }*/
        });
		
    $locationProvider.html5Mode(true);
    
});

app.config(['$httpProvider', function ($httpProvider) {    
	$httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded; charset=UTF-8';
}]);