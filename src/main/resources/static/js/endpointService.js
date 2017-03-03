/**
 * The main angularJS service to hold the factories 
 * 
 * @author Vangelis Kritsotakis
 */

angular.module('app.mainServices', [])

.factory('submitQueryService', function($http, $timeout, $q) {

	return {
		getQueryResults : function(dataObj) {

			return $http({
				'url' : '/executequery_json',
				'method' : 'POST',
				'headers' : {
					'Content-Type' : 'application/json'
				},
				'data' : dataObj
			}).success(function(data, status, headers, config) {
				return data;
			})
			.error(function(err) {
			});

		},
		
		getPageResults : function(pageParams) {
			return $http({
				'url' : '/paginator_json',
				'method' : 'GET',
				'headers' : {
					'Content-Type' : 'application/json'
				},
				'params' : pageParams
			}).success(function(data, status, headers, config) {
				return data;
			})
			.error(function(err) {
			});
		},
		
		getOutgoingIncomingResults : function(dataObj) {
			return $http({
				'url' : '/retrieve_uri_info_json',
				'method' : 'POST',
				'headers' : {
					'Content-Type' : 'application/json'
				},
				'params' : dataObj
			}).success(function(data, status, headers, config) {
				return data;
			})
			.error(function(err) {
			});
		},
		getRdfBase : function() {
			return $http({
				'url' : '/config_properties',
				'method' : 'Get',
				'headers' : {
					'Content-Type' : 'application/json'
				}
			}).success(function(data, status, headers, config) {
				return data;
			})
			.error(function(err) {
			});
		}
	}

});